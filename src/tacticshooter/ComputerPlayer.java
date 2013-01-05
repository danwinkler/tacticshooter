package tacticshooter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;

import javax.vecmath.Point2i;

import com.esotericsoftware.kryonet.Connection;
import com.phyloa.dlib.util.DMath;

import tacticshooter.Unit.UnitState;

public class ComputerPlayer implements Runnable 
{
	ServerNetworkInterface ci;
	
	Player player;
	Level l;
	
	HashMap<Integer, Unit> unitMap = new HashMap<Integer, Unit>();
	ArrayList<Unit> units = new ArrayList<Unit>();
	FakeConnection fc;
	
	public ComputerPlayer( ServerNetworkInterface si )
	{
		fc = new FakeConnection();
		ci = si;
	}
	
	public void run() 
	{
		ci.sl.received( fc, new Message( MessageType.CLIENTJOIN, null ) );
		while( true )
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
				}
			}
				
			if( player != null && l != null )
			{
				for( Unit u : units )
				{
					if( u.owner.id == player.id && u.state == UnitState.STOPPED )
					{
						Building closeb = null;
						float closed2 = Float.MAX_VALUE;
						for( Building b : l.buildings )
						{
							if( b.t == null || b.t.id != player.team.id )
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
				
				if( player.money > 10 )
				{
					ci.sl.received( fc, new Message( MessageType.BUILDUNIT, null ) );
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

}
