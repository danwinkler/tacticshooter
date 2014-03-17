package com.danwink.tacticshooter.slick;

import java.awt.Component;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.HashMap;

import org.newdawn.slick.Input;
import org.newdawn.slick.InputListener;

import com.phyloa.dlib.dui.DEventMapper;
import com.phyloa.dlib.dui.DKeyEvent;
import com.phyloa.dlib.dui.DKeyListener;
import com.phyloa.dlib.dui.DMouseEvent;
import com.phyloa.dlib.dui.DMouseListener;
import com.phyloa.dlib.util.DMath;

public class Slick2DEventMapper implements DEventMapper, InputListener
{
	public static final int[] KEYTABLE =
	{
		KeyEvent.VK_UNDEFINED,		// (0)
		KeyEvent.VK_ESCAPE,		// KEY_ESCAPE
		KeyEvent.VK_1,		// KEY_1
		KeyEvent.VK_2,		// KEY_2
		KeyEvent.VK_3,		// KEY_3
		KeyEvent.VK_4,		// KEY_4
		KeyEvent.VK_5,		// KEY_5
		KeyEvent.VK_6,		// KEY_6
		KeyEvent.VK_7,		// KEY_7
		KeyEvent.VK_8,		// KEY_8
		KeyEvent.VK_9,		// KEY_9
		KeyEvent.VK_0,		// KEY_0
		KeyEvent.VK_MINUS,		// KEY_MINUS
		KeyEvent.VK_EQUALS,		// KEY_EQUALS
		KeyEvent.VK_BACK_SPACE,		// KEY_BACK
		KeyEvent.VK_TAB,		// KEY_TAB
		KeyEvent.VK_Q,		// KEY_Q
		KeyEvent.VK_W,		// KEY_W
		KeyEvent.VK_E,		// KEY_E
		KeyEvent.VK_R,		// KEY_R
		KeyEvent.VK_T,		// KEY_T
		KeyEvent.VK_Y,		// KEY_Y
		KeyEvent.VK_U,		// KEY_U
		KeyEvent.VK_I,		// KEY_I
		KeyEvent.VK_O,		// KEY_O
		KeyEvent.VK_P,		// KEY_P
		KeyEvent.VK_BRACELEFT,		// KEY_LBRACKET
		KeyEvent.VK_BRACERIGHT,		// KEY_RBRACKET
		KeyEvent.VK_ENTER,		// KEY_ENTER
		KeyEvent.VK_CONTROL,		// KEY_LCONTROL
		KeyEvent.VK_A,		// KEY_A
		KeyEvent.VK_S,		// KEY_S
		KeyEvent.VK_D,		// KEY_D
		KeyEvent.VK_F,		// KEY_F
		KeyEvent.VK_G,		// KEY_G
		KeyEvent.VK_H,		// KEY_H
		KeyEvent.VK_J,		// KEY_J
		KeyEvent.VK_K,		// KEY_K
		KeyEvent.VK_L,		// KEY_L
		KeyEvent.VK_SEMICOLON,		// KEY_SEMICOLON
		KeyEvent.VK_UNDEFINED,		// KEY_APOSTROPHE
		KeyEvent.VK_UNDEFINED,		// KEY_GRAVE
		KeyEvent.VK_SHIFT,		// KEY_LSHIFT
		KeyEvent.VK_BACK_SLASH,		// KEY_BACKSLASH
		KeyEvent.VK_Z,		// KEY_Z
		KeyEvent.VK_X,		// KEY_X
		KeyEvent.VK_C,		// KEY_C
		KeyEvent.VK_V,		// KEY_V
		KeyEvent.VK_B,		// KEY_B
		KeyEvent.VK_N,		// KEY_N
		KeyEvent.VK_M,		// KEY_M
		KeyEvent.VK_COMMA,		// KEY_COMMA
		KeyEvent.VK_PERIOD,		// KEY_PERIOD
		KeyEvent.VK_SLASH,		// KEY_SLASH
		KeyEvent.VK_SHIFT,		// KEY_RSHIFT
		KeyEvent.VK_MULTIPLY,		// KEY_MULTIPLY
		KeyEvent.VK_CONTEXT_MENU,		// KEY_LMENU
		KeyEvent.VK_SPACE,		// KEY_SPACE
		KeyEvent.VK_CAPS_LOCK,		// KEY_CAPITAL
		KeyEvent.VK_F1,		// KEY_F1
		KeyEvent.VK_F2,		// KEY_F2
		KeyEvent.VK_F3,		// KEY_F3
		KeyEvent.VK_F4,		// KEY_F4
		KeyEvent.VK_F5,		// KEY_F5
		KeyEvent.VK_F6,		// KEY_F6
		KeyEvent.VK_F7,		// KEY_F7
		KeyEvent.VK_F8,		// KEY_F8
		KeyEvent.VK_F9,		// KEY_F9
		KeyEvent.VK_F10,		// KEY_F10
		KeyEvent.VK_NUM_LOCK,		// KEY_NUMLOCK
		KeyEvent.VK_SCROLL_LOCK,		// KEY_SCROLL
		KeyEvent.VK_NUMPAD7,		// KEY_NUMPAD7
		KeyEvent.VK_NUMPAD8,		// KEY_NUMPAD8
		KeyEvent.VK_NUMPAD9,		// KEY_NUMPAD9
		KeyEvent.VK_SUBTRACT,		// KEY_SUBTRACT
		KeyEvent.VK_NUMPAD4,		// KEY_NUMPAD4
		KeyEvent.VK_NUMPAD5,		// KEY_NUMPAD5
		KeyEvent.VK_NUMPAD6,		// KEY_NUMPAD6
		KeyEvent.VK_ADD,		// KEY_ADD
		KeyEvent.VK_NUMPAD1,		// KEY_NUMPAD1
		KeyEvent.VK_NUMPAD2,		// KEY_NUMPAD2
		KeyEvent.VK_NUMPAD3,		// KEY_NUMPAD3
		KeyEvent.VK_NUMPAD0,		// KEY_NUMPAD0
		KeyEvent.VK_DECIMAL,		// KEY_DECIMAL
		KeyEvent.VK_UNDEFINED,		// (84)
		KeyEvent.VK_UNDEFINED,		// (85)
		KeyEvent.VK_UNDEFINED,		// (86)
		KeyEvent.VK_F11,		// KEY_F11
		KeyEvent.VK_F12,		// KEY_F12
		KeyEvent.VK_UNDEFINED,		// (89)
		KeyEvent.VK_UNDEFINED,		// (90)
		KeyEvent.VK_UNDEFINED,		// (91)
		KeyEvent.VK_UNDEFINED,		// (92)
		KeyEvent.VK_UNDEFINED,		// (93)
		KeyEvent.VK_UNDEFINED,		// (94)
		KeyEvent.VK_UNDEFINED,		// (95)
		KeyEvent.VK_UNDEFINED,		// (96)
		KeyEvent.VK_UNDEFINED,		// (97)
		KeyEvent.VK_UNDEFINED,		// (98)
		KeyEvent.VK_UNDEFINED,		// (99)
		KeyEvent.VK_F13,		// KEY_F13
		KeyEvent.VK_F14,		// KEY_F14
		KeyEvent.VK_F15,		// KEY_F15
		KeyEvent.VK_UNDEFINED,		// (103)
		KeyEvent.VK_UNDEFINED,		// (104)
		KeyEvent.VK_UNDEFINED,		// (105)
		KeyEvent.VK_UNDEFINED,		// (106)
		KeyEvent.VK_UNDEFINED,		// (107)
		KeyEvent.VK_UNDEFINED,		// (108)
		KeyEvent.VK_UNDEFINED,		// (109)
		KeyEvent.VK_UNDEFINED,		// (110)
		KeyEvent.VK_UNDEFINED,		// (111)
		KeyEvent.VK_KANA,		// KEY_KANA
		KeyEvent.VK_UNDEFINED,		// (113)
		KeyEvent.VK_UNDEFINED,		// (114)
		KeyEvent.VK_UNDEFINED,		// (115)
		KeyEvent.VK_UNDEFINED,		// (116)
		KeyEvent.VK_UNDEFINED,		// (117)
		KeyEvent.VK_UNDEFINED,		// (118)
		KeyEvent.VK_UNDEFINED,		// (119)
		KeyEvent.VK_UNDEFINED,		// (120)
		KeyEvent.VK_CONVERT,		// KEY_CONVERT
		KeyEvent.VK_UNDEFINED,		// (122)
		KeyEvent.VK_NONCONVERT,		// KEY_NOCONVERT
		KeyEvent.VK_UNDEFINED,		// (124)
		KeyEvent.VK_BACK_SLASH,		// KEY_YEN
		KeyEvent.VK_UNDEFINED,		// (126)
		KeyEvent.VK_UNDEFINED,		// (127)
		KeyEvent.VK_UNDEFINED,		// (128)
		KeyEvent.VK_UNDEFINED,		// (129)
		KeyEvent.VK_UNDEFINED,		// (130)
		KeyEvent.VK_UNDEFINED,		// (131)
		KeyEvent.VK_UNDEFINED,		// (132)
		KeyEvent.VK_UNDEFINED,		// (133)
		KeyEvent.VK_UNDEFINED,		// (134)
		KeyEvent.VK_UNDEFINED,		// (135)
		KeyEvent.VK_UNDEFINED,		// (136)
		KeyEvent.VK_UNDEFINED,		// (137)
		KeyEvent.VK_UNDEFINED,		// (138)
		KeyEvent.VK_UNDEFINED,		// (139)
		KeyEvent.VK_UNDEFINED,		// (140)
		KeyEvent.VK_UNDEFINED,		// KEY_NUMPADEQUALS
		KeyEvent.VK_UNDEFINED,		// (142)
		KeyEvent.VK_UNDEFINED,		// (143)
		KeyEvent.VK_CIRCUMFLEX,		// KEY_CIRCUMFLEX
		KeyEvent.VK_AT,		// KEY_AT
		KeyEvent.VK_COLON,		// KEY_COLON
		KeyEvent.VK_UNDERSCORE,		// KEY_UNDERLINE
		KeyEvent.VK_KANJI,		// KEY_KANJI
		KeyEvent.VK_STOP,		// KEY_STOP
		KeyEvent.VK_UNDEFINED,		// KEY_AX
		KeyEvent.VK_UNDEFINED,		// KEY_UNLABELED
		KeyEvent.VK_UNDEFINED,		// (152)
		KeyEvent.VK_UNDEFINED,		// (153)
		KeyEvent.VK_UNDEFINED,		// (154)
		KeyEvent.VK_UNDEFINED,		// (155)
		KeyEvent.VK_UNDEFINED,		// KEY_NUMPADENTER
		KeyEvent.VK_CONTROL,		// KEY_RCONTROL
		KeyEvent.VK_UNDEFINED,		// (158)
		KeyEvent.VK_UNDEFINED,		// (159)
		KeyEvent.VK_UNDEFINED,		// (160)
		KeyEvent.VK_UNDEFINED,		// (161)
		KeyEvent.VK_UNDEFINED,		// (162)
		KeyEvent.VK_UNDEFINED,		// (163)
		KeyEvent.VK_UNDEFINED,		// (164)
		KeyEvent.VK_UNDEFINED,		// (165)
		KeyEvent.VK_UNDEFINED,		// (166)
		KeyEvent.VK_UNDEFINED,		// (167)
		KeyEvent.VK_UNDEFINED,		// (168)
		KeyEvent.VK_UNDEFINED,		// (169)
		KeyEvent.VK_UNDEFINED,		// (170)
		KeyEvent.VK_UNDEFINED,		// (171)
		KeyEvent.VK_UNDEFINED,		// (172)
		KeyEvent.VK_UNDEFINED,		// (173)
		KeyEvent.VK_UNDEFINED,		// (174)
		KeyEvent.VK_UNDEFINED,		// (175)
		KeyEvent.VK_UNDEFINED,		// (176)
		KeyEvent.VK_UNDEFINED,		// (177)
		KeyEvent.VK_UNDEFINED,		// (178)
		KeyEvent.VK_UNDEFINED,		// KEY_NUMPADCOMMA
		KeyEvent.VK_UNDEFINED,		// (180)
		KeyEvent.VK_DIVIDE,		// KEY_DIVIDE
		KeyEvent.VK_UNDEFINED,		// (182)
		KeyEvent.VK_PRINTSCREEN,		// KEY_SYSRQ
		KeyEvent.VK_CONTEXT_MENU,		// KEY_RMENU
		KeyEvent.VK_UNDEFINED,		// (185)
		KeyEvent.VK_UNDEFINED,		// (186)
		KeyEvent.VK_UNDEFINED,		// (187)
		KeyEvent.VK_UNDEFINED,		// (188)
		KeyEvent.VK_UNDEFINED,		// (189)
		KeyEvent.VK_UNDEFINED,		// (190)
		KeyEvent.VK_UNDEFINED,		// (191)
		KeyEvent.VK_UNDEFINED,		// (192)
		KeyEvent.VK_UNDEFINED,		// (193)
		KeyEvent.VK_UNDEFINED,		// (194)
		KeyEvent.VK_UNDEFINED,		// (195)
		KeyEvent.VK_UNDEFINED,		// (196)
		KeyEvent.VK_PAUSE,		// KEY_PAUSE
		KeyEvent.VK_UNDEFINED,		// (198)
		KeyEvent.VK_HOME,		// KEY_HOME
		KeyEvent.VK_UP,		// KEY_UP
		KeyEvent.VK_PAGE_UP,		// KEY_PRIOR
		KeyEvent.VK_UNDEFINED,		// (202)
		KeyEvent.VK_LEFT,		// KEY_LEFT
		KeyEvent.VK_UNDEFINED,		// (204)
		KeyEvent.VK_RIGHT,		// KEY_RIGHT
		KeyEvent.VK_UNDEFINED,		// (206)
		KeyEvent.VK_END,		// KEY_END
		KeyEvent.VK_DOWN,		// KEY_DOWN
		KeyEvent.VK_PAGE_DOWN,		// KEY_NEXT
		KeyEvent.VK_INSERT,		// KEY_INSERT
		KeyEvent.VK_DELETE,		// KEY_DELETE
		KeyEvent.VK_UNDEFINED,		// (212)
		KeyEvent.VK_UNDEFINED,		// (213)
		KeyEvent.VK_UNDEFINED,		// (214)
		KeyEvent.VK_UNDEFINED,		// (215)
		KeyEvent.VK_UNDEFINED,		// (216)
		KeyEvent.VK_UNDEFINED,		// (217)
		KeyEvent.VK_UNDEFINED,		// (218)
		KeyEvent.VK_WINDOWS,		// KEY_LWIN
		KeyEvent.VK_WINDOWS,		// KEY_RWIN
		KeyEvent.VK_UNDEFINED,		// KEY_APPS
		KeyEvent.VK_UNDEFINED,		// KEY_POWER
	};
	
