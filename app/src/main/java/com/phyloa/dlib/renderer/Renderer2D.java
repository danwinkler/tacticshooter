package com.phyloa.dlib.renderer;

import java.awt.Color;
import java.awt.Font;
import java.awt.Image;
import java.awt.event.KeyListener;

import jp.objectclub.vecmath.Vector2f;

/**
 * All Renderers implement the Renderer interface. The Renderer methods are for 
 * 2D drawing. 
 * 
 * @author Daniel Winkler
 *
 */
public interface Renderer2D
{
	/**
	 * begin is called to either start the render, or, if the renderer uses a
	 * game loop, to call initialize() and then loop update().
	 * 
	 * If the renderer does not implement the method, it will do nothing.
	 */
	public void begin();
	
	/**
	 * Sets the size of the rendering area/window
	 * @param x the width of the rendering area
	 * @param y the height of the rendering area
	 */
	public void size( int x, int y );
	
	/**
	 * Sets the frame rate if the Renderer uses a game loop
	 * 
	 * @param r the number of time per second update should be called
	 */
	public void frameRate( float r );
	
	/**
	 * Sets the color to draw in.
	 * @param c the color as a 32-bit ARGB integer
	 */
	public void color( int c );
	
	/**
	 * Sets the color to draw in.
	 * @param r the red component 0-255
	 * @param g the green component 0-255
	 * @param b the blue component 0-255
	 */
	public void color( float r, float g, float b );
	
	/**
	 * Sets the color to draw in.
	 * @param r the red component 0-255
	 * @param g the green component 0-255
	 * @param b the blue component 0-255
	 * @param a the alpha component 0-255
	 */
	public void color( float r, float g, float b, float a );
	
	public void color( Color color );
	
	/**
	 * Draws a line from one point to another in two-space.
	 * @param x1 the x location of the first point
	 * @param y1 the y location of the first point
	 * @param x2 the x location of the second point
	 * @param y2 the y location of the second point
	 */
	public void line( float x1, float y1, float x2, float y2 );
	
	/**
	 * Fills a rectangle in two-space.
	 * @param x the x location of the center of the rectangle
	 * @param y the y location of the center of the rectangle
	 * @param width the width of the rectangle
	 * @param height the height of the rectangle
	 */
	public void fillRect( float x, float y, float width, float height );
	
	/**
	 * Draws a rectangle in two-space.
	 * @param x the x location of the center of the rectangle
	 * @param y the y location of the center of the rectangle
	 * @param width the width of the rectangle
	 * @param height the height of the rectangle
	 */
	public void drawRect( float x, float y, float width, float height );
	
	public void fillRoundedRect( float x, float y, float width, float height, float arcWidth, float arcHeight );
	public void drawRoundedRect( float x, float y, float width, float height, float arcWidth, float arcHeight );
	
	/**
	 * Fills an ellipse in two-space.
	 * @param x the x location of the center of the ellipse
	 * @param y the y location of the center of the ellipse
	 * @param width the width of the ellipse
	 * @param height the height of the ellipse
	 */
	public void fillOval( float x, float y, float width, float height );
	
	/**
	 * Draws an ellipse in two-space.
	 * @param x the x location of the center of the ellipse
	 * @param y the y location of the center of the ellipse
	 * @param width the width of the ellipse
	 * @param height the height of the ellipse
	 */
	public void drawOval( float x, float y, float width, float height );
	
	/**
	 * Draws a string.
	 * 
	 * NOTE: there is currently no standard on how the string renders relative to 
	 * the location
	 * @param text the string to render
	 * @param x the x location of the string
	 * @param y the y location of the string
	 */
	public void text( String text, float x, float y );
	
	/**
	 * Translates the origin in two-space.
	 * @param x the amount to translate in x-space
	 * @param y the amount to translate in y-space
	 */
	public void translate( float x, float y );
	
	public void scale( float x, float y );
	
	public void rotate( float angle );

	/**
	 * Pushes the current transformation matrix onto the stack.
	 */
	public void pushMatrix();
	
	/**
	 * Pops a transformation matrix off the stack and sets it as the current transformation matrix.
	 */
	public void popMatrix();
	
	/**
	 * Called once if game loop is used.
	 */
	public void initialize();
	
	/**
	 * Called every frame if game loop is used.
	 */
	public void update();
	
	/**
	 * Draws the image at the specified coordinates in two-space.
	 * @param img the Image to render
	 * @param x the x location of the upper left corner of the image
	 * @param y the y location of the upper left corner of the image
	 */
	public void drawImage( Image img, float x, float y );
	
	public void drawImage( Image img, float x, float y, float width, float height );
	
	public void drawImage( Image img, float dx1, float dy1, float dx2, float dy2, float sx1, float sy1, float sx2, float sy2 );
	
	public int getWidth();
	
	public int getHeight();
	
	public Vector2f getStringSize( String text );
	
	public void setFont( Font font );
	
	public void setClip( int x, int y, int width, int height );
	
	public void clearClip();
	
	public void setLineWidth( float width );
}

