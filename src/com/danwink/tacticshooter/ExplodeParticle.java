package com.danwink.tacticshooter;

import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;

import com.phyloa.dlib.particle.Particle;
import com.phyloa.dlib.util.DMath;

public class ExplodeParticle extends Particle<Graphics>
{
	public Color c;
	float maxdur;
	public float size;
	public Image im;
	
	public ExplodeParticle( float x, float y, float dx, float dy, float duration )
	{
		super( x, y, 0, dx, dy, 0, duration );
		maxdur = duration;
	}

	public void update( float d )
	{
		super.update( d );
		c.a = .5f * DMath.minf( ((timeleft*2)/maxdur), 1 );
	}
	
	public void render( Graphics r )
	{
		r.setColor( c );
		r.drawImage( im, pos.x-size, pos.y-size, pos.x+size, pos.y+size, 0, 0, 64, 64, c );
	}
}
