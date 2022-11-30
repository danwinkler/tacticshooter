package com.phyloa.dlib.dui;

import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;

import com.phyloa.dlib.renderer.Renderer2D;

public class DPanel extends DUIElement
{
	boolean renderBackground = false;
	
	public DPanel( int x, int y, int width, int height )
	{
		super( x, y, width, height );
		
	}

	public void render( Renderer2D r )
	{
		if( renderBackground )
		{
			r.color( ui.theme.backgroundColor );
			r.fillRect( x, y, width, height );
			r.color(  ui.theme.borderColor );
			r.drawRect( x, y, width, height );
		}
	}

	public void update( DUI ui )
	{
		
	}
	
	public void setDrawBackground( boolean sdb )
	{
		this.renderBackground = sdb;
	}
	
	@Override
	public void keyPressed( DKeyEvent dke )
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void keyReleased( DKeyEvent dke )
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseEntered( DMouseEvent e )
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseExited( DMouseEvent e )
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mousePressed( DMouseEvent e )
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseReleased( DMouseEvent e )
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseMoved( DMouseEvent e )
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseDragged( DMouseEvent e )
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseWheel( DMouseEvent dme )
	{
		// TODO Auto-generated method stub
		
	}
}
