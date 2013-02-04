package tacticshooter;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;

import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.Music;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.Sound;

import com.danwink.tacticshooter.screens.DScreenSlideTransition;
import com.phyloa.dlib.renderer.DScreenTransition;
import com.phyloa.dlib.util.DFile;
import com.phyloa.dlib.util.DOptions;

public class StaticFiles
{
	static HashMap<String, Music> music = new HashMap<String, Music>(); 
	static HashMap<String, Sound> sound = new HashMap<String, Sound>(); 
	
	public static DOptions options = new DOptions( "options.txt" );
	
	static boolean ready = false;
	
	private static boolean started = false;
	public static String names;
	
	static
	{
		try
		{
			names = DFile.loadText( "data" + File.separator + "dist.male.first.txt" );
		} catch( FileNotFoundException e )
		{
			System.err.println( "Make sure the names file is located at: data" + File.separator + "dist.male.first.txt" );
		}
	}
	
	public static void loadAllMusic()
	{
		if( !ready && !started )
		{
			new Thread( new Runnable() {
				public void run()
				{
					started = true;
					loadMusic( "menu", "sound" + File.separator + "Deliberate Thought.ogg" );
					loadMusic( "play1", "sound" + File.separator + "Decisions.ogg" );
					loadMusic( "play2", "sound" + File.separator + "Finding the Balance.ogg" );
					loadMusic( "play3", "sound" + File.separator + "Rising.ogg" );
					ready = true;
				} 
			}).start();
		}
	}
	
	static void loadMusic( String name, String file )
	{
		try
		{
			music.put( name, new Music( file ) );
		} catch( SlickException e )
		{
			
		}
	}
	
	static void loadSound( String name, String file )
	{
		try
		{
			sound.put( name, new Sound( file ) );
		} catch( SlickException e )
		{
			
		}
	}
	
	public static void loopMusic( String name )
	{
		if( ready )
		{
			getMusic( name ).loop();
		}
		else
		{
			loopWhenReady( name );
		}
	}
	
	public static void loopWhenReady( final String name )
	{
		new Thread( new Runnable() {
			public void run()
			{
				Music m;
				while( true )
				{
					if( (m = getMusic( name )) != null )
					{
						if( !m.playing() )
							m.loop();
						break;
					}
				}
			} 
		}).start();
	}
	
	public static Music getMusic( String name )
	{
		return music.get( name );
	}
	
	public static Sound getSound( String name )
	{
		return sound.get( name );
	}
	
	public static DScreenTransition<GameContainer, Graphics> getDownMenuOut()
	{
		return new DScreenSlideTransition( -1, 0, StaticFiles.options.getF( "menuTransitionSpeed" ), false );
	}
	
	public static DScreenTransition<GameContainer, Graphics> getDownMenuIn()
	{
		return new DScreenSlideTransition( -1, 0, StaticFiles.options.getF( "menuTransitionSpeed" ), true );
	}
	
	public static DScreenTransition<GameContainer, Graphics> getUpMenuOut()
	{
		return new DScreenSlideTransition( 1, 0, StaticFiles.options.getF( "menuTransitionSpeed" ), false );
	}
	
	public static DScreenTransition<GameContainer, Graphics> getUpMenuIn()
	{
		return new DScreenSlideTransition( 1, 0, StaticFiles.options.getF( "menuTransitionSpeed" ), true );
	}
}
