package com.danwink.tacticshooter.screens;

import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;

import tacticshooter.Slick2DEventMapper;
import tacticshooter.Slick2DRenderer;

import com.phyloa.dlib.dui.DButton;
import com.phyloa.dlib.dui.DTextBox;
import com.phyloa.dlib.dui.DUI;
import com.phyloa.dlib.dui.DUIElement;
import com.phyloa.dlib.dui.DUIEvent;
import com.phyloa.dlib.dui.DUIListener;
import com.phyloa.dlib.renderer.DScreen;
import com.phyloa.dlib.renderer.DScreenHandler;

public class MultiplayerSetupScreen implements DScreen<GameContainer, Graphics>, DUIListener
{
	DScreenHandler<GameContainer, Graphics> dsh;
	
	DUI dui;
	DTextBox address;
	DButton enter;
	DButton back;
	
	Slick2DRenderer r = new Slick2DRenderer();
	
	GameContainer gc;
	
	public void onActivate( GameContainer e, DScreenHandler<GameContainer, Graphics> dsh )
	{
		this.dsh = dsh;
		this.gc = e;
		if( dui == null )
			dui = new DUI( new Slick2DEventMapper( e.getInput() ) );
		dui.setEnabled( true );
		
		address = new DTextBox( e.getWidth() / 2 - 200, e.getHeight()/2 - 120, 400, 100 );
		back = new DButton( "Back", e.getWidth() / 2 - 200, e.getHeight()/2 + 20, 200, 100 );
		enter = new DButton( "Join", e.getWidth() / 2, e.getHeight()/2 + 20, 200, 100 );
		
		dui.add( address );
		dui.add( enter );
		dui.add( back );
		
		dui.setFocus( address );
		
		dui.addDUIListener( this );
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
		if( e == enter )
		{
			dsh.message( "multiplayergame", address.getText().trim() );
			dsh.activate( "multiplayergame", gc );
		} 
		else if( e == back )
		{
			dsh.activate( "home", gc );
		} 
	}

	@Override
	public void message( Object o )
	{
		// TODO Auto-generated method stub
		
	} 
}
