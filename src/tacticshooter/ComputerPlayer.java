package tacticshooter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;

import javax.vecmath.Point2i;

import org.newdawn.slick.util.pathfinding.AStarPathFinder;
import org.newdawn.slick.util.pathfinding.Path;
import org.newdawn.slick.util.pathfinding.PathFinder;

import com.esotericsoftware.kryonet.Connection;
import com.phyloa.dlib.util.DFile;
import com.phyloa.dlib.util.DMath;

import tacticshooter.Building.BuildingType;
import tacticshooter.Level.TileType;
import tacticshooter.Unit.UnitState;
import tacticshooter.Unit.UnitType;

public class ComputerPlayer implements Runnable 
{
	ServerNetworkInterface ci;
	
	Player player;
	Level l;
	
	HashMap<Integer, Unit> unitMap = new HashMap<Integer, Unit>();
	ArrayList<Unit> units = new ArrayList<Unit>();
	FakeConnection fc;
	
	boolean playing = true;
	
	PlayType playType;
	
	Player[] players = new Player[0];
	
	PathFinder finder;
	
	//MASSER:
	boolean attacking = false;
	ArrayList<Unit> attackForce = new ArrayList<Unit>();
	float attackPropensity = DMath.randomf( 1.5f, 4 );
	Building target;
	Building closeb;
	
	public ComputerPlayer( ServerNetworkInterface si )
	{
		fc = new FakeConnection();
		ci = si;
		ci.sl.connected( fc );
	}
	
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
				if( playType == PlayType.MASSER )
				{
					//Find enemy point with most units
					int maxUnits = 0;
					for( Building b : l.buildings )
					{
						if( b.t != null && b.t.id != this.player.team.id )
						{
							int count = 0;
							for( Unit u : units )
							{
								if( u.stoppedAt != null && u.stoppedAt.id == b.id )
								{
									count++;
								}
							}
							if( count > maxUnits )
							{
								maxUnits = count;
								target = b;
							}
						}
					}
					
					if( target == null ) continue;
					//Find closest friendly building to target
					closeb = null;
					float closeDist = Float.MAX_VALUE;
					for( Building b : l.buildings )
					{
						if( b.t != null && b.t.id == this.player.team.id )
						{
							Path p = finder.findPath( null, l.getTileX( b.x ), l.getTileY( b.y ), l.getTileX( target.x ), l.getTileY( target.y ) );
							if( p == null ) continue;
							float d2 = p.getLength();
							if( d2 < closeDist )
							{
								closeDist = d2;
								closeb = b;
							}
						}
					}
					
					if( !attacking )
					{
						//count own units, enemy units
						int ownUnits = 0;
						float enemyUnits = 0;
						for( Unit u : units )
						{
							if( u.owner.id == player.id && u.stoppedAt != null && u.stoppedAt.id == closeb.id )
							{
								ownUnits++;
							} 
							else if( u.stoppedAt != null && u.stoppedAt.id == target.id )
							{
								enemyUnits++;
							}
						}
						
						if( players.length == 0 ) continue;
						
						if( ownUnits >= (enemyUnits * attackPropensity) || Math.random() < .0025 )
						{
							attacking = true;
							
							attackForce.clear();
							
							ArrayList<Integer> selected = new ArrayList<Integer>();
							
							for( Unit u : units )
							{
								if( u.owner.id == player.id )
								{
									attackForce.add( u );
									selected.add( u.id );
								}
							}
							
							ci.sl.received( fc, new Message( MessageType.SETATTACKPOINT, new Object[]{ new Point2i( target.x/Level.tileSize, target.y/Level.tileSize ), selected } ) );
						}
					}
					else
					{
						for( int i = 0; i < attackForce.size(); i++ )
						{
							Unit u = unitMap.get( attackForce.get( i ).id );
							if( u == null || !u.alive )
							{
								attackForce.remove( i );
								i--;
							}
							else
							{
								//Not necessary now that guys don't lose their path
								//ArrayList<Integer> selected = new ArrayList<Integer>();
								//selected.add( u.id );
								//ci.sl.received( fc, new Message( MessageType.SETATTACKPOINT, new Object[]{ new Point2i( target.x/Level.tileSize, target.y/Level.tileSize ), selected } ) );
							}
						}
						if( attackForce.size() == 0 || (target.t != null && target.t.id == player.team.id) )
						{
							attacking = false;
							attackPropensity = DMath.randomf( 1.5f, 4f );
						}
					}
				}
				
