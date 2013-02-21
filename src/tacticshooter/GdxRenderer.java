package tacticshooter;

import java.awt.Font;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.HashMap;

import javax.vecmath.Vector2f;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.phyloa.dlib.renderer.Renderer2D;
import com.phyloa.dlib.util.DGraphics;

public class GdxRenderer implements Renderer2D
{
	private HashMap<Image, org.newdawn.slick.Image> imageHash = new HashMap<Image, org.newdawn.slick.Image>();
	
	ShapeRenderer g;
	
	public GdxRenderer()
	{
		g = new ShapeRenderer();
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
		Color t = new Color();
		Color.rgb888ToColor( t, c );
		g.setColor( t );
	}

	@Override
	public void color( float r, float g, float b )
	{
		this.g.setColor( new Color( r/255, g/255, b/255, 1.f ) );
	}

	@Override
	public void color( float r, float g, float b, float a )
	{
		this.g.setColor( new Color( r/255, g/255, b/255, a/255 ) );
	}

	@Override
	public void color( java.awt.Color color )
	{
		Color t = new Color();
		Color.rgb888ToColor( t, color.getRGB() );
		g.setColor( t );
	}

	@Override
	public void line( float x1, float y1, float x2, float y2 )
	{
		g.begin( ShapeType.Line );
		g.line( x1, y1, x2, y2 );
		g.end();
	}

	@Override
	public void fillRect( float x, float y, float width, float height )
	{
		g.begin( ShapeType.Filled );
		g.rect( x, y, width, height );
		g.end();
	}

	@Override
	public void drawRect( float x, float y, float width, float height )
	{
		g.begin( ShapeType.Line );
		g.rect( x, y, width, height );
		g.end();
	}

	@Override
	public void fillRoundedRect( float x, float y, float width, float height, float arcWidth, float arcHeight )
	{
		
	}

	@Override
	public void drawRoundedRect( float x, float y, float width, float height, float arcWidth, float arcHeight )
	{
		
	}

	@Override
	public void fillOval( float x, float y, float width, float height )
	{
		
	}

	@Override
	public void drawOval( float x, float y, float width, float height )
	{
		
	}

	@Override
	public void text( String text, float x, float y )
	{
		SpriteBatch sb = new SpriteBatch();
		StaticFiles.f.draw( sb, text, x, y );
	}

	@Override
	public void translate( float x, float y )
	{
		g.translate( x, y, 0 );
	}

	@Override
	public void scale( float x, float y )
	{
		g.scale( x, y, 0 );
	}

	@Override
	public void rotate( float angle )
	{
		g.rotate( 0, 0, 1, angle );
	}

	@Override
	public void pushMatrix()
	{
		Gdx.gl11.glPushMatrix();
	}

	@Override
	public void popMatrix()
	{
		Gdx.gl11.glPopMatrix();
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

	@Override
	public void setLineWidth( float width )
	{
		gc.setLineWidth( width );
	}
	
}
