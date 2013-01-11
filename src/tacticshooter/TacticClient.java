package tacticshooter;

import java.awt.Color;
import java.awt.RenderingHints;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.JOptionPane;
import javax.vecmath.Point2i;

import tacticshooter.Unit.UnitType;

import com.esotericsoftware.minlog.Log;
import com.phyloa.dlib.dui.DButton;
import com.phyloa.dlib.dui.DUI;
import com.phyloa.dlib.dui.DUIElement;
import com.phyloa.dlib.dui.DUIEvent;
import com.phyloa.dlib.dui.DUIListener;
import com.phyloa.dlib.renderer.Graphics2DRenderer;
import com.phyloa.dlib.util.DMath;

public class TacticClient extends Graphics2DRenderer implements MouseListener, MouseMotionListener, DUIListener
{
	ClientInterface ci;
	
	HashMap<Integer, Unit> unitMap = new HashMap<Integer, Unit>();
	ArrayList<Unit> units = new ArrayList<Unit>();
	
	HashMap<Integer, Bullet> bulletMap = new HashMap<Integer, Bullet>();
	ArrayList<Bullet> bullets = new ArrayList<Bullet>();
	
	Level l;
	
	boolean waitingForMoveConfirmation = false;
	int mx = -1, my = -1;
	
	int sx, sy, sw, sh;
	boolean selecting = false;
	ArrayList<Integer> selected = new ArrayList<Integer>();
	
	Player player;
	
	int scrollx = 0;
	int scrolly = 0;
	
	DUI dui;
	DButton switchTeams;
	DButton buildLightUnit;
	DButton buildHeavyUnit;
	DButton buildSupplyUnit;
	
	ParticleSystem ps = new ParticleSystem();
	
	int b1s, b2s;
	
	public void initialize() 
	{
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
		
		size( 1200, 800 );
		
		ci.sendToServer( new Message( MessageType.CLIENTJOIN, null ) );
		canvas.addMouseListener( this );
		canvas.addMouseMotionListener( this );
		
		dui = new DUI( canvas );
		dui.addDUIListener( this );
		
		switchTeams = new DButton( "Switch Teams", 0, getHeight()-50, 100, 50 );
		dui.add( switchTeams );
		
		buildLightUnit = new DButton( "Build Light Unit", 100, getHeight()-50, 100, 50 );
		dui.add( buildLightUnit );
		
		buildHeavyUnit = new DButton( "Build Heavy Unit", 200, getHeight()-50, 100, 50 );
		dui.add( buildHeavyUnit );
		
		buildSupplyUnit = new DButton( "Build Supply Unit", 300, getHeight()-50, 100, 50 );
		dui.add( buildSupplyUnit );
		
		b1s = SoundPlayer.load( "sound/bullet1.wav" );
		b2s = SoundPlayer.load( "sound/bullet2.wav" );
	}

	public void update() 
	{
		g.setRenderingHint( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON );
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
					float dx = b.x - (scrollx+getWidth()/2);
					float dy = b.y - (scrolly+getHeight()/2);
					SoundPlayer.play( Math.random() > .5 ? b1s : b2s, dx, 0, dy );
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
		
		/*
		if( scrolly > 0 && (k.up || (m.y < 40 && m.inside)) ) scrolly-=5;
		if( scrolly+getHeight() < l.height*l.tileSize && (k.down || (m.y > getHeight()-40 && m.inside)) ) scrolly+=5;
		if( scrollx > 0 && (k.left || (m.x < 40 && m.inside)) ) scrollx-=5;
		if( scrollx+getWidth() < l.width*l.tileSize && (k.right || (m.x > getWidth()-40 && m.inside)) ) scrollx+=5;
		*/
		
		if( scrolly > 0 && (k.up || k.w) ) scrolly-=10;
		if( scrolly+getHeight() - 50 < l.height*l.tileSize && (k.down || k.s) ) scrolly+=10;
		if( scrollx > 0 && (k.left || k.a) ) scrollx-=10;
		if( scrollx+getWidth() < l.width*l.tileSize && (k.right || k.d) ) scrollx+=10;
		
		SoundPlayer.setPosition( scrollx, 0, scrolly );
		
		g.setColor( Color.WHITE );
		g.fillRect( 0, 0, getWidth(), getHeight() );
		g.setColor( Color.BLACK );
		
		pushMatrix();
		translate( -scrollx, -scrolly );
		
		l.render( this );
		
		g.setColor( this.waitingForMoveConfirmation ? Color.gray : Color.GREEN );
		g.drawRect( mx * Level.tileSize, my * Level.tileSize, Level.tileSize, Level.tileSize );
		
		for( int i = 0; i < units.size(); i++ )
		{
			Unit u = units.get( i );
			u.clientUpdate( this );
			if( !u.alive )
			{
				units.remove( i );
				unitMap.remove( u );
				i--;
				continue;
			}
			u.render( this );
		}
		
		g.setColor( Color.BLACK );
		
		for( int i = 0; i < bullets.size(); i++ )
		{
			Bullet b = bullets.get( i );
			b.clientUpdate( this );
			if( !b.alive )
			{
				bulletMap.remove( b );
				bullets.remove( i );
				i--;
				continue;
			}
			b.render( g );
		}
		
		ps.update();
		ps.render( this );
		
		if( selecting )
		{
			g.setColor( Color.BLUE );
			g.drawRect( sx, sy, sw, sh );
		}
		
		popMatrix();
		
		g.setColor( Color.red );
		if( player != null )
		{
			g.drawString( "Money: " + player.money, 10, 10 );
		}
		
		dui.update();
		dui.render( this );
	}
	
