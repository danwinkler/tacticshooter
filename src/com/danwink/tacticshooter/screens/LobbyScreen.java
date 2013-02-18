package com.danwink.tacticshooter.screens;

import java.awt.event.KeyEvent;
import java.io.IOException;
import java.util.ArrayList;

import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;

import tacticshooter.ClientInterface;
import tacticshooter.ClientNetworkInterface;
import tacticshooter.ComputerPlayer;
import tacticshooter.ComputerPlayer.PlayType;
import tacticshooter.Message;
import tacticshooter.MessageType;
import tacticshooter.Player;
import tacticshooter.Slick2DEventMapper;
import tacticshooter.Slick2DRenderer;
import tacticshooter.StaticFiles;

import com.phyloa.dlib.dui.DButton;
import com.phyloa.dlib.dui.DDropDown;
import com.phyloa.dlib.dui.DPanel;
import com.phyloa.dlib.dui.DTextBox;
import com.phyloa.dlib.dui.DUI;
import com.phyloa.dlib.dui.DUIEvent;
import com.phyloa.dlib.dui.DUIListener;
import com.phyloa.dlib.renderer.DScreen;
import com.phyloa.dlib.renderer.DScreenHandler;
import com.phyloa.dlib.util.DMath;

public class LobbyScreen extends DScreen<GameContainer, Graphics> implements DUIListener
{
	ClientInterface ci;
	
	DUI dui;
	Slick2DRenderer r = new Slick2DRenderer();
	
	DButton[] names = new DButton[16];
	DDropDown[] humanOrBot = new DDropDown[16];
	DDropDown[] botType = new DDropDown[16];
	DDropDown maps;
	DTextBox chatBox;
	DButton startGame;
	DButton leaveGame;
	
	Player[] slots = new Player[16];
	
	ArrayList<String> messages = new ArrayList<String>();
	
	public void onActivate( GameContainer gc, DScreenHandler<GameContainer, Graphics> dsh )
	{	
		messages.clear();
		dui = new DUI( new Slick2DEventMapper( gc.getInput() ) );
		dui.addDUIListener( this );
		for( int i = 0; i < 16; i++ )
		{
			int baseHeight = i < 8 ? 160 : 200;
			names[i] = new DButton( "Open", 20, baseHeight + i * 30, 170, 25 );
			names[i].setName( "na " + i );
			dui.add( names[i] );
			
			humanOrBot[i] = new DDropDown( 200, baseHeight + i * 30, 100, 25 );
			humanOrBot[i].name = "hb " + i;
			humanOrBot[i].addItems( "HUMAN", "BOT" );
			dui.add( humanOrBot[i] );
			
			botType[i] = new DDropDown( 310, baseHeight + i * 30, 200, 25 );
			botType[i].name = "bt " + i;
			botType[i].setVisible( false );
			for( ComputerPlayer.PlayType pt : ComputerPlayer.PlayType.values() )
			{
				botType[i].addItems( pt.name() );
			}
			dui.add( botType[i] );
		}
		
		maps = new DDropDown( 20, 100, 500, 25 );
		dui.add( maps );
		
		leaveGame = new DButton( "Leave", 105, 700, 90, 50 );
		dui.add( leaveGame );
		
		startGame = new DButton( "Start", 205, 700, 90, 50 );
		dui.add( startGame );
		
		chatBox = new DTextBox( gc.getWidth()-600, gc.getHeight() - 200, 500, 50 );
		dui.add( chatBox );
		
		DPanel chatBackground = new DPanel( gc.getWidth() - 600, 100, 500, gc.getHeight() - 310 );
		chatBackground.setDrawBackground( true );
		dui.add( chatBackground );
		
		ci.sendToServer( new Message( MessageType.CLIENTJOIN, StaticFiles.getUsername() ) );
	}
	
