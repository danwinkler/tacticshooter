package tacticshooter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map.Entry;
import java.util.TreeSet;

import javax.vecmath.Point2i;

import org.dom4j.DocumentException;
import org.newdawn.slick.util.pathfinding.AStarPathFinder;
import org.newdawn.slick.util.pathfinding.PathFinder;

import tacticshooter.Building.BuildingType;
import tacticshooter.Unit.UnitType;

import com.phyloa.dlib.util.DFile;
import com.phyloa.dlib.util.DMath;

/**
 * Handles all of the game logic, and runs the server
 * 
 * @author Daniel Winkler
 *
 */
public class TacticServer 
{
	ServerInterface si;
	
	int botCount = StaticFiles.options.getI( "botCount" );
	
	ArrayList<Team> teams = new ArrayList<Team>();
	
	ArrayList<Unit> units = new ArrayList<Unit>();
	ArrayList<Bullet> bullets = new ArrayList<Bullet>();
	
	HashMap<Integer, Player> players = new HashMap<Integer, Player>();
	Level l;
	PathFinder finder;
	
	public boolean running = true;
	
	Thread t;
	
	public ServerLoop sl;
	
	long lastTick;
	int tick = 0;
	
	Team a = Team.a;
	Team b = Team.b;
	
	boolean onTeam = false;
	
	GameStats gs = new GameStats();
	
	ArrayList<String> maps = new ArrayList<String>();
	int onMap = 0;
	
	int binSize = 5;
	float binOffset = (binSize*Level.tileSize)/4;
	Bin[][] bins;
	
	public TacticServer( ServerInterface si )
	{
		this.si = si;
	}
	
	public void begin()
	{
		Collections.shuffle( maps );
		gs.setup( a, b );
		
		try
		{
			String mapFile = DFile.loadText( "mapList.txt" );
			String[] mapArr = mapFile.split( "\n" );
			for( String s : mapArr )
			{
				maps.add( s );
			}
			Collections.shuffle( maps );
			l = LevelFileHelper.loadLevel( maps.get( onMap ) );
		} catch( IOException e )
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch( DocumentException e )
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finder = new AStarPathFinder( l, 500, StaticFiles.options.getB( "diagonalPath" ) );
		
		bins = new Bin[l.width/binSize + 1][l.height/binSize + 1];
		for( int y = 0; y < l.height/binSize; y++ )
		{
			for( int x = 0; x < l.width/binSize; x++ )
			{
				bins[x][y] = new Bin();
			}
		}
		
		sl = new ServerLoop();
		t = new Thread( sl );
		t.start();
		lastTick = System.currentTimeMillis();
		
		for( int i = 0; i < botCount; i++ )
		{
			Thread ct = new Thread( new ComputerPlayer( (ServerNetworkInterface)si ) );
			ct.start();
		}
	}
	
