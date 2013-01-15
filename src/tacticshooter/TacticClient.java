package tacticshooter;

import org.newdawn.slick.AppGameContainer;
import org.newdawn.slick.BasicGame;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.SlickException;

import com.danwink.tacticshooter.screens.HomeScreen;
import com.danwink.tacticshooter.screens.MessageScreen;
import com.danwink.tacticshooter.screens.MultiplayerGameScreen;
import com.danwink.tacticshooter.screens.MultiplayerSetupScreen;
import com.danwink.tacticshooter.screens.PostGameScreen;
import com.phyloa.dlib.renderer.DScreenHandler;

public class TacticClient extends BasicGame
{
	DScreenHandler<GameContainer, Graphics> dsh = new DScreenHandler<GameContainer, Graphics>();
	
	public TacticClient()
	{
		super( "Tactic Shooter Client" );
	}

	public void init( GameContainer gc ) throws SlickException
	{
		dsh.register( "home", new HomeScreen() );
		dsh.register( "multiplayersetup", new MultiplayerSetupScreen() );
		dsh.register( "multiplayergame", new MultiplayerGameScreen() );
		dsh.register( "message", new MessageScreen() );
		dsh.register( "postgame", new PostGameScreen() );
		
		dsh.activate( "home", gc );
	}
	
	public void update( GameContainer gc, int delta ) throws SlickException
	{
		dsh.update( gc, delta );
	}

	public void render( GameContainer gc, Graphics g ) throws SlickException 
	{
		dsh.render( gc, g );
	}
	
	public static void main( String[] args ) throws SlickException
	{
		AppGameContainer app = new AppGameContainer( new TacticClient() );
		app.setDisplayMode(1024, 768, false);
		app.start();
	}
}
