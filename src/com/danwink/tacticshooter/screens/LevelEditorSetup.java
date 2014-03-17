package com.danwink.tacticshooter.screens;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;


import com.danwink.tacticshooter.StaticFiles;
import com.danwink.tacticshooter.slick.Slick2DEventMapper;
import com.danwink.tacticshooter.slick.Slick2DRenderer;
import com.phyloa.dlib.dui.DButton;
import com.phyloa.dlib.dui.DUI;
import com.phyloa.dlib.dui.DUIElement;
import com.phyloa.dlib.dui.DUIEvent;
import com.phyloa.dlib.dui.DUIListener;
import com.phyloa.dlib.game.DScreen;
import com.phyloa.dlib.game.DScreenHandler;
import com.phyloa.dlib.util.DFile;

public class LevelEditorSetup extends DScreen<GameContainer, Graphics> implements DUIListener
{
	DUI dui;
	
	DButton newMap;
	DButton loadMap;
	DButton uploadAllMaps;
	DButton browseonlinelevels;
	DButton back;
	
	Slick2DRenderer r = new Slick2DRenderer();
	
	public void onActivate( GameContainer gc, DScreenHandler<GameContainer, Graphics> dsh )
	{
		if( dui != null )
		{
			dui.setEnabled( true );
			uploadAllMaps.setVisible( StaticFiles.user != null );
			browseonlinelevels.setVisible( StaticFiles.user != null );
		}
	}
	
	public void update( GameContainer gc, int delta )
	{
		if( dui == null )
		{
			dui = new DUI( new Slick2DEventMapper( gc.getInput() ) );
			
			newMap = new DButton( "New Map", gc.getWidth()/2 - 100, gc.getHeight()/2 - 250, 200, 100 );
			loadMap = new DButton( "Load Map", gc.getWidth()/2 - 100, gc.getHeight()/2 - 150, 200, 100 );
			back = new DButton( "Back", gc.getWidth()/2 - 100, gc.getHeight()/2 - 50, 200, 100 );
			
			uploadAllMaps = new DButton( "Upload All Maps", gc.getWidth()/2 - 100, gc.getHeight()/2 + 50, 200, 100 );
			browseonlinelevels = new DButton( "Browse Online Levels", gc.getWidth()/2 - 100, gc.getHeight()/2 + 150, 200, 100 );
			
			dui.add( newMap );
			dui.add( loadMap );
			dui.add( back );
			
			dui.add( uploadAllMaps );
			dui.add( browseonlinelevels );
			
			dui.addDUIListener( this );
			dui.setEnabled( true );
		}
		
		dui.update();
	}

	public void render( GameContainer gc, Graphics g )
	{
		if( dui != null )
			dui.render( r.renderTo( g ) );
	}

	public void onExit()
	{
		dui.setEnabled( false );
	}

	public void message( Object o )
	{
		
	}

	public void event( DUIEvent event )
	{
		DUIElement e = event.getElement();
		if( e instanceof DButton && event.getType() == DButton.MOUSE_UP )
		{
			if( e == newMap )
			{
				dsh.activate( "newmap", gc, StaticFiles.getDownMenuOut(), StaticFiles.getDownMenuIn() );
			} 
			else if( e == loadMap )
			{
				dsh.activate( "loadmap", gc, StaticFiles.getDownMenuOut(), StaticFiles.getDownMenuIn() );
			} 
			else if( e == back )
			{
				dsh.activate( "home", gc, StaticFiles.getUpMenuOut(), StaticFiles.getUpMenuIn() );
			}
			else if( e == uploadAllMaps )
			{
				if( StaticFiles.user != null )
				{
					File[] files = new File( "levels" ).listFiles();
					if( files != null )
					{
						for( int i = 0; i < files.length; i++ )
						{
							String name = files[i].getName().replace( ".xml", "" );
							if( name.contains( "." ) && !name.startsWith( StaticFiles.user.username + "." ) )
							{
								continue;
							}
							
							new Thread( new FileUploader( name ) ).start();
						}
					}
				}
				else
				{
					dsh.message( "message", "You must be logged in to upload your maps" );
					dsh.activate( "message", gc, StaticFiles.getUpMenuOut(), StaticFiles.getUpMenuIn() );
				}
			}
			else if( e == browseonlinelevels )
			{
				if( StaticFiles.user != null )
				{
					dsh.activate( "levelbrowser", gc, StaticFiles.getDownMenuOut(), StaticFiles.getDownMenuIn() );
				}
				else
				{
					dsh.message( "message", "You must be logged in to browse online maps" );
					dsh.activate( "message", gc, StaticFiles.getUpMenuOut(), StaticFiles.getUpMenuIn() );
				}
			}
		}
	}
	
	public class FileUploader implements Runnable
	{
		String name;
		
		public FileUploader( String name )
		{
			this.name = name;
		}

		public void run()
		{
			try
			{
				String level = DFile.loadText( "levels" + File.separator + name + ".xml" );
				
				if( !name.startsWith( StaticFiles.user.username + "." ) )
				{
					File f1 = new File( "levels" + File.separator + name + ".xml" ).getAbsoluteFile();
					name = StaticFiles.user.username + "." + name;
					File f2 = new File( "levels" + File.separator + name + ".xml" ).getAbsoluteFile();
					System.out.println( f1 );
					System.out.println( f2 );
					boolean worked = f1.renameTo( f2 );
					System.out.println( worked );
				}
				HttpClient client = new DefaultHttpClient();
				HttpPost httppost = new HttpPost( "http://www.tacticshooter.com/level/upload" );

				ArrayList<NameValuePair> list = new ArrayList<NameValuePair>();

				list.add( new BasicNameValuePair( "username", StaticFiles.user.username ) );
				list.add( new BasicNameValuePair( "password", StaticFiles.user.password ) );
				list.add( new BasicNameValuePair( "name", name ) );
				list.add( new BasicNameValuePair( "level", level ) );

				httppost.setEntity( new UrlEncodedFormEntity( list ) );

				HttpResponse r = client.execute( httppost );
			} 
			catch( IOException ex )
			{
				
			}
		}
		
	}
}