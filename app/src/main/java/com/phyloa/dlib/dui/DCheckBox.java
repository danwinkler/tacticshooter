package com.phyloa.dlib.dui;

import java.awt.Color;

import com.phyloa.dlib.renderer.Renderer2D;

public class DCheckBox extends DUIElement
{
	public static final int CHECKED = 0;
	public static final int UNCHECKED = 1;
	
	Color borderColor = new Color( 32, 32, 128 );
	Color color = new Color( 128, 128, 255 );
	
	public boolean checked = false;
	
	public DCheckBox( int x, int y, int width, int height )
	{
		super( x, y, width, height );
	}

	public void keyPressed( DKeyEvent dke )
	{
		
	}

	public void keyReleased( DKeyEvent dke )
	{
		
	}

	public void mouseEntered( DMouseEvent e )
	{
		
	}

	public void mouseExited( DMouseEvent e )
	{
		
	}

	public void mousePressed( DMouseEvent e )
	{
		checked = !checked;
		ui.event( new DUIEvent( this, checked ? CHECKED : UNCHECKED ) );
	}

	public void mouseReleased( DMouseEvent e )
	{
		
	}

	public void mouseMoved( DMouseEvent e )
	{
		
	}

	public void mouseDragged( DMouseEvent e )
	{
		
	}

	public void mouseWheel( DMouseEvent dme )
	{
		
	}

	public void render( Renderer2D r )
	{
		r.color( color );
		r.fillRect( x, y, width, height );
		r.color( borderColor );
		r.drawRect( x, y, width, height );
		if( checked )
		{
			r.fillOval( x+width*.1f, y+height*.1f, width*.8f, height*.8f );
		}
	}

	public void update( DUI ui )
	{
		
	}
	
}
