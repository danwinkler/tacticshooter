package tacticshooter;

import java.util.HashMap;

import org.newdawn.slick.Music;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.Sound;

public class StaticFiles
{
	static HashMap<String, Music> music = new HashMap<String, Music>(); 
	static HashMap<String, Sound> sound = new HashMap<String, Sound>(); 
	
	static boolean ready = false;
	
	public static void loadAllMusic()
	{
		if( !ready )
		{
			new Thread( new Runnable() {
				public void run()
				{
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