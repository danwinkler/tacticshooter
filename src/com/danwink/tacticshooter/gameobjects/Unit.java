package com.danwink.tacticshooter.gameobjects;

import java.io.File;
import java.util.ArrayList;
import java.util.Random;

import jp.objectclub.vecmath.Point2f;
import com.phyloa.dlib.math.Point2i;
import jp.objectclub.vecmath.Vector2f;

import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.SpriteSheet;
import org.newdawn.slick.util.pathfinding.Path;

import com.danwink.tacticshooter.ClientState;
import com.danwink.tacticshooter.TacticServer;
import com.phyloa.dlib.util.DMath;
import com.phyloa.dlib.util.DOptions;

public class Unit
{
	public static int radius = 10;
	public static final int UPDATE_TIME = 7;
	public static final float frameTime = 5;
	public static Random random = new Random();
	
	private static Image loadHelper( String s ) throws SlickException
	{
		return new Image( "data" + File.separator + "img" + File.separator + s + ".png" );
	}
	
	public int id = random.nextInt();
	
	public float sx, sy, sheading;
	
	public float x;
	public float y; 
	public float heading;
	public float health = 100;
	public boolean alive = true;
	
	public UnitType type = UnitType.LIGHT;
	public UnitState state = UnitState.STOPPED;
	public UnitState lastState = state;
	public float turnToAngle;
	public boolean turnOnStop = false;
	
	public Player owner;
	
	public ArrayList<Point2i> path = new ArrayList<Point2i>();
	public int onStep = 0;
	
	public int destx, desty;
	
	public int reloadtime = 0;
	
	public Player killer;
	
	public int updateCountdown = 0;
	
	public Building stoppedAt;
	
	public int occupyX = -1;
	public int occupyY = -1;
	
	//CLIENT ONLY
	public boolean selected = false;
	public int timeSinceUpdate = 0;
	
	public int frame = 0;
	float timeSinceLastFrame = 0;
	