				for( Unit u : units )
				{
					if( u.owner.id == player.id && (u.state == UnitState.STOPPED || (Math.random() < .1f && playType != PlayType.SNEAKY) ) )
					{
						if( playType == PlayType.MASSER )
						{
							if( !attacking )
							{
								if( closeb == null ) break;
								if( u.stoppedAt != null && u.stoppedAt == closeb ) break;
								
								ArrayList<Integer> selected = new ArrayList<Integer>();
								selected.add( u.id );
								ci.sl.received( fc, new Message( MessageType.SETATTACKPOINT, new Object[]{ new Point2i( closeb.x/Level.tileSize, closeb.y/Level.tileSize ), selected } ) );
							}
						}
						else if( playType == PlayType.SNEAKY )
						{
							Building closeb = null;
							for( Building b : l.buildings )
							{
								boolean wantToTake = b.isCapturable( l, u, finder ) && (b.t == null || b.t.id != player.team.id);
									
								if( wantToTake )
								{
									float dx = u.x-b.x;
									float dy = u.y-b.y;
									float d2 = dx*dx + dy*dy;
									if( d2 < 50 * 50 )
									{
										closeb = b;
										break;
									}
								}
							}
							if( closeb == null )
							{
								Building b = l.buildings.get( DMath.randomi( 0, DMath.randomi( 0, l.buildings.size() ) ) );
								if( b.isCapturable( l, u, finder ) && (b.t == null || b.t.id != player.team.id) )
								{
									ArrayList<Integer> selected = new ArrayList<Integer>();
									selected.add( u.id );
									ci.sl.received( fc, new Message( MessageType.SETATTACKPOINT, new Object[]{ new Point2i( b.x/Level.tileSize, b.y/Level.tileSize ), selected } ) );
								}
							}
						}
						else
						{
							Building closeb = null;
							float closed2 = Float.MAX_VALUE;
							for( Building b : l.buildings )
							{
								boolean wantToTake = false;
								switch( playType )
								{
								case AGGRESSIVE:
									wantToTake = (b.t == null || b.t.id != player.team.id) && b.isCapturable( l, u, finder );
									break;
								case MODERATE:
									wantToTake = b.isCapturable( l, u, finder ) && (b.t == null || (b.t.id != player.team.id) || (b.t.id == player.team.id && b.hold < Building.HOLDMAX));
									break;
								}
								if( wantToTake )
								{
									float dx = u.x-b.x;
									float dy = u.y-b.y;
									float d2 = dx*dx + dy*dy;
									if( d2 < closed2 )
									{
										closeb = b;
										closed2 = d2;
									}
								}
							}
							if( closeb != null )
							{
								ArrayList<Integer> selected = new ArrayList<Integer>();
								selected.add( u.id );
								ci.sl.received( fc, new Message( MessageType.SETATTACKPOINT, new Object[]{ new Point2i( closeb.x/Level.tileSize, closeb.y/Level.tileSize ), selected } ) );
							}
						}
					}
				}
				
				if( player.money > 20 )
				{
					ci.sl.received( fc, new Message( MessageType.BUILDUNIT, UnitType.values()[DMath.randomi(0, UnitType.values().length)] ) );
				}
				
				try 
				{	
					Thread.sleep( 1000 );
					if( playType == PlayType.MASSER )
					{
						Thread.sleep( 1000 );
					}
				} 
				catch( InterruptedException e )
				{
					e.printStackTrace();
				}
			}
		}
	}
	
	public class FakeConnection extends Connection
	{
		int id = DMath.randomi( 0, Integer.MAX_VALUE );
		LinkedList<Message> messages = new LinkedList<Message>();
		
		public int getID()
		{
			return id;
		}
		
		public int sendTCP( Object o )
		{
			synchronized( messages )
			{
				messages.push( (Message)o );
			}
			return 0;
		}
		
		public Message getNextClientMessage()
		{
			synchronized( messages )
			{
				return messages.pop();
			}
		}

		public boolean hasClientMessages() 
		{
			return !messages.isEmpty();
		}
	}

	public enum PlayType
	{
		AGGRESSIVE,
		MODERATE,
		SNEAKY,
		MASSER;
	}
}
