package tacticshooter;

import java.io.Serializable;

@SuppressWarnings("serial")
public class Message implements Serializable 
{
	public int sender = -1;
	public MessageType messageType;
	public Object message;
	
	public Message()
	{
		
	}
	
	public Message( MessageType messageType, Object message )
	{
		this.messageType = messageType;
		this.message = message;
	}
}