	public void update( GameContainer gc, int delta )
	{
		dui.update();
		
		while( ci.hasClientMessages() )
		{
			Message m = ci.getNextClientMessage();
			switch( m.messageType )
			{
			case PLAYERUPDATE:
			{
				Object[] oa = (Object[])m.message;
				Player p = (Player)oa[1];
				int slot = (Integer)oa[0];
				if( p == null )
				{
					names[slot].setText( "Open" );
					humanOrBot[slot].setSelected( 0 );
					botType[slot].setVisible( false );
				}
				else
				{
					names[p.slot].setText( p.name );
					humanOrBot[p.slot].setSelected( p.isBot ? 1 : 0 );
					botType[p.slot].setVisible( p.isBot );
					botType[p.slot].setSelected( p.playType.ordinal() );
				}
				break;
			}
			case KICK:
				ci.stop();
				dsh.message( "message", m.message );
				dsh.activate( "message", gc, StaticFiles.getUpMenuOut(), StaticFiles.getUpMenuIn() );
				break;
			case MESSAGE:
				messages.add( (String)m.message );
				break;
			case STARTGAME:
				dsh.message( "multiplayergame", ci );
				dsh.activate( "multiplayergame", gc, StaticFiles.getDownMenuOut(), StaticFiles.getDownMenuIn() );
				break;
			case LEVELUPDATE:
			{
				Object[] oa = (Object[])m.message;
				ArrayList<String> mapList = (ArrayList<String>)oa[1];
				int selectedMap = (Integer)oa[0];
				maps.clearItems();
				for( String s : mapList )
				{
					maps.addItems( s );
				}
				maps.setSelected( selectedMap );
				break;
			}
			}
		}
	}

	public void render( GameContainer gc, Graphics g )
	{	
		dui.render( r.renderTo( g ) );
		int count = 0;
		for( int i = messages.size()-1; i >= Math.max( messages.size() - 10, 0 ); i-- )
		{
			g.drawString( messages.get( i ), gc.getWidth() - 600 + 10, gc.getHeight() - 230 + count * -20 );
			count++;
		}	
	}

	public void onExit() 
	{
		dui.setEnabled( false );
		dui = null;
	}

	public void message( Object o )
	{
		if( o instanceof ClientInterface )
		{
			ci = (ClientInterface)o;
		}
	}

	public void event( DUIEvent event )
	{
		if( event.getElement() instanceof DDropDown )
		{
			DDropDown el = (DDropDown)event.getElement();
			if( el == maps )
			{
				ci.sendToServer( new Message( MessageType.LEVELUPDATE, el.getSelectedOrdinal() ) );
			}
			else
			{
				String[] name = el.name.split( " " );
				if( name.length == 2 )
				{
					int line = Integer.parseInt( name[1] );
					if( name[0].equals( "hb" ) )
					{
						boolean isBot = el.getSelected().equals( "BOT" );
						ci.sendToServer( new Message( MessageType.SETBOT, new Object[] { line, isBot } ) );
					}
					else if( name[0].equals( "bt" ) )
					{
						PlayType pt = PlayType.values()[el.getSelectedOrdinal()];
						ci.sendToServer( new Message( MessageType.SETPLAYTYPE, new Object[] { line, pt } ) );
					}
				}
			}
		}
		else if( event.getElement() instanceof DTextBox )
		{
			DTextBox b = (DTextBox)event.getElement();
			if( event.getType() == KeyEvent.VK_ENTER )
			{
				String text = b.getText().trim();
				if( text.length() > 0 )
				{
					ci.sendToServer( new Message( MessageType.MESSAGE, b.getText().trim() ) );
				}
				b.setText( "" );
			}
		}
		else if( event.getElement() instanceof DButton )
		{
			DButton b = (DButton)event.getElement();
			if( event.getType() == DButton.MOUSE_UP )
			{
				if( b == startGame )
				{
					ci.sendToServer( new Message( MessageType.STARTGAME, null ) );
				}
				else if( b == leaveGame )
				{
					ci.stop();
					dsh.activate( "home", gc, StaticFiles.getUpMenuOut(), StaticFiles.getUpMenuIn() );
				}
				else
				{
					String[] name = b.name.split( " " );
					if( name.length == 2 )
					{
						int line = Integer.parseInt( name[1] );
						if( name[0].equals( "na" ) )
						{
							ci.sendToServer( new Message( MessageType.SWITCHTEAMS, line ) );
						}
					}
				}
			}
		}
	}
}
