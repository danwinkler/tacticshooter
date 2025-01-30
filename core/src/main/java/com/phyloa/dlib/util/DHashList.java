package com.phyloa.dlib.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

public class DHashList<K,V> implements Iterable<V>
{
	private HashMap<K,V> map = new HashMap<K,V>();
	private ArrayList<V> list = new ArrayList<V>();
	
	public DHashList()
	{
		
	}
	
	public void put( K key, V value )
	{
		map.put( key, value );
		list.add( value );
	}
	
	public V get( K key )
	{
		return map.get( key );
	}
	
	public V getIndex( int i )
	{
		return list.get( i );
	}
	
	public int size()
	{
		return list.size();
	}
	
	public void remove( K key )
	{
		list.remove( map.remove( key ) );
	}

	public Iterator<V> iterator()
	{
		return list.iterator();
	}
	
	public ArrayList<V> getArrayList()
	{
		return list;
	}
	
	public HashMap<K,V> getHashMap()
	{
		return map;
	}
	
	public boolean containsKey( K k )
	{
		return map.containsKey( k );
	}
}
