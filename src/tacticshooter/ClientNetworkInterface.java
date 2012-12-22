package tacticshooter;

import java.io.IOException;
import java.util.LinkedList;

import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;

public class ClientNetworkInterface implements ClientInterface
{
	Client client;
	LinkedList<Message> messages = new LinkedList<Message>();
	
	public ClientNetworkInterface( String address )
	{
		client = new Client( 32000, 32000 );
		KryoHelper.register( client.getKryo() );
		client.start();
		try {
			client.connect(5000, address, 54555, 54777);
		} catch (IOException e) {
			e.printStackTrace(); //TODO actually handle that shit
		}
		
		client.addListener( new Listener() {
			public void received( Connection c, Object o ) 
			{
				if( o instanceof Message )
				{
					synchronized( messages )
					{
						messages.push( (Message)o );
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
			return messages.pop();
		}
	}

	public boolean hasClientMessages() 
	{
		return !messages.isEmpty();
	}
}
