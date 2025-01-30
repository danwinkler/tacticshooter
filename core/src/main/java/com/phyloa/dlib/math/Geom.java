package com.phyloa.dlib.math;

public interface Geom 
{
	public int getColor( float u, float v );
	public Intersection intersects( Rayf g );
}
