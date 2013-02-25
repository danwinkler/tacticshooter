package com.danwink.tacticshooter.screens;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Comparator;

import javax.vecmath.Point2i;
import javax.vecmath.Point3f;
import javax.vecmath.Vector3f;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.glu.GLU;
import org.lwjgl.util.glu.Sphere;
import org.newdawn.slick.Color;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.Input;
import org.newdawn.slick.InputListener;
import org.newdawn.slick.Music;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.Sound;
import org.newdawn.slick.geom.Polygon;
import org.newdawn.slick.geom.Rectangle;
import org.newdawn.slick.opengl.shader.ShaderProgram;

import tacticshooter.ExplodeParticle;
import tacticshooter.Building;
import tacticshooter.Building.BuildingType;
import tacticshooter.Bullet;
import tacticshooter.ClientInterface;
import tacticshooter.ClientState;
import tacticshooter.GLLevelRenderer;
import tacticshooter.Level;
import tacticshooter.Level.TileType;
import tacticshooter.AutoTileDrawer;
import tacticshooter.Message;
import tacticshooter.MessageType;
import tacticshooter.ModelHelpers;
import tacticshooter.MusicQueuer;
import tacticshooter.Player;
import tacticshooter.Slick2DEventMapper;
import tacticshooter.Slick2DRenderer;
import tacticshooter.StaticFiles;
import tacticshooter.Team;
import tacticshooter.Unit;
import tacticshooter.Unit.UnitType;