	public static void main( String[] args )
	{
		TacticClient tc = new TacticClient();
		try
		{
			tc.begin();
		}
		catch( Exception ex )
		{
			ex.printStackTrace();
			JOptionPane.showMessageDialog( tc.container, ex.getMessage() );
		}
	}

	public void mouseClicked( MouseEvent e )
	{
		if( e.getButton() == MouseEvent.BUTTON3 )
		{
			int tx = (e.getX()+scrollx) / Level.tileSize;
			int ty = (e.getY()+scrolly) / Level.tileSize;
			ci.sendToServer( new Message( MessageType.SETATTACKPOINT, new Object[]{ new Point2i( tx, ty ), selected } ) );
			this.waitingForMoveConfirmation = true;
			mx = tx;
			my = ty;
		}
	}

	public void mouseEntered( MouseEvent e )
	{
		
	}

	public void mouseExited( MouseEvent e ) 
	{
		
	}

	public void mousePressed( MouseEvent e )
	{
		if( e.getButton() == MouseEvent.BUTTON1 )
		{
			sx = e.getX() + scrollx;
			sy = e.getY() + scrolly;
			sw = 0;
			sh = 0;
			selecting = true;
		}
	}

	public void mouseReleased( MouseEvent e ) 
	{
		if( e.getButton() == MouseEvent.BUTTON1 )
		{
			selected.clear();
			
			int x1 = Math.min( sx, e.getX()+scrollx );
			int y1 = Math.min( sy, e.getY()+scrolly );
			int x2 = Math.max( sx, e.getX()+scrollx );
			int y2 = Math.max( sy, e.getY()+scrolly );
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

	public void mouseDragged( MouseEvent e )
	{	
		sw = e.getX()+scrollx - sx;
		sh = e.getY()+scrolly - sy;			
	}

	public void mouseMoved( MouseEvent e )
	{
		if( e.getButton() == MouseEvent.BUTTON1 )
		{
			sw = e.getX()+scrollx - sx;
			sh = e.getY()+scrolly - sy;
		}
	}

	public void event( DUIEvent event )
	{
		DUIElement e = event.getElement();
		if( e == switchTeams )
		{
			ci.sendToServer( new Message( MessageType.SWITCHTEAMS, player.team ) );
		} else if( e == buildLightUnit )
		{
			ci.sendToServer( new Message( MessageType.BUILDUNIT, UnitType.LIGHT ) );
		} else if( e == buildHeavyUnit )
		{
			ci.sendToServer( new Message( MessageType.BUILDUNIT, UnitType.HEAVY ) );
		} else if( e == buildSupplyUnit )
		{
			ci.sendToServer( new Message( MessageType.BUILDUNIT, UnitType.SUPPLY ) );
		}
	}
	
	public static class BloodParticle extends Particle
	{
		public BloodParticle( float x, float y, float dx, float dy )
		{
			this.x = x;
			this.y = y;
			float angle = (float)Math.atan2( dy, dx ) + DMath.PIF;
			angle += DMath.randomf( -.5f, .5f );
			this.dx = DMath.cosf( angle ) * 2;
			this.dy = DMath.sinf( angle ) * 2;
			life = 10;
		}

		public void render( Graphics2DRenderer g )
		{
			g.g.setColor( Color.red );
			g.line( x - dx*3, y-dy*3, x+dx*3, y+dy*3 );
		}
		
	}
}
