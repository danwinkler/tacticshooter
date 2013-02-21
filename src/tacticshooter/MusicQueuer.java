package tacticshooter;

import com.badlogic.gdx.audio.Music;

public class MusicQueuer
{
	String[] names;
	int onCount;
	
	MusicListener ml;
	
	boolean playing = true;
	
	public MusicQueuer( int onCount, String... names )
	{
		ml = new MusicListener();
		ml.current = StaticFiles.getMusic( names[onCount] );
		this.onCount = onCount;
		this.names = names;
	}
	
	public void start()
	{
		new Thread( ml ).start();
	}
	
	public class MusicListener implements Runnable
	{
		Music current;
		public void run()
		{
			while( playing )
			{
				while( current.isPlaying() )
				{
					try
					{
						Thread.sleep( 100 );
					} catch( InterruptedException e )
					{
						e.printStackTrace();
					}
				}
				onCount = (onCount+1) % names.length;
				current = StaticFiles.getMusic( names[onCount] );
				current.play();
			}
		}
	}
	
	public void stop()
	{
		ml.current.stop();
		playing = false;
	}
}
