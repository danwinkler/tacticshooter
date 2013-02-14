package tacticshooter;

import java.io.File;
import java.util.ArrayList;
import java.util.Random;

import javax.vecmath.Point2f;
import javax.vecmath.Point2i;
import javax.vecmath.Vector2f;

import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.SpriteSheet;
import org.newdawn.slick.util.pathfinding.Path;
import com.phyloa.dlib.util.DMath;
import com.phyloa.dlib.util.DOptions;

public class Unit
{
	public static int radius = 10;
	public static final int UPDATE_TIME = 7;
	public static final float frameTime = 5;
	public static SpriteSheet light;
	public static SpriteSheet heavy;
	
	public static void loadTextures( Level l )
	{
		try
		{
			light = new SpriteSheet( new Image( "img" + File.separator + new DOptions( "themes" + File.separator + l.theme ).getS( "light" ) ), 16, 16 );
			heavy = new SpriteSheet( new Image( "img" + File.separator + new DOptions( "themes" + File.separator + l.theme ).getS( "heavy" ) ), 32, 32 );
		} catch( SlickException e )
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public int id = new Random().nextInt();
	
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
	
	public Player owner;
	
	public ArrayList<Point2i> path = new ArrayList<Point2i>();
	public int onStep = 0;
	
	public int destx, desty;
	
	public int reloadtime = 0;
	
	public Player killer;
	
	public int updateCountdown = 0;
	
	public boolean update = false;
	
	public Building stoppedAt;
	
	//CLIENT ONLY
	public boolean selected = false;
	public int timeSinceUpdate = 0;
	
	int frame = 0;
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
				
				float nx = s.x*l.tileSize + l.tileSize/2;
				float ny = s.y*l.tileSize + l.tileSize/2;
				
				float dpx = nx-x;
				float dpy = ny-y;
				
				float tangle = (float) Math.atan2( dpy, dpx );
				heading += DMath.turnTowards( heading, tangle ) * .2f;
				
				float dx = (float)(Math.cos( heading ) * type.speed);
				float dy = (float)(Math.sin( heading ) * type.speed);
				if( l.getTile( x+dx, y ).isPassable() ) x += dx;
				if( l.getTile( x, y+dy ).isPassable() ) y += dy;
			}
			else
			{
				state = UnitState.STOPPED;
				for( int i = 0; i < l.buildings.size(); i++ )
				{
					Building tb = l.buildings.get( i );
					float dx = x - tb.x;
					float dy = y - tb.y;
					if( (dx*dx + dy*dy) < 50*50 )
					{
						stoppedAt = tb;
						break;
					}
				}
			}
			break;
		case TURNTO:
			float turnAmount = DMath.turnTowards( heading, turnToAngle ) * .4f;
			heading += turnAmount;
			if( turnAmount < .001f )
			{
				state = UnitState.STOPPED;
			}
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
	
	public void render( Graphics g, Player p, float mx, float my, Level l )
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
	}

	public void renderBody( Graphics g, Player p )
	{
		g.pushTransform();
		g.translate( x, y );
		g.rotate( 0, 0, heading / DMath.PI2F * 360 );
		drawBody( g, owner.id == p.id );
		g.popTransform();
	}
	
	public void renderDead( Graphics g )
	{
		g.pushTransform();
		g.translate( x, y );
		g.rotate( 0, 0, heading / DMath.PI2F * 360 );
		drawBody( g, false );
		g.popTransform();
	}
	
	public void drawBody( Graphics g, boolean player )
	{
		Color color = this.owner.team.getColor();
		
		switch( type )
		{
		case LIGHT:
			g.pushTransform();
			g.rotate( 0, 0, 90 );
			//g.scale( 1.0f, 1.0f );
			g.drawImage( light.getSprite( frame, 0 ), -8, -8, color );
			g.popTransform();
			break;
		case SCOUT:
		case SHOTGUN:
			if( player )
			{
				g.setColor( Color.white );
				g.fillOval( -7, -7, 14, 14 );
				g.setColor( Color.black );
				g.drawOval( -7, -7, 14, 14 );
			}
			
			g.setColor( color );
			g.fillOval( -5, -5, 10, 10 );
			g.setColor( Color.black );
			g.drawOval( -5, -5, 10, 10 );
			g.drawLine( 0, 0, 5, 0 );
			
			break;
		case SNIPER:
			if( player )
			{
				g.setColor( Color.white );
				g.fillOval( -7, -6, 14, 12 );
				g.setColor( Color.black );
				g.drawOval( -7, -6, 14, 12 );
			}
			
			g.setColor( color );
			g.fillOval( -5, -4, 10, 8 );
			g.setColor( Color.black );
			g.drawOval( -5, -4, 10, 8 );
			g.drawLine( 0, 0, 5, 0 );
			break;
		case HEAVY:
			g.pushTransform();
			g.rotate( 0, 0, 90 );
			//g.scale( 1.0f, 1.0f );
			g.drawImage( heavy.getSprite( frame, 0 ), -16, -16, color );
			g.popTransform();
			break;
			
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
			destx = tx;
			desty = ty;
			onStep = 0;
			state = UnitState.MOVING;
		}
	}
	
	public void pathToContinue( int tx, int ty, TacticServer ts )
	{
		if( state != UnitState.MOVING )
		{
			pathTo( tx, ty, ts );
			return;
		}
		Level l = ts.l;
		Point2i lastPoint = path.get( path.size()-1 );
		Path tp = ts.finder.findPath( null, lastPoint.x, lastPoint.y, tx, ty );
		if( tp != null )
		{
			for( int i = 1; i < tp.getLength(); i++ )
			{
				path.add( new Point2i( tp.getX( i ), tp.getY( i ) ) );
			}
			destx = tx;
			desty = ty;
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
		this.sheading = u.heading;
		this.health = u.health;
		this.type = u.type;
		this.state = u.state;
		this.onStep = u.onStep;
		timeSinceUpdate = 0;
	}

	public void hit( Bullet bullet, TacticServer ts )
	{
		Level l = ts.l;
		health -= bullet.damage;
		if( alive && health <= 0 )
		{
			alive = false;
			killer = bullet.owner;
			bullet.owner.money += 2;
		}
		
		if( stoppedAt != null )
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
		LIGHT( 3, 10, .05f, 10, 100, 1, 10 ),
		HEAVY( 1.5f, 3, .1f, 20, 200, 1, 10  ),
		SHOTGUN( 3.0f, 30, .3f, 15, 150, 6, 10 ),
		SCOUT( 6f, 30, .1f, 3, 30, 1, 10 ),
		SNIPER( 2.5f, 100, 0, 20, 90, 1, 100 );
		
		float speed;
		int timeBetweenBullets;
		float bulletSpread;
		int price;
		float health;
		int bulletsAtOnce;
		int damage;
		
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
}
