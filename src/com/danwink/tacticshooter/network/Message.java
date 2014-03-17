package com.danwink.tacticshooter.network;

import java.io.Serializable;

import com.danwink.tacticshooter.MessageType;

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
