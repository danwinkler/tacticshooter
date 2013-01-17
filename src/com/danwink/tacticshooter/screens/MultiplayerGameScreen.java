package com.danwink.tacticshooter.screens;

import java.io.IOException;
import java.nio.BufferOverflowException;

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
import com.phyloa.dlib.dui.DUI;
import com.phyloa.dlib.dui.DUIElement;
import com.phyloa.dlib.dui.DUIEvent;
import com.phyloa.dlib.dui.DUIListener;
import com.phyloa.dlib.renderer.DScreen;
import com.phyloa.dlib.renderer.DScreenHandler;
import com.phyloa.dlib.util.DMath;

public class MultiplayerGameScreen extends ClientState implements DScreen<GameContainer, Graphics>, InputListener, DUIListener
{
	ClientInterface ci;
	
	boolean waitingForMoveConfirmation = false;
	float mx = -1, my = -1;
	
	float sx, sy, sx2, sy2;
	boolean selecting = false;
	
	DButton switchTeams;
	DButton buildLightUnit;
	DButton buildHeavyUnit;
	DButton buildSupplyUnit;
	
	DPanel escapeMenu;
	DButton quit;
	DButton returnToGame;

    DUI dui;
	
	Input input;
	
	DScreenHandler<GameContainer, Graphics> dsh;
	
	Slick2DRenderer renderer = new Slick2DRenderer();
	
	String address;
	
	GameContainer gc;
	
	Image miniMap;
	
	int bottomOffset = 200;
	
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
			buildSupplyUnit = new DButton( "Build Supply Unit\n20", 600, gc.getHeight()-100, 200, 100 );
			
			escapeMenu = new DPanel( gc.getWidth() / 2 - 100, gc.getHeight()/2 - 100, 200, 200 );
			quit = new DButton( "Quit Game", 0, 0, 200, 100 );
			escapeMenu.add( quit );
			returnToGame = new DButton( "Return to Game", 0, 100, 200, 100 );
			escapeMenu.add( returnToGame );
			escapeMenu.setVisible( false );
			
			dui.add( switchTeams );
			dui.add( buildLightUnit );
			dui.add( buildHeavyUnit );
			dui.add( buildSupplyUnit );
			dui.add( escapeMenu );
			
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
		ci.sendToServer( new Message( MessageType.CLIENTJOIN, null ) );
		
		input = gc.getInput();
		input.addListener( this );
		
		Music music;
		int musicChoice = DMath.randomi( 1, 3 );
		while( (music = StaticFiles.getMusic( "play" + musicChoice )) == null ){};
		music.play();
		music.addListener( new MusicQueuer( DMath.randomi( 0, 2 ), "play1", "play2", "play3" ) );
		
