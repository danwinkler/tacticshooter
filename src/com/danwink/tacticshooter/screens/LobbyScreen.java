package com.danwink.tacticshooter.screens;

import java.io.IOException;

import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;

import tacticshooter.ClientInterface;
import tacticshooter.ClientNetworkInterface;
import tacticshooter.ComputerPlayer;
import tacticshooter.Message;
import tacticshooter.Slick2DEventMapper;
import tacticshooter.Slick2DRenderer;
import tacticshooter.StaticFiles;

import com.phyloa.dlib.dui.DButton;
import com.phyloa.dlib.dui.DDropDown;
import com.phyloa.dlib.dui.DText;
import com.phyloa.dlib.dui.DUI;
import com.phyloa.dlib.dui.DUIEvent;
import com.phyloa.dlib.dui.DUIListener;
import com.phyloa.dlib.renderer.DScreen;
import com.phyloa.dlib.renderer.DScreenHandler;
import com.phyloa.dlib.util.DMath;

public class LobbyScreen extends DScreen<GameContainer, Graphics> implements DUIListener
{
	String address;
	ClientInterface ci;
	
	DUI dui;
	
	DButton[] names = new DButton[16];
	DDropDown[] humanOrBot = new DDropDown[16];
	DDropDown[] botType = new DDropDown[16];
	
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
		dui.addDUIListener( this );
		for( int i = 0; i < 16; i++ )
		{
			int baseHeight = i < 8 ? 100 : 300;
			names[i] = new DButton( "Open", 20, baseHeight + i * 30, 170, 25 );
			dui.add( names[i] );
			
			humanOrBot[i] = new DDropDown( 200, baseHeight + i * 30, 100, 25 );
			humanOrBot[i].name = "hb " + i;
			humanOrBot[i].addItems( "HUMAN", "BOT" );
			dui.add( humanOrBot[i] );
			
			botType[i] = new DDropDown( 310, baseHeight + i * 30, 200, 25 );
			botType[i].name = "bt " + i;
			botType[i].setVisible( false );
			for( ComputerPlayer.PlayType pt : ComputerPlayer.PlayType.values() )
			{
				botType[i].addItems( pt.name() );
			}
			dui.add( botType[i] );
		}
	}
	
	public void update( GameContainer gc, int delta )
	{
		dui.update();
		
		while( ci.hasClientMessages() )
		{
			Message m = ci.getNextClientMessage();
			switch( m.messageType )
			{
			case PLAYERUPDATE:
				break;
			}
		}
	}

	public void render( GameContainer gc, Graphics g )
	{
		dui.render( r.renderTo( g ) );
	}

	public void onExit() 
	{
		dui.setEnabled( false );
	}

	public void message( Object o )
	{
		if( o instanceof String )
		{
			address = (String)o;
		}
	}

	public void event( DUIEvent event )
	{
		if( event.getElement() instanceof DDropDown )
		{
			DDropDown el = (DDropDown)event.getElement();
			String[] name = el.name.split( " " );
			if( name.length == 2 )
			{
				if( name[0].equals( "hb" ) )
				{
					int line = Integer.parseInt( name[1] );
					boolean isBot = el.getSelected().equals( "BOT" );
					botType[line].setVisible( isBot );
					if( isBot )
					{
						String[] rnames = StaticFiles.names.split( "\n" );
						names[line].setText( rnames[DMath.randomi( 0, names.length )].split( " " )[0] );
					}
					else 
					{
						names[line].setText( "Open" );
					}
				}
			}
		}
	}
}