	ArrayList<DKeyListener> keyListeners = new ArrayList<DKeyListener>();
	ArrayList<DMouseListener> mouseListeners = new ArrayList<DMouseListener>();
	
	Input input;
	
	int lastButton;

	private boolean enabled = true;
	
	public Slick2DEventMapper( Input input )
	{
		register( input );
	}
	
	public void register( Input input )
	{
		input.removeAllListeners();
		this.input = input;
		input.addListener( this );
	}
	
	public void addDKeyListener( DKeyListener l )
	{
		synchronized( keyListeners )
		{
			if( !keyListeners.contains( l ) )
				keyListeners.add( l );
		}
	}
	
	public void addDMouseListener( DMouseListener l )
	{
		synchronized( mouseListeners )
		{
			if( !mouseListeners.contains( l ) )
				mouseListeners.add( l );
		}
	}
	
	@Override
	public void removeDKeyListener( DKeyListener l )
	{
		synchronized( keyListeners )
		{
			keyListeners.remove( l );
		}
	}

	@Override
	public void removeDMouseListener( DMouseListener l )
	{
		synchronized( mouseListeners )
		{
			mouseListeners.remove( l );
		}
	}
	
	public void mouseClicked( int arg0, int arg1, int arg2, int arg3 )
	{
		
	}

