package com.danwink.tacticshooter.screens;

import java.awt.event.KeyEvent;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;

import tacticshooter.StaticFiles;
import tacticshooter.TacticClient;

public class MultiplayerSetupScreen implements Screen, EventListener
{
	Stage stage;
	
	TextField address;
	TextButton enter;
	TextButton back;

	TacticClient tc;
	
	public MultiplayerSetupScreen( TacticClient tc )
	{
		this.tc = tc;
	}
	
	public void show()
	{
		stage = new Stage();
		Gdx.input.setInputProcessor( stage );
		address = new DTextBox( e.getWidth() / 2 - 200, e.getHeight()/2 - 120, 400, 100 );
		back = new DButton( "Back", e.getWidth() / 2 - 200, e.getHeight()/2 + 20, 200, 100 );
		enter = new DButton( "Join", e.getWidth() / 2, e.getHeight()/2 + 20, 200, 100 );
		
		dui.add( address );
		dui.add( enter );
		dui.add( back );
		
		dui.setFocus( address );
		
		dui.addDUIListener( this );
		
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
