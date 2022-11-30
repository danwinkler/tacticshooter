package com.phyloa.dlib.math;

import jp.objectclub.vecmath.Point3f;

public class Intersection
{
	Rayf ray;
	Geom g;
	Point3f loc;
	
	public Intersection( Point3f loc, Rayf ray, Geom g )
	{
		this.ray = ray;
		this.g = g;
		this.loc = loc;
	}
	
	public float getDist()
	{
		return loc.distance( ray.loc );
	}
	
	public float getDist2()
	{
		return loc.distanceSquared( ray.loc );
	}
	
	public Geom getGeom()
	{
		return g;
	}
	
	public Point3f getLoc()
	{
		return loc;
	}
}


