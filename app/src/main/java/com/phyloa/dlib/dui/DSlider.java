package com.phyloa.dlib.dui;

import jp.objectclub.vecmath.Vector2f;

import com.phyloa.dlib.renderer.Renderer2D;
import com.phyloa.dlib.util.DMath;

public class DSlider extends DUIElement
{
	float min, max, position;
	
	public DSlider( int x, int y, int width, int height, float min, float max, float start )
	{
		super( x, y, width, height );
		this.min = min;
		this.max = max;
		this.position = start;
	}

	public void setPositionFromMouse( float mx )
	{
		position = DMath.map( Math.min( Math.max( mx-x, 10 ), width-10 ), 10, width-10, min, max );
	}
	
	public void keyPressed( DKeyEvent dke )
	{
		
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
		setPositionFromMouse( e.x );
	}

	@Override
	public void mouseReleased( DMouseEvent e )
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseMoved( DMouseEvent e )
	{
	
	}

	@Override
	public void mouseDragged( DMouseEvent e )
	{
		setPositionFromMouse( e.x );
	}

	@Override
	public void mouseWheel( DMouseEvent dme )
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void render( Renderer2D r )
	{
		r.pushMatrix();
		r.translate( x, y );
		r.color( ui.theme.backgroundColor );
		r.fillRect( 0, 0, width-1, height-1 );
		r.color( ui.theme.borderColor );
		r.drawRect( 0, 0, width-1, height-1 );
		float slidePos = DMath.map( position, min, max, 10, width-10 );
		r.drawRect( slidePos-5, 5, 10, height-10 );
		r.popMatrix();
	}

	@Override
	public void update( DUI ui )
	{
		// TODO Auto-generated method stub
		
	}

	public float getPosition()
	{
		return position;
	}
	
}
