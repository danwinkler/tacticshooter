package com.danwink.tacticshooter.screens;

import java.io.IOException;
import java.nio.BufferOverflowException;
import java.util.ArrayList;

import javax.vecmath.Point2i;

import org.newdawn.slick.Color;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.Input;
import org.newdawn.slick.InputListener;
import org.newdawn.slick.Music;
import org.newdawn.slick.MusicListener;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.Sound;
import org.newdawn.slick.geom.Rectangle;

import tacticshooter.Building;
import tacticshooter.Building.BuildingType;
import tacticshooter.Bullet;
import tacticshooter.ClientInterface;
import tacticshooter.ClientNetworkInterface;
import tacticshooter.ClientState;
import tacticshooter.Level;
import tacticshooter.Message;
import tacticshooter.MessageType;
import tacticshooter.MusicQueuer;
import tacticshooter.Player;
import tacticshooter.Slick2DEventMapper;
import tacticshooter.Slick2DRenderer;
import tacticshooter.StaticFiles;
import tacticshooter.Team;
import tacticshooter.Unit;
import tacticshooter.Unit.UnitType;

import com.esotericsoftware.minlog.Log;
import com.phyloa.dlib.dui.DButton;
import com.phyloa.dlib.dui.DPanel;
import com.phyloa.dlib.dui.DTextBox;
import com.phyloa.dlib.dui.DUI;
import com.phyloa.dlib.dui.DUIElement;
import com.phyloa.dlib.dui.DUIEvent;
import com.phyloa.dlib.dui.DUIListener;
import com.phyloa.dlib.renderer.DScreen;
import com.phyloa.dlib.renderer.DScreenHandler;
import com.phyloa.dlib.util.DMath;

public class MultiplayerGameScreen extends DScreen<GameContainer, Graphics> implements InputListener, DUIListener
{
	ClientInterface ci;
	public ClientState cs = new ClientState();
	
	boolean waitingForMoveConfirmation = false;
	float mx = -1, my = -1;
	
	float sx, sy, sx2, sy2;
	boolean selecting = false;
	
	DButton switchTeams;
	DButton buildLightUnit;
	DButton buildHeavyUnit;
	DButton buildShotgunUnit;
	
	DPanel escapeMenu;
	DButton quit;
	DButton returnToGame;
	
	DTextBox chatBox;

    DUI dui;
	
	Input input;
	
	DScreenHandler<GameContainer, Graphics> dsh;
	
	Slick2DRenderer renderer = new Slick2DRenderer();
	
	String address;
	
	GameContainer gc;
	
	Image miniMap;
	
	int bottomOffset = 200;
	
	boolean running = false;
	
	Image bloodTexture;
	Graphics btg;
	
	ArrayList<String> messages = new ArrayList<String>();
	
