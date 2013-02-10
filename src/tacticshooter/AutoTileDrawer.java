package tacticshooter;
import java.awt.image.BufferedImage;

import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;

public class AutoTileDrawer 
{
	private static Graphics gg;
	private static Image imim;
	private static int ht;
	private static int it;
	private static int f;
	public static void draw( Graphics g, Image im, int tileSize, int frame, boolean nw, boolean n, boolean ne, boolean w, boolean e, boolean sw, boolean s, boolean se )
	{
		ht = tileSize/2;
		it = im.getWidth()/6;
		gg = g;
		imim = im;
		f = frame*it*2*3;
		
		if( !n && !e && !w && !s )
		{
			//Dot
			tl( 0, 0 );
			tr( 1, 0 );
			bl( 0, 1 );
			br( 1, 1 );
		}
		else
		{
			for( int i = 0; i < 4; i++ )
			{
				switch( i )
				{
				case 0:
					if( w & n & !nw ) //inside corner
					{
						tl( 4, 0 );
						continue;
					}
					else if( !w && n && s )
					{
						tl( 0, 4 );
						continue;
					}
					else if( w && !n && e )
					{
						tl( 2, 2 );
						continue;
					}
					else if( !w && !n && s )
					{
						tl( 0, 2 );
						continue;
					}
					else if( !s && !w && n )
					{
						tl( 0, 6 );
						continue;
					}
					break;
				case 1:
					if( e & n & !ne ) //inside corner
					{
						tr( 5, 0 );
						continue;
					}
					else if( !e && n && s )
					{
						tr( 5, 4 );
						continue;
					}
					else if( e && !n && w )
					{
						tr( 3, 2 );
						continue;
					}
					else if( !e && !n && s )
					{
						tr( 5, 2 );
						continue;
					}
					else if( !e && !s && n )
					{
						tr( 5, 6 );
						continue;
					}
					break;
				case 2:
					if( w & s & !sw ) //inside corner
					{
						bl( 4, 1 );
						continue;
					}
					else if( !w && s && n )
					{
						bl( 0, 5 );
						continue;
					}
					else if( w && !s && e )
					{
						bl( 2, 7 );
						continue;
					}
					else if( !w && !n && s )
					{
						bl( 0, 3 );
						continue;
					}
					else if( !w && !s )
					{
						bl( 0, 7 );
						continue;
					}
					else if( !e && !s && w )
					{
						bl( 4, 7 );
						continue;
					}
					break;
				case 3:
					if( e & s & !se ) //inside corner
					{
						br( 5, 1 );
						continue;
					}
					else if( !e && s && n )
					{
						br( 5, 5 );
						continue;
					}
					else if( e && !s && w )
					{
						br( 3, 7 );
						continue;
					}
					else if( !e && !n && s )
					{
						br( 5, 3 );
						continue;
					}
					else if( !e && !s )
					{
						br( 5, 7 );
						continue;
					}
					else if( !w && !s && e )
					{
						br( 1, 7 );
						continue;
					}
					break;
				}
				
				if( w && n && s && e ) //center
				{
					t( 2, 4, i );
				} 
				else if( !w && !n && e ) //nw
				{
					t( 0, 2, i );
				}
				else if( w && !n && e ) //n
				{
					t( 2, 2, i );
				}
				else if( w && !n && !e ) //ne
				{
					t( 4, 2, i );
				}
				else if( !w && n && s && e ) //w
				{	
					t( 0, 4, i );
				}
				else if( w && s && n && !e ) //e
				{
					t( 4, 4, i );
				}
				else if( !w && !s && e ) //sw
				{
					t( 0, 6, i );
				}
				else if( w && !s && e ) //s
				{
					t( 2, 6, i );
				}
				else if( w && !s && !e ) //se
				{
					t( 4, 6, i );
				}
				
			}
		}
	}
	
	private static void t( int x, int y, int i )
	{
		switch( i )
		{
		case 0: tl( x, y ); break;
		case 1: tr( x+1, y ); break;
		case 2: bl( x, y+1 ); break;
		case 3: br( x+1, y+1 ); break;
		}
	}
	
	private static void tl( int x, int y )
	{
		gg.drawImage( imim, 0, 0, ht, ht, f+it*x, it*y, f+it*(x+1), it*(y+1) );
	}
	
	private static void tr( int x, int y )
	{
		gg.drawImage( imim, ht, 0, ht+ht, ht, f+it*x, it*y, f+it*(x+1), it*(y+1) );
	}
	
	private static void bl( int x, int y )
	{
		gg.drawImage( imim, 0, ht, ht, ht+ht, f+it*x, it*y, f+it*(x+1), it*(y+1) );
	}
	
	private static void br( int x, int y )
	{
		gg.drawImage( imim, ht, ht, ht+ht, ht+ht, f+it*x, it*y, f+it*(x+1), it*(y+1) );
	}
}