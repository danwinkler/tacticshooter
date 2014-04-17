package com.danwink.tacticshooter;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

import javax.vecmath.Point2i;

import org.dom4j.DocumentException;
import org.newdawn.slick.util.pathfinding.AStarPathFinder;
import org.newdawn.slick.util.pathfinding.PathFinder;




import com.danwink.tacticshooter.ComputerPlayer.PlayType;
import com.danwink.tacticshooter.gameobjects.Building;
import com.danwink.tacticshooter.gameobjects.Bullet;
import com.danwink.tacticshooter.gameobjects.Level;
import com.danwink.tacticshooter.gameobjects.Level.SlotOption;
import com.danwink.tacticshooter.gameobjects.Player;
import com.danwink.tacticshooter.gameobjects.Team;
import com.danwink.tacticshooter.gameobjects.Unit;
import com.danwink.tacticshooter.gameobjects.Building.BuildingType;
import com.danwink.tacticshooter.gameobjects.Unit.UnitState;
import com.danwink.tacticshooter.gameobjects.Unit.UnitType;
import com.danwink.tacticshooter.network.Message;
import com.danwink.tacticshooter.network.ServerInterface;
import com.danwink.tacticshooter.network.ServerNetworkInterface;
import com.phyloa.dlib.util.DMath;

/**
 * Handles all of the game logic, and runs the server
 * 
 * @author Daniel Winkler
 *
 */
public class TacticServer 
{
	public ServerInterface si;
	
	public ArrayList<Unit> units = new ArrayList<Unit>();
	public ArrayList<Bullet> bullets = new ArrayList<Bullet>();
	
	public HashMap<Integer, Player> players = new HashMap<Integer, Player>();
	public Level l;
	public PathFinder finder;
	
	public boolean running = true;
	
	Thread t;
	
	public ServerLoop sl;
	
	long lastTick;
	int tick = 0;
	
	Team a = Team.a;
	Team b = Team.b;
	
	public GameStats gs = new GameStats();
	
	ArrayList<String> maps = new ArrayList<String>();
	
	ServerState state = ServerState.LOBBY;
	
	GameType gameType = GameType.POINTCONTROL;
	
	//LOBBY
	Player[] slots = new Player[16];
	int selectedMap = 0;
	boolean fogEnabled = false;
	
	//SCRIPT
	JSAPI js;
	
	public TacticServer( ServerInterface si )
	{
		this.si = si;
	}
	
	public void begin()
	{
		File[] files = new File( "levels" ).listFiles();
		if( files != null )
		{
			for( int i = 0; i < files.length; i++ )
			{
				maps.add( files[i].getName().replace( ".xml", "" ) );
			}
		}
		
		sl = new ServerLoop();
		t = new Thread( sl );
		t.start();
		lastTick = System.currentTimeMillis();
		
		for( int i = 4; i < 8; i++ )
		{
			Player p = new Player();
			p.slot = i;
			String[] rnames = StaticFiles.names.split( "\n" );
			p.name = rnames[DMath.randomi( 0, rnames.length )].split( " " )[0];
			p.playType = PlayType.values()[i%PlayType.values().length];
			p.isBot = true;
			slots[i] = p;
		}
		
		for( int i = 12; i < 16; i++ )
		{
			Player p = new Player();
			p.slot = i;
			String[] rnames = StaticFiles.names.split( "\n" );
			p.name = rnames[DMath.randomi( 0, rnames.length )].split( " " )[0];
			p.playType = PlayType.values()[i%PlayType.values().length];
			p.isBot = true;
			slots[i] = p;
		}
	}
	
	public void setupLobby()
	{
		for( int i = 0; i < 16; i++ )
		{
			if( slots[i] != null && !slots[i].isBot )
			{
				slots[i] = null;
			}
			else if( slots[i] != null && slots[i].isBot )
			{
				slots[i].money = 0;
				slots[i].respawn = 0;
			}
		}
		
		selectedMap = (selectedMap+1) % maps.size();
		
		try {
			l = LevelFileHelper.loadLevel( maps.get( selectedMap ) );
		} catch( DocumentException e ) {
			e.printStackTrace();
		}
		
		state = ServerState.LOBBY;
	}
	
