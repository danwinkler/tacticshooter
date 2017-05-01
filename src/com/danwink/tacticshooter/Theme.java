package com.danwink.tacticshooter;

import java.io.File;
import java.util.HashMap;

import org.newdawn.slick.Image;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.SpriteSheet;

public class Theme
{
	static HashMap<String, Theme> themes = new HashMap<String, Theme>(); 
	
	String name;
	
	public Image crater;
	public Image grate;
	public Image wall;
	public Image floor;
	public Image smoke;
	
	public SpriteSheet light;
	public SpriteSheet lightColor;
	public SpriteSheet heavy;
	public SpriteSheet heavyColor;
	public SpriteSheet dir4;
	public SpriteSheet dir8;
	
	private Theme( String name ) throws SlickException
	{
		this.name = name;
		
		crater = load( "crater" );
		grate = load( "grate" );
		wall = load( "wall" );
		floor = load( "floor" );
		smoke = load( "smoke" );
		
		light = loadSS( "light", 16 );
		lightColor = loadSS( "light_color", 16 );
		heavy = loadSS( "heavy", 32 );
		heavyColor = loadSS( "heavy_color", 32 );
		
		dir4 = loadSS( "4dirtest", 16 );
		dir8 = loadSS( "8dirtest", 48 );
	}
	
	private Image load( String s ) throws SlickException
	{
		return new Image( "themes" + File.separator + name + File.separator + s + ".png" );
	}
	
	private SpriteSheet loadSS( String s, int size ) throws SlickException
	{
		return new SpriteSheet( load( s ), size, size );
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
