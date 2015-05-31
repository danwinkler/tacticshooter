package com.danwink.tacticshooter.renderer;

import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.SlickException;

import com.danwink.tacticshooter.ClientState;
import com.danwink.tacticshooter.gameobjects.Level;

public class FloorRenderer
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
	
	private void generateTexture( ClientState cs )
	{
		try
		{
			texture = new Image( cs.l.width * Level.tileSize, cs.l.height * Level.tileSize );
			cs.l.renderFloor( texture.getGraphics() );
		}
		catch( SlickException e )
		{
			e.printStackTrace();
		}
	}
}