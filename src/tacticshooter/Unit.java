package tacticshooter;

import java.io.Serializable;
import java.util.ArrayList;

import javax.vecmath.Point2f;
import javax.vecmath.Point2i;
import javax.vecmath.Vector2f;

import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.util.pathfinding.Path;
import com.phyloa.dlib.util.DMath;

public class Unit implements Serializable
{
	public static int radius = 10;
	public static final int UPDATE_TIME = 3;
	
	public int id = (int) (Math.random() * Integer.MAX_VALUE);
	
	public float sx, sy;
	
	public float x;
	public float y; 
	public float heading;
	public float health = 100;
	public boolean alive = true;
	
	public UnitType type = UnitType.LIGHT;
	public UnitState state = UnitState.MOVING;
	public UnitState lastState = state;
	public float turnToAngle;
	
	public Player owner;
	
	public ArrayList<Point2i> path = new ArrayList<Point2i>();
	public int onStep = 0;
	
	public int destx, desty;
	
	public int reloadtime = 0;
	
	public Player killer;
	
	public int updateCountdown = 0;
	
	public boolean update = false;
	
	//CLIENT ONLY
	public boolean selected = false;
	
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
					x += Math.cos( heading ) * type.speed;
					y += Math.sin( heading ) * type.speed;
				}
			}
			else
			{
				state = UnitState.STOPPED;
			}
			break;
		case TURNTO:
			float turnAmount = DMath.turnTowards( heading, turnToAngle ) * .2f;
			heading += turnAmount;
			if( turnAmount < .001f )
			{
				/*
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
				*/
				state = UnitState.STOPPED;
			}
			break;
		}
		
		//Bounce off walls
		//@TODO doesn't work well, also is probably sort of computationally expensive. Might not be worth it
		/*
		int tx = l.getTileX( x );
		int ty = l.getTileY( y );
		if( l.getTile( x, y ) == 0 )
		{
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
								this.x += vec.x * 10;
								this.y += vec.y * 10;
							}
						}
					}
				}
			}
		}
		*/
		
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
							float bangle = angletoguy + DMath.randomf( -type.bulletSpread, type.bulletSpread );
							ts.addBullet( this, bangle );
							reloadtime = type.timeBetweenBullets;
							break;
						}
					}
				}
			}
		}
		
		//To keep Unit updates from getting out of hand
		if( updateCountdown > 0 )
		{
			updateCountdown--;
			return false;
		}
		
		boolean sendToClient = state != UnitState.STOPPED || lastState == state || update;
		lastState = state;
		update = false;
		if( sendToClient )
		{
			updateCountdown = UPDATE_TIME;
		}
		return sendToClient;
	}
	
	public void clientUpdate( ClientState tc, float d )
	{
		//Predictive Movement
		if( state == UnitState.MOVING )
		{
			x += DMath.cosf( heading ) * type.speed * d;
			y += DMath.sinf( heading ) * type.speed * d;
		}
		
		//Movement Smoothing
		float dsx = sx - x;
		float dsy = sy - y;
		x += dsx * .2f * d;
		y += dsy * .2f * d;
	}
	
	public void render( Graphics g, Player p )
	{
		g.pushTransform();
		g.translate( x, y );
		
		if( selected )
		{
			g.setColor( Color.blue );
			g.drawRect( -10, -10, 20, 20 );
		}
		
		if( owner.id == p.id )
		{
			g.setColor( Color.white );
			g.fillOval( -7, -7, 14, 14 );
			g.setColor( Color.black );
			g.drawOval( -7, -7, 14, 14 );
		}
		
		g.pushTransform();
		
		g.rotate( 0, 0, heading / DMath.PI2F * 360 );
		
		g.setColor( this.owner.team.getColor() );
		g.fillOval( -5, -5, 10, 10 );
		g.setColor( Color.black );
		g.drawOval( -5, -5, 10, 10 );
		g.drawLine( 0, 0, 5, 0 );
		
		g.popTransform();
		
		g.setColor( Color.black );
		g.fillRect( -9, -9, (int)(18.f * health/type.health), 4 );
		
		g.setColor( new Color( DMath.bound( 1.f - health/type.health, 0, 1 ), DMath.bound(health/type.health, 0, 1 ), 0 ) );
		g.fillRect( -8, -8, (int)(16.f * health/type.health), 2 );
		
		
		g.popTransform();
		
		if( selected )
		{
			g.setColor( Color.lightGray );
			for( int i = 0; i < path.size()-1; i++ )
			{
				Point2i p1 = path.get( i );
				Point2i p2 = path.get( i+1 );
				g.drawLine( (p1.x+.5f) * Level.tileSize, (p1.y+.5f) * Level.tileSize, (p2.x+.5f) * Level.tileSize, (p2.y+.5f) * Level.tileSize );
			}
		}
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
			state = UnitState.MOVING;
		}
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
		this.heading = u.heading;
		this.health = u.health;
		this.type = u.type;
		this.state = u.state;
	}

	public void hit( Bullet bullet, TacticServer ts )
	{
		Level l = ts.l;
		health -= 10;
		if( health <= 0 && health > -10 )
		{
			alive = false;
			killer = bullet.owner;
			bullet.owner.money += 2;
		}
		
		Building b = null;
		if( state != UnitState.MOVING )
		{
			for( int i = 0; i < l.buildings.size(); i++ )
			{
				Building tb = l.buildings.get( i );
				float dx = x - tb.x;
				float dy = y - tb.y;
				if( (dx*dx + dy*dy) < 50*50 )
				{
					b = tb;
					break;
				}
			}
		}
		
		if( b != null )
		{
			state = UnitState.TURNTO;
			turnToAngle = (float) Math.atan2( -bullet.dy, -bullet.dx );
		}
		else
		{
			pathTo( l.getTileX( bullet.shooter.x ), l.getTileY( bullet.shooter.y ), ts );
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
		LIGHT( 3, 10, .05f, 10, 100 ),
		HEAVY( 1.5f, 3, .1f, 20, 200  ),
		SUPPLY( 2.5f, 10, .15f, 20, 100 );
		
		float speed;
		int timeBetweenBullets;
		float bulletSpread;
		int price;
		float health;
		
		UnitType( float speed, int timeBetweenBullets, float bulletSpread, int price, float health )
		{
			this.speed = speed;
			this.timeBetweenBullets = timeBetweenBullets;
			this.bulletSpread = bulletSpread;
			this.price = price;
			this.health = health;
		}
	}

	public void renderMinimap( Graphics g, Player player )
	{
		g.setColor( this.owner.team.getColor() );
		g.fillOval( x-20, y-20, 40, 40 );
	}
}
