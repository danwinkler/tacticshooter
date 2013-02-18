package com.danwink.tacticshooter.screens;

import java.io.IOException;

import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;

import tacticshooter.ClientNetworkInterface;
import tacticshooter.Message;
import tacticshooter.MessageType;
import tacticshooter.TacticServer.ServerState;

import com.phyloa.dlib.renderer.DScreen;
import com.phyloa.dlib.renderer.DScreenHandler;

public class ServerConnectScreen extends DScreen<GameContainer, Graphics>
{
	String address;
	ClientNetworkInterface ci;
	
	public void onActivate( GameContainer gc, DScreenHandler<GameContainer, Graphics> dsh )
	{
		try {
			ci = new ClientNetworkInterface( address );
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		ci.sendToServer( new Message( MessageType.CONNECTED, null ) );
	}
	
	public void update( GameContainer gc, int delta )
	{
		while( ci.hasClientMessages() )
		{
			Message m = ci.getNextClientMessage();
			if( m.messageType == MessageType.SERVERSTATE )
			{
				ServerState s = (ServerState)m.message;
				switch( s )
				{
				case LOBBY:
					dsh.message( "lobby", ci );
					dsh.activate( "lobby", gc );
					return;
				case PLAYING:
					dsh.message( "multiplayergame", ci );
					dsh.activate( "multiplayergame", gc );
					return;
				}
			}
		}
	}

	public void render( GameContainer gc, Graphics g )
	{
		
	}

	public void onExit()
	{
		
	}

	public void message( Object o )
	{
		if( o instanceof String )
		{
			address = (String)o;
		}
	}

}
