package tacticshooter;

public class Player
{
	public static final int MAX_RESPAWN = 60;
	
	int id;
	Team team;
	int money;
	int respawn = 0;
	boolean isBot = false;
	
	public Player()
	{
		reset();
	}
	
	public Player( int id )
	{
		this();
		this.id = id;
	}
	
	public void reset()
	{
		money = 0;
	}
}
