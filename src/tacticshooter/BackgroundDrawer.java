package tacticshooter;

import java.io.File;

import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.SlickException;

import com.phyloa.dlib.util.DMath;
import com.phyloa.dlib.util.DOptions;
import com.phyloa.dlib.util.ImprovedNoise;

public class BackgroundDrawer
{
	Image wall;
	Image floor;
	
	float scrollx;
	float scrolly;
	ImprovedNoise n;
	
	DOptions theme;
	
	public BackgroundDrawer()
	{
		File[] files = new File( "themes" ).listFiles();
		theme = new DOptions( files[DMath.randomi( 0, files.length )].getPath() );
		n = new ImprovedNoise( System.currentTimeMillis() );
	}
	
	public void update( int delta )
	{
		scrollx += delta * .03f;
		scrolly += delta * .03f;
	}
	
	public void render( GameContainer gc, Graphics g )
	{
		if( wall == null )
		{
			try
			{
				wall = new Image( "img" + File.separator + theme.getS( "wall" ) );
				floor = new Image( "img" + File.separator + theme.getS( "floor" ) );
			} catch( SlickException e1 )
			{
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
		g.pushTransform();
		g.setAntiAlias( true );
		g.scale( 2, 2 );
		g.translate( -scrollx, -scrolly );
		
		int scrollxTile = (int)(scrollx/Level.tileSize);
		int scrollyTile = (int)(scrolly/Level.tileSize);
		
		for( int y = scrollyTile; y < scrollyTile + gc.getHeight()/Level.tileSize/2 + 2; y++ )
		{
			for( int x = scrollxTile; x < scrollxTile + gc.getWidth()/Level.tileSize/2 + 1; x++ )
			{	
				g.pushTransform();
				g.translate( x*Level.tileSize, y*Level.tileSize );
				Image here = getTile( x, y );
				if( here == wall )
				{
					g.drawImage( floor, 0, 0, Level.tileSize, Level.tileSize, floor.getWidth()/3, 0, floor.getWidth()/3 * 2, floor.getHeight()/4 );
				}
				AutoTileDrawer.draw( g, here, Level.tileSize, 0, 
						getTile( x-1, y-1 ) == here, 
						getTile( x, y-1 ) == here, 
						getTile( x+1, y-1 ) == here, 
						getTile( x-1, y ) == here, 
						getTile( x+1, y ) == here, 
						getTile( x-1, y+1 ) == here, 
						getTile( x, y+1 ) == here, 
						getTile( x+1, y+1 ) == here );
				g.popTransform();
			}
		}
		
		g.popTransform();
	}
	
	public Image getTile( int x, int y )
	{
		return n.noise( x*.1, y*.1, 0 ) > .2f ? wall : floor;
	}
}
