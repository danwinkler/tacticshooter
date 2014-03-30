package com.danwink.tacticshooter.editor;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import com.danwink.tacticshooter.editor.AutoTileDrawer;
import com.danwink.tacticshooter.gameobjects.Level;
import com.danwink.tacticshooter.gameobjects.Level.TileType;
import com.phyloa.dlib.renderer.Graphics2DIRenderer;
import com.phyloa.dlib.renderer.Renderer2D;
import com.phyloa.dlib.util.DFile;
import com.phyloa.dlib.util.DOptions;

public class LevelRenderer
{
	Level l;
	
	BufferedImage floor;
	BufferedImage wall;
	
	public LevelRenderer( Level l )
	{
		this.l = l;
		
		try
		{
			floor = DFile.loadImage(  "img" + File.separator + new DOptions( "themes" + File.separator + l.theme ).getS( "floor" ) );
			wall = DFile.loadImage(  "img" + File.separator + new DOptions( "themes" + File.separator + l.theme ).getS( "wall" ) );
		}
		catch( IOException e )
		{
			e.printStackTrace();
		}
	}
	
	public void render( Graphics2DIRenderer g )
	{
		for( int y = 0; y < l.height; y++ )
		{
			for( int x = 0; x < l.width; x++ )
			{
				if( l.tiles[x][y] != TileType.WALL )
				{
					drawAutoTile( g, x, y, TileType.FLOOR, floor );
				}
				else if( l.tiles[x][y] == TileType.WALL )
				{
					drawAutoTile( g, x, y, TileType.WALL, wall );
				}
			}
		}
	}
	
	public void drawAutoTile( Graphics2DIRenderer g, int x, int y, TileType autoTile, BufferedImage tileImage )
	{
		g.pushMatrix();
		g.translate( x*l.tileSize, y*l.tileSize );
		//g.scale( tileSize/32f, tileSize/32f );
		AutoTileDrawer.draw( g.g, tileImage, l.tileSize, 0, 	
													l.getTile( x-1, y-1 ).connectsTo( autoTile ), 
													l.getTile( x, y-1 ).connectsTo( autoTile ), 
													l.getTile( x+1, y-1 ).connectsTo( autoTile ), 
													l.getTile( x-1, y ).connectsTo( autoTile ), 
													l.getTile( x+1, y ).connectsTo( autoTile ), 
													l.getTile( x-1, y+1 ).connectsTo( autoTile ), 
													l.getTile( x, y+1 ).connectsTo( autoTile ), 
													l.getTile( x+1, y+1 ).connectsTo( autoTile ) 
		);
		g.popMatrix();
	}
}
