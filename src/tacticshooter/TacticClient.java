package tacticshooter;

import org.newdawn.slick.AppGameContainer;
import org.newdawn.slick.BasicGame;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.SlickException;

import com.danwink.tacticshooter.screens.HomeScreen;
import com.danwink.tacticshooter.screens.LevelEditor;
import com.danwink.tacticshooter.screens.LevelEditorLoadMap;
import com.danwink.tacticshooter.screens.LevelEditorNewMapSetup;
import com.danwink.tacticshooter.screens.LevelEditorSetup;
import com.danwink.tacticshooter.screens.MessageScreen;
import com.danwink.tacticshooter.screens.MultiplayerGameScreen;
import com.danwink.tacticshooter.screens.MultiplayerSetupScreen;
import com.danwink.tacticshooter.screens.PostGameScreen;
import com.danwink.tacticshooter.screens.SelectMapScreen;
import com.danwink.tacticshooter.screens.SettingsScreen;
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
		
		dsh.register( "editorsetup", new LevelEditorSetup() );
		dsh.register( "newmap", new LevelEditorNewMapSetup() );
		dsh.register( "loadmap", new LevelEditorLoadMap() );
		dsh.register( "editor", new LevelEditor() );
		
		dsh.register( "settings", new SettingsScreen() );
		dsh.register( "selectMaps", new SelectMapScreen() );
		
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
	
	public static void main( String[] args ) throws SlickException, ClassNotFoundException
	{
		//Attempt to avoid sealed exception errors on zoe's mac
		Class.forName( "javax.vecmath.Point2i" );
		
		AppGameContainer app = new AppGameContainer( new TacticClient() );
		app.setDisplayMode(1024, 768, false);
		app.start();
	}
}
