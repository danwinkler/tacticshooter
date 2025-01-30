package com.phyloa.dlib.math;

import jp.objectclub.vecmath.Point3f;
import jp.objectclub.vecmath.Vector3f;

public class Rayf
{
	public Point3f loc;
	public Vector3f dir;
	
	public Rayf( Point3f loc, Vector3f dir )
	{
		this.loc = loc;
		this.dir = dir;
	}
	
	public Rayf( float x, float y, float z, float xd, float yd, float zd )
	{
		loc = new Point3f( x, y, z );
		dir = new Vector3f( xd, yd, zd );
	}
}

