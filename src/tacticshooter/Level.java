package tacticshooter;

import java.awt.Graphics2D;
import java.io.Serializable;
import java.util.ArrayList;

import javax.vecmath.Point2f;
import javax.vecmath.Vector2f;

import org.newdawn.slick.util.pathfinding.Mover;
import org.newdawn.slick.util.pathfinding.TileBasedMap;

import com.phyloa.dlib.renderer.Graphics2DRenderer;
import com.phyloa.dlib.util.DMath;

public class Level implements Serializable, TileBasedMap
{
	public static int tileSize = 20;
	
	public ArrayList<Building> buildings = new ArrayList<Building>();
	
	int[][] tiles;
	
	int[][] visited;
	
	int width;
	int height;
	
	public Level()
	{
		
	}
	
	public Level( int width, int height )
	{
		this.width = width;
		this.height = height;
		tiles = new int[width][height];
		visited = new int[width][height];
	}
	
	public void render( Graphics2DRenderer g )
	{
		for( int y = 0; y < height; y++ )
		{
			for( int x = 0; x < width; x++ )
			{
				switch( tiles[x][y] )
				{
				case 0: 
					//g.drawRect( x*tileSize, y*tileSize, tileSize, tileSize ); 
					break;
				case 1: 
					g.fillRect( x*tileSize, y*tileSize, tileSize, tileSize ); 
				break;
				}
			}
		}
		
		for( Building b : buildings )
		{
			b.render( g );
		}
	}
	
	public void clearVisited()
	{
		for( int y = 0; y < height; y++ )
		{
			for( int x = 0; x < width; x++ )
			{
				visited[x][y] = 0;
			}
		}
	}
	
	public int getTileX( float x )
	{
		return (int)(x / tileSize);
	}
	
	public int getTileY( float y )
	{
		return (int)(y / tileSize);
	}

	public boolean blocked( Mover m, int x, int y ) 
	{
		return tiles[x][y] == 1;
	}

	public float getCost( Mover m, int x, int y, int tx, int ty ) 
	{
		return DMath.randomf();
	}

	public int getHeightInTiles() 
	{
		return height;
	}

	public int getWidthInTiles() 
	{
		return width;
	}

	public void pathFinderVisited( int x, int y )
	{
		visited[x][y] = 1;
	}

	public int getTile( float lx, float ly )
	{
		return tiles[getTileX(lx)][getTileY(ly)];
	}
	
	public boolean hitwall( Point2f start, Vector2f direction )
	{
		Level l = this;
		int cx, cy; // current x, y, in tiles
		float cbx, cby; // starting tile cell bounds, in pixels
		float tMaxX, tMaxY; // maximum time the ray has traveled so far (not
							// distance!)
		float tDeltaX = 0, tDeltaY = 0; // the time that the ray needs to travel
										// to cross a single tile (not
										// distance!)
		int stepX, stepY; // step direction, either 1 or -1
		float outX, outY; // bounds of the tileMap where the ray would exit
		boolean hitTile = false;
		float tResult = 0;
		
		if( start == null ) return false;
		
		Point2f result = new Point2f();
		
		if( direction == null || (direction.x == 0 && direction.y == 0) )
		{
			// no direction, no ray
			result.x = start.x;
			result.y = start.y;
			return false;
		}
		
		// find the tile at the start position of the ray
		cx = l.getTileX( start.x );
		cy = l.getTileY( start.y );
		
		if( cx < 0 || cx >= l.width || cy < 0 || cy >= l.height )
		{
			// outside of the tilemap
			result.x = start.x;
			result.y = start.y;
			return false;
		}
		
		if( l.tiles[cx][cy] != 0 )
		{
			// start point is inside a block
			result.x = start.x;
			result.y = start.y;
			return true;
		}
		
		int maxTilesToCheck = l.height * l.width;
		
		// determine step direction, and initial starting block
		if( direction.x > 0 )
		{
			stepX = 1;
			outX = l.width;
			cbx = (cx + 1) * l.tileSize;
		}
		else
		{
			stepX = -1;
			outX = -1;
			cbx = cx * l.tileSize;
		}
		if( direction.y > 0 )
		{
			stepY = 1;
			outY = l.height;
			cby = (cy + 1) * Level.tileSize;
		}
		else
		{
			stepY = -1;
			outY = -1;
			cby = cy * Level.tileSize;
		}
		
		// determine tMaxes and deltas
		if( direction.x != 0 )
		{
			tMaxX = (cbx - start.x) / direction.x;
			tDeltaX = Level.tileSize * stepX / direction.x;
		}
		else tMaxX = 1000000;
		if( direction.y != 0 )
		{
			tMaxY = (cby - start.y) / direction.y;
			tDeltaY = Level.tileSize * stepY / direction.y;
		}
		else tMaxY = 1000000;
		
		// step through each block
		for( int tileCount = 0; tileCount < maxTilesToCheck; tileCount++ )
		{
			if( tMaxX < tMaxY )
			{
				cx = cx + stepX;
				if( l.tiles[cx][cy] != 0 )
				{
					hitTile = true;
					break;
				}
				if( cx == outX )
				{
					hitTile = false;
					break;
				}
				tMaxX = tMaxX + tDeltaX;
			}
			else
			{
				cy = cy + stepY;
				if( l.tiles[cx][cy] != 0 )
				{
					hitTile = true;
					break;
				}
				if( cy == outY )
				{
					hitTile = false;
					break;
				}
				tMaxY = tMaxY + tDeltaY;
			}
		}
		
		// result time
		tResult = (tMaxX < tMaxY) ? tMaxX : tMaxY;
		
		/*
		 * // store the result result.x = start.x + (direction.x * tResult);
		 * result.y = start.y + (direction.y * tResult); if(resultInTiles !=
		 * null) { resultInTiles.x = cx; resultInTiles.y = cy; }
		 */

		return hitTile && tResult < 1;
	}
}
