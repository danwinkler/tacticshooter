package com.phyloa.dlib.renderer;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.event.KeyListener;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.EmptyStackException;
import java.util.Stack;

import jp.objectclub.vecmath.Point2f;
import jp.objectclub.vecmath.Vector2f;

import com.phyloa.dlib.util.DGraphics;

public class Graphics2DIRenderer implements IRenderer
{
	public BufferedImage im;
	public Graphics2D g;
	
	Stack<AffineTransform> mat = new Stack<AffineTransform>();
	
	public Graphics2DIRenderer( int x, int y )
	{
		im = DGraphics.createBufferedImage( x, y );
		g = im.createGraphics();
	}
	
	public Image getImage()
	{
		return im;
	}

	
	public void beginShape( ShapeType type )
	{

	}

	public void box( float width, float height, float length )
	{

	}

	public void fillOval( float x, float y, float width, float height )
	{
		g.fillOval( (int)x, (int)y, (int)width, (int)height );
	}
	
	public void drawOval( float x, float y, float width, float height )
	{
		g.drawOval( (int)x, (int)y, (int)width, (int)height );
	}

	public void endShape()
	{

	}

	public void color( int c )
	{
		g.setColor( new Color( c ) );
	}

	public void color( float r, float g, float b )
	{
		this.g.setColor( new Color( (int)r, (int)g, (int)b ) );
	}

	public void color( float r, float g, float b, float a )
	{
		this.g.setColor( new Color( (int)r, (int)g, (int)b, (int)a ) );
	}

	public void line( float x1, float y1, float x2, float y2 )
	{
		g.drawLine( (int)x1, (int)y1, (int)x2, (int)y2 );
	}

	public void line( float x1, float y1, float z1, float x2, float y2, float z2 )
	{
		g.drawLine( (int)x1, (int)y1, (int)x2, (int)y2 );
	}

	public void popMatrix()
	{
		try
		{
			g.setTransform( mat.pop() );
		}
		catch( EmptyStackException e )
		{
			System.err.println( "Stack is empty" );
		}
	}

	public void pushMatrix()
	{
		mat.push( g.getTransform() );
	}

	public void drawRect( float x, float y, float width, float height )
	{
		g.drawRect( (int)x, (int)y, (int)width, (int)height );
	}
	
	public void fillRect( float x, float y, float width, float height )
	{
		g.fillRect( (int)x, (int)y, (int)width, (int)height );
	}

	public void rotate( float angle, float vx, float vy, float vz )
	{
		g.rotate( angle );
	}
	
	public void scale( float s )
	{
		g.scale( s, s );
	}

	public void scale( float x, float y )
	{
		g.scale( x, y );
	}

	public void scale( float x, float y, float z )
	{
		g.scale( x, y );
	}

	public void text( String text, float x, float y )
	{
		g.drawString( text, x, y );
	}

	public void drawImage( Image im, float x, float y )
	{
		g.drawImage( im, (int)x, (int)y, null );
	}

	public void texture( Image img )
	{
		
	}

	public void translate( float x, float y )
	{
		g.translate( x, y );
	}

	public void translate( float x, float y, float z )
	{
		g.translate( x, y );
	}

	public void vertex( float x, float y )
	{

	}

	public void vertex( float x, float y, float z )
	{

	}

	public void rotateX( float angle ){}
	public void rotateY( float angle ){}
	public void rotateZ( float angle ){}

	public void rotate( float angle )
	{
		g.rotate( angle );
	}

	public void frameRate( float r ) {}

	public void addKeyListener( KeyListener listener ){}

	public int getWidth()
	{
		return im.getWidth();
	}

	public int getHeight()
	{
		return im.getHeight();
	}

	public void textureCoords( float u, float v ) 
	{
		
	}
	
	public void drawImage( Image im, float x, float y, float width, float height )
	{
		g.drawImage( im, (int)x, (int)y, (int)width, (int)height, Color.WHITE, null );
	}
	
	public void drawImage( Image img, float dx1, float dy1, float dx2, float dy2, float sx1, float sy1, float sx2, float sy2 )
	{
		g.drawImage( img, (int)dx1, (int)dy1, (int)dx2, (int)dy2, (int)sx1, (int)sy1, (int)sx2, (int)sy2, null );
	}

	public void begin(){}

	public void initialize(){}
	
	public void size( int x, int y ){}

	public void update(){}

	public Vector2f getStringSize( String text )
	{
		return null;
	}

	@Override
	public void setFont( Font font )
	{
		
	}

	@Override
	public void color( Color color )
	{
		this.g.setColor( color );
	}

	@Override
	public void drawRoundedRect( float x, float y, float width, float height, float arcWidth, float arcHeight )
	{
		g.drawRoundRect( (int)x, (int)y, (int)width, (int)height, (int)arcWidth, (int)arcHeight );
	}

	@Override
	public void fillRoundedRect( float x, float y, float width, float height, float arcWidth, float arcHeight )
	{
		g.fillRoundRect( (int)x, (int)y, (int)width, (int)height, (int)arcWidth, (int)arcHeight );
	}

	@Override
	public void setClip( int x, int y, int width, int height )
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void clearClip()
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setLineWidth( float width )
	{
		g.setStroke( new BasicStroke( width ) );
	}
}
