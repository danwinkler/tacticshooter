package tacticshooter;

import java.awt.Color;
import java.awt.RenderingHints;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;
import java.util.HashMap;

import javax.vecmath.Point2i;

import com.esotericsoftware.minlog.Log;
import com.phyloa.dlib.renderer.Graphics2DRenderer;

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
	
	int id;
	int team;
	
	public void initialize() 
	{
		Log.set( Log.LEVEL_TRACE );
		ci = new ClientNetworkInterface( "localhost" );
		
		size( 800, 600 );
		
		ci.sendToServer( new Message( MessageType.CLIENTJOIN, null ) );
		canvas.addMouseListener( this );
		canvas.addMouseMotionListener( this );
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
			case SETID:
				this.id = (Integer)m.message;
				break;
			}
		}
		
		if( l == null )
		{
			return;
		}
		
		g.setColor( Color.WHITE );
		g.fillRect( 0, 0, l.width*Level.tileSize, l.height*Level.tileSize );
		g.setColor( Color.BLACK );
		
		l.render( g );
		
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
			int tx = e.getX() / Level.tileSize;
			int ty = e.getY() / Level.tileSize;
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
			sx = e.getX();
			sy = e.getY();
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
			
			int x1 = Math.min( sx, e.getX() );
			int y1 = Math.min( sy, e.getY() );
			int x2 = Math.max( sx, e.getX() );
			int y2 = Math.max( sy, e.getY() );
			for( Unit u : units )
			{
				u.selected = u.team == this.id && u.x > x1 && u.x < x2 && u.y > y1 && u.y < y2;
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
		sw = e.getX() - sx;
		sh = e.getY() - sy;			
	}

	public void mouseMoved( MouseEvent e )
	{
		if( e.getButton() == MouseEvent.BUTTON1 )
		{
			sw = e.getX() - sx;
			sh = e.getY() - sy;
		}
	}
}
