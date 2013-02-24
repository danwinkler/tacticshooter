package tacticshooter;

import java.io.File;
import java.util.ArrayList;

import javax.vecmath.Point2f;
import javax.vecmath.Point2i;
import javax.vecmath.Vector2f;

import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.geom.Polygon;
import org.newdawn.slick.util.pathfinding.Mover;
import org.newdawn.slick.util.pathfinding.PathFindingContext;
import org.newdawn.slick.util.pathfinding.TileBasedMap;

import tacticshooter.Level.TileType;
import tacticshooter.Unit.UnitState;

import com.phyloa.dlib.util.DMath;
import com.phyloa.dlib.util.DOptions;

public class Level implements TileBasedMap
{
	public static int tileSize = 20;
	
	public ArrayList<Building> buildings = new ArrayList<Building>();
	public ArrayList<Link> links = new ArrayList<Link>();
	
	public TileType[][] tiles;
	
	public int width;
	public int height;
	
	boolean randomFinding = true;
	
	String theme = "desertrpg.txt";
	
	public Image floor;
	public Image wall;
	public Image wall3d;
	public Image grate;
	
	public float[][] lightMap;
	
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
	}
	
	public void renderLinks( Graphics g )
	{
		g.setColor( Color.green );
		for( int i = 0; i < links.size(); i++ )
		{
			Link l = links.get( i );
			for( int j = 0; j < buildings.size(); j++ )
			{
				Building b = buildings.get( j );
				if( b.id == l.source )
				{
					g.drawLine( b.x, b.y, l.targetX*tileSize + tileSize/2, l.targetY*tileSize + tileSize/2 );
					break;
				}
			}
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
		case LIGHT:
			//g.setColor( Color.yellow );
			//g.fillOval(  x*tileSize, y*tileSize, tileSize, tileSize );
			break;
		case PASSOPEN:
			g.setColor( Color.gray );
			g.drawRect( x*tileSize + tileSize/4, y*tileSize + tileSize/4, tileSize/2, tileSize/2 );
			break;
		case PASSCLOSED:
			g.setColor( Color.gray );
			g.fillRect( x*tileSize + tileSize/4, y*tileSize + tileSize/4, tileSize/2, tileSize/2 );
			break;
		case GATEOPEN:
			g.setColor( Color.gray );
			g.drawRect( x*tileSize + tileSize/4, y*tileSize + tileSize/4, tileSize/2, tileSize/2 );
			break;
		case GATECLOSED:
			g.setColor( Color.gray );
			g.fillRect( x*tileSize + tileSize/4, y*tileSize + tileSize/4, tileSize/2, tileSize/2 );
			break;
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
			/*
			g.setColor( Color.gray );
			g.fillRect( x*tileSize, y*tileSize, tileSize, tileSize ); 
			g.setColor( Color.black );
			if( !getTile( x-1, y ).isWall )
			{
				g.drawLine( x*tileSize, y*tileSize, x*tileSize, y*tileSize+tileSize );
			}
			if( !getTile( x+1, y ).isWall )
			{
				g.drawLine( x*tileSize+tileSize, y*tileSize, x*tileSize+tileSize, y*tileSize+tileSize );
			}
			if( !getTile( x, y-1 ).isWall )
			{
				g.drawLine( x*tileSize, y*tileSize, x*tileSize+tileSize, y*tileSize );
			}
			if( !getTile( x, y+1 ).isWall )
			{
				g.drawLine( x*tileSize, y*tileSize+tileSize, x*tileSize+tileSize, y*tileSize+tileSize );
			}
			
			if( (x+y) % 4 == 0 )
			{
				g.setColor( Color.darkGray );
				g.drawLine( x*tileSize, y*tileSize+tileSize, x*tileSize+tileSize, y*tileSize );
			}
			
			if( (x-y) % 6 == 0 )
			{
				g.setColor( Color.darkGray );
				g.drawLine( x*tileSize, y*tileSize, x*tileSize+tileSize, y*tileSize+tileSize );
			}
			*/
			break;
		case TRIANGLENE:
			g.setColor( Color.gray );
			Polygon nep = new Polygon();
			nep.addPoint( x*tileSize, y*tileSize );
			nep.addPoint( x*tileSize+tileSize, y*tileSize );
			nep.addPoint( x*tileSize+tileSize, y*tileSize+tileSize );
			g.fill( nep );
			g.setColor( Color.black );
			g.drawLine( x*tileSize, y*tileSize, x*tileSize+tileSize, y*tileSize+tileSize );
			
			if( (x+y) % 4 == 0 )
			{
				g.setColor( Color.darkGray );
				g.drawLine( x*tileSize+tileSize*.5f, y*tileSize+tileSize*.5f, x*tileSize+tileSize, y*tileSize );
			}
			break;
		case TRIANGLENW:
			g.setColor( Color.gray );
			Polygon nwp = new Polygon();
			nwp.addPoint( x*tileSize, y*tileSize );
			nwp.addPoint( x*tileSize, y*tileSize+tileSize );
			nwp.addPoint( x*tileSize+tileSize, y*tileSize );
			g.fill( nwp );
			g.setColor( Color.black );
			g.drawLine( x*tileSize, y*tileSize+tileSize, x*tileSize+tileSize, y*tileSize );
			
			if( (x-y) % 6 == 0 )
			{
				g.setColor( Color.darkGray );
				g.drawLine( x*tileSize, y*tileSize, x*tileSize+tileSize*.5f, y*tileSize+tileSize*.5f );
			}
			break;
		case TRIANGLESE:
			g.setColor( Color.gray );
			Polygon sep = new Polygon();
			sep.addPoint( x*tileSize+tileSize, y*tileSize+tileSize );
			sep.addPoint( x*tileSize, y*tileSize+tileSize );
			sep.addPoint( x*tileSize+tileSize, y*tileSize );
			g.fill( sep );
			g.setColor( Color.black );
			g.drawLine( x*tileSize, y*tileSize+tileSize, x*tileSize+tileSize, y*tileSize );
			
			if( (x-y) % 6 == 0 )
			{
				g.setColor( Color.darkGray );
				g.drawLine( x*tileSize+tileSize, y*tileSize+tileSize, x*tileSize+tileSize*.5f, y*tileSize+tileSize*.5f );
			}				
			break;
		case TRIANGLESW:
			g.setColor( Color.gray );
			Polygon swp = new Polygon();
			swp.addPoint( x*tileSize, y*tileSize );
			swp.addPoint( x*tileSize, y*tileSize+tileSize );
			swp.addPoint( x*tileSize+tileSize, y*tileSize+tileSize );
			g.fill( swp );
			g.setColor( Color.black );
			g.drawLine( x*tileSize, y*tileSize, x*tileSize+tileSize, y*tileSize+tileSize );
			
			if( (x+y) % 4 == 0 )
			{
				g.setColor( Color.darkGray );
				g.drawLine( x*tileSize+tileSize*.5f, y*tileSize+tileSize*.5f, x*tileSize, y*tileSize+tileSize );
			}
			break;
		}
	}
	
	public void loadTextures()
	{
		try
		{
			wall = new Image( "img" + File.separator + new DOptions( "themes" + File.separator + theme ).getS( "wall" ) );
			wall3d = new Image( "img" + File.separator + new DOptions( "themes" + File.separator + theme ).getS( "wall3d" ) );
			floor = new Image( "img" + File.separator + new DOptions( "themes" + File.separator + theme ).getS( "floor" ) );
			grate = new Image( "img" + File.separator + new DOptions( "themes" + File.separator + theme ).getS( "grate" ) );
			
			wall.setFilter( Image.FILTER_NEAREST );
			wall3d.setFilter( Image.FILTER_NEAREST );
			floor.setFilter( Image.FILTER_NEAREST );
			grate.setFilter( Image.FILTER_NEAREST );
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
		
		/*
		 * // store the result result.x = start.x + (direction.x * tResult);
		 * result.y = start.y + (direction.y * tResult); if(resultInTiles !=
		 * null) { resultInTiles.x = cx; resultInTiles.y = cy; }
		 */

		return hitTile && tResult < 1;
	}
	
	public TileType getTile( int x, int y )
	{
		if( x < 0 || x >= width || y < 0 || y >= height )
		{
			return TileType.FLOOR;
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
		FLOOR( true, true ),
		WALL( new WallInfo() ),
		LIGHT( true, true ),
		TRIANGLENW( true, true ),
		TRIANGLESW( true, true ),
		TRIANGLENE( true, true ),
		TRIANGLESE( true, true ),
		PASSOPEN( new PassInfo() ),
		PASSCLOSED( new GateInfo() ),
		GATEOPEN( new PassInfo() ),
		GATECLOSED( new GateInfo() ),
		DOOR( new DoorInfo() ),
		GRATE( new GrateInfo() );
		
		TileInfo ti;
		
		TileType()
		{
			
		}
		
		TileType( boolean passable, boolean shootable )
		{
			ti = new TileInfo( passable, shootable );
			ti.t = this;
		}
		
		TileType( TileInfo ti )
		{
			this.ti = ti;
			ti.t = this;
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
	
	public static class PassInfo extends TileInfo
	{
		public PassInfo()
		{
			super( true, true );
		}
		
		public boolean connectsTo( TileType t )
		{
			return t == TileType.FLOOR;
		}
	}
	
	public static class GateInfo extends TileInfo
	{
		public GateInfo()
		{
			super( false, false );
		}
		
		public boolean connectsTo( TileType t )
		{
			return t == TileType.FLOOR;
		}
	}
	
	public static class Link
	{
		public int source;
		public int targetX, targetY;
		
		public Link()
		{
			
		}
		
		public Link( int source, int targetX, int targetY )
		{
			this.source = source;
			this.targetX = targetX;
			this.targetY = targetY;
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

	public void signal( Building b, boolean signal, TacticServer ts )
	{
		for( int i = 0; i < links.size(); i++ )
		{
			Link l = links.get( i );
			if( l.source == b.id )
			{
				TileType target = getTile( l.targetX, l.targetY );
				TileType set = null;
				switch( target )
				{
				case PASSOPEN:
					if( signal )
					{
						set = TileType.PASSCLOSED;
					}
					break;
				case PASSCLOSED:
					if( !signal )
					{
						set = TileType.PASSOPEN;
					}
					break;
				case GATEOPEN:
					if( !signal )
					{
						set = TileType.GATECLOSED;
					}
					break;
				case GATECLOSED:
					if( signal )
					{
						set = TileType.GATEOPEN;
					}
					break;
				}
				if( set != null && tiles[l.targetX][l.targetY] != set )
				{
					tiles[l.targetX][l.targetY] = set;
					ts.si.sendToAllClients( new Message( MessageType.TILEUPDATE, new Object[] { l.targetX, l.targetY, set } ) );
					if( !set.isPassable() )
					{
						for( int j = 0; j < ts.units.size(); j++ )
						{
							Unit u = ts.units.get( j );
							for( int k = 0; k < u.path.size(); k++ )
							{
								Point2i p = u.path.get( k );
								if( p.x == l.targetX && p.y == l.targetY )
								{
									u.path.clear();
									u.state = UnitState.STOPPED;
									u.pathTo( u.destx, u.desty, ts );
									ts.si.sendToAllClients( new Message( MessageType.UNITUPDATE, u ) );
									break;
								}
							}
						}
					}
				}
			}
		}
	}
}
