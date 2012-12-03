package tacticshooter;

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
	
	public static void addWall( Level l, int xx1, int yy1, int xx2, int yy2 )
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
			l.tiles[x][y] = 1;
			if( y == y2 && x == x2 ) break;
			error += derr;
			while( error > .5f )
			{
				y++;
				error -= 1;
				if( error > .5f )
				{
					l.tiles[x][y] = 1;
				}
			}
		}
	}
	
	public static void addBox( Level l, int x, int y, int width, int height )
	{
		addWall( l, x, y, x+width, y );
		addWall( l, x, y+height, x+width, y+height );
		addWall( l, x, y, x, y+height );
		addWall( l, x+width, y, x+width, y+height );	
	}
	
	public static void fillBox( Level l, int x, int y, int width, int height )
	{
		for( int yy = y; yy < y+height; yy++ )
		{
			for( int xx = x; xx < x+width; xx++ )
			{
				l.tiles[xx][yy] = 1;
			}
		}
	}
	
	public static void buildLevelA( Level l )
	{
		addBorder( l );
		
		addBox( l, 5, 5, l.width-10, l.height-10 );
		l.tiles[l.width/2][5] = 0;
		l.tiles[l.width/2][l.height-5] = 0;
		l.tiles[5][l.height/2] = 0;
		l.tiles[l.width-5][l.height/2] = 0;
		
		fillBox( l, 10, 10, l.width-20, l.height-20 );
	}
}
