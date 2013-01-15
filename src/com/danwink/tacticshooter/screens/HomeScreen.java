package com.danwink.tacticshooter.screens;

import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;

import tacticshooter.Slick2DEventMapper;
import tacticshooter.Slick2DRenderer;
import tacticshooter.Unit.UnitType;

import com.phyloa.dlib.dui.DButton;
import com.phyloa.dlib.dui.DUI;
import com.phyloa.dlib.dui.DUIElement;
import com.phyloa.dlib.dui.DUIEvent;
import com.phyloa.dlib.dui.DUIListener;
import com.phyloa.dlib.renderer.DScreen;
import com.phyloa.dlib.renderer.DScreenHandler;

public class HomeScreen implements DScreen<GameContainer, Graphics>, DUIListener
{
	DScreenHandler<GameContainer, Graphics> dsh;
	
	DUI dui;
	DButton singlePlayer;
	DButton multiPlayer;
	DButton settings;
	DButton exit;
	
	Slick2DRenderer r = new Slick2DRenderer();
	
	GameContainer gc;
	
	public void onActivate( GameContainer e, DScreenHandler<GameContainer, Graphics> dsh )
	{
		this.dsh = dsh;
		this.gc = e;
		if( dui == null )
		{
			dui = new DUI( new Slick2DEventMapper( e.getInput() ) );
			
			singlePlayer = new DButton( "Singleplayer", e.getWidth() / 2 - 200, e.getHeight()/2 - 200, 400, 100 );
			multiPlayer = new DButton( "Multiplayer", e.getWidth() / 2 - 200, e.getHeight()/2 - 100, 400, 100 );
			settings = new DButton( "Settings", e.getWidth() / 2 - 200, e.getHeight()/2, 400, 100 );
			exit = new DButton( "Exit", e.getWidth() / 2 - 200, e.getHeight()/2 + 100, 400, 100 );
			
			
			dui.add( singlePlayer );
			dui.add( multiPlayer );
			dui.add( settings );
			dui.add( exit );
			
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
	
	public void event( DUIEvent event )
	{
		DUIElement e = event.getElement();
		if( e instanceof DButton && event.getType() == DButton.MOUSE_UP )
		if( e == singlePlayer )
		{
			
		} 
		else if( e == multiPlayer )
		{
			dsh.activate( "multiplayersetup", gc );
		} 
		else if( e == settings )
		{
			
		} 
		else if( e == exit )
		{
			System.exit( 0 );
		}
	}

	@Override
	public void message( Object o )
	{
		// TODO Auto-generated method stub
		
	} 
}
