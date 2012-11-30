package tacticshooter;

import java.awt.Color;
import java.awt.RenderingHints;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.HashMap;

import javax.vecmath.Point2i;

import com.esotericsoftware.minlog.Log;
import com.phyloa.dlib.renderer.Graphics2DRenderer;

public class TacticClient extends Graphics2DRenderer implements MouseListener
{
	ClientInterface ci;
	
	HashMap<Integer, Unit> unitMap = new HashMap<Integer, Unit>();
	ArrayList<Unit> units = new ArrayList<Unit>();
	
	HashMap<Integer, Bullet> bulletMap = new HashMap<Integer, Bullet>();
	ArrayList<Bullet> bullets = new ArrayList<Bullet>();
	
	Level l;
	
	public void initialize() 
	{
		Log.set( Log.LEVEL_TRACE );
		ci = new ClientNetworkInterface( "triggerly.com" );
		
		size( 800, 600 );
		
		ci.sendToServer( new Message( MessageType.CLIENTJOIN, null ) );
		canvas.addMouseListener( this );
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
			}
		}
		
		g.setColor( Color.WHITE );
		g.fillRect( 0, 0, 800, 600 );
		g.setColor( Color.BLACK );
		
		if( l != null )
		{
			l.render( g );
		}
		else
		{
			return;
		}
		
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
			u.render( g );
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
	}
	
	public static void main( String[] args )
	{
		TacticClient tc = new TacticClient();
		tc.begin();
	}

	public void mouseClicked( MouseEvent e )
	{
		ci.sendToServer( new Message( MessageType.SETATTACKPOINT, new Point2i( e.getX() / Level.tileSize, e.getY() / Level.tileSize ) ) );
	}

	public void mouseEntered(MouseEvent arg0) {
		
	}

	public void mouseExited(MouseEvent arg0) {
		
	}

	public void mousePressed(MouseEvent arg0) {
		
	}

	public void mouseReleased(MouseEvent arg0) {
		
	}
}
