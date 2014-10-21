package com.danwink.tacticshooter;

import java.io.File;
import java.util.HashMap;

import org.newdawn.slick.Image;
import org.newdawn.slick.SlickException;

public class Theme
{
	static HashMap<String, Theme> themes = new HashMap<String, Theme>(); 
	
	String name;
	
	public Image crater;
	public Image grate;
	public Image wall;
	public Image floor;
	
	private Theme( String name ) throws SlickException
	{
		this.name = name;
		
		crater = load( "crater" );
		grate = load( "grate" );
		wall = load( "wall" );
		floor = load( "floor" );
	}
	
	private Image load( String s ) throws SlickException
	{
		return new Image( "themes" + File.separator + name + File.separator + s + ".png" );
	}
	
	public static Theme getTheme( String name ) throws SlickException
	{
		Theme t = themes.get( name );
		if( t == null )
		{
			t = new Theme( name );
			themes.put( name, t );
		}
		return t;
	}
}
