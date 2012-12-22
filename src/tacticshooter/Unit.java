package tacticshooter;

import java.awt.Color;
import java.awt.Graphics2D;
import java.io.Serializable;
import java.util.ArrayList;

import javax.vecmath.Point2f;
import javax.vecmath.Point2i;
import javax.vecmath.Vector2f;

import org.newdawn.slick.util.pathfinding.Path;
import org.newdawn.slick.util.pathfinding.Path.Step;

import com.phyloa.dlib.renderer.Graphics2DRenderer;
import com.phyloa.dlib.util.DMath;

public class Unit implements Serializable
{
	public static int radius = 10;
	
	int id = (int) (Math.random() * Integer.MAX_VALUE);
	float x;
	float y; 
	float heading;
	int health = 100;
	boolean alive = true;
	
	UnitState state = UnitState.MOVING;
	float turnToAngle;
	
	Player owner;
	
	ArrayList<Point2i> path = new ArrayList<Point2i>();
	int onStep = 0;
	
	int destx, desty;
	
	int reloadtime = 0;
	
	Player killer;
	
	//CLIENT ONLY
	boolean selected = false;
	
	public Unit()
	{
		
	}
	
	public Unit( float x, float y )
	{
		this();
		this.x = x;
		this.y = y;
	}
	
	public Unit( float x, float y, Player owner )
	{
		this( x, y );
		this.owner = owner;
	}
	
	public boolean update( TacticServer ts )
	{
		Level l = ts.l;
		
		int tilex = l.getTileX( x );
		int tiley = l.getTileY( y );
		
		if( health <= 0 )
		{
			alive = false;
		}
		
		if( reloadtime > 0 )
		{
			reloadtime--;
		}
		
		switch( state )
		{
		case MOVING:
			if( owner == null && onStep >= path.size() )
			{
				pathTo( DMath.randomi( 0, l.width ), DMath.randomi( 0, l.height ), ts );
			}
			
			if( onStep < path.size() )
			{
				Point2i s = path.get( onStep );
				if( s.x == tilex && s.y == tiley )
				{
					onStep++;
				}
				else
				{
					float nx = s.x*l.tileSize + l.tileSize/2;
					float ny = s.y*l.tileSize + l.tileSize/2;
					
					float tangle = (float) Math.atan2( ny - y, nx - x );
					heading += DMath.turnTowards( heading, tangle ) * .2f;
					x += Math.cos( heading ) * 3;
					y += Math.sin( heading ) * 3;
				}
			}
			break;
		case TURNTO:
			float turnAmount = DMath.turnTowards( heading, turnToAngle ) * .2f;
			heading += turnAmount;
			if( turnAmount < .01f )
			{
				int mtx = -1;
				int mty = -1;
				for( Unit u : ts.units )
				{
					if( !u.owner.team.equals( this.owner.team ) )
					{
						float angletoguy = (float)Math.atan2( u.y - y, u.x - x );
						if( Math.abs( DMath.turnTowards( heading, angletoguy ) ) < Math.PI / 4 )
						{
							if( !l.hitwall( new Point2f( x, y ), new Vector2f( u.x - x, u.y - y ) ) )
							{
								mtx = l.getTileX( u.x );
								mty = l.getTileY( u.y );
								break;
							}
						}
					}
				}
				if( mtx != -1 )
				{
					pathTo( mtx, mty, ts );
				}
				else
				{
					onStep = path.size();
				}
				state = UnitState.MOVING;
			}
			break;
		}
		
		//Bounce off walls
		//@TODO doesn't work well, also is probably sort of computationally expensive. Might not be worth it 
		int tx = l.getTileX( x );
		int ty = l.getTileY( y );
		for( int y = Math.max( ty-1, 0 ); y < Math.min( ty+1, l.height-1 ); y++ )
		{
			for( int x = Math.max( tx-1, 0 ); x < Math.min( tx+1, l.width-1 ); x++ )
			{
				if( l.tiles[x][y] == 1 )
				{
					for( int i = 0; i < 4; i++ )
					{
						Vector2f vec = null;
						switch( i )
						{
						case 0: vec = DMath.pointToLineSegment( new Point2f( x*l.tileSize, y*l.tileSize ), new Vector2f( l.tileSize, 0 ), new Point2f( this.x, this.y ) ); break;
						case 1: vec = DMath.pointToLineSegment( new Point2f( x*l.tileSize, y*l.tileSize ), new Vector2f( 0, l.tileSize ), new Point2f( this.x, this.y ) ); break;
						case 2: vec = DMath.pointToLineSegment( new Point2f( (x+1)*l.tileSize, y*l.tileSize ), new Vector2f( 0, l.tileSize ), new Point2f( this.x, this.y ) ); break;
						case 3: vec = DMath.pointToLineSegment( new Point2f( x*l.tileSize, (y+1)*l.tileSize ), new Vector2f( l.tileSize, 0 ), new Point2f( this.x, this.y ) ); break;
						}
						if( vec.lengthSquared() < this.radius )
						{
							this.x += vec.x;
							this.y += vec.y;
						}
					}
				}
			}
		}
		
		if( reloadtime <= 0 )
		{
			for( Unit u : ts.units )
			{
				if( !u.owner.team.equals( this.owner.team ) )
				{
					float angletoguy = (float)Math.atan2( u.y - y, u.x - x );
					if( Math.abs( DMath.turnTowards( heading, angletoguy ) ) < Math.PI / 4 )
					{
						if( !l.hitwall( new Point2f( x, y ), new Vector2f( u.x - x, u.y - y ) ) )
						{
							float bangle = angletoguy + DMath.randomf( -.1f, .1f );
							ts.addBullet( this, bangle );
							reloadtime = 5;
							break;
						}
					}
				}
			}
		}
		
		return true;
	}
	