	public void onActivate( GameContainer gc, DScreenHandler<GameContainer, Graphics> dsh )
	{
		this.dsh = dsh;
		this.gc = gc;
		
		if( dui == null )
		{
			dui = new DUI( new Slick2DEventMapper( gc.getInput() ) );
			switchTeams = new DButton( "Switch Teams", 0, gc.getHeight()-100, 200, 100 );
			buildLightUnit = new DButton( "Build Light Unit\n10", 200, gc.getHeight()-100, 200, 100 );
			buildHeavyUnit = new DButton( "Build Heavy Unit\n20", 400, gc.getHeight()-100, 200, 100 );
			buildShotgunUnit = new DButton( "Build Shotgun Unit\n15", 600, gc.getHeight()-100, 200, 100 );
			
			escapeMenu = new DPanel( gc.getWidth() / 2 - 100, gc.getHeight()/2 - 100, 200, 200 );
			quit = new DButton( "Quit Game", 0, 0, 200, 100 );
			escapeMenu.add( quit );
			returnToGame = new DButton( "Return to Game", 0, 100, 200, 100 );
			escapeMenu.add( returnToGame );
			escapeMenu.setVisible( false );
			
			chatBox = new DTextBox( gc.getWidth()/2-200, gc.getHeight()/2-50, 400, 100 );
			chatBox.setVisible( false );
			
			dui.add( switchTeams );
			dui.add( buildLightUnit );
			dui.add( buildHeavyUnit );
			dui.add( buildShotgunUnit );
			dui.add( escapeMenu );
			dui.add( chatBox );
			
			dui.addDUIListener( this );
		}
		dui.setEnabled( true );
		
		
		try 
		{
			ci = new ClientNetworkInterface( address );	
		} catch( IOException e )
		{
			dsh.message( "message", "Could not connect to: " + address );
			dsh.activate( "message", gc );
			return;
		}
		ci.sendToServer( new Message( MessageType.CLIENTJOIN, StaticFiles.options.getS( "name" ) ) );
		input = gc.getInput();
		input.addListener( this );
		
		Music music;
		int musicChoice = DMath.randomi( 1, 3 );
		while( (music = StaticFiles.getMusic( "play" + musicChoice )) == null ){};
		music.play();
		music.addListener( new MusicQueuer( DMath.randomi( 0, 2 ), "play1", "play2", "play3" ) );
		
		try
		{
			cs.bullet1 = new Sound( "sound/bullet1.wav" );
			cs.bullet2 = new Sound( "sound/bullet2.wav" );
			cs.ping1 = new Sound( "sound/ping1.wav" );
			cs.death1 = new Sound( "sound/death1.wav" );
			cs.death2 = new Sound( "sound/death2.wav" );
			cs.hit1 = new Sound( "sound/hit1.wav" );
		} 
		catch( SlickException e )
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		running = true;
	}
	
