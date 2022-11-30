package com.phyloa.dlib.util;

import java.io.FileNotFoundException;
import java.util.HashMap;

public class DOptions
{
	public String filename;
	public String file;
	
	public HashMap<String, String> options;
	
	public DOptions( String filename )
	{
		this.filename = filename;
		options = new HashMap<String, String>();
		try {
			file = DFile.loadText( filename );
		} catch( FileNotFoundException e )
		{
			System.err.println( "Options file: " + filename + " not found." );
		}
		parse();
	}
	
	private void parse()
	{
		String[] lines = file.split( "\n" );
		for( int i = 0; i < lines.length; i++ )
		{
			String line = lines[i].trim();
			String[] parts = line.split( " ", 2 );
			if( parts.length != 2 )
			{
				continue;
			}
			options.put( parts[0].trim(), parts[1].trim() );
		}
	}
	
	public int getI( String s )
	{
		String get = options.get( s );
		if( get == null )
		{
			return 0;
		}
		else
		{
			return Integer.parseInt( get );
		}
	}
	
	public float getF( String s )
	{
		String get = options.get( s );
		if( get == null )
		{
			return 0;
		}
		else
		{
			return Float.parseFloat( get );
		}
	}
	
	public double getD( String s )
	{
		String get = options.get( s );
		if( get == null )
		{
			return 0;
		}
		else
		{
			return Double.parseDouble( get );
		}
	}
	
	public char getC( String s )
	{
		String get = options.get( s );
		if( get == null )
		{
			return 0;
		}
		else
		{
			return get.toCharArray()[0];
		}
	}
	
	public boolean getB( String s )
	{
		String get = options.get( s );
		if( get == null )
		{
			return false;
		}
		else
		{
			return Boolean.parseBoolean( get );
		}
	}
	
	public String getS( String s )
	{
		String get = options.get( s );
		if( get == null )
		{
			return "";
		}
		else
		{
			return get;
		}
	}
}
