package tacticshooter;

import java.io.FileNotFoundException;
import java.io.PrintWriter;

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
import com.danwink.tacticshooter.screens.OptionsScreen;
import com.danwink.tacticshooter.screens.PostGameScreen;
import com.danwink.tacticshooter.screens.SelectMapScreen;
import com.danwink.tacticshooter.screens.SettingsScreen;
import com.phyloa.dlib.renderer.DScreenHandler;
import com.phyloa.dlib.util.DFile;

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
		dsh.register( "options", new OptionsScreen() );
		
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
	
	public static void main( String[] args ) throws FileNotFoundException
	{
		//Attempt to avoid sealed exception errors on zoe's mac
		try
		{
			Class.forName( "javax.vecmath.Point2i" );
			
			AppGameContainer app = new AppGameContainer( new TacticClient() );
			app.setDisplayMode( StaticFiles.options.getI( "windowWidth" ), StaticFiles.options.getI( "windowHeight" ), StaticFiles.options.getB( "fullscreen" ) );
			app.setVSync( StaticFiles.options.getB( "vsync" ) );
			app.setUpdateOnlyWhenVisible( false );
			app.setAlwaysRender( true );
			app.start();
		} catch( Exception ex )
		{
			PrintWriter pw = new PrintWriter( "tmp/error.log" );
			ex.printStackTrace( pw );
			ex.printStackTrace();
			
			pw.flush();
			pw.close();
			
			System.exit( 1 );
		}
	}
}
