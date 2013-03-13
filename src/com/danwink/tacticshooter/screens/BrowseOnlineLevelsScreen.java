package com.danwink.tacticshooter.screens;

import java.io.IOException;
import java.util.ArrayList;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.ParseException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;

import tacticshooter.Slick2DEventMapper;
import tacticshooter.Slick2DRenderer;
import tacticshooter.StaticFiles;

import com.phyloa.dlib.dui.DButton;
import com.phyloa.dlib.dui.DPanel;
import com.phyloa.dlib.dui.DScrollPane;
import com.phyloa.dlib.dui.DTextBox;
import com.phyloa.dlib.dui.DUI;
import com.phyloa.dlib.dui.DUIElement;
import com.phyloa.dlib.dui.DUIEvent;
import com.phyloa.dlib.dui.DUIListener;
import com.phyloa.dlib.renderer.DScreen;
import com.phyloa.dlib.renderer.DScreenHandler;

public class BrowseOnlineLevelsScreen extends DScreen<GameContainer, Graphics> implements DUIListener
{	
	DUI dui;
	DButton back;
	
	DButton prev;
	DButton next;
	
	DScrollPane scroll;
	
	DTextBox search;
	
	DButton nameSort;
	DButton ratingSort;
	DButton downloadSort;
	DButton creatorSort;
	
	Slick2DRenderer r = new Slick2DRenderer();
	
	Thread t = new Thread();
	
	public void onActivate( GameContainer gc, DScreenHandler<GameContainer, Graphics> dsh )
	{
		dui = new DUI( new Slick2DEventMapper( gc.getInput() ) );
		
		DPanel panel = new DPanel( gc.getWidth()/2 - 400, gc.getHeight()/2 - 300, 800, 600 );
		dui.add( panel );
		
		scroll = new DScrollPane( 0, 100, 800, 450 );
		panel.add( scroll );
		
		nameSort = new DButton( "Name", 0, 50, 400, 50 );
		panel.add( nameSort );
		ratingSort = new DButton( "Rating", 400, 50, 100, 50 );
		panel.add( ratingSort );
		downloadSort = new DButton( "Downloads", 500, 50, 100, 50 );
		panel.add( downloadSort );
		creatorSort = new DButton( "Creator", 600, 50, 150, 50 );
		panel.add( creatorSort );
		
		search = new DTextBox( 500, 0, 300, 50 );
		panel.add( search );
		
		prev = new DButton( "Previous", 300, 550, 100, 50 );
		panel.add( prev );
		next = new DButton( "Next", 400, 550, 100, 50 );
		panel.add( next );
		
		back = new DButton( "Back", 0, 550, 100, 50 );
		panel.add( back );
		
		dui.addDUIListener( this );
		
		t.interrupt();
		t = new Thread( new LevelListDownloader() );
		t.start();
	}
	
	public void update( GameContainer gc, int delta )
	{
		synchronized( dui )
		{
			dui.update();
		}
	}

	public void render( GameContainer gc, Graphics g )
	{
		synchronized( dui )
		{
			dui.render( r.renderTo( g ) );
		}
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
		DUIElement element = event.getElement();
		if( element instanceof DButton )
		{
			DButton but = (DButton)element;
			if( event.getType() == DButton.MOUSE_UP )
			{
				if( but == back )
				{
					dsh.activate( "editorsetup", gc, StaticFiles.getUpMenuOut(), StaticFiles.getUpMenuIn() );
				}
			}
		}
		else if( element instanceof DTextBox )
		{
			DTextBox textBox = (DTextBox)element;
			
			if( element == search )
			{
				t.interrupt();
				t = new Thread( new LevelListDownloader( textBox.getText() ) );
				t.start();
			}
		}
	}
	
	public class LevelListDownloader implements Runnable
	{
		String filter = "";
		
		public LevelListDownloader()
		{
			
		}
		
		public LevelListDownloader( String filter )
		{
			this.filter = filter;
		}
		
		public void run()
		{
			try
			{					
				String addr = "http://www.tacticshooter.com/levellist?";
				
				if( filter.length() > 0 )
				{
					addr += "filter=" + filter;
				}
				
				HttpClient client = new DefaultHttpClient();
				HttpGet httpget = new HttpGet( addr );
				HttpResponse r = client.execute( httpget );
		
				synchronized( dui )
				{
					if( Thread.interrupted() ) 
					{
				        return;
					}
					scroll.clearChildren();
				
					String s = EntityUtils.toString( r.getEntity() );
					String[] lines = s.split( "\n" );
					if( lines.length >= 4 )
					{
						for( int i = 0; i < lines.length; i += 4 )
						{
							scroll.add( new DButton( lines[i], 0, (i/4) * 50, 400, 50 ) );
							scroll.add( new DButton( lines[i+3], 400, (i/4) * 50, 100, 50 ) );
							scroll.add( new DButton( lines[i+2], 500, (i/4) * 50, 100, 50 ) );
							scroll.add( new DButton( lines[i+1], 600, (i/4) * 50, 150, 50 ) );
							scroll.add( new DButton( "DL", 750, (i/4) * 50, 40, 50 ) );
						}
					}
					
					scroll.setInnerPaneHeight( lines.length/4 * 50 );
				}
			} catch( ParseException e )
			{
				e.printStackTrace();
			} catch( IOException e )
			{
				e.printStackTrace();
			}
		}	
	}
}
