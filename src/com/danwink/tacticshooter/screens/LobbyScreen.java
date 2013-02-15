package com.danwink.tacticshooter.screens;

import java.io.IOException;

import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;

import tacticshooter.ClientInterface;
import tacticshooter.ClientNetworkInterface;
import tacticshooter.Message;
import tacticshooter.MessageType;
import tacticshooter.Slick2DEventMapper;
import tacticshooter.StaticFiles;

import com.phyloa.dlib.dui.DUI;
import com.phyloa.dlib.renderer.DScreen;
import com.phyloa.dlib.renderer.DScreenHandler;

public class LobbyScreen extends DScreen<GameContainer, Graphics>
{
	String address;
	ClientInterface ci;
	
	DUI dui;
	
	public void onActivate( GameContainer gc, DScreenHandler<GameContainer, Graphics> dsh )
	{
		try {
			ci = new ClientNetworkInterface( address );
		} catch (IOException e) {
			dsh.message( "message", "Could not connect to server at: " + address );
			dsh.activate( "message", gc, StaticFiles.getUpMenuOut(), StaticFiles.getUpMenuIn() );
		}
		
		dui = new DUI( new Slick2DEventMapper( gc.getInput() ) );
	}
	
	public void update( GameContainer gc, int delta )
	{
		
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
