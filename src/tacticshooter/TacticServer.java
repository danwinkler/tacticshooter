package tacticshooter;

import java.util.ArrayList;

import javax.vecmath.Point2i;

import org.newdawn.slick.util.pathfinding.AStarPathFinder;
import org.newdawn.slick.util.pathfinding.PathFinder;

import com.esotericsoftware.minlog.Log;
import com.phyloa.dlib.util.DMath;

public class TacticServer 
{
	ServerInterface si;
	
	ArrayList<Unit> units = new ArrayList<Unit>();
	ArrayList<Bullet> bullets = new ArrayList<Bullet>();
	Level l;
	PathFinder finder;
	
	public boolean running = true;
	
	Thread t;
	
	public TacticServer( ServerInterface si )
	{
		this.si = si;
	}
	
	public void begin()
	{
		l = new Level( 20, 20 );
		LevelBuilder.addBorder( l );
		LevelBuilder.addWall( l, 3, 5, 5, 5 );
		LevelBuilder.addWall( l, 5, 3, 5, 5 );
		
		LevelBuilder.addWall( l, 16, 14, 14, 14 );
		LevelBuilder.addWall( l, 14, 16, 14, 14 );
		
		LevelBuilder.addWall( l, 10, 0, 10, 7 );
		LevelBuilder.addWall( l, 9, 19, 9, 12 );
		
		finder = new AStarPathFinder( l, 500, false );
		
		t = new Thread( new ServerLoop() );
		t.start();
	}
	
	public void update()
	{
		if( Math.random() > .98 )
		{
			int x = DMath.randomi( 0, l.width );
			int y = DMath.randomi( 0, l.height );
			if( l.tiles[x][y] == 0 )
			{
				Unit u = new Unit( x*30+15, y*30+15 );
				u.team = DMath.randomi( -100, -1 );
				units.add( u );
			}
		}
		
		while( si.hasServerMessages() )
		{
			Message m = si.getNextServerMessage();
			switch( m.messageType )
			{
			case CLIENTJOIN:
				Unit u = new Unit( 60, 60 );
				u.team = m.sender;
				units.add( u );
				si.sendToClient( m.sender, new Message( MessageType.LEVELUPDATE, l ) );
				for( int i = 0; i < units.size(); i++ )
				{
					si.sendToClient( m.sender, new Message( MessageType.UNITUPDATE, units.get( i ) ) );
				}
				break;
			case SETATTACKPOINT:
				Point2i p = (Point2i)m.message;
				for( Unit unit : units )
				{
					if( unit.team == m.sender )
					{
						unit.pathTo( p.x, p.y, this );
					}
				}
				break;
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
	
	public void addBullet( float x, float y, float angle )
	{
		Bullet b = new Bullet( x, y, angle );
		bullets.add( b );
		si.sendToAllClients( new Message( MessageType.BULLETUPDATE, b ) );
	}
	
	private class ServerLoop implements Runnable 
	{
		long lastTime;
		long frameTime = (1000 / 30);
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
				long timeDiff = (lastTime + frameTime) - time;
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
		Log.set( Log.LEVEL_TRACE );
		TacticServer ts = new TacticServer( new ServerNetworkInterface() );
		ts.begin();
	}
}
