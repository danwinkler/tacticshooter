package tacticshooter;

import java.util.ArrayList;
import java.util.HashMap;

import javax.vecmath.Point2i;

import org.newdawn.slick.AppGameContainer;
import org.newdawn.slick.BasicGame;
import org.newdawn.slick.Color;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.Input;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.Sound;
import org.newdawn.slick.geom.Rectangle;
import tacticshooter.Unit.UnitType;

import com.aem.sticky.StickyListener;
import com.aem.sticky.button.Button;
import com.aem.sticky.button.SimpleButton;
import com.aem.sticky.button.events.ClickListener;
import com.esotericsoftware.minlog.Log;
import com.phyloa.dlib.dui.DUI;

public class TacticClient extends BasicGame
{
	public TacticClient( String title )
	{
		super( title );
	}

	ClientInterface ci;
	
	HashMap<Integer, Unit> unitMap = new HashMap<Integer, Unit>();
	ArrayList<Unit> units = new ArrayList<Unit>();
	
	HashMap<Integer, Bullet> bulletMap = new HashMap<Integer, Bullet>();
	ArrayList<Bullet> bullets = new ArrayList<Bullet>();
	
	Level l;
	
	boolean waitingForMoveConfirmation = false;
	float mx = -1, my = -1;
	
	float sx, sy, sx2, sy2;
	boolean selecting = false;
	ArrayList<Integer> selected = new ArrayList<Integer>();
	
	Player player;
	
	float scrollx = 0;
	float scrolly = 0;
	
	DUI dui;
	Button switchTeams;
	Button buildLightUnit;
	Button buildHeavyUnit;
	Button buildSupplyUnit;

    StickyListener listener;
	
	Sound bullet1, bullet2, ping1, death1, death2;
	
	Input input;
	
	float soundFadeDist = 1000;
	
	public void init( GameContainer gc ) throws SlickException
	{
		input = gc.getInput();
		Log.set( Log.LEVEL_TRACE );
		
		String[] addressesToTry = { "localhost", "triggerly.com" };
		
		for( String s : addressesToTry )
		{
			try
			{
				ci = new ClientNetworkInterface( s );
			} catch( Exception e )
			{
				continue;
			}
			break;
		}
		
		ci.sendToServer( new Message( MessageType.CLIENTJOIN, null ) );
		
		listener = new StickyListener();
		gc.getInput().addListener( listener );
		
		Image button = new Image( "img/buttontemp.png" );
		
		bullet1 = new Sound( "sound/bullet1.wav" );
		bullet2 = new Sound( "sound/bullet2.wav" );
		ping1 = new Sound( "sound/ping1.wav" );
		death1 = new Sound( "sound/death1.wav" );
		death2 = new Sound( "sound/death2.wav" );
		
		switchTeams = new SimpleButton( new Rectangle(0, gc.getHeight()-100, 200, 100), button, button, ping1 );
		buildLightUnit = new SimpleButton( new Rectangle(200, gc.getHeight()-100, 200, 100), button, button, ping1 );
		buildHeavyUnit = new SimpleButton( new Rectangle(400, gc.getHeight()-100, 200, 100), button, button, ping1 );
		buildSupplyUnit = new SimpleButton( new Rectangle(600, gc.getHeight()-100, 200, 100), button, button, ping1 );
		
		switchTeams.addListener( new ClickListener() {
			public void onClick( Button b, float mx, float my )
			{
				ci.sendToServer( new Message( MessageType.SWITCHTEAMS, player.team ) );
			}
			public void onDoubleClick( Button b, float mx, float my )
			{
				
			}
			public void onRightClick( Button b, float mx, float my )
			{
				
			}
		});
		buildLightUnit.addListener( new ClickListener() {
			public void onClick( Button b, float mx, float my )
			{
				ci.sendToServer( new Message( MessageType.BUILDUNIT, UnitType.LIGHT) );
			}
			public void onDoubleClick( Button b, float mx, float my )
			{
				
			}
			public void onRightClick( Button b, float mx, float my )
			{
				
			}
		});
		buildHeavyUnit.addListener( new ClickListener() {
			public void onClick( Button b, float mx, float my )
			{
				ci.sendToServer( new Message( MessageType.BUILDUNIT, UnitType.HEAVY) );
			}
			public void onDoubleClick( Button b, float mx, float my )
			{
				
			}
			public void onRightClick( Button b, float mx, float my )
			{
				
			}
		});
		buildSupplyUnit.addListener( new ClickListener() {
			public void onClick( Button b, float mx, float my )
			{
				ci.sendToServer( new Message( MessageType.BUILDUNIT, UnitType.SUPPLY) );
			}
			public void onDoubleClick( Button b, float mx, float my )
			{
				
			}
			public void onRightClick( Button b, float mx, float my )
			{
				
			}
		});
	}
	
