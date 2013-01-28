package tacticshooter;

import java.io.FileNotFoundException;
import java.util.HashMap;

import org.newdawn.slick.Music;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.Sound;

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
			names = DFile.loadText( "data/dist.male.first.txt" );
		} catch( FileNotFoundException e )
		{
			System.err.println( "Make sure the names file is located at: data/dist.male.first.txt" );
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
					loadMusic( "menu", "sound/Deliberate Thought.ogg" );
					loadMusic( "play1", "sound/Decisions.ogg" );
					loadMusic( "play2", "sound/Finding the Balance.ogg" );
					loadMusic( "play3", "sound/Rising.ogg" );
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
}