	@Override
	public void mouseDragged( int oldx, int oldy, int newx, int newy )
	{
		if( !enabled ) return;
		ArrayList<DMouseListener> tempMLs;
		synchronized( mouseListeners )
		{
			tempMLs = new ArrayList<DMouseListener>( mouseListeners );
		}
		
		DMouseEvent dme = new DMouseEvent();
		dme.x = newx;
		dme.y = newy;
		for( int i = 0; i < tempMLs.size(); i++ )
		{
			DMouseListener l = tempMLs.get( i );
			l.mouseDragged( dme );
		}
	}

	@Override
	public void mouseMoved( int oldx, int oldy, int newx, int newy )
	{
		if( !enabled ) return;
		ArrayList<DMouseListener> tempMLs;
		synchronized( mouseListeners )
		{
			tempMLs = new ArrayList<DMouseListener>( mouseListeners );
		}
		
		DMouseEvent dme = new DMouseEvent();
		dme.x = newx;
		dme.y = newy;
		for( int i = 0; i < tempMLs.size(); i++ )
		{
			DMouseListener l = tempMLs.get( i );
			l.mouseMoved( dme );
		}
	}

	@Override
	public void mousePressed( int button, int x, int y )
	{
		if( !enabled ) return;
		ArrayList<DMouseListener> tempMLs;
		synchronized( mouseListeners )
		{
			tempMLs = new ArrayList<DMouseListener>( mouseListeners );
		}
		
		DMouseEvent dme = new DMouseEvent();
		dme.x = x;
		dme.y = y;
		dme.button = button;
		for( int i = 0; i < tempMLs.size(); i++ )
		{
			DMouseListener l = tempMLs.get( i );
			l.mousePressed( dme );
		}
	}

