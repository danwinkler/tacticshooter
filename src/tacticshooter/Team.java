package tacticshooter;

import org.newdawn.slick.Color;

public class Team
{
	public static Team a = new Team( 0 );
	public static Team b = new Team( 1 );
	
	public static final Color[] teamColors = { Color.yellow, new Color( 255, 0, 255 ), Color.blue, Color.orange };
	
	public int id;
	
	public int cash = 1000;
	
	public Team()
	{
		
	}
	
	public Team( int id )
	{
		this.id = id;
	}
	
	public void update( TacticServer ts )
	{
		
	}
	
	public boolean equals( Team t )
	{
		return this.id == t.id;
	}

	public Color getColor() 
	{
		return teamColors[id];
	}
}
