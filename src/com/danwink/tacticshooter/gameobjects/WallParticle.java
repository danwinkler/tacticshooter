package com.danwink.tacticshooter.gameobjects;

import org.newdawn.slick.Graphics;

import com.phyloa.dlib.particle.Particle;
import com.phyloa.dlib.util.DMath;

public class WallParticle extends Particle<Graphics>
{
	public static WallParticle makeParticle( float x, float y )
	{
		float angle = DMath.randomf( 0, DMath.PI2F );
		return new WallParticle( x, y, DMath.cosf( angle ) * 5.f, DMath.sinf( angle ) * 5.f, DMath.randomf( .4f, 2f ) );
	}
	
	public WallParticle( float x, float y, float dx, float dy, float duration )
	{
		super( x, y, 0, dx, dy, 0, duration );
	}

	public void render( Graphics g )
	{
		g.drawLine( pos.x, pos.y, pos.x+speed.x, pos.y+speed.y );
	}
}
