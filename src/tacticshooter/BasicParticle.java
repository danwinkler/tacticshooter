package tacticshooter;

import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;

import com.phyloa.dlib.particle.Particle;

public class BasicParticle extends Particle<Graphics>
{
	public Color c;
	float maxdur;
	public float size;
	
	public BasicParticle( float x, float y, float dx, float dy, float duration )
	{
		super( x, y, dx, dy, duration );
		maxdur = duration;
	}

	public void update( float d )
	{
		super.update( d );
		c.a = .5f * (timeleft/maxdur);
	}
	
	public void render( Graphics r )
	{
		r.setColor( c );
		r.fillOval( x-size, y-size, size*2, size*2 );
	}
}
