package tacticshooter;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g2d.BitmapFont;

import com.phyloa.dlib.util.DFile;
import com.phyloa.dlib.util.DOptions;

public class StaticFiles
{
	static HashMap<String, Music> music = new HashMap<String, Music>(); 
	static HashMap<String, Sound> sound = new HashMap<String, Sound>(); 
	
	public static DOptions options = new DOptions( "options.txt" );
	public static DOptions advOptions = new DOptions( "data" + File.separator + "advoptions.txt" );
	
	public static UserInfo user;

	static boolean ready = false;
	
	private static boolean started = false;
	public static String names;
	
	static BitmapFont f;
	
	static
	{
		try
		{
			names = DFile.loadText( "data" + File.separator + "dist.male.first.txt" );
		} catch( FileNotFoundException e )
		{
			System.err.println( "Make sure the names file is located at: data" + File.separator + "dist.male.first.txt" );
		}
		
		//f = new BitmapFont( new FileHandle( "data" + File.separator + "pixelfont1_16px.fnt" ), new FileHandle( "data" + File.separator + "pixelfont1_16px_0.png" ), false );
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
		music.put( name, Gdx.audio.newMusic( new FileHandle( file ) ) );
	}
	
	static void loadSound( String name, String file )
	{
		sound.put( name, Gdx.audio.newSound( new FileHandle( file ) ) );
	}
	
	public static void loopMusic( String name )
	{
		if( ready )
		{
			Music m = getMusic( name );
			m.setLooping( true );
			m.play();
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
						if( !m.isPlaying() )
						{
							m.setLooping( true );
							m.play();
						}
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
	
	public static boolean login( String username, String password )
	{
		String s = "";
		try
		{
			HttpClient client = new DefaultHttpClient();
			HttpPost httppost = new HttpPost( "http://www.tacticshooter.com/user/checkLogin" );
	
			ArrayList<NameValuePair> list = new ArrayList<NameValuePair>();
	
			list.add( new BasicNameValuePair( "username", username.trim() ) );
			list.add( new BasicNameValuePair( "password", password ) );
	
			httppost.setEntity( new UrlEncodedFormEntity( list ) );
	
			HttpResponse r = client.execute( httppost );
			
			s = EntityUtils.toString( r.getEntity() );
			
			user = new UserInfo( username.trim(), password );
		} 
		catch( Exception ex )
		{
			return false;
		}
		
		return s.trim().equals( "1" );
	}
	
	public static Object getUsername() 
	{
		return user != null ? user.username : "Player";
	}
}