	public void update()
	{	
		float d = (System.currentTimeMillis() - sl.lastTime) / 60.f;
		if( sl.lastTime - lastTick > 100 )
		{
			lastTick += 100;
			tick++;
			
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
					int bc = 0;
					for( int i = 0; i < l.buildings.size(); i++ )
					{
						Building b = l.buildings.get( i );
						if( b.t != null && b.t.id == p.team.id )
						{
							bc++;
						}
					}
					p.money += bc;
					gs.get( p.team ).moneyEarned += bc;
					si.sendToClient( p.id, new Message( MessageType.PLAYERUPDATE, p ) );
				}
				
				HashMap<Integer, Integer> map = new HashMap<Integer, Integer>();
				for( int i = 0; i < playerArr.length; i++ )
				{
					Player p = playerArr[i];
					if( !map.containsKey( p.team.id ) )
					{
						map.put( p.team.id, 0 );
					}
					int a = map.get( p.team.id );
					a++;
					map.put( p.team.id, a );
				}
				ArrayList<Entry<Integer, Integer>> list = new ArrayList<Entry<Integer, Integer>>();
				for( Entry<Integer, Integer> e : map.entrySet() )
				{
					list.add( e );
				}
				Collections.sort( list, new Comparator<Entry<Integer, Integer>>() {
					public int compare( Entry<Integer, Integer> arg0, Entry<Integer, Integer> arg1 )
					{
						return arg1.getValue() - arg0.getValue();
					} 
				} );
				System.out.println( list );
				if( list.get( 0 ).getValue() - list.get( 1 ).getValue() > 1 )
				{
					for( int i = 0; i < playerArr.length; i++ )
					{
						Player player = playerArr[i];
						if( player.isBot && player.team.id == list.get( 0 ).getKey() )
						{
							player.respawn = 0;
							player.team = player.team.id == a.id ? b : a;
							
							for( Unit u : units )
							{
								if( u.owner == null || u.owner.id == player.id )
								{
									u.alive = false;
								}
							}
							
							si.sendToClient( player.id, new Message( MessageType.PLAYERUPDATE, player ) );
							break;
						}
					}
				}
				
				si.sendToAllClients( new Message( MessageType.PLAYERLIST, playerArr ) );
				
				Building first = l.buildings.get( 0 );
				if( first.t != null )
				{
					boolean won = true;
					for( int i = 1; i < l.buildings.size(); i++ )
					{
						Team test = l.buildings.get( i ).t;
						if( test != null && test.id != first.t.id )
						{
							won = false;
							break;
						}
					}
					
					if( won )
					{
						nextMap();
						return;
					}
				}
			}
			
