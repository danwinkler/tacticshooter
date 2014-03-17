package com.danwink.tacticshooter;

import java.util.ArrayList;
import java.util.HashMap;
import javax.vecmath.Point2i;

import org.newdawn.slick.util.pathfinding.AStarPathFinder;
import org.newdawn.slick.util.pathfinding.Path;
import org.newdawn.slick.util.pathfinding.PathFinder;

import com.danwink.tacticshooter.ai.Aggressive;
import com.danwink.tacticshooter.ai.Masser;
import com.danwink.tacticshooter.ai.Moderate;
import com.danwink.tacticshooter.ai.Sneaky;
import com.danwink.tacticshooter.gameobjects.Building;
import com.danwink.tacticshooter.gameobjects.Level;
import com.danwink.tacticshooter.gameobjects.Player;
import com.danwink.tacticshooter.gameobjects.Unit;
import com.danwink.tacticshooter.gameobjects.Level.TileType;
import com.danwink.tacticshooter.gameobjects.Unit.UnitState;
import com.danwink.tacticshooter.gameobjects.Unit.UnitType;
import com.danwink.tacticshooter.network.FakeConnection;
import com.danwink.tacticshooter.network.Message;
import com.danwink.tacticshooter.network.ServerNetworkInterface;
import com.phyloa.dlib.util.DMath;

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
	
	public void setup( ServerNetworkInterface si )
	{
		fc = new FakeConnection();
		ci = si;
		ci.sl.connected( fc );
	}
	
	public abstract void update( PathFinder finder );
	
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
				finder = new AStarPathFinder( l, 500, StaticFiles.options.getB( "diagonalMove" ) );
				update( finder );
				
				if( player.money > 20 )
				{
					ci.sl.received( fc, new Message( MessageType.BUILDUNIT, UnitType.values()[DMath.randomi(0, UnitType.values().length)] ) );
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
	
	public enum PlayType
	{
		AGGRESSIVE( Aggressive.class ),
		SNEAKY( Sneaky.class ),
		MODERATE( Moderate.class ),
		MASSER( Masser.class );
		
		Class c;
		
		PlayType( Class c )
		{
			this.c = c;
		}
	}
}
