package com.danwink.tacticshooter.editor;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import com.danwink.tacticshooter.editor.AutoTileDrawer;
import com.danwink.tacticshooter.gameobjects.Building;
import com.danwink.tacticshooter.gameobjects.Level;
import com.danwink.tacticshooter.gameobjects.Level.TileType;
import com.phyloa.dlib.renderer.Graphics2DIRenderer;
import com.phyloa.dlib.util.DFile;
import com.phyloa.dlib.util.DOptions;

public class LevelRenderer
{
	Level l;
	
	BufferedImage floor;
	BufferedImage wall;
	BufferedImage grate;
	
	public LevelRenderer( Level l )
	{
		this.l = l;
		
		try
		{
			floor = loadImage( l.themeName, "floor" );
			wall  = loadImage( l.themeName, "wall" );
			grate = loadImage( l.themeName, "grate" );
		}
		catch( IOException e )
		{
			e.printStackTrace();
		}
	}
	
	public BufferedImage loadImage( String theme, String name ) throws IOException
	{
		return DFile.loadImage( "themes" + File.separator + theme + File.separator + name + ".png" );
	}
	
	public void render( Graphics2DIRenderer g, Building selected )
	{
		for( int y = 0; y < l.height; y++ )
		{
			for( int x = 0; x < l.width; x++ )
			{
				if( l.tiles[x][y] == TileType.FLOOR )
				{
					drawAutoTile( g, x, y, TileType.FLOOR, floor );
				}
				else if( l.tiles[x][y] == TileType.WALL )
				{
					drawAutoTile( g, x, y, TileType.WALL, wall );
				}
				else if( l.tiles[x][y] == TileType.DOOR )
				{
					//drawAutoTile( g, x, y, TileType.FLOOR, floor );
					drawAutoTile( g, x, y, TileType.WALL, wall );
				}
				else if( l.tiles[x][y] == TileType.GRATE )
				{
					drawAutoTile( g, x, y, TileType.FLOOR, floor );
					drawAutoTile( g, x, y, TileType.GRATE, grate );
				}
			}
		}
		
		for( Building b : l.buildings )
		{
			float rad = b.radius;
			g.color( b == selected ? Color.red : Color.black );
			g.pushMatrix();
			g.translate( b.x, b.y );
			g.drawOval( -rad, -rad, rad*2, rad*2 );
			g.color( Color.GREEN );
			if( b.name != null )
			{
				g.text( b.name, 0, 0 );
			}
			g.popMatrix();
		}
	}
	
	public void drawAutoTile( Graphics2DIRenderer g, int x, int y, TileType autoTile, BufferedImage tileImage )
	{
		g.pushMatrix();
		g.translate( x*Level.tileSize, y*Level.tileSize );
		//g.scale( tileSize/32f, tileSize/32f );
		boolean nw = l.getTile( x-1, y-1 ).connectsTo( autoTile );
		boolean n = l.getTile( x, y-1 ).connectsTo( autoTile );
		boolean ne = l.getTile( x+1, y-1 ).connectsTo( autoTile );
		boolean w = l.getTile( x-1, y ).connectsTo( autoTile );
		boolean e = l.getTile( x+1, y ).connectsTo( autoTile );
		boolean sw = l.getTile( x-1, y+1 ).connectsTo( autoTile );
		boolean s = l.getTile( x, y+1 ).connectsTo( autoTile );
		boolean se = l.getTile( x+1, y+1 ).connectsTo( autoTile );
		
		AutoTileDrawer.draw( g.g, tileImage, Level.tileSize, 0, nw, n, ne, w, e, sw, s, se );
		
		g.popMatrix();
	}
}
