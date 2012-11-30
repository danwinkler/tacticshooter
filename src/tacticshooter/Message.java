package tacticshooter;

import java.io.Serializable;

@SuppressWarnings("serial")
public class Message implements Serializable 
{
	public int sender = -1;
	public MessageType messageType;
	public Serializable message;
	
	public Message()
	{
		
	}
	
	public Message( MessageType messageType, Serializable message )
	{
		this.messageType = messageType;
		this.message = message;
	}
}
