package com.phyloa.dlib.math;

import java.util.ArrayList;

import com.phyloa.dlib.util.DMath;

public class GeomOctTree
{
	public OctNode root;
	
	public GeomOctTree()
	{
		root = new OctNode( 0, 0, 0, 1 );
	}
	
	public void addGeom( Geom g )
	{
		if( g instanceof Trianglef )
		{
			Trianglef t = (Trianglef)g;
			addTriangle( t,
							DMath.maxf( t.p1.x, t.p2.x, t.p3.x ),
							DMath.maxf( t.p1.y,t.p2.y, t.p3.y ),
							DMath.maxf( t.p1.z,t.p2.z, t.p3.z ));
		}
	}
	
	private void addTriangle( Trianglef t, float maxx, float maxy, float maxz )
	{
		int scale = 1;
		while( scale < maxx && scale < maxy && scale < maxz )
		{
			scale *= 2;
			if( root.size < scale )
			{
				OctNode oldRoot = root;
				root = new OctNode( 0, 0, 0, scale );
				root.subs[0][0][0] = oldRoot;
			}
		}
		
		root.addTriangle( t );
	}
	
	public Intersection closestIntersect( Rayf r )
	{
		return root.closestIntersect( r );
	}
	
	public class OctNode
	{
		public OctNode[][][] subs = new OctNode[2][2][2];
		int size;
		public ArrayList<Geom> geom = new ArrayList<Geom>();
		
		Boxf box;
		int lx, ly, lz;
		
		public OctNode( int x, int y, int z, int size )
		{
			box = new Boxf( x+(size*.5f), y+(size*.5f), z+(size*.5f), size, size, size );
			this.size = size;
			this.lx = x;
			this.ly = y;
			this.lz = z;
		}
		
		public void addTriangle( Trianglef t )
		{
			if( size == 1 )
			{
				geom.add( t );
				return;
			}
			for( int x = 0; x < 2; x++ )
			{
				for( int y = 0; y < 2; y++ )
				{
					for( int z = 0; z < 2; z++ )
					{
						OctNode n = subs[x][y][z];
						if( n == null )
						{
							int newSize = size/2;
							n = new OctNode( lx + newSize*x, ly + newSize*y, lz + newSize*z, newSize );
							subs[x][y][z] = n;
						}
						if( n.box.contains( t.p1 ) && n.box.contains( t.p2 ) && n.box.contains( t.p3 ) )
						{
							n.addTriangle( t );
							return;
						}
					}
				}
			}
			geom.add( t );
		}
		
		public Intersection closestIntersect( Rayf r )
		{
			Intersection closest = null;
			float d2 = Float.MAX_VALUE;
			for( int i = 0; i < geom.size(); i++ )
			{
				Intersection temp = geom.get( i ).intersects( r );
				if( temp != null )
				{
					float tempd2 = temp.getDist2();
					if( tempd2 < d2 )
					{
						closest = temp;
						d2 = tempd2;
					}
				}
			}
			for( int x = 0; x < 2; x++ )
			{
				for( int y = 0; y < 2; y++ )
				{
					for( int z = 0; z < 2; z++ )
					{
						OctNode n = subs[x][y][z];
						if( n != null )
						{
							if( n.box.intersects( r ) != null )
							{
								Intersection temp = n.closestIntersect( r );
								if( temp != null )
								{
									float tempd2 = temp.getDist2();
									if( tempd2 < d2 )
									{
										closest = temp;
										d2 = tempd2;
									}
								}
							}
						}
					}
				}
			}
			return closest;
		}
	}
}

