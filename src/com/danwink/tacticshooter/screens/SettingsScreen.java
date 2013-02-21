package com.danwink.tacticshooter.screens;

import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.SlickException;

import tacticshooter.GdxEventMapper;
import tacticshooter.GdxRenderer;
import tacticshooter.StaticFiles;

import com.phyloa.dlib.dui.DButton;
import com.phyloa.dlib.dui.DUI;
import com.phyloa.dlib.dui.DUIElement;
import com.phyloa.dlib.dui.DUIEvent;
import com.phyloa.dlib.dui.DUIListener;
import com.phyloa.dlib.renderer.DScreen;
import com.phyloa.dlib.renderer.DScreenHandler;

public class SettingsScreen extends DScreen<GameContainer, Graphics> implements DUIListener
{
	DUI dui;
	DButton toggleFullscreen;
	DButton options;
	DButton advOptions;
	DButton back;
	
	GdxRenderer r = new GdxRenderer();
	
	public void onActivate( GameContainer gc, DScreenHandler<GameContainer, Graphics> dsh )
	{
		if( dui == null )
		{
			dui = new DUI( new GdxEventMapper( gc.getInput() ) );
			
			toggleFullscreen = new DButton( "Toggle Fullscreen", gc.getWidth()/2-100, gc.getHeight()/2-200, 200, 100 );
			options = new DButton( "Options", gc.getWidth()/2-100, gc.getHeight()/2-100, 200, 100 );
			advOptions = new DButton( "Advanced Options", gc.getWidth()/2-100, gc.getHeight()/2, 200, 100 );
			back = new DButton( "Back", gc.getWidth()/2-100, gc.getHeight()/2+100, 200, 100 );
			
			dui.add( toggleFullscreen );
			dui.add( options );
			dui.add( advOptions );
			dui.add( back );
			
			dui.addDUIListener( this );
		}
		dui.setEnabled( true );
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
			if( e == toggleFullscreen )
			{
				try
				{
					gc.setFullscreen( !gc.isFullscreen() );
				} catch( SlickException e1 )
				{
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
			else if( e == options )
			{
				dsh.activate( "options", gc, StaticFiles.getDownMenuOut(), StaticFiles.getDownMenuIn() );
			}
			else if( e == advOptions )
			{
				dsh.activate( "advoptions", gc, StaticFiles.getDownMenuOut(), StaticFiles.getDownMenuIn() );
			}
			else if( e == back )
			{
				dsh.activate( "home", gc, StaticFiles.getUpMenuOut(), StaticFiles.getUpMenuIn() );
			} 
		}
	}
}
