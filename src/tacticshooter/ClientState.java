package tacticshooter;

import java.util.ArrayList;
import java.util.HashMap;

import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Sound;

public class ClientState
{
	HashMap<Integer, Unit> unitMap = new HashMap<Integer, Unit>();
	ArrayList<Unit> units = new ArrayList<Unit>();
	
	HashMap<Integer, Bullet> bulletMap = new HashMap<Integer, Bullet>();
	ArrayList<Bullet> bullets = new ArrayList<Bullet>();
	
	Level l;
	
	ArrayList<Integer> selected = new ArrayList<Integer>();
	
	Player player;
	
	Sound bullet1, bullet2, ping1, death1, death2, hit1;
	
	float scrollx = 0;
	float scrolly = 0;
	
	float soundFadeDist = 1000;
	
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
