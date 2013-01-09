package tacticshooter;

import com.phyloa.dlib.renderer.Graphics2DRenderer;

public abstract class Particle
{
	float x, y, dx, dy, friction, gx, gy;
	int life;
	
	public Particle()
	{
		life = 100;
		friction = .01f;
	}
	
	public void update()
	{
		life--;
		
		dx -= dx * friction;
		dy -= dy * friction;
		
		dx += gx;
		dy += gy;
		
		x += dx;
		y += dy;
	}
	
	public abstract void render( Graphics2DRenderer g );
}
