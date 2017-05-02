package com.danwink.tacticshooter.ai;

import java.lang.reflect.Field;
import java.util.ArrayList;

import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.util.pathfinding.PathFinder;

import com.danwink.tacticshooter.gameobjects.Building;
import com.danwink.tacticshooter.gameobjects.Level;
import com.danwink.tacticshooter.gameobjects.Team;
import com.danwink.tacticshooter.gameobjects.Building.BuildingType;
import com.phyloa.dlib.util.DMath;

public class LevelAnalysis
{
	int width, height;
	TileAnalysis[][] tiles;
	Level l;
	ArrayList<Zone> zones = new ArrayList<Zone>();
	
	public void spreadTile( TileAnalysis ta, int x, int y, int tx, int ty, TileAnalysis[][] changes, Field f ) throws IllegalArgumentException, IllegalAccessException
	{
		if( tx >= width || tx < 0 || ty >= height || ty < 0 ) return;
		if( l.tiles[tx][ty].isPassable() && f.get( tiles[tx][ty] ) == null ) 
		{
			TileAnalysis nt = new TileAnalysis( tiles[tx][ty] );
			f.set( nt, f.get( ta ) );
			changes[tx][ty] = nt;
		}
	}
	
	public void fillField( String fieldName ) throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException
	{
		Field f = TileAnalysis.class.getField( fieldName );
		for( int i = 0; i < 500; i++ )
		{
			TileAnalysis[][] changes = new TileAnalysis[width][height];
			for( int x = 0; x < width; x++ )
			{
				for( int y = 0; y < height; y++ )
				{
					TileAnalysis t = tiles[x][y];
					
					if( f.get( t ) == null ) continue;
					
					spreadTile( t, x, y, x-1, y, changes, f );
					spreadTile( t, x, y, x+1, y, changes, f );
					spreadTile( t, x, y, x, y-1, changes, f );
					spreadTile( t, x, y, x, y+1, changes, f );
				}
			}
			
			boolean canExit = true;
			for( int x = 0; x < width; x++ )
			{
				for( int y = 0; y < height; y++ )
				{
					if( changes[x][y] != null ) 
					{
						tiles[x][y] = changes[x][y];
						canExit = false;
					}
				}
			}
			if( canExit )
			{
				break;
			}
			
			if( i % 10 == 0 ) 
			{
				System.out.println( i );
			}
		}
	}
	
	public void setAdjacent( int x, int y, int tx, int ty )
	{
		if( tx < 0 || tx >= width || ty < 0 || ty >= height ) return;
		TileAnalysis t = tiles[x][y];
		TileAnalysis a = tiles[tx][ty];
		if( t.zone != null && a.zone != null && t.zone != a.zone )
		{
			t.zone.addNeightbor( a.zone );
		}
	}
	
	public void build( Level l, PathFinder finder )
	{
		this.l = l;
		width = l.width;
		height = l.height;
		
		tiles = new TileAnalysis[width][height];
		for( int x = 0; x < width; x++ )
		{
			for( int y = 0; y < height; y++ )
			{
				tiles[x][y] = new TileAnalysis();
			}
		}
		
		//Set initial zones
		for( Building b : l.buildings )
		{
			if( b.bt == BuildingType.CENTER ) 
			{
				int x = l.getTileX( b.x );
				int y = l.getTileY( b.y );
				Zone z = new Zone();
				z.c = new Color( b.t.getColor().r, b.t.getColor().g, b.t.getColor().b, .3f );
				z.b = b;
				tiles[x][y].zone = z;
				zones.add( z );
				tiles[x][y].side = b.t;
			} 
			else if( b.bt == BuildingType.POINT )
			{
				int x = l.getTileX( b.x );
				int y = l.getTileY( b.y );
				Zone z = new Zone();
				z.c = new Color( DMath.randomf(), DMath.randomf(), DMath.randomf(), .3f );
				z.b = b;
				tiles[x][y].zone = z;
				zones.add( z );
			}
		}
		
		//Flood fill
		try
		{
			fillField( "zone" );
			fillField( "side" );
		}
		catch( IllegalArgumentException | IllegalAccessException | NoSuchFieldException | SecurityException e )
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		//for each zone, calculate adjacent zones, and distances to each zone
		for( int x = 0; x < width; x++ )
		{
			for( int y = 0; y < height; y++ )
			{
				setAdjacent( x, y, x-1, y );
				setAdjacent( x, y, x+1, y );
				setAdjacent( x, y, x, y-1 );
				setAdjacent( x, y, x, y+1 );
			}
		}
		
		for( Zone z : zones )
		{
			for( Neighbor n : z.neighbors )
			{
				n.distance = finder.findPath( null, l.getTileX( z.b.x ), l.getTileY( z.b.y ), l.getTileX( n.z.b.x ), l.getTileY( n.z.b.y ) ).getLength();
			}
		}
	}
	
	class TileAnalysis
	{
		public Team side;
		public Zone zone;
		
		public TileAnalysis()
		{
			
		}
		
		public TileAnalysis( TileAnalysis ta )
		{
			this.side = ta.side;
			this.zone = ta.zone;
		}
	}
	
	class Zone
	{
		Color c;
		ArrayList<Neighbor> neighbors = new ArrayList<Neighbor>();
		Building b;
		
		public void addNeightbor( Zone z )
		{
			for( Neighbor n : neighbors )
			{
				if( n.z == z ) return;
			}
			neighbors.add( new Neighbor( z ) );
		}
	}
	
	class Neighbor
	{
		Zone z;
		int distance;
		
		public Neighbor( Zone z )
		{
			this.z = z;
		}
	}
	
	public void render( Graphics g )
	{
		for( int x = 0; x < width; x++ )
		{
			for( int y = 0; y < height; y++ )
			{
				TileAnalysis ta = tiles[x][y];
				if( ta.zone != null ) 
				{
					g.setColor( ta.zone.c );
					g.fillRect( x * Level.tileSize, y * Level.tileSize, Level.tileSize, Level.tileSize );
				}
				if( ta.side != null )
				{
					g.setColor( new Color( ta.side.getColor().r, ta.side.getColor().g, ta.side.getColor().b, .2f ) );
					g.fillRect( x * Level.tileSize, y * Level.tileSize, Level.tileSize, Level.tileSize );
				}
			}
		}
	}
}
