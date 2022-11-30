package com.phyloa.dlib.dui;

import java.util.HashMap;

import com.phyloa.dlib.renderer.Renderer2D;

public class DDialog extends DPanel implements DUIListener
{	
	DButton confirm;
	DButton cancel;
	
	boolean confirmed;
	boolean canceled;
	
	public DDialog( int width, int height )
	{
		super( 0, 0, width, height );
		
		confirm = new DButton( "Confirm", 10, height-50, width/2 - 15, 40 );
		cancel = new DButton( "Cancel", width/2 + 5, height-50, width/2 - 15, 40 );
		
		this.add( confirm );
		this.add( cancel );
		
		renderBackground = true;
	}
	
	@Override
	public void render( Renderer2D r )
	{
		super.render( r );
	}

	@Override
	public void update( DUI ui )
	{
		// TODO Auto-generated method stub
		
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
	
	
	@Override
	public void setUI( DUI ui )
	{
		super.setUI( ui );
		ui.addDUIListener( this );
	}
	
	public boolean isComplete()
	{
		return canceled || confirmed;
	}

	@Override
	public void event( DUIEvent event )
	{
		if( event.getElement() == cancel )
		{
			canceled = true;
		}
		else if( event.getElement() == confirm )
		{
			confirmed = true;
		}
	}

	public boolean isConfirmed()
	{
		return confirmed;
	}
	
	public boolean isCacnceled()
	{
		return canceled;
	}
}
