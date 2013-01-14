package tacticshooter;

import java.awt.Component;
import java.util.ArrayList;

import org.newdawn.slick.Input;
import org.newdawn.slick.InputListener;

import com.phyloa.dlib.dui.DEventMapper;
import com.phyloa.dlib.dui.DKeyEvent;
import com.phyloa.dlib.dui.DKeyListener;
import com.phyloa.dlib.dui.DMouseEvent;
import com.phyloa.dlib.dui.DMouseListener;

public class Slick2DEventMapper implements DEventMapper, InputListener
{
	ArrayList<DKeyListener> keyListeners = new ArrayList<DKeyListener>();
	ArrayList<DMouseListener> mouseListeners = new ArrayList<DMouseListener>();
	
	Input input;
	
	int lastButton;
	
	public Slick2DEventMapper( Input input )
	{
		register( input );
	}
	
	public void register( Input input )
	{
		this.input = input;
		input.addListener( this );
	}
	
	public void addDKeyListener( DKeyListener l )
	{
		keyListeners.add( l );
	}
	
	public void addDMouseListener( DMouseListener l )
	{
		mouseListeners.add( l );
	}
	
	public void mouseClicked( int arg0, int arg1, int arg2, int arg3 )
	{
		
	}

	@Override
	public void mouseDragged( int oldx, int oldy, int newx, int newy )
	{
		DMouseEvent dme = new DMouseEvent();
		dme.x = newx;
		dme.y = newy;
		for( DMouseListener l : mouseListeners )
		{
			l.mouseDragged( dme );
		}
	}

	@Override
	public void mouseMoved( int oldx, int oldy, int newx, int newy )
	{
		DMouseEvent dme = new DMouseEvent();
		dme.x = newx;
		dme.y = newy;
		for( DMouseListener l : mouseListeners )
		{
			l.mouseMoved( dme );
		}
	}

	@Override
	public void mousePressed( int button, int x, int y )
	{
		DMouseEvent dme = new DMouseEvent();
		dme.x = x;
		dme.y = y;
		dme.button = button;
		for( DMouseListener l : mouseListeners )
		{
			l.mousePressed( dme );
		}
	}

	@Override
	public void mouseReleased( int button, int x, int y )
	{
		DMouseEvent dme = new DMouseEvent();
		dme.x = x;
		dme.y = y;
		dme.button = button;
		for( DMouseListener l : mouseListeners )
		{
			l.mouseReleased( dme );
		}
	}

	@Override
	public void mouseWheelMoved( int arg0 )
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void inputEnded()
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void inputStarted()
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean isAcceptingInput()
	{
		return true;
	}

	@Override
	public void setInput( Input input )
	{
		this.input = input;
	}

	@Override
	public void keyPressed( int key, char c )
	{
		DKeyEvent dke = new DKeyEvent();
		dke.keyCode = key;
		dke.keyChar = c;
		for( DKeyListener l : keyListeners )
		{
			l.keyPressed( dke );
		}
	}

	@Override
	public void keyReleased( int key, char c )
	{
		DKeyEvent dke = new DKeyEvent();
		dke.keyCode = key;
		dke.keyChar = c;
		for( DKeyListener l : keyListeners )
		{
			l.keyReleased( dke );
		}
	}

	@Override
	public void controllerButtonPressed( int arg0, int arg1 )
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void controllerButtonReleased( int arg0, int arg1 )
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void controllerDownPressed( int arg0 )
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void controllerDownReleased( int arg0 )
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void controllerLeftPressed( int arg0 )
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void controllerLeftReleased( int arg0 )
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void controllerRightPressed( int arg0 )
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void controllerRightReleased( int arg0 )
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void controllerUpPressed( int arg0 )
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void controllerUpReleased( int arg0 )
	{
		// TODO Auto-generated method stub
		
	}
}
