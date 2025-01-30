package com.phyloa.dlib.util;

import java.awt.Component;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.HashMap;

public class DKeyHandler implements KeyListener
{
	static HashMap<Component, DKeyHandler> keyHandlers = new HashMap<Component, DKeyHandler>();
	
	public boolean k1 = false;
	public boolean k2 = false;
	public boolean k3 = false;
	public boolean k4 = false;
	public boolean k5 = false;
	public boolean k6 = false;
	public boolean k7 = false;
	public boolean k8 = false;
	public boolean k9 = false;
	public boolean a = false;
	public boolean b = false;
	public boolean c = false;
	public boolean d = false;
	public boolean e = false;
	public boolean f = false;
	public boolean g = false;
	public boolean h = false;
	public boolean i = false;
	public boolean j = false;
	public boolean k = false;
	public boolean l = false;
	public boolean m = false;
	public boolean n = false;
	public boolean o = false;
	public boolean p = false;
	public boolean q = false;
	public boolean r = false;
	public boolean s = false;
	public boolean t = false;
	public boolean u = false;
	public boolean v = false;
	public boolean w = false;
	public boolean x = false;
	public boolean y = false;
	public boolean z = false;
	public boolean up = false;
	public boolean down = false;
	public boolean left = false;
	public boolean right = false;
	public boolean enter = false;
	public boolean shift = false;
	public boolean ctrl = false;
	public boolean space = false;
	public boolean alt = false;
	public boolean delete = false;
	
	public int lastKeyCodePressed;
	public int lastModifiersExPressed;
	
	public int lastKeyCodeReleased;
	public int lastModifiersExReleased;
	
	public int lastChar = 0;
	
	private DKeyHandler( Component c )
	{
		c.addKeyListener( this );
	}
	
	public static DKeyHandler get( Component c )
	{
		DKeyHandler k = keyHandlers.get( c );
		if( k == null )
		{
			k = new DKeyHandler( c );
			keyHandlers.put( c, k );
		}
		return k;
	}
	
	public void keyPressed( KeyEvent ke )
	{
		lastKeyCodePressed = ke.getKeyCode();
		lastModifiersExPressed = ke.getModifiersEx();
		switch( ke.getKeyCode() )
		{
		case KeyEvent.VK_A: a = true; break;
		case KeyEvent.VK_B: b = true; break;
		case KeyEvent.VK_C: c = true; break;
		case KeyEvent.VK_D: d = true; break;
		case KeyEvent.VK_E: e = true; break;
		case KeyEvent.VK_F: f = true; break;
		case KeyEvent.VK_G: g = true; break;
		case KeyEvent.VK_H: h = true; break;
		case KeyEvent.VK_I: i = true; break;
		case KeyEvent.VK_J: j = true; break;
		case KeyEvent.VK_K: k = true; break;
		case KeyEvent.VK_L: l = true; break;
		case KeyEvent.VK_M: m = true; break;
		case KeyEvent.VK_N: n = true; break;
		case KeyEvent.VK_O: o = true; break;
		case KeyEvent.VK_P: p = true; break;
		case KeyEvent.VK_Q: q = true; break;
		case KeyEvent.VK_R: r = true; break;
		case KeyEvent.VK_S: s = true; break;
		case KeyEvent.VK_T: t = true; break;
		case KeyEvent.VK_U: u = true; break;
		case KeyEvent.VK_V: v = true; break;
		case KeyEvent.VK_W: w = true; break;
		case KeyEvent.VK_X: x = true; break;
		case KeyEvent.VK_Y: y = true; break;
		case KeyEvent.VK_Z: z = true; break;
		case KeyEvent.VK_SPACE: space = true; break;
		case KeyEvent.VK_ENTER: enter = true; break;
		case KeyEvent.VK_ALT: alt = true; break;
		case KeyEvent.VK_SHIFT: shift = true; break;
		case KeyEvent.VK_CONTROL: ctrl = true; break;
		case KeyEvent.VK_UP: up = true; break;
		case KeyEvent.VK_DOWN: down = true; break;
		case KeyEvent.VK_LEFT: left = true; break;
		case KeyEvent.VK_RIGHT: right = true; break;
		case KeyEvent.VK_DELETE: delete = true; break;
		}
	}
	
	public void keyReleased( KeyEvent ke )
	{
		lastKeyCodeReleased = ke.getKeyCode();
		lastModifiersExReleased = ke.getModifiersEx();
		switch( ke.getKeyCode() )
		{
		case KeyEvent.VK_A: a = false; break;
		case KeyEvent.VK_B: b = false; break;
		case KeyEvent.VK_C: c = false; break;
		case KeyEvent.VK_D: d = false; break;
		case KeyEvent.VK_E: e = false; break;
		case KeyEvent.VK_F: f = false; break;
		case KeyEvent.VK_G: g = false; break;
		case KeyEvent.VK_H: h = false; break;
		case KeyEvent.VK_I: i = false; break;
		case KeyEvent.VK_J: j = false; break;
		case KeyEvent.VK_K: k = false; break;
		case KeyEvent.VK_L: l = false; break;
		case KeyEvent.VK_M: m = false; break;
		case KeyEvent.VK_N: n = false; break;
		case KeyEvent.VK_O: o = false; break;
		case KeyEvent.VK_P: p = false; break;
		case KeyEvent.VK_Q: q = false; break;
		case KeyEvent.VK_R: r = false; break;
		case KeyEvent.VK_S: s = false; break;
		case KeyEvent.VK_T: t = false; break;
		case KeyEvent.VK_U: u = false; break;
		case KeyEvent.VK_V: v = false; break;
		case KeyEvent.VK_W: w = false; break;
		case KeyEvent.VK_X: x = false; break;
		case KeyEvent.VK_Y: y = false; break;
		case KeyEvent.VK_Z: z = false; break;
		case KeyEvent.VK_SPACE: space = false; break;
		case KeyEvent.VK_ENTER: enter = false; break;
		case KeyEvent.VK_ALT: alt = false; break;
		case KeyEvent.VK_SHIFT: shift = false; break;
		case KeyEvent.VK_CONTROL: ctrl = false; break;
		case KeyEvent.VK_UP: up = false; break;
		case KeyEvent.VK_DOWN: down = false; break;
		case KeyEvent.VK_LEFT: left = false; break;
		case KeyEvent.VK_RIGHT: right = false; break;
		case KeyEvent.VK_DELETE: delete = false; break;
		}
	}

	public void keyTyped( KeyEvent e )
	{
		lastChar = e.getKeyChar();
	}
}
