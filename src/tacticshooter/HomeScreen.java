package tacticshooter;

import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;

import com.phyloa.dlib.renderer.DScreen;
import com.phyloa.dlib.renderer.DScreenHandler;

public class HomeScreen implements DScreen<GameContainer, Graphics>
{
	DScreenHandler<GameContainer, Graphics> dsh;
	
	public void onActivate( GameContainer e, DScreenHandler<GameContainer, Graphics> dsh )
	{
		this.dsh = dsh;
	}
	
	public void update( GameContainer e, int delta )
	{
		
	}

	public void render( GameContainer e, Graphics f )
	{
		
	}

	public void onExit()
	{
		
	}
}
