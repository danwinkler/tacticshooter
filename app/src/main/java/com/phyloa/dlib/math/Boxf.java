package com.phyloa.dlib.math;

import jp.objectclub.vecmath.Point3f;

import com.phyloa.dlib.renderer.ShapeType;
import com.phyloa.dlib.util.DGraphics;

public class Boxf implements Geom
{
	Trianglef[] tris = new Trianglef[12];
	int color;
	float x, y, z, width, height, length;
	
	public Boxf( float x, float y, float z, float width, float height, float length, int color )
	{
		this.color = color;
		float x2 = width/2;
		float y2 = height/2;
		float z2 = length/2;
		Point3f p000 = new Point3f( x-x2, y-y2, z-z2 );
		Point3f p100 = new Point3f( x+x2, y-y2, z-z2 );
		Point3f p110 = new Point3f( x+x2, y+y2, z-z2 );
		Point3f p010 = new Point3f( x-x2, y+y2, z-z2 );
		Point3f p001 = new Point3f( x-x2, y-y2, z+z2 );
		Point3f p101 = new Point3f( x+x2, y-y2, z+z2 );
		Point3f p111 = new Point3f( x+x2, y+y2, z+z2 );
		Point3f p011 = new Point3f( x-x2, y+y2, z+z2 ); 
		//TOP
		tris[0] = new Trianglef( p001, p101, p111 );
		tris[1] = new Trianglef( p001, p011, p111 );
		//BOTTOM
		tris[2] = new Trianglef( p000, p100, p110 );
		tris[3] = new Trianglef( p000, p010, p110 );
		//FRONT
		tris[4] = new Trianglef( p010, p110, p111 );
		tris[5] = new Trianglef( p010, p011, p111 );
		//BACK
		tris[6] = new Trianglef( p000, p100, p101 );
		tris[7] = new Trianglef( p000, p001, p101 );
		//LEFT
		tris[8] = new Trianglef( p000, p010, p011 );
		tris[9] = new Trianglef( p000, p001, p011 );
		//RIGHT
		tris[10] = new Trianglef( p100, p110, p111 );
		tris[11] = new Trianglef( p100, p101, p111 );
		this.x = x;
		this.y = y;
		this.z = z;
		this.width = width;
		this.height = height;
		this.length = length;
	}
	
	public Boxf( float x, float y, float z, float width, float height, float length )
	{
		this( x, y, z, width, height, length, DGraphics.rgb( 255, 0, 0 ) );
	}
	
	public int getColor( float u, float v )
	{
		return color;
	}

	public Intersection intersects( Rayf g )
	{
		for( int i = 0; i < tris.length; i++ )
		{
			Intersection in = tris[i].intersects( g );
			if( in != null )
			{
				return in;
			}
		}
		return null;
	}
	
	public boolean contains( Point3f p )
	{
		float w2 = width/2;
		float h2 = height/2;
		float l2 = length/2;
		float x1 = x - w2;
		float y1 = y - h2;
		float z1 = z - l2;
		float x2 = x + w2;
		float y2 = y + h2;
		float z2 = z + l2;
		if( p.x < x1 || p.x > x2 )
			return false;
		if( p.y < y1 || p.y > y2 )
			return false;
		if( p.z < z1 || p.z > z2 )
			return false;
		return true;
	}
}
