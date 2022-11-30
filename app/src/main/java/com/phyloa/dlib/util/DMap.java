package com.phyloa.dlib.util;

import java.util.HashMap;

public class DMap<A, B>
{
	HashMap<A, B> m1;
	HashMap<B, A> m2;
	
	public DMap()
	{
		m1 = new HashMap<A, B>();
		m2 = new HashMap<B, A>();
	}
	
	public A getA( B b )
	{
		return m2.get( b );
	}
	
	public B getB( A a )
	{
		return m1.get( a );
	}
	
	public void put( A a, B b )
	{
		m1.put( a, b );
		m2.put( b, a );
	}
}
