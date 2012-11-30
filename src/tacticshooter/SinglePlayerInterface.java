package tacticshooter;

import java.util.LinkedList;

public class SinglePlayerInterface implements ServerInterface, ClientInterface
{
	LinkedList<Message> messagesForServer = new LinkedList<Message>();
	LinkedList<Message> messagesForClient = new LinkedList<Message>();
	
	public void sendToClient( int id, Message m ) 
	{
		synchronized( messagesForClient )
		{
			messagesForClient.push( m );
		}
	}

	public void sendToAllClients( Message m )
	{
		synchronized( messagesForClient )
		{
			messagesForClient.push( m );
		}
	}

	public Message getNextServerMessage() 
	{
		synchronized( messagesForServer )
		{
			return messagesForServer.pop();
		}
	}

	public boolean hasServerMessages() 
	{
		return !messagesForServer.isEmpty();
	}

	public void sendToServer( Message m )
	{
		synchronized( messagesForServer )
		{
			messagesForServer.push( m );
		}
	}

	public Message getNextClientMessage()
	{
		synchronized( messagesForClient )
		{
			return messagesForClient.pop();
		}
	}

	public boolean hasClientMessages()
	{
		return !messagesForClient.isEmpty();
	}
}
