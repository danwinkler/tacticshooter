package com.danwink.tacticshooter.screens;

import org.newdawn.slick.Color;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;

import com.danwink.tacticshooter.StaticFiles;
import com.phyloa.dlib.game.DScreen;
import com.phyloa.dlib.game.DScreenHandler;

public class OpenLoadScreen extends DScreen<GameContainer, Graphics>
{
	public void onActivate( GameContainer e, DScreenHandler<GameContainer, Graphics> dsh )
	{
		StaticFiles.loadAllMusic();
		StaticFiles.loopWhenReady( "menu" );
	}
	
	public void update( GameContainer gc, int delta )
	{
		if( StaticFiles.ready )
		{
			dsh.activate( "home", gc );
		}
	}

	public void render( GameContainer gc, Graphics g )
	{
		g.setColor( new Color( 0, 0, 0, 100 ) );
		g.fillRect( 0, 0, gc.getWidth(), gc.getHeight() );
		
		g.setColor( Color.red );
		g.drawString( StaticFiles.status, 50, 50 );
	}

	public void onExit()
	{
		
	}

	public void message( Object o )
	{
		// TODO Auto-generated method stub
		
	}
}

