package com.phyloa.dlib.math;

import jp.objectclub.vecmath.Point3f;
import jp.objectclub.vecmath.Vector3f;

public class Trianglef implements Geom
{
	public static final float EPSILON = .00001f;
	public Point3f p1;
	public Point3f p2;
	public Point3f p3;
	public int color;
	
	public Trianglef( Point3f p1, Point3f p2, Point3f p3 )
	{
		this.p1 = new Point3f( p1 );
		this.p2 = new Point3f( p2 );
		this.p3 = new Point3f( p3 );
	}
	
	public Trianglef( Point3f p1, Point3f p2, Point3f p3, int color )
	{
		this.p1 = new Point3f( p1 );
		this.p2 = new Point3f( p2 );
		this.p3 = new Point3f( p3 );
		this.color = color;
	}
	
	public void setColor( int color )
	{
		this.color = color;
	}
	
	public int getColor( float u, float v )
	{
		return color;
	}
	
	public Intersection intersects( Rayf ray )
	{
		Trianglef tri = this;
		float epsilon = .00001f;
		
		Point3f pt0 = tri.p1;
		Point3f pt1 = tri.p2;
		Point3f pt2 = tri.p3;
		Vector3f e1 = new Vector3f();
		Vector3f e2 = new Vector3f();
		e1.sub( pt1, pt0 );
		e2.sub( pt2, pt0 );
		Vector3f p = new Vector3f();
		p.cross( ray.dir, e2 );
		float a;
		a = e1.dot( p );
		if( a > -epsilon && a < epsilon )
		{
			return null;
		}
		
		float f = 1.f / a;
		Vector3f s = new Vector3f();
		s.sub( ray.loc, pt0 );
		float u = f * s.dot( p );
		
		if( u < 0 || u > 1 )
		{
			return null;
		}
		
		Vector3f q = new Vector3f();
		q.cross( s, e1 );
		float v = f * ray.dir.dot( q );
		
		if( v < 0 || u + v > 1 )
		{
			return null;
		}
		
		float t = f * e2.dot( q );
		
		if( t < 0 )
		{
			return null;
		}
		t *= .99999;
		Point3f pos = new Point3f();
		pos.set( ray.dir );
		pos.scale( t );
		
		Vector3f dist = new Vector3f();
		dist.set( pos );
		pos.add( ray.loc );
		return new Intersection( pos, ray, tri );
	}
}