	public void render( TacticClient g )
	{
		g.pushMatrix();
		g.translate( x, y );
		g.g.setColor( Color.BLACK );
		g.g.drawString( owner.id + "", 0, -10 );
		if( selected )
		{
			g.color( Color.BLUE );
			g.g.drawRect( -5, -5, 10, 10 );
		}
		g.rotate( heading );
		g.g.setColor( new Color( 1.f - health*.01f, health*.01f, 0 ) );
		g.g.fillOval( -5, -5, 10, 10 );
		g.g.setColor( Color.BLACK );
		g.g.drawOval( -5, -5, 10, 10 );
		g.line( 0, 0, 5, 0 );
		g.popMatrix();
		/*
		g.setColor( Color.BLACK );
		for( int i = 0; i < path.size()-1; i++ )
		{
			Point2i p1 = path.get( i );
			Point2i p2 = path.get( i+1 );
			g.drawLine( p1.x*Level.tileSize+15, p1.y*Level.tileSize+15, p2.x*Level.tileSize+15, p2.y*Level.tileSize+15 );
		}
		*/
	}
	
	public void pathTo( int tx, int ty, TacticServer ts )
	{
		Level l = ts.l;
		Path tp = ts.finder.findPath( null, l.getTileX( x ), l.getTileY( y ), tx, ty );
		if( tp != null )
		{
			path.clear();
			for( int i = 0; i < tp.getLength(); i++ )
			{
				path.add( new Point2i( tp.getX( i ), tp.getY( i ) ) );
			}
			l.clearVisited();
			destx = tx;
			desty = ty;
			onStep = 0;
		}
	}
	
	public void sync( Unit u )
	{
		assert( u.id == this.id );
		
		this.x = u.x;
		this.y = u.y;
		this.destx = u.destx;
		this.desty = u.desty;
		this.path = u.path;
		this.alive = u.alive;
		this.heading = u.heading;
		this.health = u.health;
	}

	public void hit( Bullet bullet )
	{
		health -= 10;
		if( health <= 0 && health > -10 )
		{
			alive = false;
			killer = bullet.owner;
		}
		state = UnitState.TURNTO;
		turnToAngle = (float) Math.atan2( -bullet.dy, -bullet.dx );
	}
	
	public enum UnitState
	{
		MOVING,
		TURNTO;
	}
}
