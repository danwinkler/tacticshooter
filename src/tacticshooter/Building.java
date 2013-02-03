package tacticshooter;

import java.util.Random;

import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.util.pathfinding.PathFinder;

import com.phyloa.dlib.util.DMath;

public class Building
{
	public static final int UPDATE_TIME = 5;
	
	public Team t;
	public BuildingType bt;
	public int x;
	public int y;
	
	public static final int HOLDMAX = 500;
	
	public int hold = 0;
	
	public int id = new Random().nextInt();
	
	public int updateCountdown = 0;
	
	public Building()
	{
		
	}
	
	public Building( int x, int y, BuildingType bt, Team t )
	{
		this.t = t;
		this.bt = bt;
		this.x = x;
		this.y = y;
		if( t != null )
			hold = HOLDMAX;
	}

	public void render( Graphics g )
	{
		g.pushTransform();
		g.translate( x, y );
		if( t != null )
		{
			Color teamColor = t.getColor();
			g.setColor( new Color( teamColor.r, teamColor.g, teamColor.b, .3f ) );
			float rad = ((float)hold / (float)HOLDMAX) * bt.bu.getRadius();
			g.fillOval( -rad, -rad, rad*2, rad*2 );
			g.setColor( teamColor );
			g.drawOval( -rad, -rad, rad*2, rad*2 );
		}
		
		g.setColor( new Color( 0, 0, 0 ) );
		
		g.drawOval( -bt.bu.getRadius(), -bt.bu.getRadius(), bt.bu.getRadius()*2, bt.bu.getRadius()*2 );
		
		g.setColor( Color.black );
		g.popTransform();
	}
	
	public boolean update( TacticServer ts )
	{
		boolean updateClient = false;
		bt.update( ts, this );
		int[] teamcount = new int[32];
		Team[] teams = new Team[32]; //ugh ugly
		int cc = 0;
		int oc = 0;
		
		for( Unit u : ts.units )
		{
			float dx = u.x - x;
			float dy = u.y - y;
			float dist = (float) /*Math.sqrt*/( dx*dx + dy*dy );
			
			if( dist < bt.bu.getRadius()*bt.bu.getRadius() )
			{
				teamcount[u.owner.team.id]++;
				teams[u.owner.team.id] = u.owner.team;
			}
		}
		
		if( bt.bu.isCapturable( ts.l, this ) )
		{
			for( Unit u : ts.units )
			{
				float dx = u.x - x;
				float dy = u.y - y;
				float dist = (float) /*Math.sqrt*/( dx*dx + dy*dy );
				
				if( dist < 50 * 50 )
				{
					teamcount[u.owner.team.id]++;
					teams[u.owner.team.id] = u.owner.team;
					if( t != null )
					{
						if( u.owner.team.id == t.id )
						{
							cc++;
						}
						else
						{
							oc++;
						}
					}
					updateClient = true;
				}
			}
			
			if( cc > oc )
			{
				if( hold < HOLDMAX )
				{
					hold += cc-oc;
					if( hold > HOLDMAX )
						hold = HOLDMAX;
				}
			} else if( oc > cc )
			{
				hold -= oc-cc;
				if( hold <= 0 )
				{
					t = null;
				}
			}
			
			if( t == null )
			{
				int max = DMath.max( teamcount );
				int count = 0;
				int index = 0;
				for( int i = 0; i < teamcount.length; i++ )
				{
					if( teamcount[i] == max )
					{
						count++;
						index = i;
					}
				}
				//Make sure there isn't a tie for number of people at the position.
				if( count == 1 )
				{
					t = teams[index];
					ts.gs.get( t ).pointsTaken++;
				}
			}
		}
		
		//To keep Unit updates from getting out of hand
		if( updateCountdown > 0 )
		{
			updateCountdown--;
			return false;
		}
		
		if( updateClient )
		{
			updateCountdown = UPDATE_TIME;
		}
		
		return updateClient;
	}
	
	public enum BuildingType
	{
		CENTER( new CenterInfo() ),
		POINT( new PointInfo() ),
		PRESSUREPAD( new PressurePadInfo() );
		
		BuildingInfo bu;
		
		BuildingType( BuildingInfo bu )
		{
			this.bu = bu;
		}
		
		public void update( TacticServer ts, Building b )
		{
			if( bu != null )
			{
				bu.update( ts, b );
			}
		}
	}
	
	public static interface BuildingInfo
	{
		public void update( TacticServer ts, Building b );
		public boolean isCapturable( Level l, Building tb );
		public float getRadius();
	}
	
	public static class CenterInfo implements BuildingInfo
	{
		public void update( TacticServer ts, Building b )
		{
			b.healFriendlies( ts );
		}

		public boolean isCapturable( Level l, Building tb )
		{
			for( Building b : l.buildings )
			{
				if( b.t != null && b.t.id == tb.t.id && b.bt == BuildingType.POINT )
				{
					return false;
				}
			}
			return true;
		}

		public float getRadius()
		{
			return 50;
		}
		
	}
	
	public static class PointInfo implements BuildingInfo 
	{
		public void update( TacticServer ts, Building b )
		{
			b.healFriendlies( ts );
		}

		public boolean isCapturable( Level l, Building tb )
		{
			return true;
		}
		
		public float getRadius()
		{
			return 50;
		}
	}
	
	public static class PressurePadInfo implements BuildingInfo
	{
		boolean isSteppedOn = false;
		
		public void update( TacticServer ts, Building b )
		{
			boolean stepCheck = false;
			for( int i = 0; i < ts.units.size(); i++ )
			{
				Unit u = ts.units.get( i );
				float dx = u.x - b.x;
				float dy = u.y - b.y;
				float dist = (float) /*Math.sqrt*/( dx*dx + dy*dy );
				
				if( u.alive && dist < getRadius() * getRadius() )
				{
					stepCheck = true;
					break;
				}
			}
			isSteppedOn = stepCheck;
			ts.l.signal( b, isSteppedOn, ts );
		}

		public boolean isCapturable( Level l, Building tb )
		{
			return false;
		}
		
		public float getRadius()
		{
			return 20;
		}
	}
	
	public void healFriendlies( TacticServer ts )
	{
		for( Unit u : ts.units )
		{
			float dx = u.x - x;
			float dy = u.y - y;
			float dist = (float) /*Math.sqrt*/( dx*dx + dy*dy );
			
			if( dist < bt.bu.getRadius() * bt.bu.getRadius() )
			{
				if( t != null )
				{
					if( u.owner.team.id == t.id )
					{
						if( u.health < u.type.health )
						{
							u.health += hold / (float)HOLDMAX;
						}
					}
				}
			}
		}
	}
	
	public boolean isCapturable( Level l )
	{
		return bt.bu.isCapturable( l, this );
	}

	public boolean isCapturable( Level l, Unit u, PathFinder finder )
	{
		boolean bcheck = isCapturable( l );
		if( !bcheck ) return false;
		
		int utx = l.getTileX( u.x );
		int uty = l.getTileY( u.y );
		
		int btx = l.getTileX( this.x );
		int bty = l.getTileY( this.y );
		
		if( utx == btx && uty == bty ) return true;
		
		return finder.findPath( null, utx, uty, btx, bty ) != null;
	}
}
