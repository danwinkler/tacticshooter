package com.danwink.tacticshooter.screens;

import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;

import tacticshooter.Slick2DEventMapper;
import tacticshooter.Slick2DRenderer;

import com.phyloa.dlib.dui.DButton;
import com.phyloa.dlib.dui.DUI;
import com.phyloa.dlib.dui.DUIElement;
import com.phyloa.dlib.dui.DUIEvent;
import com.phyloa.dlib.dui.DUIListener;
import com.phyloa.dlib.renderer.DScreen;
import com.phyloa.dlib.renderer.DScreenHandler;

public class LevelEditorSetup extends DScreen<GameContainer, Graphics> implements DUIListener
{
	DUI dui;
	
	DButton newMap;
	DButton loadMap;
	DButton back;
	
	Slick2DRenderer r = new Slick2DRenderer();
	
	public void onActivate( GameContainer gc, DScreenHandler<GameContainer, Graphics> dsh )
	{
		if( dui != null )
			dui.setEnabled( true );
	}
	
	public void update( GameContainer gc, int delta )
	{
		if( dui == null )
		{
			dui = new DUI( new Slick2DEventMapper( gc.getInput() ) );
			
			newMap = new DButton( "New Map", gc.getWidth()/2 - 100, gc.getHeight()/2 - 150, 200, 100 );
			loadMap = new DButton( "Load Map", gc.getWidth()/2 - 100, gc.getHeight()/2 - 50, 200, 100 );
			back = new DButton( "Back", gc.getWidth()/2 - 100, gc.getHeight()/2 + 50, 200, 100 );
			
			dui.add( newMap );
			dui.add( loadMap );
			dui.add( back );
			
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
				dsh.activate( "newmap", gc );
			} 
			else if( e == loadMap )
			{
				dsh.activate( "loadmap", gc );
			} 
			else if( e == back )
			{
				dsh.activate( "home", gc );
			}
		}
	}
}
