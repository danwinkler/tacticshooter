package tacticshooter;

import java.util.ArrayList;
import java.util.Random;

import javax.vecmath.Point2f;
import javax.vecmath.Point2i;
import javax.vecmath.Vector2f;

import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.util.pathfinding.Path;

import tacticshooter.TacticServer.Bin;

import com.phyloa.dlib.util.DMath;

public class Unit
{
	public static int radius = 10;
	public static final int UPDATE_TIME = 15;
	
	public int id = new Random().nextInt();
	
	public float sx, sy;
	
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
	
	Bin[][] bins = new Bin[2][2];
	Bin[][] oldBin = new Bin[2][2];
	
	UnitPacket packet = new UnitPacket();
	
	//CLIENT ONLY
	public boolean selected = false;
	public int timeSinceUpdate = 0;
	
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
	
	public boolean update( TacticServer ts, float d )
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
				else
				{
					float nx = s.x*l.tileSize + l.tileSize/2;
					float ny = s.y*l.tileSize + l.tileSize/2;
					
					float tangle = (float) Math.atan2( ny - y, nx - x );
					heading += DMath.turnTowards( heading, tangle ) * .2f;
					
					float dx = (float)(Math.cos( heading ) * type.speed * d);
					float dy = (float)(Math.sin( heading ) * type.speed * d);
					if( l.getTile( x+dx, y ).passable ) x += dx;
					if( l.getTile( x, y+dy ).passable ) y += dy;
				}
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
		
		bins[0][0] = ts.getBin( x-ts.binOffset, y-ts.binOffset );
		bins[1][0] = ts.getBin( x+ts.binOffset, y-ts.binOffset );
		bins[0][1] = ts.getBin( x-ts.binOffset, y+ts.binOffset );
		bins[1][1] = ts.getBin( x+ts.binOffset, y+ts.binOffset );
		
		if( oldBin[0][0] != bins[0][0] || oldBin[1][1] != bins[1][1] )
		{
			if( oldBin[0][0] != null ) oldBin[0][0].remove( this );
			if( oldBin[1][0] != null ) oldBin[1][0].remove( this );
			if( oldBin[0][1] != null ) oldBin[0][1].remove( this );
			if( oldBin[1][1] != null ) oldBin[1][1].remove( this );
			
			if( bins[0][0] != null ) bins[0][0].add( this );
			if( bins[1][0] != null ) bins[1][0].add( this );
			if( bins[0][1] != null ) bins[0][1].add( this );
			if( bins[1][1] != null ) bins[1][1].add( this );
		}
		
		bins[0][0] = oldBin[0][0];
		bins[1][0] = oldBin[1][0];
		bins[0][1] = oldBin[0][1];
		bins[1][1] = oldBin[1][1];
		
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
		int tilex = tc.l.getTileX( x );
		int tiley = tc.l.getTileY( y );
		
		switch( state )
		{
		case MOVING:
			if( onStep < path.size() )
			{
				Point2i s = path.get( onStep );
				if( s.x == tilex && s.y == tiley )
				{
					onStep++;
				}
				else
				{
					float nx = s.x*tc.l.tileSize + tc.l.tileSize/2;
					float ny = s.y*tc.l.tileSize + tc.l.tileSize/2;
					
					float tangle = (float) Math.atan2( ny - y, nx - x );
					heading += DMath.turnTowards( heading, tangle ) * .2f;
					sx += Math.cos( heading ) * type.speed * d/2;
					sy += Math.sin( heading ) * type.speed * d/2;
				}
			}
			break;
		case TURNTO:
			float turnAmount = DMath.turnTowards( heading, turnToAngle ) * .4f;
			heading += turnAmount;
			break;
		}
		
		//Movement Smoothing
		float dsx = sx - x;
		float dsy = sy - y;
		x += dsx * .2f * d;
		y += dsy * .2f * d;
		
		if( health <= 0 )
		{
			alive = false;
		}
		
		timeSinceUpdate++;
	}
	
	public void render( Graphics g, Player p, float mx, float my, Level l )
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
		
		Color color = this.owner.team.getColor();
		
		g.setColor( color );
		g.fillOval( -5, -5, 10, 10 );
		g.setColor( Color.black );
		g.drawOval( -5, -5, 10, 10 );
		g.drawLine( 0, 0, 5, 0 );
		
		g.popTransform();
		
		g.setColor( Color.black );
		g.fillRect( -9, -9, (int)(18.f * health/type.health), 4 );
		
		g.setColor( new Color( DMath.bound( 1.f - health/type.health, 0, 1 ), DMath.bound(health/type.health, 0, 1 ), 0 ) );
		g.fillRect( -8, -8, (int)(16.f * health/type.health), 2 );
		
		float dmx = x - mx;
		float dmy = y - my;
		if( dmx*dmx + dmy*dmy < 100 )
		{
			float strWidth = g.getFont().getWidth( owner.name );
			g.setColor( Color.black );
			g.drawString( owner.name, -strWidth/2, 10 );
		}
		
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
			destx = tx;
			desty = ty;
			onStep = 0;
			state = UnitState.MOVING;
		}
	}
	
	public void sync( UnitPacket u )
	{
		assert( u.id == this.id );
		
		this.id = u.id;
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
		this.owner = u.owner;
		this.onStep = u.onStep;
		timeSinceUpdate = 0;
	}
	
	public static class UnitPacket
	{
		public int id;
		public int destx;
		public int desty;
		public float x;
		public float y;
		public float heading;
		public float health;
		public boolean alive;
		public UnitType type;
		public UnitState state;
		public Player owner;
		public ArrayList<Point2i> path;
		public int onStep;
		
		public UnitPacket()
		{
			
		}
	}
	
	public UnitPacket getPacket()
	{
		packet.id = this.id;
		packet.x = this.x;
		packet.y = this.y;
		packet.destx = this.destx;
		packet.desty = this.desty;
		packet.path = this.path;
		packet.alive = this.alive;
		packet.heading = this.heading;
		packet.health = this.health;
		packet.type = this.type;
		packet.state = this.state;
		packet.owner = this.owner;
		packet.onStep = this.onStep;
		return packet;
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
		LIGHT( 5, 10, .05f, 10, 100, 1 ),
		HEAVY( 2.5f, 3, .1f, 20, 200, 1  ),
		SHOTGUN( 5, 30, .3f, 15, 150, 5 );
		
		float speed;
		int timeBetweenBullets;
		float bulletSpread;
		int price;
		float health;
		int bulletsAtOnce;
		
		UnitType( float speed, int timeBetweenBullets, float bulletSpread, int price, float health, int bulletsAtOnce )
		{
			this.speed = speed;
			this.timeBetweenBullets = timeBetweenBullets;
			this.bulletSpread = bulletSpread;
			this.price = price;
			this.health = health;
			this.bulletsAtOnce = bulletsAtOnce;
		}
	}

	public void renderMinimap( Graphics g, Player player )
	{
		g.setColor( this.owner.id == player.id ? Color.blue : this.owner.team.getColor() );
		g.fillOval( x-20, y-20, 40, 40 );
	}
}
