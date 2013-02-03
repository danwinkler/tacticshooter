package tacticshooter;

public class Player
{
	public static final int MAX_RESPAWN = 6;
	
	public int id;
	public Team team;
	public int money;
	public int respawn = 0;
	public boolean isBot = false;
	public String name;
	
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
