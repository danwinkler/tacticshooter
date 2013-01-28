package tacticshooter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;

import javax.vecmath.Point2i;

import com.esotericsoftware.kryonet.Connection;
import com.phyloa.dlib.util.DFile;
import com.phyloa.dlib.util.DMath;

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
	
	PlayType playType = PlayType.values()[DMath.randomi(0, PlayType.values().length)];
	
	public ComputerPlayer( ServerNetworkInterface si )
	{
		fc = new FakeConnection();
		ci = si;
	}
	
	public void run() 
	{
		String name = "DUDE";
		try 
		{
			String[] names = StaticFiles.names.split( "\n" );
			name = names[DMath.randomi( 0, names.length )].split( " " )[0];
		} catch( Exception ex )
		{
			ex.printStackTrace();
		}
		
		ci.sl.received( fc, new Message( MessageType.CLIENTJOIN, playType.name() + "_BOT_" + name ) );
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
						l.buildings.set( building.index, building );
					}
					break;
				case PLAYERLIST:
					break;
				case GAMEOVER:
					ci.sl.disconnected( fc );
					playing = false;
					break;
				}
			}
				
			if( player != null && l != null )
			{
				for( Unit u : units )
				{
					if( u.owner.id == player.id && (u.state == UnitState.STOPPED || Math.random() < .1f) )
					{
						if( playType == PlayType.SNEAKY )
						{
							Building closeb = null;
							for( Building b : l.buildings )
							{
								boolean wantToTake = b.t == null || b.t.id != player.team.id && b.isCapturable( l );
									
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
								if( b.t == null || b.t.id != player.team.id && b.isCapturable( l ) )
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
									wantToTake = b.t == null || b.t.id != player.team.id && b.isCapturable( l );
									break;
								case MODERATE:
									wantToTake = b.t == null || (b.t.id != player.team.id && b.isCapturable( l )) || (b.t.id == player.team.id && b.hold < Building.HOLDMAX);
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
			}
			
			try 
			{
				Thread.sleep( 1000 );
			} 
			catch( InterruptedException e )
			{
				e.printStackTrace();
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
		SNEAKY;
	}
}
