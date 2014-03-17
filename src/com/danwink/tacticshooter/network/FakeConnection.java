package com.danwink.tacticshooter.network;

import java.util.LinkedList;

import com.esotericsoftware.kryonet.Connection;
import com.phyloa.dlib.util.DMath;

public class FakeConnection extends Connection
{
	public int id = DMath.randomi( 0, Integer.MAX_VALUE );
	public LinkedList<Message> messages = new LinkedList<Message>();
	
	public int getID()
	{
		return id;
	}
	
	public int sendTCP( Object o )
	{
		synchronized( messages )
		{
			messages.push( (Message)o );
		}
		return 0;
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