import com.phyloa.dlib.dui.DButton;
import com.phyloa.dlib.dui.DCheckBox;
import com.phyloa.dlib.dui.DPanel;
import com.phyloa.dlib.dui.DText;
import com.phyloa.dlib.dui.DTextBox;
import com.phyloa.dlib.dui.DUI;
import com.phyloa.dlib.dui.DUIElement;
import com.phyloa.dlib.dui.DUIEvent;
import com.phyloa.dlib.dui.DUIListener;
import com.phyloa.dlib.particle.Particle;
import com.phyloa.dlib.particle.ParticleSystem;
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
	DButton buildSaboteurUnit;
	
	DPanel escapeMenu;
	DButton quit;
	DButton returnToGame;
	DButton switchTeams;
	
	DPanel chatPanel;
	DTextBox chatBox;
	DCheckBox teamChat;

    DUI dui;
	
	Input input;
	
	DScreenHandler<GameContainer, Graphics> dsh;
	
	Slick2DRenderer renderer = new Slick2DRenderer();
	
	public GameContainer gc;
	
	Image miniMap;
	
	int bottomOffset = 200;
	
	public float zoom = 700;
	public float targetZoom = 700;
	
	boolean running = false;
	
	public Image bloodTexture;
	Graphics btg;
	
	Image backgroundTexture;
	Image craterTexture;
	Image smoke1;
	
	ArrayList<String> messages = new ArrayList<String>();
	
	public ParticleSystem<MultiplayerGameScreen> ps = new ParticleSystem<MultiplayerGameScreen>();
	
	boolean mapChanged = true;
	
	ArrayList<Vector3f> pings = new ArrayList<Vector3f>();
	
	ArrayList<Integer>[] battleGroups = new ArrayList[10];
	
	public Point2i mouseOnMap = new Point2i();
	
	public Point3f eye = new Point3f();
	Point3f center = new Point3f();
	
	public GLLevelRenderer lr;
	
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
			buildScoutUnit = new DButton( "Build Scout\n3", 0, gc.getHeight()-75, 150, 75 );
			buildLightUnit = new DButton( "Build Light\n10", 150, gc.getHeight()-75, 150, 75 );
			buildHeavyUnit = new DButton( "Build Heavy\n20", 300, gc.getHeight()-75, 150, 75 );
			buildShotgunUnit = new DButton( "Build Shotgun\n15", 450, gc.getHeight()-75, 150, 75 );
			buildSniperUnit = new DButton( "Build Sniper\n20", 600, gc.getHeight()-75, 150, 75 );
			buildSaboteurUnit = new DButton( "Build Saboteur\n" + UnitType.SABOTEUR.price, 750, gc.getHeight()-75, 150, 75 );
			
			
			escapeMenu = new DPanel( gc.getWidth() / 2 - 100, gc.getHeight()/2 - 100, 200, 300 );
			quit = new DButton( "Quit Game", 0, 0, 200, 100 );
			escapeMenu.add( quit );
			returnToGame = new DButton( "Return to Game", 0, 100, 200, 100 );
			escapeMenu.add( returnToGame );
			escapeMenu.setVisible( false );
			
			chatPanel = new DPanel( gc.getWidth()/2-200, gc.getHeight()/2-50, 400, 100 );
			chatBox = new DTextBox( 0, 50, 400, 50 );
			teamChat = new DCheckBox( 10, 10, 30, 30 );
			
			chatPanel.add( new DText( "Team Chat", 50, 25 ) );
			chatPanel.add( teamChat );
			chatPanel.add( chatBox );
			
			chatPanel.setVisible( false );
			
			dui.add( buildScoutUnit );
			dui.add( buildLightUnit );
			dui.add( buildHeavyUnit );
			dui.add( buildShotgunUnit );
			dui.add( buildSniperUnit );
			dui.add( buildSaboteurUnit );
			dui.add( escapeMenu );
			dui.add( chatPanel );
			
			dui.addDUIListener( this );
		}
		dui.setEnabled( true );
		
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
			cs.explode1 = new Sound( "sound" + File.separator + "explode1.wav" );
			
			craterTexture = new Image( "img" + File.separator + "crater1.png" );
			smoke1 = new Image( "img" + File.separator + "smoke1.png" );
		} 
		catch( SlickException e )
		{
			e.printStackTrace();
		}
		
		lr = new GLLevelRenderer( this );
		
		running = true;
	}
	
	long lastClean;
	public void update( GameContainer gc, int delta )
	{
		if( !running ) return;
		if( System.currentTimeMillis()-lastClean > 1000 )
		{
			Runtime runtime = Runtime.getRuntime();
			if( runtime.totalMemory() - runtime.freeMemory() > 400000000 )
			{
				new Thread( new Runnable() {
					public void run()
					{
						System.gc();
					}
				} ).start();
			}
			lastClean = System.currentTimeMillis();
		}
		
		
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
					lr.addUnit( u );
					
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
					
					for( Building b : cs.l.buildings )
					{
						lr.updateBuilding( b );
					}
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
					lr.updateBuilding( building );
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
		
		if( !chatPanel.isVisible() && !escapeMenu.isVisible() )
		{
			float scrollSpeed = .1f*zoom;
			Rectangle screenBounds = getScreenBounds();
			
			boolean scrollup = cs.scrolly > screenBounds.getMinY() && (input.isKeyDown( Input.KEY_UP ) || input.isKeyDown( Input.KEY_W ) || (gc.isFullscreen() && input.getMouseY() < 10 ));
			boolean scrolldown = cs.scrolly < screenBounds.getMaxY() && (input.isKeyDown( Input.KEY_DOWN ) || input.isKeyDown( Input.KEY_S ) || (gc.isFullscreen() && input.getMouseY() > gc.getHeight()-10));
			boolean scrollleft = cs.scrollx > screenBounds.getMinX() && (input.isKeyDown( Input.KEY_LEFT ) || input.isKeyDown( Input.KEY_A ) || (gc.isFullscreen() && input.getMouseX() < 10));
			boolean scrollright = cs.scrollx < screenBounds.getMaxX() && (input.isKeyDown( Input.KEY_RIGHT ) || input.isKeyDown( Input.KEY_D ) || (gc.isFullscreen() && input.getMouseX() > gc.getWidth()-10));
			
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
				lr.removeUnit( u );
				u.renderDead( btg );
				for( int j = 0; j < 10; j++ )
				{
					drawBlood( u.x, u.y );
				}
				if( u.type == UnitType.SABOTEUR )
				{
					cs.explode1.play();
					btg.drawImage( craterTexture, u.x - 16, u.y - 16, u.x + 16, u.y + 16, 0, 0, 32, 32 );
					btg.flush();
					
					for( int j = 0; j < 7; j++ )
					{
						float magmax = DMath.randomf( .4f, 1 );
						float heading = DMath.randomf( 0, DMath.PI2F );
						for( float mag = .1f; mag < 1; mag += .1f )
						{
							float r = DMath.lerp( mag, .5f, 1f );
							float g = DMath.lerp( mag, .5f, .25f );
							float b = DMath.lerp( mag, .5f, 0f );
							ExplodeParticle p = new ExplodeParticle( u.x, u.y, 0, DMath.cosf( heading ) * 25 * mag * magmax, DMath.sinf( heading ) * 25 * mag * magmax, -DMath.randomf( 1, 25 ), 30 );
							p.c = new Color( r, g, b );
							p.friction = .075f;
							p.im = smoke1;
							p.size = (1.f-mag) * magmax * 20;
							ps.add( p );
						}
					}
				}
				else
				{
					(Math.random() > .5 ? cs.death1 : cs.death2).play( 1.f, cs.getSoundMag( gc, u.x, u.y ) );
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
		
		ps.update( d );
		ps.sort( new Comparator<Particle<MultiplayerGameScreen>>() {
			public int compare( Particle<MultiplayerGameScreen> p1, Particle<MultiplayerGameScreen> p2 )
			{
				Vector3f d1 = new Vector3f( p1.pos );
				d1.sub( lr.world.getCamera() );
				
				Vector3f d2 = new Vector3f( p2.pos );
				d2.sub( lr.world.getCamera() );
				
				return d1.lengthSquared() > d2.lengthSquared() ? -1 : 1;
			} 
		} );
		
		zoom += (targetZoom-zoom) * .4f * d;
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
				btg.setColor( Color.black );
				for( Building b : cs.l.buildings )
				{
					btg.pushTransform();
					btg.translate( b.x, b.y );
					btg.drawOval( -b.bt.bu.getRadius(), -b.bt.bu.getRadius(), b.bt.bu.getRadius()*2, b.bt.bu.getRadius()*2 );
					btg.popTransform();
				}
				btg.flush();
				btg.setColor( new Color( 255, 0, 0, 200 ) );
				
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
				
				lr.setupMap();
			}
			catch( SlickException ex )
			{
				ex.printStackTrace();
			}
		}
		
		if( mapChanged )
		{
			//update map
			lr.setupMap();
			
			mapChanged = false;
		}
		
		make3D();
		
		lr.render();
		
		//Hack for finding mouseCoords on surface
		GL11.glDisable( GL11.GL_TEXTURE_2D );
		GL11.glColor3f( 1, 1, 1 );
		GL11.glBegin( GL11.GL_QUADS );
		GL11.glVertex3f( -10000, -10000, 1 );
		GL11.glVertex3f( 10000, -10000, 1 );
		GL11.glVertex3f( 10000, 10000, 1 );
		GL11.glVertex3f( -10000, 10000, 1 );
		GL11.glEnd();
		GL11.glEnable( GL11.GL_TEXTURE_2D );
		
		mouseOnMap = getMouseOnMap( input.getMouseX(), input.getMouseY() );
		
		//Find minimap view box before you go back to 2d
		Polygon poly = new Polygon();
		Point2i a = getMouseOnMap( 1, 1 );
		poly.addPoint( a.x, a.y );
		Point2i b = getMouseOnMap( 1, gc.getHeight()-1 );
		poly.addPoint( b.x, b.y );
		Point2i c = getMouseOnMap( gc.getWidth()-1, gc.getHeight()-1 );
		poly.addPoint( c.x, c.y );
		Point2i d = getMouseOnMap( gc.getWidth()-1, 1 );
		poly.addPoint( d.x, d.y );
		
		GL11.glDisable( GL11.GL_DEPTH_TEST );
		
		if( selecting )
		{
			g.setColor( Color.blue );
			float x1 = Math.min( sx, sx2 );
			float y1 = Math.min( sy, sy2 );
			float x2 = Math.max( sx, sx2 );
			float y2 = Math.max( sy, sy2 );
			g.drawRect( x1, y1, x2-x1, y2-y1 );
		}
		
		make2D();
		
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
		
		g.pushTransform();
		g.scale( xScale, yScale );
		g.draw( poly );
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
	
	public void make2D() {
		GL11.glDisable (GL11.GL_DEPTH_TEST );
	    GL11.glMatrixMode( GL11.GL_PROJECTION );
	    GL11.glLoadIdentity();
	    GL11.glOrtho( 0.0f, gc.getWidth(), gc.getHeight(), 0.0f, 0.0f, 1.0f );

	    GL11.glMatrixMode( GL11.GL_MODELVIEW );
	    GL11.glLoadIdentity();
	}

	public void make3D() {
		GL11.glEnable( GL11.GL_DEPTH_TEST );
	    GL11.glMatrixMode( GL11.GL_PROJECTION );
	    GL11.glLoadIdentity(); // Reset The Projection Matrix
	    GLU.gluPerspective( 45.0f, ((float) gc.getWidth() / (float) gc.getHeight()), 5, 5000.0f ); // Calculate The Aspect Ratio Of The Window

	    GL11.glMatrixMode( GL11.GL_MODELVIEW );
	    GL11.glLoadIdentity();
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
		cs.scrollx = DMath.bound( destX, screenBounds.getMinX(), screenBounds.getMaxX() );
		cs.scrolly = DMath.bound( destY, screenBounds.getMinY(), screenBounds.getMaxY() );
	}
	
	public static Color bloodColor = new Color( 255, 0, 0 );
	public void drawBlood( float x, float y )
	{
		x += DMath.randomf( -8, 8 );
		y += DMath.randomf( -8, 8 );
		//btg.fillOval( x-2, y-2, 4, 4 );
		btg.drawImage( smoke1, x-4, y-4, x+4, y+4, 0, 0, 64, 64, bloodColor );
		btg.flush();
	}
	
	public Rectangle getScreenBounds()
	{
		return new Rectangle( 0, 0, cs.l.width*Level.tileSize, cs.l.height*Level.tileSize );
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
					cs.scrollx = DMath.bound( (minimapX / 200.f) * cs.l.width*Level.tileSize, screenBounds.getMinX(), screenBounds.getMaxX() );
					cs.scrolly = DMath.bound( (minimapY / 200.f) * cs.l.height*Level.tileSize, screenBounds.getMinY(), screenBounds.getMaxY() );
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
			Point2i mapPos = mouseOnMap;
			x = mapPos.x;
			y = mapPos.y;
			if( button == Input.MOUSE_LEFT_BUTTON )
			{
				sx = x;
				sy = y;
				sx2 = sx;
				sy2 = sy;
				selecting = true;
			} 
			else if( button == Input.MOUSE_RIGHT_BUTTON )
			{
				int tx = (int)((x) / Level.tileSize);
				int ty = (int)((y) / Level.tileSize);
				ci.sendToServer( new Message( input.isKeyDown( Input.KEY_LCONTROL ) ? MessageType.SETATTACKPOINTCONTINUE : MessageType.SETATTACKPOINT, new Object[]{ new Point2i( tx, ty ), cs.selected } ) );
				this.waitingForMoveConfirmation = true;
				mx = tx;
				my = ty;
			}
		}
	}

	public void mouseReleased( int button, int x, int y )
	{
		Point2i mapPos = mouseOnMap;
		x = mapPos.x;
		y = mapPos.y;
		if( button == Input.MOUSE_LEFT_BUTTON && selecting )
		{
			cs.selected.clear();
			
			float x1 = Math.min( sx, x );
			float y1 = Math.min( sy, y );
			float x2 = Math.max( sx, x );
			float y2 = Math.max( sy, y );
			
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
				cs.scrollx = DMath.bound( (minimapX / 200.f) * cs.l.width*Level.tileSize, screenBounds.getMinX(), screenBounds.getMaxX() );
				cs.scrolly = DMath.bound( (minimapY / 200.f) * cs.l.height*Level.tileSize, screenBounds.getMinY(), screenBounds.getMaxY() );
			}
		}	
		else
		{
			if( input.isMouseButtonDown( Input.MOUSE_LEFT_BUTTON ) )
			{
				Point2i mapPos = mouseOnMap;
				newx = mapPos.x;
				newy = mapPos.y;
				sx2 = newx;
				sy2 = newy;
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
	public void mouseWheelMoved( int a )
	{
		targetZoom += targetZoom * .003f * -a;
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
	
	public Point2i getMouseOnMap( int x, int y )
	{
		int winX = x;
		int winY = gc.getHeight() - y;
		FloatBuffer winZ = BufferUtils.createFloatBuffer(1); //the x coordinate of the click, will be calculated
		FloatBuffer pos = BufferUtils.createFloatBuffer(3); // the final position of the click
		FloatBuffer modelview = BufferUtils.createFloatBuffer(16); 
		FloatBuffer projection = BufferUtils.createFloatBuffer(16); 
		IntBuffer viewport = BufferUtils.createIntBuffer(16); 

		GL11.glGetInteger( GL11.GL_VIEWPORT, viewport );
		GL11.glGetFloat( GL11.GL_MODELVIEW_MATRIX, modelview );
		GL11.glGetFloat( GL11.GL_PROJECTION_MATRIX, projection );

		GL11.glReadPixels( winX, winY, 1, 1, GL11.GL_DEPTH_COMPONENT, GL11.GL_FLOAT, winZ ); //calculate the Z Coordinate of the Click
		GLU.gluUnProject((float)(winX), (float)(winY), (float)(winZ.get(0)), modelview, projection, viewport, pos);
		
		return new Point2i( (int)pos.get( 0 ), (int)pos.get( 1 ) );
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
			if( chatPanel.isVisible() && chatBox.getText().trim().length() > 0 )
			{
				chatPanel.setVisible( false );
				ci.sendToServer( new Message( MessageType.MESSAGE, (teamChat.checked ? "/team " : "") + chatBox.getText().trim() ) );
				chatBox.setText( "" );
			}
			else
			{
				chatBox.setText( "" );
				chatPanel.setVisible( true );
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
		this.ci = (ClientInterface)o;
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
			else if( e == buildSaboteurUnit )
			{
				ci.sendToServer( new Message( MessageType.BUILDUNIT, UnitType.SABOTEUR ) );
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
