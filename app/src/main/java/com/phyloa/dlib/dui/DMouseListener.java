package com.phyloa.dlib.dui;

public interface DMouseListener {
	public static final int LEFT_MOUSE = 0;
	public static final int CENTER_MOUSE = 1;
	public static final int RIGHT_MOUSE = 2;

	public void mouseEntered(DMouseEvent e);

	public void mouseExited(DMouseEvent e);

	public boolean mousePressed(DMouseEvent e);

	public boolean mouseReleased(DMouseEvent e);

	public void mouseMoved(DMouseEvent e);

	public boolean mouseDragged(DMouseEvent e);

	public void mouseWheel(DMouseEvent dme);
}
