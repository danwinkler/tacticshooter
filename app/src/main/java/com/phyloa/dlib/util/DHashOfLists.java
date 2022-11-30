package com.phyloa.dlib.util;

import java.util.ArrayList;
import java.util.HashMap;

import com.esotericsoftware.kryonet.Connection;

public class DHashOfLists<K,V>
{
	HashMap<K, ArrayList<V>> map = new HashMap<K, ArrayList<V>>();
	
	public void put( K k, V v )
	{
		ArrayList<V> list = map.get( k );
		if( list == null )
		{
			list = new ArrayList<>();
			map.put( k, list );
		}
		list.add( v );
	}
	
	public ArrayList<V> remove( K k )
	{
		ArrayList<V> r = map.remove( k );
		return r == null ? new ArrayList<V>() : r;
	}
	
	public ArrayList<V> get( K k )
	{
		ArrayList<V> r = map.get( k );
		return r == null ? new ArrayList<V>() : r;
	}

	public void remove( K k, V v )
	{
		ArrayList<V> a = map.get( k );
		if( a != null )
		{
			a.remove( v );
		}
	}
}