	public void update( GameContainer gc, int delta ) throws SlickException
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
				l = (Level)m.message;
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
				this.player = (Player)m.message;
				break;
			case BUILDINGUPDATE:
				if( l != null )
				{
					Building building = (Building)m.message;
					l.buildings.set( building.index, building );
				}
			}
		}
		
		if( l == null )
		{
			return;
		}
		
		float scrollSpeed = 20;
		
		if( scrolly > 0 && (input.isKeyDown( Input.KEY_UP ) || input.isKeyDown( Input.KEY_W )) ) scrolly-=scrollSpeed*d;
		if( scrolly+gc.getHeight() - 50 < l.height*l.tileSize && (input.isKeyDown( Input.KEY_DOWN ) || input.isKeyDown( Input.KEY_S )) ) scrolly+=scrollSpeed*d;
		if( scrollx > 0 && (input.isKeyDown( Input.KEY_LEFT ) || input.isKeyDown( Input.KEY_A )) ) scrollx-=scrollSpeed*d;
		if( scrollx+gc.getWidth() < l.width*l.tileSize && (input.isKeyDown( Input.KEY_RIGHT ) || input.isKeyDown( Input.KEY_D )) ) scrollx+=scrollSpeed*d;
		
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
			b.clientUpdate( this, d );
			if( !b.alive )
			{
				bulletMap.remove( b );
				bullets.remove( i );
				i--;
				continue;
			}
		}
		
		switchTeams.update( gc, delta );
		buildLightUnit.update( gc, delta );
		buildHeavyUnit.update( gc, delta );
		buildSupplyUnit.update( gc, delta );
	}

	public void render( GameContainer gc, Graphics g ) throws SlickException 
	{
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
		
		g.setColor( this.waitingForMoveConfirmation ? Color.gray : Color.green );
		g.drawRect( mx * Level.tileSize, my * Level.tileSize, Level.tileSize, Level.tileSize );
		
		for( int i = 0; i < units.size(); i++ )
		{
			Unit u = units.get( i );
			u.render( g );
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
		
		g.setColor( Color.red );
		if( player != null )
		{
			g.drawString( "Money: " + player.money, 100, 10 );
		}
		
		switchTeams.render( gc, g );
		buildLightUnit.render( gc, g );
		buildHeavyUnit.render( gc, g );
		buildSupplyUnit.render( gc, g );
	}
	
	public static void main( String[] args ) throws SlickException
	{
		AppGameContainer app = new AppGameContainer( new TacticClient( "Tactic Client" ) );
		app.setDisplayMode(1024, 768, false);
		app.start();
	}
	
	public float getSoundMag( GameContainer gc, float x, float y )
	{
		float dx = (scrollx+gc.getWidth()/2) - x;
		float dy = (scrolly+gc.getHeight()/2) - y;
		return (float)((soundFadeDist - Math.sqrt( dx*dx+dy*dy )) / soundFadeDist);
	}

	public void mousePressed( int button, int x, int y )
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
		if( input.isMouseButtonDown( Input.MOUSE_LEFT_BUTTON ) )
		{
			sx2 = newx+scrollx;
			sy2 = newy+scrolly;
		}
	}
}