	public void setupServer()
	{
		try {
			l = LevelFileHelper.loadLevel( maps.get( selectedMap ) );
		} catch( DocumentException e ) {
			e.printStackTrace();
		}
		
		js = new JSAPI( this );
		if( gameType == GameType.UMS )
		{
			js.load( l.ums );
		}
		else if( gameType == GameType.POINTCONTROL )
		{
			js.loadFile( "data/gamemodes/pointcapture.js" );
		}
			
		gs.setup( a, b );
		for( int i = 0; i < 16; i++ )
		{
			if( slots[i] != null )
			{
				slots[i].team = i < 8 ? a : b;
				if( slots[i].isBot )
				{
					ComputerPlayer cp = null;
					try {
						cp = (ComputerPlayer)slots[i].playType.c.newInstance();
					} catch (InstantiationException e) {
						e.printStackTrace();
					} catch (IllegalAccessException e) {
						e.printStackTrace();
					}
					cp.setup( (ServerNetworkInterface)si );
					cp.player = slots[i];
					cp.l = l;
					slots[i].id = cp.fc.id;
					players.put( cp.fc.id, slots[i] );
					Thread ct = new Thread( cp );
					ct.start();
				}
				else
				{
					players.put( slots[i].id, slots[i] );
				}
			}
		}
		
		si.sendToAllClients( new Message( MessageType.FOGUPDATE, fogEnabled ) );
		si.sendToAllClients( new Message( MessageType.LEVELUPDATE, l ) );
		
		for( int i = 0; i < 16; i++ )
		{
			if( slots[i] != null )
			{
				si.sendToClient( slots[i].id, new Message( MessageType.PLAYERUPDATE, slots[i] ) );	
			}
		}
		
		finder = new AStarPathFinder( l, 500, StaticFiles.advOptions.getB( "diagonalMove" )  );
		
		lastTick = System.currentTimeMillis();
		state = ServerState.PLAYING;
	}
	
	public void setBot( int line, boolean isBot, boolean update )
	{
		Player p = slots[line];
		
		if( p != null )
		{
			if( isBot )
			{
				si.sendToClient( p.id, new Message( MessageType.KICK, "Your slot turned into a bot." ) );
			}
			else if( !isBot && p.isBot )
			{
				slots[line] = null;
			}
		}
		
		if( isBot )
		{
			p = new Player();
			p.slot = line;
			String[] rnames = StaticFiles.names.split( "\n" );
			p.name = rnames[DMath.randomi( 0, rnames.length )].split( " " )[0];
			p.isBot = isBot;
			slots[line] = p;
		}
		
		if( update )
		{
			si.sendToAllClients( new Message( MessageType.PLAYERUPDATE, new Object[] { line, slots[line] } ) );
		}
	}
	
	public void setPlayType( int line, PlayType pt )
	{
		Player p = slots[line];
		
		if( p != null )
		{
			p.playType = pt;
		}
		
		si.sendToAllClients( new Message( MessageType.PLAYERUPDATE, new Object[] { line, slots[line] } ) );
	}
	
