package com.danwink.tacticshooter.gameobjects;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import javax.vecmath.Point2f;
import javax.vecmath.Vector2f;

import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.util.pathfinding.Mover;
import org.newdawn.slick.util.pathfinding.PathFindingContext;
import org.newdawn.slick.util.pathfinding.TileBasedMap;

import com.danwink.tacticshooter.AutoTileDrawer;
import com.danwink.tacticshooter.ComputerPlayer.PlayType;
import com.phyloa.dlib.util.DMath;
import com.phyloa.dlib.util.DOptions;

public class Level implements TileBasedMap
{
	public static int tileSize = 20;
	
	public ArrayList<Building> buildings = new ArrayList<Building>();
	
	public TileType[][] tiles;
	
	public int width;
	public int height;
	
	public boolean randomFinding = true;
	
	public String theme = "desertrpg.txt";
	
	public Image floor;
	public Image wall;
	public Image grate;
	
	public String code;
	public String ums;
	
	public SlotOption[] slotOptions = new SlotOption[16];
	
	public Level()
	{
		
	}
	
	public Level( int width, int height )
	{
		this.width = width;
		this.height = height;
		tiles = new TileType[width][height];
		for( int y = 0; y < height; y++ )
		{
			for( int x = 0; x < width; x++ )
			{
				tiles[x][y] = TileType.FLOOR;
			}
		}
		for( int i = 0; i < slotOptions.length; i++ )
		{
			slotOptions[i] = new SlotOption();
		}
	}
	
	public void renderFloor( Graphics g )
	{
		for( int y = 0; y < height; y++ )
		{
			for( int x = 0; x < width; x++ )
			{
				if( tiles[x][y] != TileType.WALL )
				{
					drawAutoTile( g, x, y, TileType.FLOOR, floor );
				}
			}
		}
	}
	
	public void render( Graphics g )
	{
		g.setLineWidth( 1 );
		//draw walls
		
		for( int y = 0; y < height; y++ )
		{
			for( int x = 0; x < width; x++ )
			{
				drawTile( x, y, g );
			}
		}
		g.setLineWidth( 1 );
	}
	
	public void drawTile( int x, int y, Graphics g )
	{
		switch( tiles[x][y] )
		{
		case DOOR:
			drawAutoTile( g, x, y, TileType.FLOOR, floor );
			drawAutoTile( g, x, y, TileType.WALL, wall );
			break;
		case GRATE:
			drawAutoTile( g, x, y, TileType.FLOOR, floor );
			drawAutoTile( g, x, y, TileType.GRATE, grate );
			break;
		case WALL:
			g.drawImage( floor, x*tileSize, y*tileSize, x*tileSize+tileSize, y*tileSize+tileSize, floor.getWidth()/3, 0, floor.getWidth()/3 * 2, floor.getHeight()/4 );
			drawAutoTile( g, x, y, tiles[x][y], wall );
			break;
		case FLOOR:
			break;
		default:
			break;
		}
	}
	
	public void loadTextures()
	{
		try
		{
			wall = new Image( "img" + File.separator + new DOptions( "themes" + File.separator + theme ).getS( "wall" ) );
			floor = new Image( "img" + File.separator + new DOptions( "themes" + File.separator + theme ).getS( "floor" ) );
			grate = new Image( "img" + File.separator + new DOptions( "themes" + File.separator + theme ).getS( "grate" ) );
		} catch( SlickException e )
		{
			e.printStackTrace();
		}
	}
	
	public void drawAutoTile( Graphics g, int x, int y, TileType autoTile, Image tileImage )
	{
		g.pushTransform();
		g.translate( x*tileSize, y*tileSize );
		//g.scale( tileSize/32f, tileSize/32f );
		AutoTileDrawer.draw( g, tileImage, tileSize, 0, 	
													getTile( x-1, y-1 ).connectsTo( autoTile ), 
													getTile( x, y-1 ).connectsTo( autoTile ), 
													getTile( x+1, y-1 ).connectsTo( autoTile ), 
													getTile( x-1, y ).connectsTo( autoTile ), 
													getTile( x+1, y ).connectsTo( autoTile ), 
													getTile( x-1, y+1 ).connectsTo( autoTile ), 
													getTile( x, y+1 ).connectsTo( autoTile ), 
													getTile( x+1, y+1 ).connectsTo( autoTile ) 
							);
		g.popTransform();
	}
	