	public void update( GameContainer gc, int delta )
	{
		if( !running ) return;
		
		float d = delta / 60.f;
		
		while( ci.hasClientMessages() )
		{
			Message m = ci.getNextClientMessage();
			switch( m.messageType )
			{
			case UNITUPDATE:
				Unit u = (Unit)m.message;
				Unit tu = cs.unitMap.get( u.id );
				if( tu == null )
				{
					cs.unitMap.put( u.id, u );
					cs.units.add( u );
					cs.ping1.play( 1.f, cs.getSoundMag( gc, u.x, u.y ) );
					tu = u;
				}
				tu.sync( u );
				break;
			case LEVELUPDATE:
				if( cs.l == null && cs.player != null )
				{
					cs.l = (Level)m.message;
					scrollToTeamBase( cs.player.team );
				}
				else
				{
					cs.l = (Level)m.message;
				}
				break;
			case BULLETUPDATE:
				Bullet b = (Bullet)m.message;
				Bullet tb = cs.bulletMap.get( b.id );
				if( tb == null )
				{
					cs.bulletMap.put( b.id, b );
					cs.bullets.add( b );
					(Math.random() > .5 ? cs.bullet1 : cs.bullet2).play( 1.f, cs.getSoundMag( gc, b.x, b.y ) * .2f );
					tb = b;
				}
				tb.sync( b );
				break;
			case MOVESUCCESS:
				this.waitingForMoveConfirmation = false;
				break;
			case PLAYERUPDATE:
				Player newPlayer = (Player)m.message;
				if( (cs.player == null || newPlayer.team.id != cs.player.team.id) && cs.l != null )
				{
					scrollToTeamBase( newPlayer.team );
				}
				this.cs.player = newPlayer;
				break;
			case BUILDINGUPDATE:
				if( cs.l != null )
				{
					Building building = (Building)m.message;
					cs.l.buildings.set( building.index, building );
				}
				break;
			case PLAYERLIST:
				cs.players = (Player[])m.message;
				break;
			case MESSAGE:
				String mess = (String)m.message;
				int lineLength = 32;
				do
				{
					String p1 = mess.substring( 0, Math.min( lineLength, mess.length() ) );
					mess = mess.substring( Math.min( lineLength, mess.length() ), mess.length() );
					messages.add( p1 + (mess.length() > 0 ? "-" : "") );
				} while( mess.length() > 0 );
				break;
			case GAMEOVER:
				dsh.message( "postgame", m.message );
				dsh.activate( "postgame", gc );
				return;
			}
		}
		
		if( cs.l == null )
		{
			return;
		}
		
		if( !chatBox.isVisible() && !escapeMenu.isVisible() )
		{
			float scrollSpeed = 20;
			Rectangle screenBounds = getScreenBounds();
			
			boolean scrollup = cs.scrolly > screenBounds.getMinY() && (input.isKeyDown( Input.KEY_UP ) || input.isKeyDown( Input.KEY_W ) || (gc.isFullscreen() && input.getMouseY() < 10 ));
			boolean scrolldown = cs.scrolly+gc.getHeight() < screenBounds.getMaxY() && (input.isKeyDown( Input.KEY_DOWN ) || input.isKeyDown( Input.KEY_S ) || (gc.isFullscreen() && input.getMouseY() > gc.getHeight()-10));
			boolean scrollleft = cs.scrollx > screenBounds.getMinX() && (input.isKeyDown( Input.KEY_LEFT ) || input.isKeyDown( Input.KEY_A ) || (gc.isFullscreen() && input.getMouseX() < 10));
			boolean scrollright = cs.scrollx+gc.getWidth() < screenBounds.getMaxX() && (input.isKeyDown( Input.KEY_RIGHT ) || input.isKeyDown( Input.KEY_D ) || (gc.isFullscreen() && input.getMouseX() > gc.getWidth()-10));
			
			if( scrollup ) cs.scrolly-=scrollSpeed*d;
			if( scrolldown ) cs.scrolly+=scrollSpeed*d;
			if( scrollleft ) cs.scrollx-=scrollSpeed*d;
			if( scrollright ) cs.scrollx+=scrollSpeed*d;
		}
		
		for( int i = 0; i < cs.units.size(); i++ )
		{
			Unit u = cs.units.get( i );
			u.clientUpdate( cs, d );
			if( u.timeSinceUpdate > 100 )
			{
				ci.sendToServer( new Message( MessageType.UNITUPDATE, u ) );
				u.timeSinceUpdate = 0;
			}
			if( !u.alive )
			{
				cs.units.remove( i );
				cs.unitMap.remove( u );
				(Math.random() > .5 ? cs.death1 : cs.death2).play( 1.f, cs.getSoundMag( gc, u.x, u.y ) );
				for( int j = 0; j < 10; j++ )
				{
					drawBlood( u.x, u.y );
				}
				i--;
				continue;
			}
		}
		
		for( int i = 0; i < cs.bullets.size(); i++ )
		{
			Bullet b = cs.bullets.get( i );
			b.clientUpdate( this, d, gc );
			if( !b.alive )
			{
				cs.bulletMap.remove( b );
				cs.bullets.remove( i );
				i--;
				continue;
			}
		}
		
		dui.update();
	}

