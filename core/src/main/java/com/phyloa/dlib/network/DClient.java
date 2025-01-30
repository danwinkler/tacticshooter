package com.phyloa.dlib.network;

import java.io.IOException;
import java.util.LinkedList;

import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;

public class DClient<E> implements ClassRegister
{
	boolean running = false;
	public Client client;
	LinkedList<DMessage<E>> messages = new LinkedList<DMessage<E>>();
	
	public DClient( int writeBuffer, int objectBuffer )
	{
		client = new Client( writeBuffer, objectBuffer );
		
		client.getKryo().register( DMessage.class );
		client.getKryo().register( DMessageType.class );
	}
	
	public void start( String address, int timeout, int portTCP, int portUDP ) throws IOException
	{
		running = true;
		new Thread( client ).start();
		client.connect( timeout, address, portTCP, portUDP );
		
		
		client.addListener( new Listener() {
			public void received( Connection c, Object o ) 
			{
				if( o instanceof DMessage<?> )
				{
					synchronized( messages )
					{
						messages.push( (DMessage<E>)o );
					}
				}
			}
			
			public void disconnected( Connection c )
			{
				
			}
		});
	}

	public void sendToServer( E m ) 
	{
		client.sendTCP( new DMessage<E>( m, DMessageType.DATA, client.getID() ) );
	}
	
	public void sendUDP( E m )
	{
		client.sendUDP( new DMessage<E>( m, DMessageType.DATA, client.getID() ) );
	}
	
	@SuppressWarnings("rawtypes")
	public void register( Class c )
	{
		client.getKryo().register( c );
	}

	public DMessage<E> getNextClientMessage()
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

	public void stop()
	{
		client.close();
		running = false;
	}
	
	public void manualUpdate( int blockTime )
	{
		try
		{
			client.update( blockTime );
		} catch( IOException e )
		{
			e.printStackTrace();
		}
	}
}
