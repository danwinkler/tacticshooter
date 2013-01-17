package tacticshooter;

import org.newdawn.slick.Music;
import org.newdawn.slick.MusicListener;

public class MusicQueuer implements MusicListener
{
	String[] names;
	int onCount;
	public MusicQueuer( int onCount, String... names )
	{
		onCount = (onCount+1) % names.length;
		this.names = names;
	}
	
	public void musicEnded( Music arg0 )
	{
		arg0.removeListener( this );
		Music m = StaticFiles.getMusic( names[onCount] );
		m.play();
		m.addListener( new MusicQueuer( onCount, names ) );
	}

	public void musicSwapped( Music arg0, Music arg1 )
	{
		
	}
}