	public void render( GameContainer gc, Graphics g )
	{
		if( !running ) return;
		
		g.setAntiAlias( true );
		
		if( cs.l == null )
		{
			return;
		} else if( bloodTexture == null )
		{
			try
			{
				bloodTexture = new Image( cs.l.width * Level.tileSize, cs.l.height * Level.tileSize );
				btg = bloodTexture.getGraphics();
				btg.setColor( new Color( 255, 0, 0, 200 ) );
			} catch( SlickException e )
			{
				e.printStackTrace();
			}
		}
		
		g.setColor( Color.white );
		g.fillRect( 0, 0, gc.getWidth(), gc.getHeight() );
		g.setColor( Color.black );
		
		g.pushTransform();
		g.translate( -cs.scrollx, -cs.scrolly );
		
		cs.l.renderBuildings( g );
		g.drawImage( bloodTexture, 0, 0 );
		cs.l.render( g );
		
		
		g.setColor( this.waitingForMoveConfirmation ? Color.gray : Color.green );
		g.drawRect( mx * Level.tileSize, my * Level.tileSize, Level.tileSize, Level.tileSize );
		
		for( int i = 0; i < cs.units.size(); i++ )
		{
			Unit u = cs.units.get( i );
			u.render( g, cs.player );
		}
		
		g.setColor( Color.black );
		
		for( int i = 0; i < cs.bullets.size(); i++ )
		{
			Bullet b = cs.bullets.get( i );
			b.render( g );
		}
		
		if( selecting )
		{
			g.setColor( Color.blue );
			float x1 = Math.min( sx, sx2 );
			float y1 = Math.min( sy, sy2 );
			float x2 = Math.max( sx, sx2 );
			float y2 = Math.max( sy, sy2 );
			g.drawRect( x1, y1, x2-x1, y2-y1 );
		}
		
		g.popTransform();
		
		g.setColor( new Color( 0, 0, 0, 128 ) );
		g.fillRect( 0, 0, gc.getWidth(), 30 );
		g.setColor( Color.white );
		if( cs.player != null )
		{
			g.drawString( "Money: " + cs.player.money, 100, 10 );
			g.drawString( "Selected: " + cs.selected.size(), 200, 10 );
			g.setColor( Color.black );
			if( messages.size() > 0 )
			{
				for( int i = messages.size()-1; i >= Math.max( messages.size()-12, 0 ); i-- )
				{
					g.drawString( messages.get( i ), gc.getWidth()-295, 300 - (messages.size()-1-i)*25 );
				}
			}
		}
		
		dui.render( renderer.renderTo( g ) );
		
		//Draw minimap
		g.setClip( gc.getWidth()-200, gc.getHeight()-200, 200, 200 );
		float xScale = 200.f / (cs.l.width*cs.l.tileSize);
		float yScale = 200.f / (cs.l.height*cs.l.tileSize);
		g.pushTransform();
		g.translate( gc.getWidth()-200, gc.getHeight()-200 );
		g.setColor( Color.white );
		g.fillRect( 0, 0, 200, 300 );
		g.pushTransform();
		if( miniMap == null )
		{
			g.scale( xScale, yScale );
			cs.l.render( g );
			try
			{
				miniMap = new Image( 200, 200 );
			} catch( SlickException e )
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			g.copyArea( miniMap, gc.getWidth()-200, gc.getHeight()-200 );
		}
		else
		{
			g.drawImage( miniMap, 0, 0 );
			g.scale( xScale, yScale );
		}
		cs.l.renderBuildings( g );
		for( int i = 0; i < cs.units.size(); i++ )
		{
			Unit u = cs.units.get( i );
			u.renderMinimap( g, cs.player );
		}
		
		g.setColor( Color.blue );
		g.drawRect( cs.scrollx, cs.scrolly, gc.getWidth(), gc.getHeight() );
		
		g.popTransform();
		
		g.setColor( Color.black );
		g.setLineWidth( 2 );
		g.drawRect( 0, 0, 200, 300 );
		g.setLineWidth( 1 );
		g.popTransform();
		
		g.clearClip();
		
		if( escapeMenu.isVisible() )
		{
			g.setColor( new Color( 0, 0, 0, 128 ) );
			//Left side
			g.fillRect( 0, 0, gc.getWidth()/2 - 100, gc.getHeight() );
			
			//Right side
			g.fillRect( gc.getWidth()/2 + 100, 0, gc.getWidth()/2 - 100, gc.getHeight() );
			
			//Top
			g.fillRect( gc.getWidth()/2 - 100, 0, 200, gc.getHeight()/2-100 );
			
			//bottom
			g.fillRect( gc.getWidth()/2 - 100, gc.getHeight()/2 + 100, 200, gc.getHeight()/2-100 );
		}
		
		if( input.isKeyDown( Input.KEY_TAB ) && cs.players != null )
		{
			g.setColor( new Color( 128, 128, 128, 200 ) );
			g.fillRect( gc.getWidth()/2 - 200, gc.getHeight()/2-300, 400, 500 );
			g.setColor( Color.black );
			g.drawRect( gc.getWidth()/2 - 200, gc.getHeight()/2-300, 400, 500 );
			for( int i = 0; i < cs.players.length; i++ )
			{
				g.drawString( cs.players[i].id + " " + (cs.players[i].isBot ? "BOT" : "HUMAN"), gc.getWidth()/2 - 190, gc.getHeight()/2-270 + i*30 );
			}
		}
	}