	@Override
	public void mouseReleased( int button, int x, int y )
	{
		if( !enabled ) return;
		ArrayList<DMouseListener> tempMLs;
		synchronized( mouseListeners )
		{
			tempMLs = new ArrayList<DMouseListener>( mouseListeners );
		}
		DMouseEvent dme = new DMouseEvent();
		dme.x = x;
		dme.y = y;
		dme.button = button;
		for( int i = 0; i < tempMLs.size(); i++ )
		{
			DMouseListener l = tempMLs.get( i );
			l.mouseReleased( dme );
		}
	}

	@Override
	public void mouseWheelMoved( int a )
	{
		if( !enabled ) return;
		ArrayList<DMouseListener> tempMLs;
		synchronized( mouseListeners )
		{
			tempMLs = new ArrayList<DMouseListener>( mouseListeners );
		}
		
		DMouseEvent dme = new DMouseEvent();
		dme.wheel = a;
		dme.x = input.getMouseX();
		dme.y = input.getMouseY();
		for( int i = 0; i < tempMLs.size(); i++ )
		{
			DMouseListener l = tempMLs.get( i );
			l.mouseWheel( dme );
		}
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
		ArrayList<DKeyListener> tempKLs = new ArrayList<DKeyListener>( keyListeners );
		DKeyEvent dke = new DKeyEvent();
		dke.keyCode = KEYTABLE[key];
		dke.keyChar = c;
		dke.isActionKey = false;
		dke.lctrl = input.isKeyDown( Input.KEY_LCONTROL );
		for( int i = 0; i < tempKLs.size(); i++ )
		{
			DKeyListener l = tempKLs.get( i );
			l.keyPressed( dke );
		}
	}

	@Override
	public void keyReleased( int key, char c )
	{
		ArrayList<DKeyListener> tempKLs = new ArrayList<DKeyListener>( keyListeners );
		DKeyEvent dke = new DKeyEvent();
		dke.keyCode = KEYTABLE[key];
		dke.keyChar = c;
		dke.isActionKey = false;
		dke.lctrl = input.isKeyDown( Input.KEY_LCONTROL );
		for( int i = 0; i < tempKLs.size(); i++ )
		{
			DKeyListener l = tempKLs.get( i );
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

	@Override
	public void setEnabled( boolean enabled )
	{
		this.enabled = enabled;
		if( enabled )
		{
			input.removeAllListeners();
			input.addListener( this );
		}
	}
}
