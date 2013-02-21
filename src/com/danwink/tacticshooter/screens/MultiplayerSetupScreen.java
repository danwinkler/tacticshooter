package com.danwink.tacticshooter.screens;

import java.awt.event.KeyEvent;

import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;

import tacticshooter.GdxEventMapper;
import tacticshooter.GdxRenderer;
import tacticshooter.StaticFiles;

import com.phyloa.dlib.dui.DButton;
import com.phyloa.dlib.dui.DTextBox;
import com.phyloa.dlib.dui.DUI;
import com.phyloa.dlib.dui.DUIElement;
import com.phyloa.dlib.dui.DUIEvent;
import com.phyloa.dlib.dui.DUIListener;
import com.phyloa.dlib.renderer.DScreen;
import com.phyloa.dlib.renderer.DScreenHandler;

public class MultiplayerSetupScreen extends DScreen<GameContainer, Graphics> implements DUIListener
{
	DUI dui;
	DTextBox address;
	DButton enter;
	DButton back;
	
	GdxRenderer r = new GdxRenderer();
	
	public void onActivate( GameContainer e, DScreenHandler<GameContainer, Graphics> dsh )
	{
		if( dui == null )
		{
			dui = new DUI( new GdxEventMapper( e.getInput() ) );
			address = new DTextBox( e.getWidth() / 2 - 200, e.getHeight()/2 - 120, 400, 100 );
			back = new DButton( "Back", e.getWidth() / 2 - 200, e.getHeight()/2 + 20, 200, 100 );
			enter = new DButton( "Join", e.getWidth() / 2, e.getHeight()/2 + 20, 200, 100 );
			
			dui.add( address );
			dui.add( enter );
			dui.add( back );
			
			dui.setFocus( address );
			
			dui.addDUIListener( this );
		}
		dui.setEnabled( true );
		
		if( ((HomeScreen)dsh.get( "home" )).server != null )
		{
			address.setText( "localhost" );
		}
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
		{
			if( e == enter )
			{
				dsh.message( "connect", address.getText().trim() );
				dsh.activate( "connect", gc, StaticFiles.getDownMenuOut(), StaticFiles.getDownMenuIn() );
			} 
			else if( e == back )
			{
				dsh.activate( "home", gc, StaticFiles.getDownMenuOut(), StaticFiles.getDownMenuIn() );
			}
		} else if( e instanceof DTextBox )
		{
			if( event.getType() == KeyEvent.VK_ENTER )
			{
				dsh.message( "connect", address.getText().trim() );
				dsh.activate( "connect", gc, StaticFiles.getDownMenuOut(), StaticFiles.getDownMenuIn() );
			}
		}
	}

	@Override
	public void message( Object o )
	{
		// TODO Auto-generated method stub
		
	} 
}
