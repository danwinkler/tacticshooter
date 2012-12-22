package tacticshooter;

public class Team
{
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
}