	@SuppressWarnings( { "incomplete-switch", "unchecked" } )
	public void update()
	{	
		if( state == ServerState.LOBBY )
		{
			while( si.hasServerMessages() )
			{
				Message m = si.getNextServerMessage();
				switch( m.messageType )
				{
				case DISCONNECTED:
					for( int i = 0; i < 16; i++ )
					{
						if( slots[i] != null && slots[i].id == m.sender )
						{
							si.sendToAllClients( new Message( MessageType.MESSAGE, slots[i].name + " left the game." ) );
							if( !slots[i].isBot )
							{
								slots[i] = null;
							}
							si.sendToClient( m.sender, new Message( MessageType.PLAYERUPDATE, new Object[] { i, slots[i] } ) );
							break;
						}
					}
					break;
				case CONNECTED:
					si.sendToClient( m.sender, new Message( MessageType.SERVERSTATE, this.state ) );
					break;
				case CLIENTJOIN:
					Player player = new Player( m.sender );
					if( m.message != null )
					{
						String name = (String)m.message;
						player.name = name;
					}
					
					boolean foundSlot = false;
					for( int i = 0; i < 16; i++ )
					{
						if( slots[i] == null )
						{
							slots[i] = player;
							player.slot = i;
							foundSlot = true;
							break;
						}
					}
					
					if( !foundSlot )
					{
						si.sendToClient( m.sender, new Message( MessageType.KICK, "Sorry, the server is full." ) );
						si.sendToAllClients( new Message( MessageType.MESSAGE, player.name + " tried to join the game but the game is full." ) );
					}
					else
					{
						for( int i = 0; i < 16; i++ )
						{
							if( slots[i] != null )
							{
								si.sendToClient( m.sender, new Message( MessageType.PLAYERUPDATE, new Object[] { i, slots[i] } ) );
							}
						}
						si.sendToClient( m.sender, new Message( MessageType.LEVELUPDATE, new Object[] { selectedMap, maps } ) );
						si.sendToClient( m.sender, new Message( MessageType.FOGUPDATE, fogEnabled ) );
						si.sendToAllClients( new Message( MessageType.PLAYERUPDATE, new Object[] { player.slot, player } ) );
						si.sendToAllClients( new Message( MessageType.MESSAGE, player.name + " joined." ) );
					}
					break;
				case SETBOT:
				{
					Object[] oa = (Object[])m.message;
					int line = (Integer)oa[0];
					boolean isBot = (Boolean)oa[1];
					
					setBot( line, isBot, true );
					break;
				}
				case SETPLAYTYPE:	
				{
					Object[] oa = (Object[])m.message;
					int line = (Integer)oa[0];
					PlayType pt = (PlayType)oa[1];
					setPlayType( line, pt );
					break;
				}
				case MESSAGE:
				{
					String text = (String)m.message;
					for( int i = 0; i < slots.length; i++ )
					{
						if( slots[i] != null && slots[i].id == m.sender )
						{
							si.sendToAllClients( new Message( MessageType.MESSAGE, slots[i].name + ": " + text ) );
							break;
						}
					}
					break;
				}
				case STARTGAME:
				{
					si.sendToAllClients( new Message( MessageType.STARTGAME, null ) );
					setupServer();
					break;
				}
				case LEVELUPDATE:
				{
					selectedMap = (Integer)m.message;
					si.sendToAllClients( new Message( MessageType.LEVELUPDATE, new Object[] { selectedMap, maps } ) );
					try {
						l = LevelFileHelper.loadLevel( maps.get( selectedMap ) );
					} catch( DocumentException e ) {
						e.printStackTrace();
					}
					break;
				}
				case SWITCHTEAMS:
				{
					int target = (Integer)m.message;
					if( slots[target] == null )
					{
						for( int i = 0; i < slots.length; i++ )
						{
							if( slots[i] != null && slots[i].id == m.sender )
							{
								slots[target] = slots[i];
								slots[target].slot = target;
								slots[i] = null;
								si.sendToAllClients( new Message( MessageType.PLAYERUPDATE, new Object[] { i, null } ) );
								si.sendToAllClients( new Message( MessageType.PLAYERUPDATE, new Object[] { target, slots[target] } ) );
								break;
							}
						}
					}
					break;
				}
				case FOGUPDATE:
					fogEnabled = (Boolean)m.message;
					si.sendToAllClients( new Message( MessageType.FOGUPDATE, fogEnabled ) );
					break;
				case GAMETYPE:
					gameType = (GameType)m.message;
					si.sendToAllClients( new Message( MessageType.GAMETYPE, gameType ) );
					if( gameType == GameType.UMS )
					{
						for( int i = 0; i < l.slotOptions.length; i++ )
						{
							SlotOption so = l.slotOptions[i];
							switch( so.st )
							{
							case COMPUTER:
								setBot( i, true, false );
								setPlayType( i, so.bt );
								break;
							case PLAYER:
								setBot( i, false, true );
								break;
							}
						}
					}
					break;
				}
			}
			return;
		}
		
		//This isn't used at the moment, but we should be using it
		//float d = (System.currentTimeMillis() - sl.lastTime) / 60.f;
		if( sl.lastTime - lastTick > 100 )
		{
			lastTick += 100;
			tick++;
			
			if( state == ServerState.LOBBY ) return;
			
			js.tick( tick );
			
			//Every 100 ticks
			if( tick % 100 == 0 )
			{
				
				//Count how many units and points for each team for postgame stats
				int apoints = 0;
				int bpoints = 0;
				int aunits = 0;
				int bunits = 0;
				for( Building bu : l.buildings )
				{
					if( bu.t != null && bu.t.id == a.id )
					{
						apoints++;
					}
					else if( bu.t != null && bu.t.id == b.id )
					{
						bpoints++;
					}
				}
				for( int i = 0; i < units.size(); i++ )
				{
					Unit u = units.get( i );
					if( u.owner.team.id == a.id )
					{
						aunits++;
					}
					else
					{
						bunits++;
					}
				}
				
				gs.get( a ).pointCount.add( apoints );
				gs.get( a ).unitCount.add( aunits );
				gs.get( b ).pointCount.add( bpoints );
				gs.get( b ).unitCount.add( bunits );
				gs.totalPoints = l.buildings.size();
				
				((ServerNetworkInterface)si).printDebug();
				Player[] playerArr = new Player[players.entrySet().size()];
				int pi = 0;
				for( Entry<Integer, Player> e : players.entrySet() )
				{
					Player p = e.getValue();
					playerArr[pi++] = p;
				}
				si.sendToAllClients( new Message( MessageType.PLAYERLIST, playerArr ) );
			}
			
			//Every tick
			for( int i = 0; i < l.buildings.size(); i++ )
			{
				Building b = l.buildings.get( i );
				if( b.update( this ) )
				{
					si.sendToAllClients( new Message( MessageType.BUILDINGUPDATE, b ) );
				}
			}
		}
		while( si.hasServerMessages() )
		{
			Message m = si.getNextServerMessage();
			switch( m.messageType )
			{
			case CONNECTED:
				si.sendToClient( m.sender, new Message( MessageType.SERVERSTATE, this.state ) );
				break;
			case CLIENTJOIN:
			{
				si.sendToClient( m.sender, new Message( MessageType.KICK, "Game is in progress." ) );
				break;
			}
			case SETATTACKPOINT:
			{
				Object[] oa = (Object[])m.message;
				Point2i p = (Point2i)oa[0];
				ArrayList<Integer> selected = (ArrayList<Integer>)oa[1];
				for( Unit unit : units )
				{
					if( unit.owner.id == m.sender && selected.contains( unit.id ) )
					{
						unit.pathTo( p.x, p.y, this );
					}
					si.sendToAllClients( new Message( MessageType.UNITUPDATE, unit ) );
				}
				si.sendToClient( m.sender, new Message( MessageType.MOVESUCCESS, null ) );
				break;
			}
			case SETATTACKPOINTCONTINUE:
			{
				Object[] oa = (Object[])m.message;
				Point2i p = (Point2i)oa[0];
				ArrayList<Integer> selected = (ArrayList<Integer>)oa[1];
				for( Unit unit : units )
				{
					if( unit.owner.id == m.sender && selected.contains( unit.id ) )
					{
						unit.pathToContinue( p.x, p.y, this );
					}
				}
				si.sendToClient( m.sender, new Message( MessageType.MOVESUCCESS, null ) );
				break;
			}
			case LOOKTOWARD:
			{
				Object[] oa = (Object[])m.message;
				Point2i p = (Point2i)oa[0];
				ArrayList<Integer> selected = (ArrayList<Integer>)oa[1];
				for( Unit unit : units )
				{
					if( unit.owner.id == m.sender && selected.contains( unit.id ) )
					{
						unit.state = UnitState.TURNTO;
						unit.turnToAngle = (float) Math.atan2( p.y - unit.y, p.x - unit.x );
					}
				}
				break;
			}
			case BUILDUNIT:
			{
				Player player = players.get( m.sender );
				UnitType type = (UnitType)m.message;
				if( player.money >= type.price )
				{
					player.money -= type.price;
					Building base = null;
					for( Building bu : l.buildings )
					{
						if( bu.bt == BuildingType.CENTER && bu.t.id == player.team.id )
						{
							base = bu;
						}
					}
					if( base != null )
					{
						Unit u = new Unit( base.x, base.y, player );
						u.setType( type );
						u.stoppedAt = base;
						units.add( u );
						si.sendToAllClients( new Message( MessageType.UNITUPDATE, u ) );
						gs.get( u.owner.team ).unitsCreated++;
					}
				}
				si.sendToClient( m.sender, new Message( MessageType.PLAYERUPDATE, player ) );
				break;
			}
			case DISCONNECTED:
			{
				Player player = players.get( m.sender );
				if( player != null )
				{
					Player chosenPlayer = null;
					for( int i = 0; i < slots.length; i++ )
					{
						if( slots[i] != null && slots[i].team == player.team && slots[i].id != player.id )
						{
							chosenPlayer = slots[i];
							break;
						}
					}
					for( int i = 0; i < units.size(); i++ )
					{
						Unit u = units.get( i );
						if( chosenPlayer != null )
						{
							u.owner = chosenPlayer;
						}
						else
						{
							u.alive = false;
						}
					}
					players.remove( m.sender );
				}
				break;
			}
			case MESSAGE:
			{
				String text = (String)m.message;
				if( !text.startsWith( "/" ) )
				{
					si.sendToAllClients( new Message( MessageType.MESSAGE, players.get( m.sender ).name + ": " + text ) );
				}
				else
				{
					if( text.trim().startsWith( "/ping" ) )
					{
						try
						{
							String[] commands = text.split( " " );
							Player tp = players.get( m.sender );
							for( Entry<Integer, Player> e : players.entrySet() )
							{
								Player p = e.getValue();
								if( p.team.id == tp.team.id )
								{
									si.sendToClient( p.id, new Message( MessageType.PINGMAP, new Point2i( Integer.parseInt( commands[1] ), Integer.parseInt( commands[2] ) ) ) );
								}
							}
						} catch( Exception ex )
						{
							si.sendToClient( m.sender, new Message( MessageType.MESSAGE, "SERVER: Malformed command." ) );
						}
					}
					else if( text.trim().startsWith( "/team" ) )
					{
						try
						{
							String[] commands = text.split( " ", 2 );
							Player tp = players.get( m.sender );
							for( Entry<Integer, Player> e : players.entrySet() )
							{
								Player p = e.getValue();
								if( p.team.id == tp.team.id )
								{
									si.sendToClient( p.id, new Message( MessageType.MESSAGE, "(TEAM)" + players.get( m.sender ).name + ": " + commands[1] ) );
								}
							}
						} catch( Exception ex )
						{
							si.sendToClient( m.sender, new Message( MessageType.MESSAGE, "SERVER: Malformed command." ) );
						}
					}
				}
				break;
			}
			case UNITUPDATE:
			{
				Unit u = (Unit)m.message;
				Unit find = null;
				for( int i = 0; i < units.size(); i++ )
				{
					Unit tu = units.get( i );
					if( tu.id == u.id )
					{
						si.sendToClient( m.sender, new Message( MessageType.UNITUPDATE, tu ) );
						find = tu;
						break;
					}
				}
				if( find == null )
				{
					u.alive = false;
					u.health = 0;
					si.sendToClient( m.sender, new Message( MessageType.UNITUPDATE, u ) );
				}
			}
			}
		}
		
		for( int i = 0; i < units.size(); i++ )
		{
			Unit u = units.get( i );
			int update = u.update( this );
			if( update == 2 )
			{
				si.sendToAllClients( new Message( MessageType.UNITUPDATE, u ) );
			}
			else if( update == 1 )
			{
				si.sendToAllClients( new Message( MessageType.UNITMINIUPDATE, new Unit.UnitUpdate( u.id, u.x, u.y, u.heading, u.health ) ) );
			}
			
			if( !u.alive )
			{
				gs.get( u.owner.team ).unitsLost++;
				units.remove( i );
				i--;
			}
		}
		
		for( int i = 0; i < bullets.size(); i++ )
		{
			Bullet b = bullets.get( i );
			b.update( this );
			if( !b.alive )
			{
				bullets.remove( i );
				i--;
			}
		}
	}
	