	public void renderBuildings( Graphics g )
	{
		for( Building b : buildings )
		{
			b.render( g );
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
		return !getTile( x, y ).isPassable();
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
		
	}

	public TileType getTile( float lx, float ly )
	{
		return getTile( getTileX(lx), getTileY(ly) );
	}
	
	public boolean hitwall( Point2f start, Vector2f direction )
	{
		return hitwall( start, direction, new Point2f() );
	}
	
	public boolean hitwall( Point2f start, Vector2f direction, Point2f result )
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
		
		if( !l.tiles[cx][cy].isShootable() )
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
			cbx = (cx + 1) * Level.tileSize;
		}
		else
		{
			stepX = -1;
			outX = -1;
			cbx = cx * Level.tileSize;
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
				if( !getTile( cx, cy ).isShootable() )
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
				if( !getTile( cx, cy ).isShootable() )
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
		
		
		// store the result 
		result.x = start.x + (direction.x * tResult);
		result.y = start.y + (direction.y * tResult);
		
		/*
		if( resultInTiles != null ) { 
			resultInTiles.x = cx; 
			resultInTiles.y = cy; 
		}
		*/

		return hitTile && tResult < 1;
	}
	
	public TileType getTile( int x, int y )
	{
		if( x < 0 || x >= width || y < 0 || y >= height )
		{
			return TileType.WALL;
		}
		return tiles[x][y];
	}
	
	public void setTile( int x, int y, TileType val )
	{
		if( x < 0 || x >= width || y < 0 || y >= height )
		{
			return;
		}
		tiles[x][y] = val;
	}

	@Override
	public boolean blocked( PathFindingContext arg0, int x, int y )
	{
		return !getTile( x, y ).isPassable();
	}

	@Override
	public float getCost( PathFindingContext arg0, int x, int y )
	{
		return randomFinding ? DMath.randomf( .1f, .9f ) : 1;
	}
	
	public enum TileType
	{
		FLOOR( true, true, 0 ),
		WALL( new WallInfo(), 1 ),
		DOOR( new DoorInfo(), 11 ),
		GRATE( new GrateInfo(), 12 );
		
		TileInfo ti;
		public int data;
		
		TileType()
		{
			
		}
		
		TileType( boolean passable, boolean shootable, int data )
		{
			ti = new TileInfo( passable, shootable );
			ti.t = this;
			this.data = data;
		}
		
		TileType( TileInfo ti, int data )
		{
			this.ti = ti;
			ti.t = this;
			this.data = data;
		}
		
		public boolean isPassable()
		{
			return ti.passable;
		}
		
		public boolean isShootable()
		{
			return ti.shootable;
		}

		public boolean connectsTo( TileType tt )
		{
			return ti.connectsTo( tt );
		}
		
		public static HashMap<Integer, TileType> map = new HashMap<Integer, TileType>();
		static {
			for( TileType t : values() )
			{
				map.put( t.data, t );
			}
		}
		
		public static TileType getTile( int data )
		{
			return map.get( data );
		}
	}
	
	public static class TileInfo
	{
		public TileType t;
		public boolean passable;
		public boolean shootable;
		
		public TileInfo( boolean passable, boolean shootable)
		{
			this.passable = passable;
			this.shootable = shootable;
		}
		
		public boolean connectsTo( TileType t )
		{
			return this.t == t;
		}
	}

	public static class DoorInfo extends TileInfo
	{
		public DoorInfo()
		{
			super( true, false );
		}
		
		public boolean connectsTo( TileType t )
		{
			return t == TileType.DOOR || t == TileType.FLOOR || t == TileType.WALL;
		}
	}
	
	public static class GrateInfo extends TileInfo
	{
		public GrateInfo()
		{
			super( false, true );
		}
		
		public boolean connectsTo( TileType t )
		{
			return t == TileType.GRATE || t == TileType.FLOOR;
		}
	}
	
	public static class WallInfo extends TileInfo
	{
		public WallInfo()
		{
			super( false, false );
		}
		
		public boolean connectsTo( TileType t )
		{
			return t == TileType.GRATE || t == TileType.WALL;
		}
	}

	public Building getBuilding( int x, int y )
	{
		for( Building b : buildings )
		{
			if( x*tileSize+tileSize/2 == b.x && y*tileSize+tileSize/2 == b.y )
			{
				return b;
			}
		}
		return null;
	}
	
	public static class SlotOption
	{
		public SlotType st = SlotType.ANY;
		public PlayType bt = PlayType.AGGRESSIVE;
		
		public SlotOption()
		{
			
		}
	}
	
	public enum SlotType
	{
		ANY,
		PLAYER,
		COMPUTER,
		CLOSED;

		public boolean allowPlayer()
		{
			return this == ANY || this == PLAYER;
		}
	}

	public void resize( int width, int height )
	{
		TileType[][] newTiles = new TileType[width][height];
		for( int y = 0; y < height; y++ )
		{
			for( int x = 0; x < width; x++ )
			{
				if( x < this.width && y < this.height )
				{
					newTiles[x][y] = tiles[x][y];
				}
				else
				{
					newTiles[x][y] = TileType.FLOOR;
				}
			}
		}
		this.width = width;
		this.height = height;
		this.tiles = newTiles;
	}
}
