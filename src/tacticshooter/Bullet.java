package tacticshooter;

import javax.vecmath.Point2f;
import javax.vecmath.Point2i;
import javax.vecmath.Vector2f;

import org.newdawn.slick.Color;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;

import com.danwink.tacticshooter.screens.MultiplayerGameScreen;
import com.phyloa.dlib.util.DMath;

public class Bullet
{
	public static float bulletSpeed = 30;
	
	public int id = (int)(Math.random() * Integer.MAX_VALUE);
	
	public float x, y, lx, ly;
	public float dx, dy;
	
	public boolean alive = true;
	
	public Player owner;
	public Unit shooter;
	
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
			if( u.owner.team.id != owner.team.id )
			{
				p.x = u.x;
				p.y = u.y;
				if( DMath.pointToLineSegment( l1, dir, p ).lengthSquared() < Unit.radius * Unit.radius )
				{
					u.hit( this, ts );
					alive = false;
					break;
				}
			}
		}
		
		if( x > l.width * l.tileSize || x < 0 || y > l.height * l.tileSize
				|| y < 0 )
		{
			alive = false;
		}
	}
	
	public void clientUpdate( MultiplayerGameScreen cs, float d, GameContainer gc )
	{
		ClientState ts = cs.cs;
		Level l = ts.l;
		lx = x;
		ly = y;
		x += dx * d;
		y += dy * d;
		
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
			if( u.owner.team.id != owner.team.id )
			{
				p.x = u.x;
				p.y = u.y;
				Vector2f sect = DMath.pointToLineSegment( l1, dir, p );
				if( sect.lengthSquared() < Unit.radius * Unit.radius )
				{
					alive = false;
					//for( int i = 0; i < 5; i++ )
						//ts.ps.add( new TacticClient.BloodParticle( p.x, p.y, dx, dy ) );
					ts.hit1.play( DMath.randomf( .9f, 1.1f ), ts.getSoundMag( gc, x, y ) * .2f );
					cs.drawBlood( x, y );
					break;
				}
			}
		}
		
		if( x > l.width * l.tileSize || x < 0 || y > l.height * l.tileSize || y < 0 )
		{
			alive = false;
		}
	}
	
	public void render( Graphics g )
	{
		//g.setColor( Color.white );
		Vector2f vec = new Vector2f( x-lx, y-ly );
		vec.normalize();
		g.drawLine( x, y, x+vec.x*10, y+vec.y*10 );
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
