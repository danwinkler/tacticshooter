package com.phyloa.dlib.dui;

public interface DMouseListener
{
	public static final int LEFT_MOUSE = 0;
	public static final int CENTER_MOUSE = 1;
	public static final int RIGHT_MOUSE = 2;
	
	public void mouseEntered( DMouseEvent e );
	
	public void mouseExited( DMouseEvent e );

	public void mousePressed( DMouseEvent e );

	public void mouseReleased( DMouseEvent e );
	
	public void mouseMoved( DMouseEvent e );
	
	public void mouseDragged( DMouseEvent e );

	public void mouseWheel( DMouseEvent dme );
}
