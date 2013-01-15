package tacticshooter;

import java.util.ArrayList;
import java.util.HashMap;

import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Sound;

public class ClientState
{
	public HashMap<Integer, Unit> unitMap = new HashMap<Integer, Unit>();
	public ArrayList<Unit> units = new ArrayList<Unit>();
	
	public HashMap<Integer, Bullet> bulletMap = new HashMap<Integer, Bullet>();
	public ArrayList<Bullet> bullets = new ArrayList<Bullet>();
	
	public Level l;
	
	public ArrayList<Integer> selected = new ArrayList<Integer>();
	
	public Player player;
	
	public Sound bullet1, bullet2, ping1, death1, death2, hit1;
	
	public float scrollx = 0;
	public float scrolly = 0;
	
	public float soundFadeDist = 1000;
	
	public void resetState()
	{
		unitMap.clear();
		units.clear();
		bulletMap.clear();
		bullets.clear();
		l = null;
		selected.clear();
		scrollx = 0;
		scrolly = 0;
		bullet1 = null;
		bullet2 = null;
		ping1 = null;
		death1 = null;
		death2 = null;
		hit1 = null;
	}
	
	public float getSoundMag( GameContainer gc, float x, float y )
	{
		float dx = (scrollx+gc.getWidth()/2) - x;
		float dy = (scrolly+gc.getHeight()/2) - y;
		return (float)((soundFadeDist - Math.sqrt( dx*dx+dy*dy )) / soundFadeDist);
	}
}
