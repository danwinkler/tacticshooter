package com.phyloa.dlib.network;

public class DMessage<E>
{
	public int sender = -1;
	public DMessageType messageType;
	public E message;
	
	public DMessage()
	{
		
	}
	
	public DMessage( E m, DMessageType messageType, int sender )
	{
		this.message = m;
		this.messageType = messageType;
		this.sender = sender;
	}
}
