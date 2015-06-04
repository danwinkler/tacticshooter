package com.danwink.tacticshooter.network;

import java.io.IOException;
import java.util.LinkedList;

import com.danwink.tacticshooter.KryoHelper;
import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;

public class ClientNetworkInterface implements ClientInterface
{
	public Client client;
	public LinkedList<Message> messages = new LinkedList<Message>();
	
	public ClientNetworkInterface( String address ) throws IOException
	{
		client = new Client( 128000, 32000 );
		KryoHelper.register( client.getKryo() );
		client.start();
		client.connect( 2500, address, 54555, 54777 );
		
		
		client.addListener( new Listener() {
			public void received( Connection c, Object o ) 
			{
				if( o instanceof Message )
				{
					synchronized( messages )
					{
						messages.addLast( (Message)o );
					}
				}
			}
			
			public void disconnected( Connection c )
			{
				
			}
		});
	}

	public void sendToServer( Message m ) 
	{
		client.sendTCP( m );
	}

	public Message getNextClientMessage()
	{
		synchronized( messages )
		{
			return messages.removeFirst();
		}
	}

	public boolean hasClientMessages() 
	{
		return !messages.isEmpty();
	}

	@Override
	public void stop()
	{
		client.close();
	}
	
	public String getServerAddr()
	{
		if( client == null ) return "";
		try {
			return client.getRemoteAddressTCP().getHostName();
		} catch( Exception ex )
		{
			return "";
		}
	}
}
