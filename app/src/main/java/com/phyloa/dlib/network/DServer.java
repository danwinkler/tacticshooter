package com.phyloa.dlib.network;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;

import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.Server;

public class DServer<E> implements ClassRegister
{
	public static final boolean DEBUG = false;
	static int[] messageCount;
	
	static {
		if( DEBUG )
		{
			messageCount = new int[DMessageType.values().length];
		}
	}
	
	Server server;
	LinkedList<DMessage<E>> messages = new LinkedList<DMessage<E>>();
	HashMap<Integer, Connection> connections = new HashMap<Integer, Connection>();
	ArrayList<Connection> connectionsArr = new ArrayList<Connection>();
	ServerListener sl;
	
	public DServer( int writeBuffer, int objectBuffer )
	{
		server = new Server( writeBuffer, objectBuffer );
		
		server.getKryo().register( DMessage.class );
		server.getKryo().register( DMessageType.class );
	}
	
	public void start( int portTCP, int portUDP )
	{
		server.start();
		try {
			server.bind( portTCP, portUDP );
		} catch (IOException e) {
			e.printStackTrace();
		}
		sl = new ServerListener();
		server.addListener( sl );
	}
	
	public void sendToClient( int id, E e )
	{
		synchronized( connectionsArr )
		{
			Connection c = connections.get( id );
			if( c != null )
			{
				DMessage<E> m = new DMessage<E>( e, DMessageType.DATA, -1 );
				c.sendTCP( m );
			}
		}
	}

	public void sendToAllClients( E e ) 
	{
		synchronized( connectionsArr )
		{
			DMessage<E> m = new DMessage<E>( e, DMessageType.DATA, -1 );
			for( int i = 0; i < connectionsArr.size(); i++ )
			{
				if( connectionsArr.get( i ) == null ) break;
				connectionsArr.get( i ).sendTCP( m );
			}
		}
	}
	
	public void sendToClientUDP( int id, E e )
	{
		synchronized( connectionsArr )
		{
			Connection c = connections.get( id );
			if( c != null )
			{
				DMessage<E> m = new DMessage<E>( e, DMessageType.DATA, -1 );
				c.sendUDP( m );
			}
		}
	}

	public void sendToAllClientsUDP( E e ) 
	{
		synchronized( connectionsArr )
		{
			DMessage<E> m = new DMessage<E>( e, DMessageType.DATA, -1 );
			for( int i = 0; i < connectionsArr.size(); i++ )
			{
				if( connectionsArr.get( i ) == null ) break;
				connectionsArr.get( i ).sendUDP( m );
			}
		}
	}

	public DMessage<E> getNextServerMessage()
	{
		synchronized( messages )
		{
			return messages.pop();
		}
	}
	
	public void register( Class c )
	{
		server.getKryo().register( c );
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
			if( o instanceof DMessage<?> )
			{
				synchronized( messages )
				{
					DMessage<E> m = (DMessage<E>)o;
					m.sender = c.getID();
					messages.push( m );
				}
			}
		}
		
		public void connected( Connection c )
		{
			synchronized( messages )
			{
				
				DMessage<E> m = new DMessage<E>();
				m.message = null;
				m.sender = c.getID();
				m.messageType = DMessageType.CONNECTED;
				messages.push( m );
			}
			synchronized( connectionsArr )
			{
				if( connections.get( c.getID() ) == null )
				{
					connections.put( c.getID(), c );
					connectionsArr.add( c );
				}
			}
		}
		
		public void disconnected( Connection c )
		{
			synchronized( messages )
			{		
				DMessage<E> m = new DMessage<E>();
				m.message = null;
				m.sender = c.getID();
				m.messageType = DMessageType.DISCONNECTED;
				messages.push( m );
			}
			synchronized( connectionsArr )
			{
				connections.remove( c.getID() );
				connectionsArr.remove( c );
			}
		}
	}

	public void stop()
	{
		server.close();
	}
}
