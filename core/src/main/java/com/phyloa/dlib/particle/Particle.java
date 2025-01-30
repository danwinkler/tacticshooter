package com.phyloa.dlib.particle;

import jp.objectclub.vecmath.Point3f;
import jp.objectclub.vecmath.Vector3f;

public abstract class Particle<R>
{
	public Point3f pos;
	public Vector3f speed;
	
	public float timeleft;
	
	public float friction = 0;
	
	public boolean alive = true;
	
	public Particle( float x, float y, float z, float dx, float dy, float dz, float duration )
	{
		pos = new Point3f( x, y, z );
		speed = new Vector3f( dx, dy, dz );
		this.timeleft = duration;
	}
	
	public void update( float time )
	{
		speed.scale( 1-friction );
		pos.x += speed.x * time;
		pos.y += speed.y * time;
		pos.z += speed.z * time;
		
		timeleft -= time;
		
		if( timeleft <= 0 )
		{
			alive = false;
		}
	}
	
	public abstract void render( R r );
}