		try
		{
			bullet1 = new Sound( "sound/bullet1.wav" );
			bullet2 = new Sound( "sound/bullet2.wav" );
			ping1 = new Sound( "sound/ping1.wav" );
			death1 = new Sound( "sound/death1.wav" );
			death2 = new Sound( "sound/death2.wav" );
			hit1 = new Sound( "sound/hit1.wav" );
		} 
		catch( SlickException e )
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void update( GameContainer gc, int delta )
	{
		float d = delta / 60.f;
		
		while( ci.hasClientMessages() )
		{
			Message m = ci.getNextClientMessage();
			switch( m.messageType )
			{
			case UNITUPDATE:
				Unit u = (Unit)m.message;
				Unit tu = unitMap.get( u.id );
				if( tu == null )
				{
					unitMap.put( u.id, u );
					units.add( u );
					ping1.play( 1.f, getSoundMag( gc, u.x, u.y ) );
					tu = u;
				}
				tu.sync( u );
				break;
			case LEVELUPDATE:
				if( l == null && player != null )
				{
					l = (Level)m.message;
					scrollToTeamBase( player.team );
				}
				else
				{
					l = (Level)m.message;
				}
				break;
			case BULLETUPDATE:
				Bullet b = (Bullet)m.message;
				Bullet tb = bulletMap.get( b.id );
				if( tb == null )
				{
					bulletMap.put( b.id, b );
					bullets.add( b );
					(Math.random() > .5 ? bullet1 : bullet2).play( 1.f, getSoundMag( gc, b.x, b.y ) * .2f );
					tb = b;
				}
				tb.sync( b );
				break;
			case MOVESUCCESS:
				this.waitingForMoveConfirmation = false;
				break;
			case PLAYERUPDATE:
				Player newPlayer = (Player)m.message;
				if( (player == null || newPlayer.team.id != player.team.id) && l != null )
				{
					scrollToTeamBase( newPlayer.team );
				}
				this.player = newPlayer;
				break;
			case BUILDINGUPDATE:
				if( l != null )
				{
					Building building = (Building)m.message;
					l.buildings.set( building.index, building );
				}
				break;
			case GAMEOVER:
				dsh.message( "postgame", m.message );
				dsh.activate( "postgame", gc );
				return;
			}
		}
		
		if( l == null )
		{
			return;
		}
		
		float scrollSpeed = 20;
		Rectangle screenBounds = getScreenBounds();
		if( scrolly > screenBounds.getMinY() && (input.isKeyDown( Input.KEY_UP ) || input.isKeyDown( Input.KEY_W )) ) scrolly-=scrollSpeed*d;
		if( scrolly+gc.getHeight() < screenBounds.getMaxY() && (input.isKeyDown( Input.KEY_DOWN ) || input.isKeyDown( Input.KEY_S )) ) scrolly+=scrollSpeed*d;
		if( scrollx > screenBounds.getMinX() && (input.isKeyDown( Input.KEY_LEFT ) || input.isKeyDown( Input.KEY_A )) ) scrollx-=scrollSpeed*d;
		if( scrollx+gc.getWidth() < screenBounds.getMaxX() && (input.isKeyDown( Input.KEY_RIGHT ) || input.isKeyDown( Input.KEY_D )) ) scrollx+=scrollSpeed*d;
		
		
		
		for( int i = 0; i < units.size(); i++ )
		{
			Unit u = units.get( i );
			u.clientUpdate( this, d );
			if( !u.alive )
			{
				units.remove( i );
				unitMap.remove( u );
				(Math.random() > .5 ? death1 : death2).play( 1.f, getSoundMag( gc, u.x, u.y ) );
				i--;
				continue;
			}
		}
		
		for( int i = 0; i < bullets.size(); i++ )
		{
			Bullet b = bullets.get( i );
			b.clientUpdate( this, d, gc );
			if( !b.alive )
			{
				bulletMap.remove( b );
				bullets.remove( i );
				i--;
				continue;
			}
		}
		
		dui.update();
	}

