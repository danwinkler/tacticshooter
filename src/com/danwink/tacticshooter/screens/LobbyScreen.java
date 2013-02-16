package com.danwink.tacticshooter.screens;

import java.io.IOException;

import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;

import tacticshooter.ClientInterface;
import tacticshooter.ClientNetworkInterface;
import tacticshooter.Message;
import tacticshooter.MessageType;
import tacticshooter.Slick2DEventMapper;
import tacticshooter.Slick2DRenderer;
import tacticshooter.StaticFiles;

import com.phyloa.dlib.dui.DDropDown;
import com.phyloa.dlib.dui.DUI;
import com.phyloa.dlib.renderer.DScreen;
import com.phyloa.dlib.renderer.DScreenHandler;

public class LobbyScreen extends DScreen<GameContainer, Graphics>
{
	String address;
	ClientInterface ci;
	
	DUI dui;
	
	DDropDown[] humanOrBot = new DDropDown[16];
	
	Slick2DRenderer r = new Slick2DRenderer();
	
	public void onActivate( GameContainer gc, DScreenHandler<GameContainer, Graphics> dsh )
	{
		try {
			ci = new ClientNetworkInterface( address );
		} catch (IOException e) {
			dsh.message( "message", "Could not connect to server at: " + address );
			dsh.activate( "message", gc, StaticFiles.getUpMenuOut(), StaticFiles.getUpMenuIn() );
		}
		
		dui = new DUI( new Slick2DEventMapper( gc.getInput() ) );
		for( int i = 0; i < 8; i++ )
		{
			humanOrBot[i] = new DDropDown( 200, 100 + i * 30, 100, 25 );
			humanOrBot[i].addItems( "HUMAN", "BOT" );
			dui.add( humanOrBot[i] );
		}
		for( int i = 8; i < 16; i++ )
		{
			humanOrBot[i] = new DDropDown( 200, 300 + i * 30, 100, 25 );
			humanOrBot[i].addItems( "HUMAN", "BOT" );
			dui.add( humanOrBot[i] );
		}
	}
	
	public void update( GameContainer gc, int delta )
	{
		dui.update();
	}

	public void render( GameContainer gc, Graphics g )
	{
		dui.render( r.renderTo( g ) );
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