	public void onExit()
	{
		running = false;
		if( ci != null )
		{
			ci.stop();
		}
		cs.resetState();
		miniMap = null;
		bloodTexture = null;
		dui.setEnabled( false );
		messages.clear();
	}
	
	public void scrollToTeamBase( Team t )
	{
		int destX = 0;
		int destY = 0;
		for( int i = 0; i < cs.l.buildings.size(); i++ )
		{
			Building b = cs.l.buildings.get( i );
			if( b.bt == BuildingType.CENTER && b.t.id == t.id )
			{
				destX = b.x;
				destY = b.y;
				break;
			}
		}
		
		Rectangle screenBounds = getScreenBounds();
		cs.scrollx = DMath.bound( destX-gc.getWidth()/2, screenBounds.getMinX(), screenBounds.getMaxX() );
		cs.scrolly = DMath.bound( destY-gc.getHeight()/2, screenBounds.getMinY(), screenBounds.getMaxY() );
	}
	
	public void drawBlood( float x, float y )
	{
		//btg.setColor( Color.red );
		x += DMath.randomf( -8, 8 );
		y += DMath.randomf( -8, 8 );
		btg.fillOval( x-2, y-2, 4, 4 );
		btg.flush();
	}
	
	public Rectangle getScreenBounds()
	{
		return new Rectangle( -gc.getWidth()/2, -gc.getHeight()/2, cs.l.width*Level.tileSize + gc.getWidth(), cs.l.height*Level.tileSize + gc.getHeight() );
	}

	public void mousePressed( int button, int x, int y )
	{
		if( x > gc.getWidth()-200 && y > gc.getHeight()-200 && !selecting )
		{	
			//miniMap
			float minimapX = x - (gc.getWidth()-200);
			float minimapY = y - (gc.getHeight()-200);
			Rectangle screenBounds = getScreenBounds();
			cs.scrollx = DMath.bound( (minimapX / 200.f) * cs.l.width*Level.tileSize - gc.getWidth()/2, screenBounds.getMinX(), screenBounds.getMaxX() );
			cs.scrolly = DMath.bound( (minimapY / 200.f) * cs.l.height*Level.tileSize - gc.getHeight()/2, screenBounds.getMinY(), screenBounds.getMaxY() );
		}	
		else
		{
			if( button == Input.MOUSE_LEFT_BUTTON )
			{
				sx = x + cs.scrollx;
				sy = y + cs.scrolly;
				sx2 = sx;
				sy2 = sy;
				selecting = true;
			} 
			else if( button == Input.MOUSE_RIGHT_BUTTON )
			{
				int tx = (int)((x+cs.scrollx) / Level.tileSize);
				int ty = (int)((y+cs.scrolly) / Level.tileSize);
				ci.sendToServer( new Message( MessageType.SETATTACKPOINT, new Object[]{ new Point2i( tx, ty ), cs.selected } ) );
				this.waitingForMoveConfirmation = true;
				mx = tx;
				my = ty;
			}
		}
	}

