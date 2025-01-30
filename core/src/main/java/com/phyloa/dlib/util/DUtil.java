package com.phyloa.dlib.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class DUtil
{
	public static int[] integerArrayListToIntArray( ArrayList<Integer> list )
	{
		int[] ret = new int[list.size()];
		for( int i = 0; i < list.size(); i++ )
		{
			ret[i] = list.get( i );
		}
		return ret;
	}
	
	public static String capitalize( String s )
	{
		return s.substring(0, 1).toUpperCase() + s.substring(1);
	}
	
	/**
	 * From http://stackoverflow.com/a/17376764/356882
	 * @param i
	 * @return String representation of i in Roman Numerals
	 */
	public static String romanNumeral( int i )
	{
		LinkedHashMap<String, Integer> roman_numerals = new LinkedHashMap<String, Integer>();
		roman_numerals.put("M", 1000);
		roman_numerals.put("CM", 900);
		roman_numerals.put("D", 500);
		roman_numerals.put("CD", 400);
		roman_numerals.put("C", 100);
		roman_numerals.put("XC", 90);
		roman_numerals.put("L", 50);
		roman_numerals.put("XL", 40);
		roman_numerals.put("X", 10);
		roman_numerals.put("IX", 9);
		roman_numerals.put("V", 5);
		roman_numerals.put("IV", 4);
		roman_numerals.put("I", 1);
		String res = "";
		for( Map.Entry<String, Integer> entry : roman_numerals.entrySet() )
		{
			int matches = i / entry.getValue();
			res += repeat( entry.getKey(), matches );
			i = i % entry.getValue();
		}
		return res;
	}
	
	public static String repeat( String s, int n )
	{
		if( s == null )
		{
			return null;
		}
		final StringBuilder sb = new StringBuilder();
		for( int i = 0; i < n; i++ )
		{
			sb.append( s );
		}
		return sb.toString();
	}
	  
	@SuppressWarnings( "unchecked" )
	public static <C> List<Class<? extends C>> getSubclasses( Class<?> main, Class<C> sup )
	{
		List<Class<?>> list = Arrays.asList( main.getDeclaredClasses() );
		List<Class<? extends C>> rList = new ArrayList<>();
		for( Class<?> c : list ) 
		{
			if( sup.isAssignableFrom( c ) ) 
			{
				rList.add( (Class<? extends C>)c );
			}
		}
		return rList;
	}
}
