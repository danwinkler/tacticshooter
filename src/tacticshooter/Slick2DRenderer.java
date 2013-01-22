package tacticshooter;

import java.awt.Font;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.HashMap;

import javax.vecmath.Vector2f;

import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.opengl.Texture;
import org.newdawn.slick.util.BufferedImageUtil;

import com.phyloa.dlib.renderer.Renderer2D;
import com.phyloa.dlib.util.DGraphics;

public class Slick2DRenderer implements Renderer2D
{
	Graphics gc;
	private HashMap<Image, org.newdawn.slick.Image> imageHash = new HashMap<Image, org.newdawn.slick.Image>();
	
	public Slick2DRenderer renderTo( Graphics g )
	{
		gc = g;
		return this;
	}
	
	public void begin()
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void size( int x, int y )
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void frameRate( float r )
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void color( int c )
	{
		gc.setColor( new Color( c ) );
	}

	@Override
	public void color( float r, float g, float b )
	{
		gc.setColor( new Color( r/255, g/255, b/255 ) );
	}

	@Override
	public void color( float r, float g, float b, float a )
	{
		gc.setColor( new Color( r/255, g/255, b/255, a/255 ) );
	}

	@Override
	public void color( java.awt.Color color )
	{
		gc.setColor( new Color( color.getRGB() ) );
	}

	@Override
	public void line( float x1, float y1, float x2, float y2 )
	{
		gc.drawLine( x1, y1, x2, y2 );
	}

	@Override
	public void fillRect( float x, float y, float width, float height )
	{
		gc.fillRect( x, y, width, height );
	}

	@Override
	public void drawRect( float x, float y, float width, float height )
	{
		gc.drawRect( x, y, width, height );
	}

	@Override
	public void fillRoundedRect( float x, float y, float width, float height,
			float arcWidth, float arcHeight )
	{
		gc.fillRoundRect( x, y, width, height, (int)arcWidth );
	}

	@Override
	public void drawRoundedRect( float x, float y, float width, float height,
			float arcWidth, float arcHeight )
	{
		gc.drawRoundRect( x, y, width, height, (int)arcWidth );
	}

	@Override
	public void fillOval( float x, float y, float width, float height )
	{
		gc.fillOval( x, y, width, height );
	}

	@Override
	public void drawOval( float x, float y, float width, float height )
	{
		gc.drawOval( x, y, width, height );
	}

	@Override
	public void text( String text, float x, float y )
	{
		gc.drawString( text, x, y );
	}

	@Override
	public void translate( float x, float y )
	{
		gc.translate( x, y );
	}

	@Override
	public void scale( float x, float y )
	{
		gc.scale( x, y );
	}

	@Override
	public void rotate( float angle )
	{
		gc.rotate( 0, 0, angle );
	}

	@Override
	public void pushMatrix()
	{
		gc.pushTransform();
	}

	@Override
	public void popMatrix()
	{
		gc.popTransform();
	}

	@Override
	public void initialize()
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void update()
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void drawImage( Image img, float x, float y )
	{
		gc.drawImage( getImage( img ), x, y );
	}
	
	private org.newdawn.slick.Image getImage( Image img )
	{
		org.newdawn.slick.Image simg = imageHash.get( img );
		if( simg == null )
		{
			simg = getSlickImage( convertToBufferedImage( img ) );
			imageHash.put( img, simg );
		}
		return simg;
	}
	
	private org.newdawn.slick.Image getSlickImage( BufferedImage img )
	{
		
		org.newdawn.slick.Image slickImage = null;
		try
		{
			Texture texture = BufferedImageUtil.getTexture( "", img ); 
			slickImage = new org.newdawn.slick.Image( texture.getImageWidth(), texture.getImageHeight() );
			slickImage.setTexture(texture) ;
		} catch( SlickException e )
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch( IOException e )
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return slickImage;
	}
	
	private BufferedImage convertToBufferedImage( Image img )
	{
		  BufferedImage buffImg = DGraphics.createBufferedImage( img.getWidth(null), img.getHeight(null) );
		  java.awt.Graphics g = buffImg.getGraphics();
		  g.drawImage(img, 0, 0, null);
		  return buffImg;
	}

	@Override
	public void drawImage( Image img, float x, float y, float width,
			float height )
	{
		//gc.drawImage( getImage( img ), x, y, width, height );
	}

	@Override
	public void drawImage( Image img, float dx1, float dy1, float dx2,
			float dy2, float sx1, float sy1, float sx2, float sy2 )
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public int getWidth()
	{
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getHeight()
	{
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public Vector2f getStringSize( String text )
	{
		Vector2f v = new Vector2f();
		org.newdawn.slick.Font f = gc.getFont();
		v.x = f.getWidth( text );
		v.y = f.getHeight( text );
		return v;
	}

	@Override
	public void setFont( Font font )
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setClip( int x, int y, int width, int height )
	{
		gc.setClip( x, y, width, height );
	}

	@Override
	public void clearClip()
	{
		gc.clearClip();
	}
	
}