	public void mouseReleased( int button, int x, int y )
	{
		if( button == Input.MOUSE_LEFT_BUTTON && selecting )
		{
			cs.selected.clear();
			
			float x1 = Math.min( sx, x+cs.scrollx );
			float y1 = Math.min( sy, y+cs.scrolly );
			float x2 = Math.max( sx, x+cs.scrollx );
			float y2 = Math.max( sy, y+cs.scrolly );
			for( Unit u : cs.units )
			{
				u.selected = u.owner.id == this.cs.player.id && u.x > x1 && u.x < x2 && u.y > y1 && u.y < y2;
				if( u.selected )
				{
					cs.selected.add( u.id );
				}
			}
			selecting = false;
		}
	}

	public void mouseDragged( int oldx, int oldy, int newx, int newy )
	{
		if( newx > gc.getWidth()-200 && newy > gc.getHeight()-200 && !selecting )
		{	
			//miniMap
			float minimapX = newx - (gc.getWidth()-200);
			float minimapY = newy - (gc.getHeight()-200);
			Rectangle screenBounds = getScreenBounds();
			cs.scrollx = DMath.bound( (minimapX / 200.f) * cs.l.width*Level.tileSize - gc.getWidth()/2, screenBounds.getMinX(), screenBounds.getMaxX() );
			cs.scrolly = DMath.bound( (minimapY / 200.f) * cs.l.height*Level.tileSize - gc.getHeight()/2, screenBounds.getMinY(), screenBounds.getMaxY() );
		}	
		else
		{
			if( input.isMouseButtonDown( Input.MOUSE_LEFT_BUTTON ) )
			{
				sx2 = newx+cs.scrollx;
				sy2 = newy+cs.scrolly;
			}
		}
	}

	@Override
	public void mouseClicked( int arg0, int arg1, int arg2, int arg3 )
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseMoved( int arg0, int arg1, int arg2, int arg3 )
	{
		// TODO Auto-generated method stub
		
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

	public boolean isAcceptingInput()
	{
		return true;
	}

	@Override
	public void setInput( Input arg0 )
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void keyPressed( int keyCode, char arg1 )
	{
		if( keyCode == Input.KEY_ESCAPE )
		{
			escapeMenu.setVisible( !escapeMenu.isVisible() );
		}
		else if( keyCode == Input.KEY_ENTER )
		{
			if( chatBox.isVisible() )
			{
				chatBox.setVisible( false );
				ci.sendToServer( new Message( MessageType.MESSAGE, cs.player.name + ": " + chatBox.getText() ) );
				chatBox.setText( "" );
			}
			else
			{
				chatBox.setVisible( true );
				dui.setFocus( chatBox );
			}
		}
	}

	@Override
	public void keyReleased( int arg0, char arg1 )
	{
		// TODO Auto-generated method stub
		
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
	public void message( Object o )
	{
		address = (String)o;
	}
	
	public void event( DUIEvent event )
	{
		DUIElement e = event.getElement();
		if( e instanceof DButton && event.getType() == DButton.MOUSE_UP )
		{
			if( e == switchTeams )
			{
				ci.sendToServer( new Message( MessageType.SWITCHTEAMS, cs.player.team ) );
			} else if( e == buildLightUnit )
			{
				ci.sendToServer( new Message( MessageType.BUILDUNIT, UnitType.LIGHT ) );
			} else if( e == buildHeavyUnit )
			{
				ci.sendToServer( new Message( MessageType.BUILDUNIT, UnitType.HEAVY ) );
			} else if( e == buildShotgunUnit )
			{
				ci.sendToServer( new Message( MessageType.BUILDUNIT, UnitType.SHOTGUN ) );
			} else if( e == quit )
			{
				running = false;
				escapeMenu.setVisible( false );
				dsh.activate( "home", gc );
			} else if( e == returnToGame )
			{
				escapeMenu.setVisible( false );
			}
		}
	}
}
