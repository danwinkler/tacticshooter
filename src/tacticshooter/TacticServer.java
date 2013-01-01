package tacticshooter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

import javax.vecmath.Point2i;

import org.newdawn.slick.util.pathfinding.AStarPathFinder;
import org.newdawn.slick.util.pathfinding.PathFinder;

import tacticshooter.Building.BuildingType;

import com.phyloa.dlib.util.DMath;

public class TacticServer 
{
	ServerInterface si;
	
	ArrayList<Team> teams = new ArrayList<Team>();
	
	ArrayList<Unit> units = new ArrayList<Unit>();
	ArrayList<Bullet> bullets = new ArrayList<Bullet>();
	
	HashMap<Integer, Player> players = new HashMap<Integer, Player>();
	Level l;
	PathFinder finder;
	
	public boolean running = true;
	
	Thread t;
	
	ServerLoop sl;
	
	long lastTick;
	int tick = 0;
	
	Team a = new Team();
	Team b = new Team();
	
	public TacticServer( ServerInterface si )
	{
		this.si = si;
	}
	
	public void begin()
	{
		l = new Level( 100, 100 );
		LevelBuilder.buildLevelB( l, a, b );
		
		finder = new AStarPathFinder( l, 500, false );
		
		sl = new ServerLoop();
		t = new Thread( sl );
		t.start();
		lastTick = System.currentTimeMillis();
	}
	
	public void update()
	{	
		if( sl.lastTime - lastTick > 100 )
		{
			lastTick += 100;
			tick++;
			
			
			//Every 100 ticks
			if( tick % 100 == 0 )
			{
				for( Entry<Integer, Player> e : players.entrySet() )
				{
					Player p = e.getValue();
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
					si.sendToClient( p.id, new Message( MessageType.PLAYERUPDATE, p ) );
				}
			}
			
			//Every tick
			for( Entry<Integer, Player> e : players.entrySet() )
			{
				Player p = e.getValue();
				boolean contains = false;
				for( Unit u : units )
				{
					if( u.owner == p )
					{
						contains = true;
						break;
					}
				}
				if( !contains )
				{
					Building base = null;
					for( Building bu : l.buildings )
					{
						if( bu.bt == BuildingType.CENTER && bu.t == p.team )
						{
							base = bu;
						}
					}
					if( base != null )
						units.add( new Unit( base.x, base.y, p ) );
				}
			}
			for( int i = 0; i < l.buildings.size(); i++ )
			{
				Building b = l.buildings.get( i );
				b.update( this );
				b.index = i;
				si.sendToAllClients( new Message( MessageType.BUILDINGUPDATE, b ) );
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
				player.team = Math.random() > .5 ? a : b;
				players.put( m.sender, player );
				
				si.sendToClient( m.sender, new Message( MessageType.PLAYERUPDATE, player ) );
				si.sendToClient( m.sender, new Message( MessageType.LEVELUPDATE, l ) );
				for( int i = 0; i < units.size(); i++ )
				{
					si.sendToClient( m.sender, new Message( MessageType.UNITUPDATE, units.get( i ) ) );
				}
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
				player.team = t.id == a.id ? b : a;
				
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
				if( player.money >= 100 )
				{
					player.money -= 100;
					si.sendToClient( m.sender, new Message( MessageType.PLAYERUPDATE, player ) );
					Building base = null;
					for( Building bu : l.buildings )
					{
						if( bu.bt == BuildingType.CENTER && bu.t == player.team )
						{
							base = bu;
						}
					}
					if( base != null )
						units.add( new Unit( base.x, base.y, player ) );
				}
				break;
			}
			case DISCONNECTED:
			{
				Player player = players.get( m.sender );
				for( int i = 0; i < units.size(); i++ )
				{
					Unit u = units.get( i );
					if( u.owner == null || u.owner.id == player.id )
					{
						u.alive = false;
					}
				}
				players.remove( m.sender );
				break;
			}
			}
		}
		
		for( int i = 0; i < units.size(); i++ )
		{
			Unit u = units.get( i );
			if( u.update( this ) )
			{
				si.sendToAllClients( new Message( MessageType.UNITUPDATE, u ) );
			}
			if( !u.alive )
			{
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
	
	public void addBullet( Unit u, float angle )
	{
		Bullet b = new Bullet( u.x + DMath.cosf( angle ) * (Unit.radius+5), u.y + DMath.sinf( angle ) * (Unit.radius+5), angle );
		b.owner = u.owner;
		bullets.add( b );
		si.sendToAllClients( new Message( MessageType.BULLETUPDATE, b ) );
	}
	
	private class ServerLoop implements Runnable 
	{
		long lastTime;
		long frameTime = (1000 / 30);
		long timeDiff;
		public ServerLoop()
		{
			
		}

		public void run() 
		{
			lastTime = System.currentTimeMillis();
			while( true )
			{
				update();
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
				lastTime = System.currentTimeMillis();
			}
		}	
	}
	
	public static void main( String[] args )
	{
		TacticServer ts = new TacticServer( new ServerNetworkInterface() );
		ts.begin();
	}
}
