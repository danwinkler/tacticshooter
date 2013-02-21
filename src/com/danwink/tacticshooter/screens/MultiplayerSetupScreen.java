package com.danwink.tacticshooter.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.ui.TextField.TextFieldStyle;

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
		
		Skin skin = StaticFiles.skin;
		
		TextFieldStyle tfs = skin.get( TextFieldStyle.class );
		TextButtonStyle tbs = skin.get( TextButtonStyle.class );
		
		address = new TextField( "", tfs );
		back = new TextButton( "Back", tbs );
		enter = new TextButton( "Join", tbs );
		
		address.addListener( this );
		back.addListener( this );
		enter.addListener( this );
		
		Table table = new Table();
		table.setFillParent( true );
		
		table.add( address );
		table.row();
		table.add( back );
		table.add( enter );
		
		stage.addActor( table );
		
		if( tc.home.server != null )
		{
			address.setText( "localhost" );
		}
	}
	
	public void render( float d )
	{
		if( Gdx.input.isKeyPressed( Input.Keys.ENTER ) )
		{
			tc.connect.address = address.getText();
			//tc.setScreen( tc.connect );
		}
		
		stage.act( Gdx.graphics.getDeltaTime() );
		stage.draw();
	}

	public void hide()
	{
		
	}
	
	public boolean handle( Event event )
	{
		Actor e = event.getListenerActor();
		if( e instanceof TextButton )
		{
			TextButton button = (TextButton)e;
			if( button.isPressed() )
			{
				if( e == enter )
				{
					tc.connect.address = address.getText();
					//tc.setScreen( tc.connect );
				} 
				else if( e == back )
				{
					tc.setScreen( tc.home );
				}
			}
		}
		return true;
	}

	public void dispose()
	{
		stage.dispose();
	}

	public void pause()
	{
		
	}

	public void resize( int width, int height )
	{
		stage.setViewport(width, height, true);
	}

	public void resume()
	{
		
	}
}
