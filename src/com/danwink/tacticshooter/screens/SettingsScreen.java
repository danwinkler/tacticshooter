package com.danwink.tacticshooter.screens;

import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.SlickException;

import tacticshooter.Slick2DEventMapper;
import tacticshooter.Slick2DRenderer;

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
	DButton back;
	
	Slick2DRenderer r = new Slick2DRenderer();
	
	public void onActivate( GameContainer gc, DScreenHandler<GameContainer, Graphics> dsh )
	{
		if( dui == null )
		{
			dui = new DUI( new Slick2DEventMapper( gc.getInput() ) );
			
			toggleFullscreen = new DButton( "Toggle Fullscreen", gc.getWidth()/2-100, gc.getHeight()/2-100, 200, 100 );
			back = new DButton( "Back", gc.getWidth()/2-100, gc.getHeight()/2, 200, 100 );
			
			dui.add( toggleFullscreen );
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
				if( gc.isFullscreen() )
				{
					try
					{
						gc.setFullscreen( false );
					} catch( SlickException e1 )
					{
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				} 
				else
				{
					try
					{
						gc.setFullscreen( true );
					} catch( SlickException e1 )
					{
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				}
			} 
			else if( e == back )
			{
				dsh.activate( "home", gc );
			} 
		}
	}
}
