package tacticshooter;

import tacticshooter.Building.BuildingType;

import com.phyloa.dlib.util.DMath;

public class LevelBuilder 
{
	public static void addBorder( Level l )
	{
		for( int x = 0; x < l.width; x++ )
		{
			l.tiles[x][0] = 1;
			l.tiles[x][l.height-1] = 1;
		}
		
		for( int y = 0; y < l.height; y++ )
		{
			l.tiles[0][y] = 1;
			l.tiles[l.width-1][y] = 1;
		}
	}
	
	public static void addWall( Level l, int xx1, int yy1, int xx2, int yy2, int val )
	{
		int x1 = Math.min( xx1, xx2 );
		int y1 = Math.min( yy1, yy2 );
		
		int x2 = Math.max( xx1, xx2 );
		int y2 = Math.max( yy1, yy2 );
		
		int dx = x2 - x1;
		if( dx == 0 )
		{
			for( int y = y1; y <= y2; y++ )
			{
				l.tiles[x1][y] = 1;
			}
			return;
		}
		int dy = y2 - y1;
		float error = 0;
		float derr = Math.abs((float)dy / (float)dx);
		int y = y1;
		for( int x = x1; x <= x2; x++ )
		{
			l.tiles[x][y] = val;
			if( y == y2 && x == x2 ) break;
			error += derr;
			while( error > .5f )
			{
				y++;
				error -= 1;
				if( error > .5f )
				{
					l.tiles[x][y] = val;
				}
			}
		}
	}
	
	public static void addBox( Level l, int x, int y, int width, int height, int val )
	{
		addWall( l, x, y, x+width, y, val );
		addWall( l, x, y+height, x+width, y+height, val );
		addWall( l, x, y, x, y+height, val );
		addWall( l, x+width, y, x+width, y+height, val );	
	}
	
	public static void fillBox( Level l, int x, int y, int width, int height, int val )
	{
		for( int yy = y; yy < y+height; yy++ )
		{
			for( int xx = x; xx < x+width; xx++ )
			{
				l.tiles[xx][yy] = val;
			}
		}
	}
	
	public static void buildLevelA( Level l )
	{
		addBorder( l );
		
		addBox( l, 5, 5, l.width-10, l.height-10, 1 );
		l.tiles[l.width/2][5] = 0;
		l.tiles[l.width/2][l.height-5] = 0;
		l.tiles[5][l.height/2] = 0;
		l.tiles[l.width-5][l.height/2] = 0;
		
		fillBox( l, 10, 10, l.width-20, l.height-20, 1 );
	}
	
	public static void buildLevelB( Level l, Team a, Team b )
	{
		for( int y = 0; y < l.height; y++ )
		{
			for( int x = 0; x < l.width; x++ )
			{
				l.tiles[x][y] = 1;
			}
		}
		
		fillBox( l, 1, 1, 5, 5, 0 );
		fillBox( l, l.width-6, l.height-6, 5, 5, 0 );
		
		l.buildings.add( new Building( 3 * l.tileSize, 3 * l.tileSize, BuildingType.CENTER, a ) );
		l.buildings.add( new Building( (l.width-3) * l.tileSize, (l.height-3) * l.tileSize, BuildingType.CENTER, b ) );
		
		int[][] boxes = new int[5][4];
		for( int i = 0; i < 5; i++ )
		{
			boxes[i][2] = DMath.randomi( 6, 15 ); //width
			boxes[i][3] = DMath.randomi( 6, 15 ); //height
			boxes[i][0] = DMath.randomi( 1, l.width - boxes[i][2] ); //x
			boxes[i][1] = DMath.randomi( 1, l.height - boxes[i][3] ); //y
		}
		
		for( int i = 0; i < 5; i++ )
		{
			fillBox( l, boxes[i][0], boxes[i][1], boxes[i][2], boxes[i][3], 0 );
			for( int j = 0; j < 5; j++ )
			{
				for( int y = 0; y < 2; y++ )
				{
					for( int x = 0; x < 2; x++ )
					{
						//addWall( l, boxes[i][0] + boxes[i][2]/2 + x, boxes[i][1] + boxes[i][3]/2 + y, boxes[j][0] + boxes[j][2]/2 + x, boxes[j][1] + boxes[j][3]/2 + y, 0 );
					}
				}
			}
			for( int y = 0; y < 2; y++ )
			{
				for( int x = 0; x < 2; x++ )
				{
					addWall( l, boxes[i][0] + boxes[i][2]/2 + x, boxes[i][1] + boxes[i][3]/2 + y, 3 + x, 3 + y, 0 );
					addWall( l, boxes[i][0] + boxes[i][2]/2 + x, boxes[i][1] + boxes[i][3]/2 + y, l.width-3 + x, l.height-3 + y, 0 );
				}
			}
		}
	}
}
