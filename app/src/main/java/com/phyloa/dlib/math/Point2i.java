package com.phyloa.dlib.math;

public class Point2i
{
	public int x, y;
	
	public Point2i()
	{
		
	}
	
	public Point2i( int x, int y )
	{
		this.x = x;
		this.y = y;
	}

	public void set( int x, int y )
	{
		this.x = x;
		this.y = y;
	}
	
	@Override
	public String toString()
	{
		return super.toString() + " [" + x + "," + y + "]";
	}
	
	@Override
	public boolean equals( Object o )
	{
		if( o instanceof Point2i )
		{
			Point2i p = (Point2i)o;
			return p.x == x && p.y == y;
		}
		return false;
	}
}
