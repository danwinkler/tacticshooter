package tacticshooter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;

import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.Server;

public class ServerNetworkInterface implements ServerInterface 
{
	public static final boolean DEBUG = false;
	static int[] messageCount;
	
	static {
		if( DEBUG )
		{
			messageCount = new int[MessageType.values().length];
		}
	}
	
	Server server;
	LinkedList<Message> messages = new LinkedList<Message>();
	HashMap<Integer, Connection> connections = new HashMap<Integer, Connection>();
	ArrayList<Connection> connectionsArr = new ArrayList<Connection>();
	ServerListener sl;
	
	public ServerNetworkInterface()
	{
		server = new Server( 512000, 64000 );
		KryoHelper.register( server.getKryo() );
		server.start();
		try {
			server.bind( 54555, 54777 );
		} catch (IOException e) {
			e.printStackTrace();
		}
		sl = new ServerListener();
		server.addListener( sl );
	}
	
	public void sendToClient( int id, Message m )
	{
		synchronized( messages )
		{
			Connection c = connections.get( id );
			if( c != null )
			{
				c.sendTCP( m );
				if( DEBUG )
				{
					messageCount[m.messageType.ordinal()]++;
				}
			}
		}
	}

	public void sendToAllClients( Message m ) 
	{
		synchronized( connectionsArr )
		{
			for( int i = 0; i < connectionsArr.size(); i++ )
			{
				if( connectionsArr.get( i ) == null ) break;
				connectionsArr.get( i ).sendTCP( m );
				if( DEBUG )
				{
					messageCount[m.messageType.ordinal()]++;
				}
			}
		}
	}

	public Message getNextServerMessage()
	{
		synchronized( messages )
		{
			return messages.pop();
		}
	}

	public boolean hasServerMessages() 
	{
		return !messages.isEmpty();
	}
	
	class ServerListener extends Listener
	{
		public void received( Connection c, Object o ) 
		{
			if( connections.get( c.getID() ) == null )
			{
				connections.put( c.getID(), c );
				connectionsArr.add( c );
			}
			if( o instanceof Message )
			{
				synchronized( messages )
				{
					Message m = (Message)o;
					m.sender = c.getID();
					messages.push( m );
				}
			}
		}
		
		public void disconnected( Connection c )
		{
			synchronized( messages )
			{
				synchronized( connectionsArr )
				{
					Message m = new Message();
					m.message = c.getID();
					m.sender = c.getID();
					m.messageType = MessageType.DISCONNECTED;
					messages.push( m );
					connections.remove( c.getID() );
					connectionsArr.remove( c );
				}
			}
		}
	}
	
	public void printDebug()
	{
		if( DEBUG )
		{
			System.out.println( "MESSAGETYPE BREAKDOWN" );
			for( MessageType t : MessageType.values() )
			{
				System.out.println( t.name() + " " + messageCount[t.ordinal()] );
			}
			System.out.println( "-------------------------" );
			System.out.println( "" );
		}
	}

	@Override
	public void stop()
	{
		server.close();
	}
}
