package tacticshooter;

import java.awt.Color;
import java.awt.Graphics2D;
import java.io.Serializable;

import javax.vecmath.Point2f;
import javax.vecmath.Point2i;
import javax.vecmath.Vector2f;

import com.phyloa.dlib.util.DMath;

public class Bullet implements Serializable
{
	public static float bulletSpeed = 30;
	
	int id = (int)(Math.random() * Integer.MAX_VALUE);
	
	float x, y, lx, ly;
	float dx, dy;
	
	boolean alive = true;
	
	public Bullet()
	{
		
	}
	
	public Bullet( float x, float y, float angle )
	{
		this.x = x;
		this.y = y;
		dx = DMath.cosf( angle ) * bulletSpeed;
		dy = DMath.sinf( angle ) * bulletSpeed;
	}
	
	public void update( TacticServer ts )
	{
		lx = x;
		ly = y;
		x += dx;
		y += dy;
		
		Level l = ts.l;
		
		// Test if hits wall
		if( l.hitwall( new Point2f( lx, ly ), new Vector2f( dx, dy ) ) )
		{
			alive = false;
			return;
		}
		
		Point2f p = new Point2f();
		Point2f l1 = new Point2f( lx, ly );
		Vector2f dir = new Vector2f( x - lx, y - ly );
		for( Unit u : ts.units )
		{
			p.x = u.x;
			p.y = u.y;
			if( DMath.pointToLineSegment( l1, dir, p ).lengthSquared() < Unit.radius * Unit.radius )
			{
				u.health -= 10;
				alive = false;
			}
		}
		
		if( x > l.width * l.tileSize || x < 0 || y > l.height * l.tileSize
				|| y < 0 )
		{
			alive = false;
		}
	}
	
	public void clientUpdate( TacticClient ts )
	{
		Level l = ts.l;
		lx = x;
		ly = y;
		x += dx;
		y += dy;
		
		// Test if hits wall
		if( l.hitwall( new Point2f( lx, ly ), new Vector2f( dx, dy ) ) )
		{
			alive = false;
			return;
		}
		
		Point2f p = new Point2f();
		Point2f l1 = new Point2f( lx, ly );
		Vector2f dir = new Vector2f( x - lx, y - ly );
		for( Unit u : ts.units )
		{
			p.x = u.x;
			p.y = u.y;
			if( DMath.pointToLineSegment( l1, dir, p ).lengthSquared() < Unit.radius * Unit.radius )
			{
				alive = false;
			}
		}
		
		if( x > l.width * l.tileSize || x < 0 || y > l.height * l.tileSize
				|| y < 0 )
		{
			alive = false;
		}
	}
	
	public void render( Graphics2D g )
	{
		// g.setColor( Color.YELLOW );
		g.drawLine( (int)x, (int)y, (int)lx, (int)ly );
	}
	
	public void sync( Bullet b )
	{
		this.x = b.x;
		this.y = b.y;
		this.lx = b.lx;
		this.ly = b.ly;
		this.dx = b.dx;
		this.dy = b.dy;
	}
}
