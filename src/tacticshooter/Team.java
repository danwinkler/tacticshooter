package tacticshooter;

import org.newdawn.slick.Color;

public class Team
{
	public static final Color[] teamColors = { Color.red, Color.green, Color.blue, Color.orange };
	
	private static int onID = -1;
	
	private static int getID()
	{
		onID++;
		return onID;
	}
	
	int id = getID();
	int cash = 1000;
	
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
