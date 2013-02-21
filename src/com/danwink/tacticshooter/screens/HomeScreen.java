package com.danwink.tacticshooter.screens;

import java.io.File;
import java.net.InetAddress;
import java.net.UnknownHostException;

import tacticshooter.AutoTileDrawer;
import tacticshooter.Level;
import tacticshooter.ServerNetworkInterface;
import tacticshooter.GdxEventMapper;
import tacticshooter.GdxRenderer;
import tacticshooter.StaticFiles;
import tacticshooter.TacticServer;
import tacticshooter.Unit.UnitType;
import tacticshooter.UserInfo;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.graphics.Texture;
import com.phyloa.dlib.dui.DButton;
import com.phyloa.dlib.dui.DUI;
import com.phyloa.dlib.dui.DUIElement;
import com.phyloa.dlib.dui.DUIEvent;
import com.phyloa.dlib.dui.DUIListener;
import com.phyloa.dlib.renderer.DScreen;
import com.phyloa.dlib.renderer.DScreenHandler;
import com.phyloa.dlib.util.DMath;
import com.phyloa.dlib.util.DOptions;
import com.phyloa.dlib.util.ImprovedNoise;

public class HomeScreen extends DScreen<Graphics, Graphics> implements DUIListener
{
	TacticServer server;
	
	DUI dui;
	DButton singlePlayer;
	DButton multiPlayer;
	DButton levelEditor;
	DButton settings;
	DButton exit;
	
	DButton login;
	
	GdxRenderer r = new GdxRenderer();
	
	String ip;
	
	Texture title;
	
	public void onActivate( Graphics e, DScreenHandler<Graphics, Graphics> dsh )
	{
		if( dui == null )
		{
			dui = new DUI( new GdxEventMapper() );
			
			singlePlayer = new DButton( "Start Local Server", e.getWidth() / 2 - 200, e.getHeight()/2 - 200, 400, 100 );
			multiPlayer = new DButton( "Multiplayer", e.getWidth() / 2 - 200, e.getHeight()/2 - 100, 400, 100 );
			levelEditor = new DButton( "Level Editor", e.getWidth() / 2 - 200, e.getHeight()/2, 400, 100 );
			settings = new DButton( "Settings", e.getWidth() / 2 - 200, e.getHeight()/2 + 100, 400, 100 );
			exit = new DButton( "Exit", e.getWidth() / 2 - 200, e.getHeight()/2 + 200, 400, 100 );
			
			login = new DButton( "Login", 50, 50, 200, 100 );
			
			dui.add( singlePlayer );
			dui.add( multiPlayer );
			dui.add( levelEditor );
			dui.add( settings );
			dui.add( exit );
			
			dui.add( login );
			
			dui.addDUIListener( this );
		}
		
		StaticFiles.loadAllMusic();
		StaticFiles.loopWhenReady( "menu" );
		
		dui.setEnabled( true );
		
		if( StaticFiles.user == null )
		{
			login.setText( "Login" );
		}
		else
		{
			login.setText( "Logout" );
		}
		
		title = new Texture( "img" + File.separator + "title.png" );		
	}
	
	public void update( Graphics gc, int delta )
	{
		dui.update();
	}

	public void render( Graphics gc, Graphics g )
	{
		dui.render( r );
		if( ip != null )
		{
			g.setColor( Color.white );
			g.drawString( "Server Address: " + ip, 200, 15 );
		}
		
		g.drawImage( title, gc.getWidth()/2 - title.getWidth()/2, 150, new Color( 0, 0, 0, 128 ) );
	}

	public void onExit()
	{
		dui.setEnabled( false );
	}
	
	public void event( DUIEvent event )
	{
		DUIElement e = event.getElement();
		if( e instanceof DButton && event.getType() == DButton.MOUSE_UP )
		if( e == singlePlayer )
		{
			if( server == null )
			{
				singlePlayer.setText( "Stop Local Server" );
				server = new TacticServer( new ServerNetworkInterface() );
				server.begin();
				try
				{
					InetAddress thisIp = InetAddress.getLocalHost();
					ip = thisIp.getHostAddress();
				} catch( UnknownHostException e1 )
				{
				}
			}
			else
			{
				singlePlayer.setText( "Start Local Server" );
				server.sl.running = false;
				server = null;
				ip = null;
			}
		} 
		else if( e == multiPlayer )
		{
			dsh.activate( "multiplayersetup", gc, StaticFiles.getDownMenuOut(), StaticFiles.getDownMenuIn() );
		} 
		else if( e == levelEditor )
		{
			dsh.activate( "editorsetup", gc, StaticFiles.getDownMenuOut(), StaticFiles.getDownMenuIn() );
		}
		else if( e == settings )
		{
			dsh.activate( "settings", gc, StaticFiles.getDownMenuOut(), StaticFiles.getDownMenuIn() );
		} 
		else if( e == exit )
		{
			gc.exit();
		}
		else if( e == login )
		{
			if( StaticFiles.user == null )
			{
				dsh.activate( "login", gc, StaticFiles.getUpMenuOut(), StaticFiles.getUpMenuIn() );
			}
			else
			{
				StaticFiles.user = null;
				login.setText( "Login" );
			}
		} 
	}

	@Override
	public void message( Object o )
	{
		if( StaticFiles.user != null )
		{
			login.setText( "Logout" );
		}
	} 
}
