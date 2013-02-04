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
	public static float bulletSpeed = 35;
	
	public int id = (int)(Math.random() * Integer.MAX_VALUE);
	
	public Point2f loc;
	public Point2f lastLoc;
	public Vector2f dir;
	
	public boolean alive = true;
	
	public Player owner;
	public Unit shooter;
	
	Point2f p = new Point2f();
	
	public Bullet()
	{
		
	}
	
	public Bullet( float x, float y, float angle )
	{
		loc = new Point2f( x, y );
		dir = new Vector2f( DMath.cosf( angle ) * bulletSpeed, DMath.sinf( angle ) * bulletSpeed );
		lastLoc = new Point2f( x - dir.x, y - dir.y );
	}
	
	public void update( TacticServer ts )
	{
		lastLoc.set( loc );
		loc.add( dir );
		
		Level l = ts.l;
		
		// Test if hits wall
		if( l.hitwall( lastLoc, dir ) )
		{
			alive = false;
			return;
		}
		
		for( Unit u : ts.units )
		{
			if( u.owner.team.id != owner.team.id )
			{
				p.x = u.x;
				p.y = u.y;
				if( DMath.pointToLineSegment( lastLoc, dir, p ).lengthSquared() < Unit.radius * Unit.radius )
				{
					u.hit( this, ts );
					alive = false;
					break;
				}
			}
		}
		
		if( loc.x > l.width * l.tileSize || loc.x < 0 || loc.y > l.height * l.tileSize || loc.y < 0 )
		{
			alive = false;
		}
	}
	
	public void clientUpdate( MultiplayerGameScreen cs, float d, GameContainer gc )
	{
		ClientState ts = cs.cs;
		Level l = ts.l;
		lastLoc.set( loc );
		loc.x += dir.x * d;
		loc.y += dir.y * d;
		
		// Test if hits wall
		if( l.hitwall( lastLoc, dir ) )
		{
			alive = false;
			return;
		}
		
		for( Unit u : ts.units )
		{
			if( u.owner.team.id != owner.team.id )
			{
				p.x = u.x;
				p.y = u.y;
				Vector2f sect = DMath.pointToLineSegment( lastLoc, dir, p );
				if( sect.lengthSquared() < Unit.radius * Unit.radius )
				{
					alive = false;
					
					ts.hit1.play( DMath.randomf( .9f, 1.1f ), ts.getSoundMag( gc, loc.x, loc.y ) * .2f );
					cs.drawBlood( loc.x, loc.y );
					break;
				}
			}
		}
		
		if( loc.x > l.width * l.tileSize || loc.x < 0 || loc.y > l.height * l.tileSize || loc.y < 0 )
		{
			alive = false;
		}
	}
	
	public void render( Graphics g )
	{
		//g.setColor( Color.white );
		g.drawLine( loc.x, loc.y, loc.x+dir.x*.5f, loc.y+dir.y*.5f );
	}
}
