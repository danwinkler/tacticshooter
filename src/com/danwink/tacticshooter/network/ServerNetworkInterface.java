package com.danwink.tacticshooter.network;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedDeque;

import com.danwink.tacticshooter.KryoHelper;
import com.danwink.tacticshooter.MessageType;
import com.danwink.tacticshooter.gameobjects.Unit;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.Server;

public class ServerNetworkInterface implements ServerInterface 
{
	public static final boolean DEBUG = false;
	public static final int WRITE_BUFFER = 5000000;
	public static final int OBJECT_BUFFER = 32000; 
	
	Server server;
	ConcurrentLinkedDeque<Message> messages = new  ConcurrentLinkedDeque<Message>();
	ConcurrentLinkedDeque<Object[]> messagesToSend = new  ConcurrentLinkedDeque<Object[]>();
	HashMap<Integer, Connection> connections = new HashMap<Integer, Connection>();
	ArrayList<Connection> connectionsArr = new ArrayList<Connection>();
	public ServerListener sl;
	MessageSender ms;
	Thread mst;
	boolean stopped;
	
	public ServerNetworkInterface()
	{
		server = new Server( WRITE_BUFFER, OBJECT_BUFFER );
		KryoHelper.register( server.getKryo() );
		server.start();
		try {
			server.bind( 54555, 54777 );
		} catch (IOException e) {
			e.printStackTrace();
		}
		sl = new ServerListener();
		server.addListener( sl );
		ms = new MessageSender();
		mst = new Thread( ms );
		mst.start();
	}
	
	public void sendToClient( int id, Message m )
	{
		synchronized( connections )
		{
			Connection c = connections.get( id );
			if( c != null )
			{
				messagesToSend.addLast( new Object[] { id, m } );
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
				sendToClient( connectionsArr.get( i ).getID(), m );
			}
		}
	}

	public Message getNextServerMessage()
	{	
		return messages.removeFirst();
	}
	

	public boolean hasServerMessages() 
	{
		return !messages.isEmpty();
	}
	
	public class ServerListener extends Listener
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
				Message m = (Message)o;
				m.sender = c.getID();
				messages.addLast( m );
			}
		}
		
		public void connected( Connection c )
		{
			synchronized( connectionsArr )
			{
				Message m = new Message();
				m.message = c.getID();
				m.sender = c.getID();
				m.messageType = MessageType.CONNECTED;
				messages.push( m );
				if( connections.get( c.getID() ) == null )
				{
					connections.put( c.getID(), c );
					connectionsArr.add( c );
				}
			}
		}
		
		public void disconnected( Connection c )
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

	@Override
	public void stop()
	{
		server.close();
		stopped = true;
	}
	
	public class MessageSender implements Runnable
	{
		public void run()
		{
			while( !stopped )
			{
				Iterator<Object[]> i = messagesToSend.iterator();
				while( i.hasNext() )
				{
					Object[] o = i.next();
					Integer id = (Integer)o[0];
					Message m = (Message)o[1];
					synchronized( connections )
					{
						Connection c = connections.get( id );
						if( c == null )
						{
							i.remove();
							continue;
						}
						int bytes = 0;
						
						try 
						{
							bytes = c.getTcpWriteBufferSize();
						}
						catch( NullPointerException e ){}
						
						if( bytes < WRITE_BUFFER - OBJECT_BUFFER ) 
						{
							c.sendTCP( m );
							i.remove();
						}
						else
						{
							if( m.messageType == MessageType.UNITUPDATE )
							{
								Unit u = (Unit)m.message;
								if( u.alive )
								{
									i.remove();
								}
							}
						}
					}
				}
				
				try
				{
					Thread.sleep( 10 );
				}
				catch( InterruptedException e )
				{
					e.printStackTrace();
				}
			}
		}
	}
}
