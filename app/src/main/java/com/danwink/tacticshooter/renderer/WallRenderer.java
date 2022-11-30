package com.danwink.tacticshooter.renderer;

import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.SlickException;

import com.danwink.tacticshooter.AutoTileDrawer;
import com.danwink.tacticshooter.ClientState;
import com.danwink.tacticshooter.gameobjects.Level;
import com.danwink.tacticshooter.gameobjects.Level.TileType;

public class WallRenderer
{
	public Image texture;
	
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
			//Figure out lower wall stuff
			RWTile[][] rt = new RWTile[cs.l.width][cs.l.height];
			for( int y = 0; y < cs.l.height; y++ )
			{
				for( int x = 0; x < cs.l.width; x++ )
				{
					if( cs.l.getTile( x, y ) == TileType.WALL ) 
					{
						if( cs.l.getTile( x, y+1 ) != TileType.WALL ) 
						{
							TileType left = cs.l.getTile( x-1, y );
							TileType right = cs.l.getTile( x+1, y );
							if( left == TileType.WALL && right == TileType.WALL )
							{
								rt[x][y] = RWTile.WALL_BOTH;
							}
							else if( left == TileType.WALL )
							{
								rt[x][y] = RWTile.WALL_RIGHT;
							}
							else if( right == TileType.WALL )
							{
								rt[x][y] = RWTile.WALL_LEFT;
							}
							else
							{
								rt[x][y] = RWTile.WALL_MID;
							}
							
						}
						else
						{
							rt[x][y] = RWTile.ROOF;
						}
					}
				}
			}
			
			Graphics g = texture.getGraphics();
			g.clear();
			
			g.setLineWidth( 1 );
			//draw walls
			
			for( int y = 0; y < cs.l.height; y++ )
			{
				for( int x = 0; x < cs.l.width; x++ )
				{
					drawTile( rt, cs.l, x, y, g );
				}
			}
			g.setLineWidth( 1 );

			g.flush();
		}
		catch( SlickException e )
		{
			e.printStackTrace();
		}
	}
	
	public void drawTile( RWTile[][] rt, Level l, int x, int y, Graphics g )
	{
		switch( l.tiles[x][y] )
		{
		case DOOR:
			drawAutoTile( l, g, x, y, TileType.FLOOR, l.theme.floor );
			drawAutoTile( l, g, x, y, TileType.WALL, l.theme.wall );
			break;
		case GRATE:
			drawAutoTile( l, g, x, y, TileType.FLOOR, l.theme.floor );
			drawAutoTile( l, g, x, y, TileType.GRATE, l.theme.grate );
			break;
		case WALL:
			/*
			g.drawImage( 
				l.theme.floor, 
				x*Level.tileSize, y*Level.tileSize, 
				x*Level.tileSize+Level.tileSize, y*Level.tileSize+Level.tileSize, 
				l.theme.floor.getWidth()/3, 0, 
				l.theme.floor.getWidth()/3 * 2, l.theme.floor.getHeight()/4 
			);
			*/
			
			drawAutoTile( l, g, x, y, l.tiles[x][y], l.theme.wall );
			/*
			switch( rt[x][y] )
			{
			case ROOF:
				drawWallAutoTile( rt, l, g, x, y, l.tiles[x][y], l.theme.wall );
			case WALL_BOTH:
				break;
			case WALL_LEFT:
				break;
			case WALL_MID:
				g.drawImage( l.theme.wall, x*Level.tileSize, y*Level.tileSize, Level.tileSize, Level.tileSize*3, Level.tileSize, Level.tileSize );
				break;
			case WALL_RIGHT:
				//l.theme.wall.draw( x, y,  );
				break;
			}
			*/
			break;
		case FLOOR:
			break;
		default:
			break;
		}
	}
	
	public RWTile getTile( RWTile[][] rt, int x, int y )
	{
		if( x < 0 || x >= rt.length || y < 0 || y >= rt[0].length )
		{
			return RWTile.ROOF;
		}
		return rt[x][y];
	}
	
	public void drawWallAutoTile( RWTile[][] rt, Level l, Graphics g, int x, int y, TileType autoTile, Image tileImage )
	{
		g.pushTransform();
		g.translate( x*Level.tileSize, y*Level.tileSize );
		//g.scale( tileSize/32f, tileSize/32f );
		AutoTileDrawer.draw( g, tileImage, Level.tileSize, 0, 	
													getTile( rt, x-1, y-1 ) == RWTile.ROOF, 
													getTile( rt, x, y-1 ) == RWTile.ROOF, 
													getTile( rt, x+1, y-1 ) == RWTile.ROOF, 
													getTile( rt, x-1, y ) == RWTile.ROOF, 
													getTile( rt, x+1, y ) == RWTile.ROOF, 
													getTile( rt, x-1, y+1 ) == RWTile.ROOF, 
													getTile( rt, x, y+1 ) == RWTile.ROOF, 
													getTile( rt, x+1, y+1 ) == RWTile.ROOF 
							);
		g.popTransform();
	}
	
	public void drawAutoTile( Level l, Graphics g, int x, int y, TileType autoTile, Image tileImage )
	{
		g.pushTransform();
		g.translate( x*Level.tileSize, y*Level.tileSize );
		//g.scale( tileSize/32f, tileSize/32f );
		AutoTileDrawer.draw( g, tileImage, Level.tileSize, 0, 	
													l.getTile( x-1, y-1 ).connectsTo( autoTile ), 
													l.getTile( x, y-1 ).connectsTo( autoTile ), 
													l.getTile( x+1, y-1 ).connectsTo( autoTile ), 
													l.getTile( x-1, y ).connectsTo( autoTile ), 
													l.getTile( x+1, y ).connectsTo( autoTile ), 
													l.getTile( x-1, y+1 ).connectsTo( autoTile ), 
													l.getTile( x, y+1 ).connectsTo( autoTile ), 
													l.getTile( x+1, y+1 ).connectsTo( autoTile ) 
							);
		g.popTransform();
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
	
	public enum RWTile {
		WALL_LEFT,
		WALL_MID,
		WALL_RIGHT,
		WALL_BOTH,
		ROOF;
		
		int x, y;
		RWTile(){}
		RWTile( int x, int y ) {
			this.x = x;
			this.y = y;
		}
	}
}