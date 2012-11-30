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
	
	int team = -1;
	
	ArrayList<Point2i> path = new ArrayList<Point2i>();
	int onStep = 0;
	
	int destx, desty;
	
	int reloadtime = 0;
	
	public Unit()
	{
		
	}
	
	public Unit( float x, float y )
	{
		this.x = x;
		this.y = y;
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
		
		if( team < 0 && onStep >= path.size() )
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
		
		if( reloadtime <= 0 )
		{
			for( Unit u : ts.units )
			{
				if( u.team != this.team )
				{
					float angletoguy = (float)Math.atan2( u.y - y, u.x - x );
					if( Math.abs( DMath.turnTowards( heading, angletoguy ) ) < Math.PI / 4 )
					{
						if( !l.hitwall( new Point2f( x, y ), new Vector2f( u.x - x, u.y - y ) ) )
						{
							float bangle = angletoguy + DMath.randomf( -.1f, .1f );
							ts.addBullet( x + DMath.cosf( bangle ) * (radius+5), y + DMath.sinf( bangle ) * (radius+5), bangle );
							reloadtime = 5;
							break;
						}
					}
				}
			}
		}
		
		return true;
	}
	
	public void render( Graphics2D g )
	{
		g.setColor( new Color( 1.f - health*.01f, health*.01f, 0 ) );
		g.fillOval( (int)x - 5, (int)y - 5, 10, 10 );
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
}
