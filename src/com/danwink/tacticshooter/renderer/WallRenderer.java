package com.danwink.tacticshooter.renderer;

import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.SlickException;

import com.danwink.tacticshooter.ClientState;
import com.danwink.tacticshooter.gameobjects.Level;

public class WallRenderer
{
	Image texture;
	
	public void render( Graphics g, ClientState cs ) 
	{
		if( texture == null ) 
		{
			if( cs.l != null )
			{
				generateTexture( cs );
			}
			else
			{
				return;
			}
		}
		
		g.drawImage( texture, 0, 0 );
	}
	
	public void renderWalls( ClientState cs )
	{
		if( texture == null ) return;
		try
		{
			Graphics g = texture.getGraphics();
			g.clear();
			cs.l.render( g );
			g.flush();
		}
		catch( SlickException e )
		{
			e.printStackTrace();
		}
	}
	
	private void generateTexture( ClientState cs )
	{
		try
		{
			texture = new Image( cs.l.width * Level.tileSize, cs.l.height * Level.tileSize );
			renderWalls( cs );
		}
		catch( SlickException e )
		{
			e.printStackTrace();
		}
	}
}