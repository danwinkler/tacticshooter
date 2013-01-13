package tacticshooter;

import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;
import com.phyloa.dlib.util.DMath;

public class Building
{
	public static final int UPDATE_TIME = 5;
	
	Team t;
	BuildingType bt;
	int x;
	int y;
	int width = 50;
	int height = 50;
	
	public static final int HOLDMAX = 500;
	
	int hold = 0;
	
	int index;
	
	int updateCountdown = 0;
	
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
		g.setColor( Color.green );
		g.drawRect( -width/2, -height/2, width, height );
		
		g.setColor( new Color( 0, 0, 255, 128 ) );
		
		g.drawOval( -50, -50, 100, 100 );
		
		g.setColor( Color.black );
		//Font awtFont = new Font( "Arial", Font.BOLD, 10 );
		//TrueTypeFont font = new TrueTypeFont(awtFont, false);
		//.setFont( font );
		g.drawString( "Team: " + (t != null ? t.id : "empty"), -15, -15 );
		g.drawString( "Hold: " + hold, -15, 0 );
		g.drawString( bt.name(), -15, 15 );
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
		if( bt == BuildingType.POINT )
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
								u.health += .1f;
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
