package com.danwink.tacticshooter.screens;

import java.io.File;
import java.io.IOException;
import java.nio.BufferOverflowException;
import java.util.ArrayList;

import javax.vecmath.Point2i;
import javax.vecmath.Vector3f;

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
import org.newdawn.slick.opengl.shader.ShaderProgram;
import org.newdawn.slick.particles.ParticleSystem;

import tacticshooter.Building;
import tacticshooter.Building.BuildingType;
import tacticshooter.Bullet;
import tacticshooter.ClientInterface;
import tacticshooter.ClientNetworkInterface;
import tacticshooter.ClientState;
import tacticshooter.Level;
import tacticshooter.Level.TileType;
import tacticshooter.AutoTileDrawer;
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
	
	DButton buildScoutUnit;
	DButton buildLightUnit;
	DButton buildHeavyUnit;
	DButton buildShotgunUnit;
	DButton buildSniperUnit;
	
	DPanel escapeMenu;
	DButton quit;
	DButton returnToGame;
	DButton switchTeams;
	
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
	
	Image wallTexture;
	Image bloodTexture;
	Graphics btg;
	
	Image backgroundTexture;
	
	ArrayList<String> messages = new ArrayList<String>();
	
	ShaderProgram shader;
	
	boolean mapChanged = true;
	
	ArrayList<Vector3f> pings = new ArrayList<Vector3f>();
	
	ArrayList<Integer>[] battleGroups = new ArrayList[10];
	
	public void onActivate( GameContainer gc, DScreenHandler<GameContainer, Graphics> dsh )
	{
		this.dsh = dsh;
		this.gc = gc;
		
		for( int i = 0; i < 10; i++ )
		{
			battleGroups[i] = new ArrayList<Integer>();
		}
		
		if( dui == null )
		{
			dui = new DUI( new Slick2DEventMapper( gc.getInput() ) );
			buildScoutUnit = new DButton( "Build Scout Unit\n3", 0, gc.getHeight()-100, 200, 100 );
			buildLightUnit = new DButton( "Build Light Unit\n10", 200, gc.getHeight()-100, 200, 100 );
			buildHeavyUnit = new DButton( "Build Heavy Unit\n20", 400, gc.getHeight()-100, 200, 100 );
			buildShotgunUnit = new DButton( "Build Shotgun Unit\n15", 600, gc.getHeight()-100, 200, 100 );
			buildSniperUnit = new DButton( "Build Sniper Unit\n20", 800, gc.getHeight()-100, 200, 100 );
			
			
			escapeMenu = new DPanel( gc.getWidth() / 2 - 100, gc.getHeight()/2 - 100, 200, 300 );
			quit = new DButton( "Quit Game", 0, 0, 200, 100 );
			escapeMenu.add( quit );
			returnToGame = new DButton( "Return to Game", 0, 100, 200, 100 );
			escapeMenu.add( returnToGame );
			escapeMenu.setVisible( false );
			
			chatBox = new DTextBox( gc.getWidth()/2-200, gc.getHeight()/2-50, 400, 100 );
			chatBox.setVisible( false );
			
			dui.add( buildScoutUnit );
			dui.add( buildLightUnit );
			dui.add( buildHeavyUnit );
			dui.add( buildShotgunUnit );
			dui.add( buildSniperUnit );
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
			cs.bullet1 = new Sound( "sound" + File.separator + "bullet1.wav" );
			cs.bullet2 = new Sound( "sound" + File.separator + "bullet2.wav" );
			cs.ping1 = new Sound( "sound" + File.separator + "ping1.wav" );
			cs.death1 = new Sound( "sound" + File.separator + "death1.wav" );
			cs.death2 = new Sound( "sound" + File.separator + "death2.wav" );
			cs.hit1 = new Sound( "sound" + File.separator + "hit1.wav" );
		} 
		catch( SlickException e )
		{
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
					cs.l.loadTextures();
					Unit.loadTextures( cs.l );
				}
				else
				{
					cs.l = (Level)m.message;
					cs.l.loadTextures();
					Unit.loadTextures( cs.l );
				}
				break;
			case BULLETUPDATE:
				Bullet b = (Bullet)m.message;
				cs.bullets.add( b );
				(Math.random() > .5 ? cs.bullet1 : cs.bullet2).play( 1.f, cs.getSoundMag( gc, b.loc.x, b.loc.y ) * .2f );
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
					for( int i = 0; i < cs.l.buildings.size(); i++ )
					{
						Building bt = cs.l.buildings.get( i );
						if( bt.id == building.id )
						{
							cs.l.buildings.set( i, building );
						}
					}
				}
				break;
			case PLAYERLIST:
				cs.players = (Player[])m.message;
				break;
			case MESSAGE:
				String mess = (String)m.message;
				int lineLength = 60;
				do
				{
					String p1 = mess.substring( 0, Math.min( lineLength, mess.length() ) );
					mess = mess.substring( Math.min( lineLength, mess.length() ), mess.length() );
					messages.add( p1 + (mess.length() > 0 ? "-" : "") );
				} while( mess.length() > 0 );
				break;
			case TILEUPDATE:
				Object[] arr = (Object[])m.message;
				int tx = (Integer)arr[0];
				int ty = (Integer)arr[1];
				TileType change = (TileType)arr[2];
				cs.l.tiles[tx][ty] = change;
				mapChanged = true;
				break;
			case PINGMAP:
				Point2i pingLoc = (Point2i)m.message;
				pings.add( new Vector3f( pingLoc.x, pingLoc.y, 100 ) );
				cs.ping1.play( 2.f, 1.f );
				break;
			case GAMEOVER:
				dsh.message( "postgame", m.message );
				dsh.activate( "postgame", gc, StaticFiles.getUpMenuOut(), StaticFiles.getUpMenuIn() );
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
				cs.selected.remove( (Object)u.id );
				(Math.random() > .5 ? cs.death1 : cs.death2).play( 1.f, cs.getSoundMag( gc, u.x, u.y ) );
				u.renderDead( btg );
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
				cs.bullets.remove( i );
				i--;
				continue;
			}
		}
		
		for( int i = 0; i < pings.size(); i++ )
		{
			Vector3f v = pings.get( i );
			v.z -= d;
			if( v.z < 0 )
			{
				pings.remove( i );
				i--;
			}
		}
		
		dui.update();
		
		if( cs.l != null && miniMap == null )
		{
			try
			{
				float xScale = 200.f / (cs.l.width*Level.tileSize);
				float yScale = 200.f / (cs.l.height*Level.tileSize);
				miniMap = new Image( 200, 200 );
				Graphics mg = miniMap.getGraphics();
				mg.scale( xScale, yScale );
				cs.l.renderFloor( mg );
				cs.l.render( mg );
				mg.flush();
				
			} catch( SlickException e )
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public void render( GameContainer gc, Graphics g )
	{
		if( !running ) return;
		
		if( cs.l == null )
		{
			return;
		} else if( bloodTexture == null )
		{
			try
			{
				bloodTexture = new Image( cs.l.width * Level.tileSize, cs.l.height * Level.tileSize );
				btg = bloodTexture.getGraphics();
				cs.l.renderFloor( btg );
				btg.flush();
				btg.setColor( new Color( 255, 0, 0, 200 ) );
				wallTexture = new Image( cs.l.width * Level.tileSize, cs.l.height * Level.tileSize );
				
				Graphics wtg = wallTexture.getGraphics();
				wtg.clearAlphaMap();
				cs.l.render( wtg );
				wtg.flush();
				
				backgroundTexture = new Image( gc.getWidth() + Level.tileSize*2, gc.getHeight() + Level.tileSize*2 );
				Graphics bgg = backgroundTexture.getGraphics();
				for( int y = 0; y < gc.getHeight() + Level.tileSize*2; y += Level.tileSize )
				{
					for( int x = 0; x < gc.getWidth() + Level.tileSize*2; x += Level.tileSize )
					{	
						bgg.pushTransform();
						bgg.translate( x, y );
						AutoTileDrawer.draw( bgg, cs.l.wall, Level.tileSize, 0, true, true, true, true, true, true, true, true );
						bgg.popTransform();
					}
				}
				
			} catch( SlickException e )
			{
				e.printStackTrace();
			}
		}
		
		if( mapChanged )
		{
			try
			{	
				Graphics wtg = wallTexture.getGraphics();
				wtg.clear();
				cs.l.render( wtg );
				wtg.flush();
			} catch( SlickException e )
			{
				e.printStackTrace();
			}
			mapChanged = false;
		}
		
		g.drawImage( backgroundTexture, -Level.tileSize-(cs.scrollx - ((int)(cs.scrollx/Level.tileSize))*Level.tileSize), -Level.tileSize-(cs.scrolly - ((int)(cs.scrolly/Level.tileSize)*Level.tileSize)) );
		
		g.setColor( Color.black );
		
		g.pushTransform();
		g.translate( -cs.scrollx, -cs.scrolly );
		
		g.drawImage( bloodTexture, 0, 0 );
		cs.l.renderBuildings( g );
		
		g.setColor( this.waitingForMoveConfirmation ? Color.gray : Color.green );
		g.drawRect( mx * Level.tileSize, my * Level.tileSize, Level.tileSize, Level.tileSize );
		
		for( int i = 0; i < cs.units.size(); i++ )
		{
			Unit u = cs.units.get( i );
			u.renderBody( g, cs.player );
		}
		
		g.drawImage( wallTexture, 0, 0 );
		
		g.setColor( Color.black );
		
		for( int i = 0; i < cs.bullets.size(); i++ )
		{
			Bullet b = cs.bullets.get( i );
			b.render( g );
		}
		
		for( int i = 0; i < cs.units.size(); i++ )
		{
			Unit u = cs.units.get( i );
			u.render( g, cs.player, input.getMouseX() + cs.scrollx, input.getMouseY() + cs.scrolly, cs.l );
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
					g.drawString( messages.get( i ), 10, 300 - (messages.size()-1-i)*25 );
				}
			}
		}
		
		dui.render( renderer.renderTo( g ) );
		
		//Draw minimap
		g.setClip( gc.getWidth()-200, gc.getHeight()-200, 200, 200 );
		float xScale = 200.f / (cs.l.width*Level.tileSize);
		float yScale = 200.f / (cs.l.height*Level.tileSize);
		g.pushTransform();
		g.translate( gc.getWidth()-200, gc.getHeight()-200 );
		g.setColor( Color.white );
		g.fillRect( 0, 0, 200, 300 );
		g.pushTransform();
		
		if( miniMap != null )
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
		
		g.popTransform();
		
		for( int i = 0; i < pings.size(); i++ )
		{
			Vector3f v = pings.get( i );
			float size = (v.z / 100.f) * 10;
			g.setColor( Color.pink );
			g.fillOval( v.x - size/2, v.y - size/2, size, size );
			g.setColor( Color.black );
			g.drawOval( v.x - size/2, v.y - size/2, size, size );
		}
		
		g.setColor( Color.blue );
		g.drawRect( cs.scrollx*xScale, cs.scrolly * yScale, gc.getWidth() * xScale, gc.getHeight() * yScale );
		
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
			g.fillRect( gc.getWidth()/2 - 100, gc.getHeight()/2 + 200, 200, gc.getHeight()/2-100 );
		}
		
		if( input.isKeyDown( Input.KEY_TAB ) && cs.players != null )
		{
			g.setColor( new Color( 128, 128, 128, 200 ) );
			g.fillRect( gc.getWidth()/2 - 400, gc.getHeight()/2-300, 800, 600 );
			g.setColor( Color.black );
			g.drawRect( gc.getWidth()/2 - 400, gc.getHeight()/2-300, 800, 600 );
			int red = 0, green = 0;
			for( int i = 0; i < cs.players.length; i++ )
			{
				Player p = cs.players[i];
				boolean teamRed = p.team.id == Team.a.id;
				g.drawString( p.name + " - " + (teamRed ? "RED" : "GREEN"), gc.getWidth()/2 - (teamRed ? 390 : -10), gc.getHeight()/2-270 + (teamRed ? red : green)*30 );
				if( p.team.id == Team.a.id )
				{
					red++;
				}
				else 
				{
					green++;
				}
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
		wallTexture = null;
		mapChanged = true;
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
		btg.setColor( new Color( 255, 0, 0, 200 ) );
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
			if( input.isKeyDown( Input.KEY_LCONTROL ) )
			{
				ci.sendToServer( new Message( MessageType.MESSAGE, "/ping " + (input.getMouseX()-(gc.getWidth()-200)) + " " + (input.getMouseY()-(gc.getHeight()-200)) ) );
			}
			else
			{
				if( button == Input.MOUSE_LEFT_BUTTON )
				{
					//miniMap
					float minimapX = x - (gc.getWidth()-200);
					float minimapY = y - (gc.getHeight()-200);
					Rectangle screenBounds = getScreenBounds();
					cs.scrollx = DMath.bound( (minimapX / 200.f) * cs.l.width*Level.tileSize - gc.getWidth()/2, screenBounds.getMinX(), screenBounds.getMaxX() );
					cs.scrolly = DMath.bound( (minimapY / 200.f) * cs.l.height*Level.tileSize - gc.getHeight()/2, screenBounds.getMinY(), screenBounds.getMaxY() );
				}
				else if( button == Input.MOUSE_RIGHT_BUTTON )
				{
					int tx = cs.l.getTileX( ((x - (gc.getWidth()-200.f)) / 200.f) * cs.l.width*Level.tileSize );
					int ty = cs.l.getTileY( ((y - (gc.getHeight()-200.f)) / 200.f) * cs.l.height*Level.tileSize );
					ci.sendToServer( new Message( input.isKeyDown( Input.KEY_LCONTROL ) ? MessageType.SETATTACKPOINTCONTINUE : MessageType.SETATTACKPOINT, new Object[]{ new Point2i( tx, ty ), cs.selected } ) );
					this.waitingForMoveConfirmation = true;
					mx = tx;
					my = ty;
				}
			}
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
				ci.sendToServer( new Message( input.isKeyDown( Input.KEY_LCONTROL ) ? MessageType.SETATTACKPOINTCONTINUE : MessageType.SETATTACKPOINT, new Object[]{ new Point2i( tx, ty ), cs.selected } ) );
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
			
			if( x2 - x1 > 2 || y2 - y1 > 2 )
			{
				for( Unit u : cs.units )
				{
					u.selected = u.owner.id == this.cs.player.id && u.x > x1 && u.x < x2 && u.y > y1 && u.y < y2;
					if( u.selected )
					{
						cs.selected.add( u.id );
					}
				}
			}
			else
			{
				for( Unit u : cs.units )
				{
					float dx = x2 - u.x;
					float dy = y2 - u.y;
					if( u.owner.id == this.cs.player.id && dx*dx + dy*dy < u.radius * u.radius )
					{
						u.selected = true;
						cs.selected.add( u.id );
						break;
					}
					else
					{
						u.selected = false;
					}
				}
			}
			selecting = false;
		}
	}

	public void mouseDragged( int oldx, int oldy, int newx, int newy )
	{
		if( newx > gc.getWidth()-200 && newy > gc.getHeight()-200 && !selecting )
		{	
			if( input.isMouseButtonDown( Input.MOUSE_LEFT_BUTTON ) && !input.isKeyDown( Input.KEY_LCONTROL ) )
			{
				//miniMap
				float minimapX = newx - (gc.getWidth()-200);
				float minimapY = newy - (gc.getHeight()-200);
				Rectangle screenBounds = getScreenBounds();
				cs.scrollx = DMath.bound( (minimapX / 200.f) * cs.l.width*Level.tileSize - gc.getWidth()/2, screenBounds.getMinX(), screenBounds.getMaxX() );
				cs.scrolly = DMath.bound( (minimapY / 200.f) * cs.l.height*Level.tileSize - gc.getHeight()/2, screenBounds.getMinY(), screenBounds.getMaxY() );
			}
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
			if( chatBox.isVisible() && chatBox.getText().trim().length() > 0 )
			{
				chatBox.setVisible( false );
				ci.sendToServer( new Message( MessageType.MESSAGE, chatBox.getText().trim() ) );
				chatBox.setText( "" );
			}
			else
			{
				chatBox.setText( "" );
				chatBox.setVisible( true );
				dui.setFocus( chatBox );
			}
		}
		else if( keyCode >= Input.KEY_1 && keyCode <= Input.KEY_0 )
		{
			ArrayList<Integer> bg = battleGroups[keyCode-Input.KEY_1];
			if( input.isKeyPressed( Input.KEY_LCONTROL ) )
			{
				bg.clear();
				for( int i = 0; i < cs.selected.size(); i++ )
				{
					bg.add( cs.selected.get( i ) );
				}
			}
			else
			{
				//Deselect all units
				for( int i = 0; i < cs.selected.size(); i++ )
				{
					cs.unitMap.get( cs.selected.get( i ) ).selected = false;
				}
				cs.selected.clear();
				
				//Select units in battlegroup
				for( int i = 0; i < bg.size(); i++ )
				{
					Unit u = cs.unitMap.get( bg.get( i ) );
					if( u != null && u.alive )
					{
						u.selected = true;
						cs.selected.add( bg.get( i ) );
					}
				}
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
			}
			else if( e == buildScoutUnit )
			{
				ci.sendToServer( new Message( MessageType.BUILDUNIT, UnitType.SCOUT ) );
			} 
			else if( e == buildLightUnit )
			{
				ci.sendToServer( new Message( MessageType.BUILDUNIT, UnitType.LIGHT ) );
			} 
			else if( e == buildHeavyUnit )
			{
				ci.sendToServer( new Message( MessageType.BUILDUNIT, UnitType.HEAVY ) );
			} 
			else if( e == buildShotgunUnit )
			{
				ci.sendToServer( new Message( MessageType.BUILDUNIT, UnitType.SHOTGUN ) );
			} 
			else if( e == buildSniperUnit )
			{
				ci.sendToServer( new Message( MessageType.BUILDUNIT, UnitType.SNIPER ) );
			}
			else if( e == quit )
			{
				running = false;
				escapeMenu.setVisible( false );
				dsh.activate( "home", gc, StaticFiles.getUpMenuOut(), StaticFiles.getUpMenuIn() );
			} else if( e == returnToGame )
			{
				escapeMenu.setVisible( false );
			}
		}
	}
}
