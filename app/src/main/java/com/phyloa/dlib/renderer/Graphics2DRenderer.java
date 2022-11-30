package com.phyloa.dlib.renderer;

import java.awt.BasicStroke;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.KeyListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferStrategy;
import java.util.EmptyStackException;
import java.util.Stack;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import jp.objectclub.vecmath.Point2f;
import jp.objectclub.vecmath.Vector2f;

import com.phyloa.dlib.util.DKeyHandler;
import com.phyloa.dlib.util.DMouseHandler;

public abstract class Graphics2DRenderer implements Renderer2D, ComponentListener
{
	public JFrame container;
	public JPanel panel;
	public Canvas canvas;

	public Graphics2D g;
	BufferStrategy bs;

	public DKeyHandler k;
	public DMouseHandler m;

	//Drawing vars
	long frameTime = 1000000000 / 30; //30 frames per second
	Stack<AffineTransform> mat = new Stack<AffineTransform>();
	//End Draw vars

	public Graphics2DRenderer()
	{
		container = new JFrame( "Graphics2DRenderer Window" );
		container.addComponentListener( this );
		panel = (JPanel) container.getContentPane();
		panel.setPreferredSize( new Dimension( 50, 50 ) );
		panel.setLayout(null);
		canvas = new Canvas();
	
		canvas.setBounds(0,0,50,50);
		panel.add( canvas );
	
		container.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
	
		container.setLocation( 50, 50 );
	
		container.pack();
		container.setResizable( true );
		container.setVisible( true );
	
		canvas.requestFocus();
	
		canvas.createBufferStrategy( 2 );
		bs = canvas.getBufferStrategy();
	
		k = DKeyHandler.get( canvas );
		m = DMouseHandler.get( canvas );
	}

	public void size( int x, int y )
	{
		canvas.setBounds( 0, 0, x, y );
		canvas.setSize( x, y );
		panel.setPreferredSize( new Dimension( x, y ) );
		container.pack();
	}

	public void begin()
	{
		initialize();
		while( true )
		{
			long startTime = System.nanoTime();
			g = (Graphics2D) bs.getDrawGraphics();
			mat.clear();
			mat.push( g.getTransform() );
			update();
		
			g.dispose();
			bs.show();
			try { Thread.sleep( Math.max( (frameTime - (System.nanoTime() - startTime)) / 1000000, 0 ) ); } catch (InterruptedException e) {}
		}
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
	
	public void color( Color color )
	{
		this.g.setColor( color );
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
		text( text, x, y, false, false );
	}
	
	public void text( String text, float x, float y, boolean centerX, boolean centerY )
	{
		Vector2f p = getStringSize( text );
		float dx = centerX ? x - p.x*.5f : x;
		float dy = centerY ? y - p.y*.5f : y;
		
		g.drawString( text, dx, dy );
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

	public void frameRate( float r )
	{
		frameTime = (long) (1000000000 / r);
	}

	public abstract void initialize();

	public abstract void update();

	public void addKeyListener( KeyListener listener )
	{
		canvas.addKeyListener( listener );
	}

	public int getWidth()
	{
		return canvas.getWidth();
	}

	public int getHeight()
	{
		return canvas.getHeight();
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

	public void componentHidden( ComponentEvent arg0 )
	{
		
	}

	public void componentMoved( ComponentEvent arg0 )
	{
		
	}

	public void componentResized( ComponentEvent e )
	{
		JRootPane rp = container.getRootPane();
		canvas.setBounds( 0, 0, rp.getWidth(), rp.getHeight() );
		canvas.setSize( rp.getWidth(), rp.getHeight() );
	}

	public void componentShown( ComponentEvent arg0 )
	{
		
	}

	public Vector2f getStringSize( String text )
	{
		Rectangle2D rect = g.getFontMetrics().getStringBounds( text, g );
		return new Vector2f( (float)rect.getWidth(), (float)rect.getY() );
	}

	public void setFont( Font font )
	{
		g.setFont( font );
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
	
	public void setClip( int x, int y, int width, int height )
	{
		g.setClip( x, y, width, height );
	}
	
	public void clearClip()
	{
		g.setClip( null );
	}
	
	public void setLineWidth( float w )
	{
		g.setStroke( new BasicStroke( w ) );
	}
}
