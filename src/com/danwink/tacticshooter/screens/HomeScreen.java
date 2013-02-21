package com.danwink.tacticshooter.screens;

import java.io.File;
import java.net.InetAddress;
import java.net.UnknownHostException;

import tacticshooter.AutoTileDrawer;
import tacticshooter.Level;
import tacticshooter.ServerNetworkInterface;
import tacticshooter.StaticFiles;
import tacticshooter.TacticClient;
import tacticshooter.TacticServer;
import tacticshooter.Unit.UnitType;
import tacticshooter.UserInfo;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.phyloa.dlib.util.DMath;
import com.phyloa.dlib.util.DOptions;
import com.phyloa.dlib.util.ImprovedNoise;

public class HomeScreen implements Screen, EventListener
{
	TacticServer server;
	
	Stage stage;
	
	TextButton singlePlayer;
	TextButton multiPlayer;
	TextButton levelEditor;
	TextButton settings;
	TextButton exit;
	
	TextButton login;
	
	String ip;
	
	Texture title;

	TacticClient tc;
	
	public HomeScreen( TacticClient tc )
	{
		this.tc = tc;
	}
	
	public void show()
	{
		stage = new Stage();
		Gdx.input.setInputProcessor( stage );
		
		//TextureAtlas atlas = new TextureAtlas( new FileHandle( "data" + File.separator + "uiskin.atlas" ) );
		Skin skin = StaticFiles.skin;
		TextButton.TextButtonStyle tbs = skin.get( TextButton.TextButtonStyle.class );
		
		singlePlayer = new TextButton( "Start Local Server", tbs );
		multiPlayer = new TextButton( "Multiplayer", tbs );
		levelEditor = new TextButton( "Level Editor", tbs );
		settings = new TextButton( "Settings", tbs );
		exit = new TextButton( "Exit", tbs );
		login = new TextButton( "Login", tbs );

		singlePlayer.addListener( this );
		exit.addListener( this );
		
		Table table = new Table();
		table.setFillParent( true );
		
		table.add( singlePlayer );
		table.row();
		table.add( multiPlayer );
		table.row();
		table.add( levelEditor );
		table.row();
		table.add( settings );
		table.row();
		table.add( exit );
		table.row();
		table.add( login );
		
		stage.addActor( table );
		
		StaticFiles.loadAllMusic();
		StaticFiles.loopWhenReady( "menu" );
		
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

	public void render( float d )
	{
		stage.act( Gdx.graphics.getDeltaTime() );
		stage.draw();
		if( ip != null )
		{
			//g.drawString( "Server Address: " + ip, 200, 15 );
		}
		
		//g.drawImage( title, gc.getWidth()/2 - title.getWidth()/2, 150, new Color( 0, 0, 0, 128 ) );
	}

	public void onExit()
	{
		stage.dispose();
	}
	
	public boolean handle( Event event )
	{
		Actor e = event.getListenerActor();
		if( e instanceof TextButton )
		{
			TextButton button = (TextButton)e;
			if( button.isPressed() )
			{
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
					//tc.setScreen( tc.multiplayersetup );
				} 
				else if( e == levelEditor )
				{
					//tc.setScreen( tc.editorsetup );
				}
				else if( e == settings )
				{
					//tc.setScreen( tc.settings );
				} 
				else if( e == exit )
				{
					Gdx.app.exit();
				}
				else if( e == login )
				{
					if( StaticFiles.user == null )
					{
						//tc.setScreen( login );
					}
					else
					{
						StaticFiles.user = null;
						login.setText( "Login" );
					}
				}
			}
		}
		return true;
	}

	public void dispose()
	{
		stage.dispose();
	}

	public void hide()
	{
		
	}

	public void pause()
	{
		
	}

	public void resize( int width, int height )
	{
		stage.setViewport(width, height, true);
	}

	public void resume()
	{
		// TODO Auto-generated method stub
		
	}
}
