package com.danwink.tacticshooter.screens;

import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;


import com.danwink.tacticshooter.StaticFiles;
import com.danwink.tacticshooter.slick.Slick2DEventMapper;
import com.danwink.tacticshooter.slick.Slick2DRenderer;
import com.phyloa.dlib.dui.DButton;
import com.phyloa.dlib.dui.DText;
import com.phyloa.dlib.dui.DUI;
import com.phyloa.dlib.dui.DUIElement;
import com.phyloa.dlib.dui.DUIEvent;
import com.phyloa.dlib.dui.DUIListener;
import com.phyloa.dlib.game.DScreen;
import com.phyloa.dlib.game.DScreenHandler;

public class MessageScreen extends DScreen<GameContainer, Graphics> implements DUIListener
{
	DUI dui;
	DButton okay;
	DText text;
	
	Slick2DRenderer r = new Slick2DRenderer();
		
	String message;
	
	public void onActivate( GameContainer e, DScreenHandler<GameContainer, Graphics> dsh )
	{
		dui = new DUI( new Slick2DEventMapper( e.getInput() ) );
		
		okay = new DButton( "Okay", e.getWidth() / 2 - 100, e.getHeight()/2, 200, 100 );
		text = new DText( message, e.getWidth()/2 - 100, e.getHeight()/2-100 );
		text.setCentered( true );
		
		dui.add( okay );
		dui.add( text );
		
		dui.addDUIListener( this );
		
		dui.setEnabled( true );
	}
	
	public void update( GameContainer gc, int delta )
	{
		dui.update();
	}

	public void render( GameContainer gc, Graphics g )
	{
		//g.setColor( new Color( 0, 0, 0, 100 ) );
		//g.fillRect( 0, 0, gc.getWidth(), gc.getHeight() );
		
		dui.render( r.renderTo( g ) );
	}

	public void onExit()
	{
		dui.setEnabled( false );
		dui = null;
	}
	
	public void event( DUIEvent event )
	{
		DUIElement e = event.getElement();
		if( e instanceof DButton && event.getType() == DButton.MOUSE_UP )
		if( e == okay )
		{
			dsh.activate( "home", gc, StaticFiles.getUpMenuOut(), StaticFiles.getUpMenuIn() );
		} 	
	}

	@Override
	public void message( Object o )
	{
		message = (String)o;
	} 
}