	public void render( GameContainer gc, Graphics g )
	{
		g.setAntiAlias( true );
		
		if( l == null )
		{
			return;
		}
		
		g.setColor( Color.white );
		g.fillRect( 0, 0, gc.getWidth(), gc.getHeight() );
		g.setColor( Color.black );
		
		g.pushTransform();
		g.translate( -scrollx, -scrolly );
		
		l.render( g );
		l.renderBuildings( g );
		
		g.setColor( this.waitingForMoveConfirmation ? Color.gray : Color.green );
		g.drawRect( mx * Level.tileSize, my * Level.tileSize, Level.tileSize, Level.tileSize );
		
		for( int i = 0; i < units.size(); i++ )
		{
			Unit u = units.get( i );
			u.render( g, player );
		}
		
		g.setColor( Color.black );
		
		for( int i = 0; i < bullets.size(); i++ )
		{
			Bullet b = bullets.get( i );
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
		if( player != null )
		{
			g.drawString( "Money: " + player.money, 100, 10 );
		}
		
		dui.render( renderer.renderTo( g ) );
		
		//Draw minimap
		g.setClip( gc.getWidth()-200, gc.getHeight()-200, 200, 200 );
		float xScale = 200.f / (l.width*l.tileSize);
		float yScale = 200.f / (l.height*l.tileSize);
		g.pushTransform();
		g.translate( gc.getWidth()-200, gc.getHeight()-200 );
		g.setColor( Color.white );
		g.fillRect( 0, 0, 200, 300 );
		g.pushTransform();
		if( miniMap == null )
		{
			g.scale( xScale, yScale );
			l.render( g );
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
		l.renderBuildings( g );
		for( int i = 0; i < units.size(); i++ )
		{
			Unit u = units.get( i );
			u.renderMinimap( g, player );
		}
		
		g.setColor( Color.blue );
		g.drawRect( scrollx, scrolly, gc.getWidth(), gc.getHeight() );
		
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
	}

	public void onExit()
	{
		if( ci != null )
		{
			ci.stop();
		}
		resetState();
		miniMap = null;
		dui.setEnabled( false );
	}
	
	public void scrollToTeamBase( Team t )
	{
		int destX = 0;
		int destY = 0;
		for( int i = 0; i < l.buildings.size(); i++ )
		{
			Building b = l.buildings.get( i );
			if( b.bt == BuildingType.CENTER && b.t.id == t.id )
			{
				destX = b.x;
				destY = b.y;
				break;
			}
		}
		
		Rectangle screenBounds = getScreenBounds();
		scrollx = DMath.bound( destX-gc.getWidth()/2, screenBounds.getMinX(), screenBounds.getMaxX() );
		scrolly = DMath.bound( destY-gc.getHeight()/2, screenBounds.getMinY(), screenBounds.getMaxY() );
	}
	
	public Rectangle getScreenBounds()
	{
		return new Rectangle( -gc.getWidth()/2, -gc.getHeight()/2, l.width*Level.tileSize + gc.getWidth(), l.height*Level.tileSize + gc.getHeight() );
	}

	public void mousePressed( int button, int x, int y )
	{
		if( x > gc.getWidth()-200 && y > gc.getHeight()-200 )
		{	
			//miniMap
			float minimapX = x - (gc.getWidth()-200);
			float minimapY = y - (gc.getHeight()-200);
			Rectangle screenBounds = getScreenBounds();
			scrollx = DMath.bound( (minimapX / 200.f) * l.width*Level.tileSize - gc.getWidth()/2, screenBounds.getMinX(), screenBounds.getMaxX() );
			scrolly = DMath.bound( (minimapY / 200.f) * l.height*Level.tileSize - gc.getHeight()/2, screenBounds.getMinY(), screenBounds.getMaxY() );
		}	
		else
		{
			if( button == Input.MOUSE_LEFT_BUTTON )
			{
				sx = x + scrollx;
				sy = y + scrolly;
				sx2 = sx;
				sy2 = sy;
				selecting = true;
			} 
			else if( button == Input.MOUSE_RIGHT_BUTTON )
			{
				int tx = (int)((x+scrollx) / Level.tileSize);
				int ty = (int)((y+scrolly) / Level.tileSize);
				ci.sendToServer( new Message( MessageType.SETATTACKPOINT, new Object[]{ new Point2i( tx, ty ), selected } ) );
				this.waitingForMoveConfirmation = true;
				mx = tx;
				my = ty;
			}
		}
	}

	public void mouseReleased( int button, int x, int y )
	{
		if( button == Input.MOUSE_LEFT_BUTTON )
		{
			selected.clear();
			
			float x1 = Math.min( sx, x+scrollx );
			float y1 = Math.min( sy, y+scrolly );
			float x2 = Math.max( sx, x+scrollx );
			float y2 = Math.max( sy, y+scrolly );
			for( Unit u : units )
			{
				u.selected = u.owner.id == this.player.id && u.x > x1 && u.x < x2 && u.y > y1 && u.y < y2;
				if( u.selected )
				{
					selected.add( u.id );
				}
			}
			selecting = false;
		}
	}

	public void mouseDragged( int oldx, int oldy, int newx, int newy )
	{
		if( newx > gc.getWidth()-200 && newy > gc.getHeight()-200 )
		{	
			//miniMap
			float minimapX = newx - (gc.getWidth()-200);
			float minimapY = newy - (gc.getHeight()-200);
			Rectangle screenBounds = getScreenBounds();
			scrollx = DMath.bound( (minimapX / 200.f) * l.width*Level.tileSize - gc.getWidth()/2, screenBounds.getMinX(), screenBounds.getMaxX() );
			scrolly = DMath.bound( (minimapY / 200.f) * l.height*Level.tileSize - gc.getHeight()/2, screenBounds.getMinY(), screenBounds.getMaxY() );
		}	
		else
		{
			if( input.isMouseButtonDown( Input.MOUSE_LEFT_BUTTON ) )
			{
				sx2 = newx+scrollx;
				sy2 = newy+scrolly;
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
				ci.sendToServer( new Message( MessageType.SWITCHTEAMS, player.team ) );
			} else if( e == buildLightUnit )
			{
				ci.sendToServer( new Message( MessageType.BUILDUNIT, UnitType.LIGHT) );
			} else if( e == buildHeavyUnit )
			{
				ci.sendToServer( new Message( MessageType.BUILDUNIT, UnitType.HEAVY) );
			} else if( e == buildSupplyUnit )
			{
				ci.sendToServer( new Message( MessageType.BUILDUNIT, UnitType.SUPPLY) );
			} else if( e == quit )
			{
				dsh.activate( "home", gc );
			} else if( e == returnToGame )
			{
				escapeMenu.setVisible( false );
			}
		}
	}
}
