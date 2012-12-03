package tacticshooter;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;

import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.Server;

public class ServerNetworkInterface implements ServerInterface 
{
	Server server;
	LinkedList<Message> messages = new LinkedList<Message>();
	HashMap<Integer, Connection> connections = new HashMap<Integer, Connection>();
	
	public ServerNetworkInterface()
	{
		server = new Server( 32000, 32000 );
		KryoHelper.register( server.getKryo() );
		server.start();
		try {
			server.bind(54555, 54777);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		server.addListener( new Listener() {
			public void received( Connection c, Object o ) 
			{
				if( connections.get( c.getID() ) == null )
				{
					connections.put( c.getID(), c );
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
		});
	}
	
	public void sendToClient( int id, Message m )
	{
		connections.get( id ).sendTCP( m );
	}

	public void sendToAllClients( Message m ) 
	{
		server.sendToAllTCP( m );
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

}
