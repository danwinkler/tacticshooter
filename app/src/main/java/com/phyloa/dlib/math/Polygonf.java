package com.phyloa.dlib.math;

import java.util.ArrayList;

import jp.objectclub.vecmath.Point2f;
import jp.objectclub.vecmath.Vector2f;

import com.phyloa.dlib.util.DMath;

public class Polygonf
{
	ArrayList<Point2f> points;
	
	Vector2f center;
	
	public Polygonf()
	{
		points = new ArrayList<Point2f>();
	}
	
	public void add( Point2f vec )
	{
		points.add( vec );
		center = null;
	}
	
	public boolean contains( Point2f loc )
	{
		if( points.size() == 0 ) return false;
		
		Point2f farthest = points.get( 0 );
		for( int i = 1; i < points.size(); i++ )
		{
			if( farthest.x < points.get( i ).x )
			{
				farthest = points.get( i );
			}
		}
		
		farthest = new Point2f( farthest.x + 10, farthest.y );
		
		int colCount = 0;
		for( int i = 0; i < points.size(); i++ )
		{
			Point2f a = points.get( i );
			Point2f b = points.get( (i+1)%points.size() );
			if( DMath.lineLineIntersection( a, b, farthest, loc ) != null )
			{
				colCount++;
			}
		}
		
		return colCount % 2 == 1;
	}

	public Vector2f getCenter()
	{
		center = new Vector2f();
		for( int i = 0; i < points.size(); i++ )
		{
			center.add( points.get( i ) );
		}
		center.x /= points.size();
		center.y /= points.size();
		return center;
	}

	public int numPoints()
	{
		return points.size();
	}

	public Point2f getPoint( int i )
	{
		return points.get( i );
	}
}
