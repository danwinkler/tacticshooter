package com.danwink.tacticshooter;

import java.util.ArrayList;
import java.util.HashMap;

import jp.objectclub.vecmath.Point2f;

import org.newdawn.slick.util.pathfinding.AStarPathFinder;
import org.newdawn.slick.util.pathfinding.Path;
import org.newdawn.slick.util.pathfinding.PathFinder;

import com.danwink.tacticshooter.ai.Aggressive;
import com.danwink.tacticshooter.ai.Fortifier;
import com.danwink.tacticshooter.ai.Good2;
import com.danwink.tacticshooter.ai.Masser;
import com.danwink.tacticshooter.ai.Moderate;
import com.danwink.tacticshooter.ai.Passive;
import com.danwink.tacticshooter.ai.Sneaky;
import com.danwink.tacticshooter.gameobjects.Building;
import com.danwink.tacticshooter.gameobjects.Level;
import com.danwink.tacticshooter.gameobjects.Player;
import com.danwink.tacticshooter.gameobjects.Unit;
import com.danwink.tacticshooter.gameobjects.Level.TileType;
import com.danwink.tacticshooter.network.FakeConnection;
import com.danwink.tacticshooter.network.Message;
import com.danwink.tacticshooter.network.ServerNetworkInterface;

public abstract class ComputerPlayer implements Runnable 
{
	public ServerNetworkInterface ci;
	
	public Player player;
	public Level l;
	
	public HashMap<Integer, Unit> unitMap = new HashMap<Integer, Unit>();
	public ArrayList<Unit> units = new ArrayList<Unit>();
	public FakeConnection fc;
	
	boolean playing = true;
	
	public Player[] players = new Player[0];
	
	public PathFinder finder;
	
	public int sleepDuration = 1000;
	
	boolean hasSetup = false;
	
	public void setup( ServerNetworkInterface si )
	{
		fc = new FakeConnection();
		ci = si;
		ci.sl.connected( fc );
	}
	
	public abstract void update( PathFinder finder );
	
	@SuppressWarnings( "incomplete-switch" )
	public void run() 
	{
		while( playing )
		{
			while( fc.hasClientMessages() )
			{
				Message m = fc.getNextClientMessage();
				switch( m.messageType )
				{
				case UNITUPDATE:
					Unit u = (Unit)m.message;
					Unit tu = unitMap.get( u.id );
					if( tu == null )
					{
						unitMap.put( u.id, u );
						units.add( u );
						tu = u;
					}
					tu.sync( u );
					
					if( !u.alive )
					{
						units.remove( unitMap.get( u.id ) );
						unitMap.remove( u.id );
					}
					break;
				case LEVELUPDATE:
					l = (Level)m.message;
					break;
				case PLAYERUPDATE:
					this.player = (Player)m.message;
					break;
				case BUILDINGUPDATE:
					if( l != null )
					{
						Building building = (Building)m.message;
						for( int i = 0; i < l.buildings.size(); i++ )
						{
							Building b = l.buildings.get( i );
							if( b.id == building.id )
							{
								l.buildings.set( i, building );
							}
						}
					}
					break;
				case PLAYERLIST:
					this.players = (Player[])m.message;
					break;
				case TILEUPDATE:
					Object[] arr = (Object[])m.message;
					int tx = (Integer)arr[0];
					int ty = (Integer)arr[1];
					TileType change = (TileType)arr[2];
					l.tiles[tx][ty] = change;
					break;
				case GAMEOVER:
					ci.sl.disconnected( fc );
					playing = false;
					return;
				}
			}
			
			if( player != null && l != null )
			{	
				if( !hasSetup )
				{
					finder = new AStarPathFinder( l, 500, StaticFiles.options.getB( "diagonalMove" ) );
					hasSetup = true;
				}
				
				//If the AI code has a bug, don't stop the whole AI
				try 
				{
					update( finder );
				}
				catch( Exception e )
				{
					e.printStackTrace();
				}
				
				try 
				{	
					Thread.sleep( sleepDuration );
				} 
				catch( InterruptedException e )
				{
					e.printStackTrace();
				}
			}
		}
	}
	
	public Building findBuildingClosest( Point2f p, Filter<Building> f )
	{
		Building closeb = null;
		float closed2 = Float.MAX_VALUE;
		for( Building b : l.buildings )
		{
			if( f.valid( b ) )
			{
				float dx = p.x-b.x;
				float dy = p.y-b.y;
				float d2 = dx*dx + dy*dy;
				if( d2 < closed2 )
				{
					closeb = b;
					closed2 = d2;
				}
			}
		}
		return closeb;
	}
	
	public Building findBuildingShortestPath( Point2f p, PathFinder finder, Filter<Building> f )
	{
		Building closeb = null;
		float closeDist = Float.MAX_VALUE;
		for( Building b : l.buildings )
		{
			if( f.valid( b ) )
			{
				Path path = finder.findPath( null, l.getTileX( b.x ), l.getTileY( b.y ), l.getTileX( p.x ), l.getTileY( p.y ) );
				if( path == null ) continue;
				float d2 = path.getLength();
				if( d2 < closeDist )
				{
					closeDist = d2;
					closeb = b;
				}
			}
		}
		return closeb;
	}
	
	public enum PlayType
	{
		AGGRESSIVE( Aggressive.class ),
		SNEAKY( Sneaky.class ),
		MODERATE( Moderate.class ),
		MASSER( Masser.class ),
		FORTIFIER( Fortifier.class ),
		PASSIVE( Passive.class ),
		GOOD2( Good2.class );
		
		Class<? extends ComputerPlayer> c;
		
		PlayType( Class<? extends ComputerPlayer> c )
		{
			this.c = c;
		}
	}
	
	public abstract class Filter<E>
	{
		public Object[] o;
		
		public Filter()
		{
			
		}
		
		public Filter( Object... o )
		{
			this.o = o;
		}
		
		public abstract boolean valid( E e );
	}
}
