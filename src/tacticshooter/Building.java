package tacticshooter;

import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;
import com.phyloa.dlib.util.DMath;

public class Building
{
	public static final int UPDATE_TIME = 5;
	
	public Team t;
	public BuildingType bt;
	public int x;
	public int y;
	public int width = 50;
	public int height = 50;
	
	public static final int HOLDMAX = 500;
	
	public int hold = 0;
	
	public int index;
	
	public int updateCountdown = 0;
	
	//public float radius = 50.f;
	
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
			float rad = ((float)hold / (float)HOLDMAX) * 50;
			g.fillOval( -rad, -rad, rad*2, rad*2 );
			g.setColor( teamColor );
			g.drawOval( -rad, -rad, rad*2, rad*2 );
		}
		
		g.setColor( new Color( 0, 0, 0 ) );
		
		g.drawOval( -50, -50, 100, 100 );
		
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
		
		//FOR HEALZ
		if( bt == BuildingType.POINT || bt == BuildingType.CENTER )
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
							if( u.health < 100 )
							{
								u.health += hold / (float)HOLDMAX;
							}
						}
					}
				}
			}
		}
		
		if( isCapturable( ts.l ) )
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
		CENTER,
		POINT( new PointInfo() );
		
		BuildingInfo bu;
		
		BuildingType()
		{
			bu = null;
		}
		
		BuildingType( BuildingInfo bu )
		{
			
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
	}
	
	public static class PointInfo implements BuildingInfo 
	{
		public void update( TacticServer ts, Building b )
		{
		
		}
	}

	public boolean isCapturable( Level l ) 
	{	
		if( this.bt == BuildingType.CENTER )
		{
			for( Building b : l.buildings )
			{
				if( b.t != null && b.t.id == this.t.id && b.bt == BuildingType.POINT )
				{
					return false;
				}
			}
		}
		return true;
	}
}