	public void endGame()
	{
		//send stats to everyone
		si.sendToAllClients( new Message( MessageType.GAMEOVER, gs ) );
		
		//Clear everything
		units.clear();
		bullets.clear();
		l = null;
		players.clear();
	}
	
	public void addBullet( Unit u, float angle )
	{
		Bullet b = new Bullet( u.x + DMath.cosf( angle ) * (Unit.radius), u.y + DMath.sinf( angle ) * (Unit.radius), angle );
		b.owner = u.owner;
		b.shooter = u;
		b.damage = u.type.damage;
		gs.get( b.owner.team ).bulletsShot++;
		bullets.add( b );
		si.sendToAllClients( new Message( MessageType.BULLETUPDATE, b ) );
	}
	
	public void addBullet( Bullet b )
	{
		gs.get( b.owner.team ).bulletsShot++;
		bullets.add( b );
		si.sendToAllClients( new Message( MessageType.BULLETUPDATE, b ) );
	}
	
	public void updatePlayer( int id )
	{
		si.sendToClient( id, new Message( MessageType.PLAYERUPDATE, players.get( id ) ) );
	}
	
	public class ServerLoop implements Runnable 
	{
		long lastTime;
		long frameTime = (1000 / 30);
		long timeDiff;
		public boolean running = true;
		public ServerLoop()
		{
			
		}

		public void run() 
		{
			lastTime = System.currentTimeMillis();
			while( running )
			{
				try{
				update();
				} catch( Exception ex )
				{
					ex.printStackTrace();
				}
				long time = System.currentTimeMillis();
				timeDiff = (lastTime + frameTime) - time;
				if( timeDiff > 0 )
				{
					try {
						Thread.sleep( timeDiff );
					} catch( InterruptedException e ) {
						e.printStackTrace();
					}
				}
				lastTime = System.currentTimeMillis();
			}
			si.stop();
		}	
	}
	
	public enum ServerState
	{
		LOBBY,
		PLAYING;
	}
	
	public static void main( String[] args )
	{
		TacticServer ts = new TacticServer( new ServerNetworkInterface() );
		ts.begin();
	}
}
