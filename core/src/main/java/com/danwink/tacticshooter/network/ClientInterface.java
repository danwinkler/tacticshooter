package com.danwink.tacticshooter.network;


public interface ClientInterface 
{
	public abstract void sendToServer( Message m );
	public abstract Message getNextClientMessage();
	public abstract boolean hasClientMessages();
	public abstract void stop();
	public String getServerAddr();
}