			//Every 10 ticks test to see if player has no units. If the player has no units and it's been a little while, give them a unit.
			if( tick % 10 == 0 )
			{
				for( Entry<Integer, Player> e : players.entrySet() )
				{
					Player p = e.getValue();
					boolean contains = false;
					for( Unit u : units )
					{
						if( u.owner.id == p.id )
						{
							contains = true;
							break;
						}
					}
					if( !contains )
					{
						if( p.respawn > 0 )
						{
							p.respawn--;
						}
						else
						{
							p.respawn = Player.MAX_RESPAWN;
							Building base = null;
							for( Building bu : l.buildings )
							{
								if( bu.bt == BuildingType.CENTER && bu.t.id == p.team.id )
								{
									base = bu;
								}
							}
							if( base != null )
							{
								Unit u = new Unit( base.x, base.y, p );
								units.add( u );
								si.sendToAllClients( new Message( MessageType.UNITUPDATE, u.getPacket() ) );
								gs.get( u.owner.team ).unitsCreated++;
							}
						}
					}
				}
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
			case CLIENTJOIN:
			{
				Player player = new Player( m.sender );
				if( m.message != null )
				{
					String name = (String)m.message;
					player.name = name;
					if( name.startsWith( "BOT" ) )
					{
						player.isBot = true;
					}
				}
				player.team = onTeam ? a : b;
				onTeam = !onTeam;
				players.put( m.sender, player );
				
				si.sendToClient( m.sender, new Message( MessageType.PLAYERUPDATE, player ) );
				si.sendToClient( m.sender, new Message( MessageType.LEVELUPDATE, l ) );
				for( int i = 0; i < units.size(); i++ )
				{
					si.sendToClient( m.sender, new Message( MessageType.UNITUPDATE, units.get( i ).getPacket() ) );
				}
				si.sendToAllClients( new Message( MessageType.MESSAGE, player.name + " has joined the game." ) );
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
				}
				si.sendToClient( m.sender, new Message( MessageType.MOVESUCCESS, null ) );
				break;
			}
			case SWITCHTEAMS:
			{
				Team t = (Team)m.message;
				Player player = players.get( m.sender );
				player.respawn = 0;
				player.team = t.id == a.id ? b : a;
				player.money = 0;
				
				for( Unit u : units )
				{
					if( u.owner == null || u.owner.id == player.id )
					{
						u.alive = false;
					}
				}
				
				si.sendToClient( m.sender, new Message( MessageType.PLAYERUPDATE, player ) );
				break;
			}
			case BUILDUNIT:
			{
				Player player = players.get( m.sender );
				UnitType type = (UnitType)m.message;
				if( player.money >= type.price )
				{
					player.money -= type.price;
					si.sendToClient( m.sender, new Message( MessageType.PLAYERUPDATE, player ) );
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
						units.add( u );
						si.sendToAllClients( new Message( MessageType.UNITUPDATE, u.getPacket() ) );
						gs.get( u.owner.team ).unitsCreated++;
					}
				}
				break;
			}
			case DISCONNECTED:
			{
				Player player = players.get( m.sender );
				if( player != null )
				{
					for( int i = 0; i < units.size(); i++ )
					{
						Unit u = units.get( i );
						if( u.owner == null || u.owner.id == player.id )
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
					if( text.trim().equals( "/endmap" ) )
					{
						nextMap();
						return;
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
						si.sendToClient( m.sender, new Message( MessageType.UNITUPDATE, tu.getPacket() ) );
						find = tu;
						break;
					}
				}
				if( find == null )
				{
					u.alive = false;
					u.health = 0;
					si.sendToClient( m.sender, new Message( MessageType.UNITUPDATE, u.getPacket() ) );
				}
			}
			}
		}
		
		for( int i = 0; i < units.size(); i++ )
		{
			Unit u = units.get( i );
			if( u.update( this, d ) || !u.alive )
			{
				si.sendToAllClients( new Message( MessageType.UNITUPDATE, u.getPacket() ) );
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
	
	public void nextMap()
	{
		//reset
		si.sendToAllClients( new Message( MessageType.GAMEOVER, gs ) );
		
		//kill all guys
		for( int i = 0; i < units.size(); i++ )
		{
			units.get( i ).alive = false;
		}
		for( Entry<Integer, Player> e : players.entrySet() )
		{
			Player p = e.getValue();
			p.money = 0;
			si.sendToClient( p.id, new Message( MessageType.PLAYERUPDATE, p ) );
		}
		//make new level
		gs.setup( a, b );
		//l = new Level( 100, 100 );
		//LevelBuilder.buildLevelB( l, a, b );
		onMap = (onMap + 1) % maps.size();
		try
		{
			l = LevelFileHelper.loadLevel( maps.get( onMap ) );
		} catch( DocumentException e )
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finder = new AStarPathFinder( l, 500, StaticFiles.options.getB( "diagonalPath" ) );
		for( int i = 0; i < botCount; i++ )
		{
			Thread ct = new Thread( new ComputerPlayer( (ServerNetworkInterface)si ) );
			ct.start();
		}
	}
	
	public void addBullet( Unit u, float angle )
	{
		Bullet b = new Bullet( u.x + DMath.cosf( angle ) * (Unit.radius), u.y + DMath.sinf( angle ) * (Unit.radius), angle );
		b.owner = u.owner;
		b.shooter = u;
		gs.get( b.owner.team ).bulletsShot++;
		bullets.add( b );
		si.sendToAllClients( new Message( MessageType.BULLETUPDATE, b ) );
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
				long time = System.currentTimeMillis();
				timeDiff = (lastTime + frameTime) - time;
				if( timeDiff > 0 )
				{
					try {
						Thread.sleep( timeDiff );
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				update();
				lastTime = System.currentTimeMillis();
			}
			si.stop();
		}	
	}
	
	public class Bin
	{
		LinkedHashSet<Unit> units = new LinkedHashSet<Unit>();
		
		public void add( Unit u )
		{
			units.add( u );
		}
		
		public void remove( Unit u )
		{
			units.remove( u );
		}
	}
	
	public Bin getBin( float x, float y )
	{
		int tx = l.getTileX( x );
		int ty = l.getTileY( y );
		if( tx < 0 || tx >= l.width || ty < 0 || ty >= l.height )
		{
			return null;
		}
		return bins[tx/binSize][ty/binSize];
	}
	
	public static void main( String[] args )
	{
		TacticServer ts = new TacticServer( new ServerNetworkInterface() );
		ts.begin();
	}
}
