package com.danwink.tacticshooter.renderer;

import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.SlickException;

import com.danwink.tacticshooter.AutoTileDrawer;
import com.danwink.tacticshooter.ClientState;
import com.danwink.tacticshooter.gameobjects.Level;

public class OutsideFloorRenderer
{
	Image texture;
	
	public void render( Graphics g, ClientState cs, GameContainer gc )
	{
		if( texture == null ) 
		{
			if( cs.l != null ) 
			{
				generateTexture( cs, gc );
			}
			else
			{
				return;
			}
		}
		
		float x = -Level.tileSize-(cs.scrollx - ((int)(cs.scrollx/Level.tileSize))*Level.tileSize);
		float y = -Level.tileSize-(cs.scrolly - ((int)(cs.scrolly/Level.tileSize)*Level.tileSize));
		g.drawImage( texture, x, y );
	}
	
	private void generateTexture( ClientState cs, GameContainer gc )
	{
		try 
		{
			texture = new Image( gc.getWidth() + Level.tileSize*2, gc.getHeight() + Level.tileSize*2 );
			
			Graphics bgg = texture.getGraphics();
			for( int y = 0; y < gc.getHeight() + Level.tileSize*2; y += Level.tileSize )
			{
				for( int x = 0; x < gc.getWidth() + Level.tileSize*2; x += Level.tileSize )
				{	
					bgg.pushTransform();
					bgg.translate( x, y );
					AutoTileDrawer.draw( bgg, cs.l.theme.wall, Level.tileSize, 0, true, true, true, true, true, true, true, true );
					bgg.popTransform();
				}
			}
		} 
		catch( SlickException e )
		{
			e.printStackTrace();
		}
	}
}