	public Unit()
	{
		heading = DMath.randomf( 0, DMath.PI2F );
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
	
	public int update( TacticServer ts )
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
			if( occupyX != -1 )
			{
				ts.unitGrid[occupyX][occupyY] = null;
				occupyX = -1;
				occupyY = -1;
			}
			stoppedAt = null;
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
				else if( path.size() > onStep+1 )
				{
					Point2i s2 = path.get( onStep+1 );
					if( s2.x == tilex && s2.y == tiley )
					{
						onStep += 2;
					}
				}
				
				float nx = s.x*Level.tileSize + Level.tileSize/2;
				float ny = s.y*Level.tileSize + Level.tileSize/2;
				
				float dpx = nx-x;
				float dpy = ny-y;
				
				float tangle = (float) Math.atan2( dpy, dpx );
				heading += DMath.turnTowards( heading, tangle ) * .2f;
				
				float dx = (float)(Math.cos( heading ) * type.speed);
				float dy = (float)(Math.sin( heading ) * type.speed);
				
				//Slow down when almost on point
				if( path.size() - onStep <= 1 ) 
				{
					dx *= .5f;
					dy *= .5f;
				}
				
				if( l.getTile( x+dx, y ).isPassable() ) x += dx;
				if( l.getTile( x, y+dy ).isPassable() ) y += dy;
			}
			else
			{
				Point2i closestSpot = findClosestRestSpot( ts );
				float txd = closestSpot.x - tilex*TacticServer.UNITS_PER_TILE;
				float tyd = closestSpot.y - tiley*TacticServer.UNITS_PER_TILE;
				if( (txd >= 0 && txd < TacticServer.UNITS_PER_TILE) & (tyd >= 0 && tyd < TacticServer.UNITS_PER_TILE) )
				{
					//Found a good spot
					float tilePart = Level.tileSize/(float)TacticServer.UNITS_PER_TILE;
					x = tilex*Level.tileSize + tilePart*.5f + tilePart * txd;
					y = tiley*Level.tileSize + tilePart*.5f + tilePart * tyd;
					ts.unitGrid[closestSpot.x][closestSpot.y] = this;
					occupyX = closestSpot.x;
					occupyY = closestSpot.y;
					state = UnitState.STOPPED;
					if( turnOnStop )
					{
						state = UnitState.TURNTO;
						turnOnStop = false;
					}
					for( int i = 0; i < l.buildings.size(); i++ )
					{
						Building tb = l.buildings.get( i );
						float dx = x - tb.x;
						float dy = y - tb.y;
						if( (dx*dx + dy*dy) < tb.bt.bu.getRadius()*tb.bt.bu.getRadius() )
						{
							stoppedAt = tb;
							ts.js.stop( tb, this );
							break;
						}
					}
				}
				else
				{
					//Need to path to other spot
					pathTo( closestSpot.x/TacticServer.UNITS_PER_TILE, closestSpot.y/TacticServer.UNITS_PER_TILE, ts );
				}
			}
			break;
		case TURNTO:
			float turnAmount = DMath.turnTowards( heading, turnToAngle ) * .4f;
			heading += turnAmount;
			if( turnAmount < .00005f )
			{
				state = UnitState.STOPPED;
			}
			break;
		case STOPPED:
			break;
		default:
			break;
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
							for( int i = 0; i < type.bulletsAtOnce; i++ )
							{
								float bangle = angletoguy + DMath.randomf( -type.bulletSpread, type.bulletSpread );
								ts.addBullet( this, bangle );
							}
							reloadtime = type.timeBetweenBullets;
							break;
						}
					}
				}
			}
		}
		
		int ret = 0;
		
		if( lastState != state || !alive )
		{
			ret = 2;
		}
		else if( state != UnitState.STOPPED )
		{
			if( updateCountdown > 0 )
			{
				updateCountdown--;
			}
			else
			{
				updateCountdown = UPDATE_TIME;
				ret = 1;
			}
		}
		
		lastState = state;
		return ret;
	}
	
	public void clientUpdate( ClientState tc, float d )
	{
		//Predictive Movement
		if( state == UnitState.MOVING )
		{
			sx += DMath.cosf( sheading ) * type.speed * d * 2;
			sy += DMath.sinf( sheading ) * type.speed * d * 2;
			if( timeSinceLastFrame < frameTime/type.speed )
			{
				timeSinceLastFrame += d;
			}
			else 
			{
				timeSinceLastFrame -= frameTime/type.speed;
				frame = (frame+1) % 4;
			}
		}
		if( state == UnitState.STOPPED )
		{
			frame = 0;
		}
		
		//Movement Smoothing
		float dsx = sx - x;
		float dsy = sy - y;
		x += dsx * .2f * d;
		y += dsy * .2f * d;
		
		heading += DMath.turnTowards( heading, sheading ) * .1f;
		
		if( health <= 0 )
		{
			alive = false;
		}
		
		timeSinceUpdate++;
	}
	
	public void render( Graphics g, Player p, float mx, float my, Level l, Graphics fog )
	{
		if( selected && state == UnitState.MOVING )
		{
			g.setColor( Color.black );
			g.setLineWidth( 3 );
			for( int i = Math.max( onStep-2, 0 ); i < path.size()-1; i++ )
			{
				Point2i p1 = path.get( i );
				Point2i p2 = path.get( i+1 );
				g.drawLine( (p1.x+.5f) * Level.tileSize, (p1.y+.5f) * Level.tileSize, (p2.x+.5f) * Level.tileSize, (p2.y+.5f) * Level.tileSize );
			}
			
			g.setColor( Color.lightGray );
			g.setLineWidth( 1 );
			for( int i = Math.max( onStep-2, 0 ); i < path.size()-1; i++ )
			{
				Point2i p1 = path.get( i );
				Point2i p2 = path.get( i+1 );
				g.drawLine( (p1.x+.5f) * Level.tileSize, (p1.y+.5f) * Level.tileSize, (p2.x+.5f) * Level.tileSize, (p2.y+.5f) * Level.tileSize );
			}
		}
		
		g.pushTransform();
		g.translate( x, y );
		
		if( selected )
		{
			g.setColor( Color.blue );
			g.drawRect( -10, -10, 20, 20 );
		}
		
		int healthBarDist = 0;
		switch( type )
		{
		case SCOUT:
		case SHOTGUN:
		case LIGHT:
		case SNIPER:
		case SABOTEUR:
			healthBarDist = -11;
			break;
		case HEAVY:
			healthBarDist = -14;
			break;
		}
		
		g.setColor( Color.black );
		g.fillRect( -9, healthBarDist, (18.f * health/type.health), 4 );
		
		g.setColor( new Color( DMath.bound( 1.f - health/type.health, 0, 1 ), DMath.bound(health/type.health, 0, 1 ), 0 ) );
		g.fillRect( -8, healthBarDist+1, (16.f * health/type.health), 2 );
		
		float dmx = x - mx;
		float dmy = y - my;
		if( dmx*dmx + dmy*dmy < 100 )
		{
			float strWidth = g.getFont().getWidth( owner.name );
			g.setColor( Color.black );
			g.drawString( owner.name, -strWidth/2, 10 );
		}
		
		g.popTransform();
		
		if( fog != null && this.owner.team.id == p.team.id )
		{
			fog.setColor( Color.white );
			int lx = l.getTileX( this.x );
			int ly = l.getTileY( this.y );
			
			Point2f loc = new Point2f( x, y );
			Vector2f v = new Vector2f();
			Point2f result = new Point2f();
			
			int xmin = Math.max( lx-10, 0 );
			int ymin = Math.max( ly-10, 0 );
			int xmax = Math.min( lx+10, l.width-1 );
			int ymax = Math.min( ly+10, l.height-1 );
			
			float maxView = 10*Level.tileSize;
			float maxView2 = maxView * maxView;
			
			for( int xx = xmin; xx <= xmax; xx++ )
			{
				for( int yy = ymin; yy < ymax; yy++ )
				{
					v.set( xx*Level.tileSize - x + (Level.tileSize*.5f), yy*Level.tileSize - y + (Level.tileSize*.5f) );
					if( v.lengthSquared() < maxView2 && !l.hitwall( loc, v, result ) )
					{
						fog.fillOval( xx*Level.tileSize-(Level.tileSize*.5f), yy*Level.tileSize-(Level.tileSize*.5f), Level.tileSize*2, Level.tileSize*2 );
					}
				}
			}
		}
	}
	
	public void pathTo( int tx, int ty, TacticServer ts )
	{
		Level l = ts.l;
		Path tp = ts.finder.findPath( null, l.getTileX( x ), l.getTileY( y ), tx, ty );
		path.clear();
		if( tp != null )
		{
			for( int i = 0; i < tp.getLength(); i++ )
			{
				path.add( new Point2i( tp.getX( i ), tp.getY( i ) ) );
			}
		}
		destx = tx;
		desty = ty;
		onStep = 0;
		state = UnitState.MOVING;
	}
	
	//Note: doesnt actually find best spot, rather does a random walk until finds
	public Point2i findClosestRestSpot( TacticServer ts )
	{
		int tilex = ts.l.getTileX( x );
		int tiley = ts.l.getTileY( y );
		while( true )
		{
			for( int xx = tilex*TacticServer.UNITS_PER_TILE; xx < (tilex+1)*(TacticServer.UNITS_PER_TILE); xx++ )
			{
				for( int yy = tiley*TacticServer.UNITS_PER_TILE; yy < (tiley+1)*(TacticServer.UNITS_PER_TILE); yy++ )
				{
					if( ts.unitGrid[xx][yy] == null )
					{
						return new Point2i( xx, yy );
					}
				}
			}
			
			float dir = DMath.randomf() > .5f ? -1 : 1;
			float rot = DMath.randomf();
			
			int ntx = tilex;
			int nty = tiley;
			
			if( rot > .5f ) 
			{
				ntx += dir;
			}
			else
			{
				nty += dir;
			}
			
			if( ts.l.tiles[ntx][nty].isPassable() )
			{
				tilex = ntx;
				tiley = nty;
			}
		}
	}
	
	public void pathToContinue( int tx, int ty, TacticServer ts )
	{
		if( state != UnitState.MOVING )
		{
			pathTo( tx, ty, ts );
			return;
		}
		Point2i lastPoint = path.get( path.size()-1 );
		Path tp = ts.finder.findPath( null, lastPoint.x, lastPoint.y, tx, ty );
		if( tp != null )
		{
			for( int i = 1; i < tp.getLength(); i++ )
			{
				path.add( new Point2i( tp.getX( i ), tp.getY( i ) ) );
			}
		}
		destx = tx;
		desty = ty;
	}
	
	public void sync( Unit u )
	{
		assert( u.id == this.id );
		
		this.sx = u.x;
		this.sy = u.y;
		this.destx = u.destx;
		this.desty = u.desty;
		this.path = u.path;
		this.alive = u.alive;
		this.sheading = u.heading;
		this.health = u.health;
		this.type = u.type;
		this.state = u.state;
		this.onStep = u.onStep;
		this.owner = u.owner;
		timeSinceUpdate = 0;
	}
	
	public void explode( TacticServer ts )
	{
		for( Unit u : ts.units )
		{
			if( u.owner.team.id != owner.team.id )
			{
				float dx = u.x - x;
				float dy = u.y - y;
				float dist = dx*dx+dy*dy;
				float dmg = Math.max( 0, 1500-(dist) );
				if( dmg > 0 )
				{
					Bullet b = new Bullet( x, y, heading );
					b.owner = this.owner;
					b.damage = (int)dmg;
					b.shooter = this;
					u.hit( b, ts );
				}
			}
		}
	}

	public void hit( Bullet bullet, TacticServer ts )
	{
		health -= bullet.damage;
		if( alive && health <= 0 )
		{
			alive = false;
			killer = bullet.owner;
			bullet.owner.money += 2;
			if( type == UnitType.SABOTEUR )
			{
				explode( ts );
			}
		}
		
		if( alive )
		{
			if( type == UnitType.SABOTEUR )
			{
				for( Unit u : ts.units )
				{
					if( u.owner.team.id != owner.team.id )
					{
						float dx = u.x - x;
						float dy = u.y - y;
						float dist = dx*dx+dy*dy;
						if( dist < 25*25 )
						{
							alive = false;
							explode( ts );
						}
					}
				}
			}
		}
		
		if( stoppedAt != null && !bullet.isRicochet )
		{
			state = UnitState.TURNTO;
			turnToAngle = (float) Math.atan2( -bullet.dir.y, -bullet.dir.x );
			for( int i = 0; i < ts.units.size(); i++ )
			{
				Unit u = ts.units.get( i );
				if( u.owner.id == owner.id && u.stoppedAt == this.stoppedAt )
				{
					u.state = UnitState.TURNTO;
					u.turnToAngle = turnToAngle;
				}
			}
		}
		else if( state == UnitState.STOPPED )
		{
			//People didn't like the units running off
			//pathTo( l.getTileX( bullet.shooter.x ), l.getTileY( bullet.shooter.y ), ts );
			state = UnitState.TURNTO;
			turnToAngle = (float) Math.atan2( -bullet.dir.y, -bullet.dir.x );
		}
	}
	
	public void setType( UnitType type )
	{
		this.type = type;
		this.health = type.health;
	}
	
	public enum UnitState
	{
		MOVING,
		TURNTO,
		STOPPED;
	}
	
	public enum UnitType
	{
		LIGHT( 3, 10, .05f, 10, 100, 1, 10 ),
		HEAVY( 1.5f, 3, .1f, 20, 200, 1, 10  ),
		SHOTGUN( 3.0f, 30, .3f, 15, 150, 6, 10 ),
		SCOUT( 6f, 30, .1f, 3, 30, 1, 10 ),
		SNIPER( 2.5f, 100, 0, 15, 90, 1, 100 ),
		SABOTEUR( 4f, 10000, 0, 20, 150, 0, 0 );
		
		public float speed;
		public int timeBetweenBullets;
		public float bulletSpread;
		public int price;
		public float health;
		public int bulletsAtOnce;
		public int damage;
		
		UnitType( float speed, int timeBetweenBullets, float bulletSpread, int price, float health, int bulletsAtOnce, int damage )
		{
			this.speed = speed;
			this.timeBetweenBullets = timeBetweenBullets;
			this.bulletSpread = bulletSpread;
			this.price = price;
			this.health = health;
			this.bulletsAtOnce = bulletsAtOnce;
			this.damage = damage;
		}
	}

	public void renderMinimap( Graphics g, Player player )
	{
		g.setColor( this.owner.id == player.id ? Color.blue : this.owner.team.getColor() );
		g.fillOval( x-20, y-20, 40, 40 );
	}
	
	public static class UnitUpdate
	{
		public int id;
		public float x, y, heading, health;
		public UnitUpdate() {}
		public UnitUpdate( int id, float x, float y, float heading, float health ) { this.id = id; this.x = x; this.y = y; this.heading = heading; this.health = health; }
	}
}
