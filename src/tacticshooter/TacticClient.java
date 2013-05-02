package tacticshooter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.HashMap;

import org.newdawn.slick.AngelCodeFont;
import org.newdawn.slick.AppGameContainer;
import org.newdawn.slick.BasicGame;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.SlickException;

import com.danwink.tacticshooter.screens.BrowseOnlineLevelsScreen;
import com.danwink.tacticshooter.screens.HomeScreen;
import com.danwink.tacticshooter.screens.LevelEditor;
import com.danwink.tacticshooter.screens.LevelEditorLoadMap;
import com.danwink.tacticshooter.screens.LevelEditorNewMapSetup;
import com.danwink.tacticshooter.screens.LevelEditorSetup;
import com.danwink.tacticshooter.screens.LobbyScreen;
import com.danwink.tacticshooter.screens.LoginScreen;
import com.danwink.tacticshooter.screens.MessageScreen;
import com.danwink.tacticshooter.screens.MultiplayerGameScreen;
import com.danwink.tacticshooter.screens.MultiplayerSetupScreen;
import com.danwink.tacticshooter.screens.OptionsScreen;
import com.danwink.tacticshooter.screens.PostGameScreen;
import com.danwink.tacticshooter.screens.ServerConnectScreen;
import com.danwink.tacticshooter.screens.SettingsScreen;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.AppState;
import com.jme3.app.state.AppStateManager;
import com.phyloa.dlib.renderer.DScreenHandler;
import com.phyloa.dlib.util.DFile;

public class TacticClient extends SimpleApplication
{
	HashMap<String, AppState> states = new HashMap<String, AppState>();
	
	public TacticClient()
	{
		
	}

	@Override
	public void simpleInitApp() 
	{
		AppStateManager asm = this.getStateManager();
		
		states.put( "home", new HomeScreen() );
		dsh.register( "login", new LoginScreen() );
		
		dsh.register( "multiplayersetup", new MultiplayerSetupScreen() );
		dsh.register( "multiplayergame", new MultiplayerGameScreen() );
		dsh.register( "connect", new ServerConnectScreen() );
		dsh.register( "lobby", new LobbyScreen() );
		
		dsh.register( "message", new MessageScreen() );
		dsh.register( "postgame", new PostGameScreen() );
		
		dsh.register( "editorsetup", new LevelEditorSetup() );
		dsh.register( "newmap", new LevelEditorNewMapSetup() );
		dsh.register( "loadmap", new LevelEditorLoadMap() );
		dsh.register( "editor", new LevelEditor() );
		dsh.register( "levelbrowser", new BrowseOnlineLevelsScreen() );
		
		dsh.register( "settings", new SettingsScreen() );
		dsh.register( "options", new OptionsScreen( "options.txt", "settings" ) );
		dsh.register( "advoptions", new OptionsScreen( "data" + File.separator + "advoptions.txt", "settings" ) );
		
		dsh.activate( "home", null );
		
		new Thread( new Runnable() {
			public void run()
			{
				try
				{
					String[] loginFile = (String[])DFile.loadObject( "data" + File.separator + "l.tmp" );
					StaticFiles.login( loginFile[0], loginFile[1] );
					dsh.message( "home", null );
				}
				catch( Exception ex )
				{
					
				}
			}
		}).start();
		
		try
		{
			f = new AngelCodeFont( "data" + File.separator + "pixelfont1_16px.fnt", "data" + File.separator + "pixelfont1_16px_0.png" );
		} catch( SlickException e )
		{
			e.printStackTrace();
		}
		
		//gc.setMusicVolume( StaticFiles.options.getF( "slider.music" ) );
		//gc.setSoundVolume( StaticFiles.options.getF( "slider.sound" ) );
	}
	
	@Override
	public void simpleUpdate( float delta ) 
	{
		dsh.update( null, (int)(delta*1000) );
	}
	
	public static void main( String[] args )
	{
		TacticClient tc = new TacticClient();
		tc.start();
	}
}
