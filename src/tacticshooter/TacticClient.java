package tacticshooter;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;
import java.util.HashMap;

import javax.vecmath.Point2i;

import com.esotericsoftware.minlog.Log;
import com.phyloa.dlib.dui.DButton;
import com.phyloa.dlib.dui.DUI;
import com.phyloa.dlib.dui.DUIElement;
import com.phyloa.dlib.dui.DUIEvent;
import com.phyloa.dlib.dui.DUIListener;
import com.phyloa.dlib.renderer.Graphics2DRenderer;

import de.lessvoid.nifty.Nifty;
import de.lessvoid.nifty.java2d.input.InputSystemAwtImpl;
import de.lessvoid.nifty.java2d.renderer.GraphicsWrapper;
import de.lessvoid.nifty.java2d.renderer.RenderDeviceJava2dImpl;
import de.lessvoid.nifty.sound.SoundSystem;
import de.lessvoid.nifty.tools.TimeProvider;

public class TacticClient extends Graphics2DRenderer implements MouseListener, MouseMotionListener
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
	
	Nifty nifty;
	
	public void initialize() 
	{
		Log.set( Log.LEVEL_TRACE );
		ci = new ClientNetworkInterface( "localhost" );
		
		size( 1200, 800 );
		
		ci.sendToServer( new Message( MessageType.CLIENTJOIN, null ) );
		canvas.addMouseListener( this );
		canvas.addMouseMotionListener( this );
		
		nifty = new Nifty( new RenderDeviceJava2dImpl( new GraphicsWrapper() {
			public Graphics2D getGraphics2d()
			{
				return TacticClient.this.g;
			}

			public int getHeight()
			{
				return TacticClient.this.getHeight();
			}

			public int getWidth()
			{
				return TacticClient.this.getWidth();
			} } ), null, new InputSystemAwtImpl(), new TimeProvider() );
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
		
		if( scrolly > 0 && (k.up || k.w) ) scrolly-=5;
		if( scrolly+getHeight() - 50 < l.height*l.tileSize && (k.down || k.s) ) scrolly+=5;
		if( scrollx > 0 && (k.left || k.a) ) scrollx-=5;
		if( scrollx+getWidth() < l.width*l.tileSize && (k.right || k.d) ) scrollx+=5;
		
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
	}
	
	public static void main( String[] args )
	{
		TacticClient tc = new TacticClient();
		tc.begin();
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

	public void mouseEntered(MouseEvent arg0) {
		
	}

	public void mouseExited(MouseEvent arg0) {
		
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
}
