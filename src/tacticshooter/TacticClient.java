package tacticshooter;

import java.awt.Font;
import java.awt.FontFormatException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
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
import com.phyloa.dlib.renderer.DScreenHandler;
import com.phyloa.dlib.util.DFile;

public class TacticClient extends Game
{
	public HomeScreen home;
	public LoginScreen login;
	
	public MultiplayerSetupScreen multiplayersetup;
	public MultiplayerGameScreen multiplayergame;
	public ServerConnectScreen connect;
	public LobbyScreen lobby;
	
	public MessageScreen message;
	public PostGameScreen postgame;
	
	public LevelEditorSetup editorsetup;
	public LevelEditorNewMapSetup newmap;
	public LevelEditorLoadMap loadmap;
	public LevelEditor editor;
	
	public SettingsScreen settings;
	public OptionsScreen options;
	public OptionsScreen advoptions;
	public TacticClient()
	{
		
	}

	public void create()
	{
		home = new HomeScreen( this );
		/*login = new LoginScreen();
		
		multiplayersetup = new MultiplayerSetupScreen();
		multiplayergame = new MultiplayerGameScreen();
		connect = new ServerConnectScreen();
		lobby = new LobbyScreen();
		
		message = new MessageScreen();
		postgame = new PostGameScreen();
		
		editorsetup = new LevelEditorSetup();
		newmap = new LevelEditorNewMapSetup();
		loadmap = new LevelEditorLoadMap();
		editor = new LevelEditor();
		
		settings = new SettingsScreen();
		options = new OptionsScreen( "options.txt", "settings" );
		advoptions = new OptionsScreen( "data" + File.separator + "advoptions.txt", "settings" );
		*/
		setScreen( home );
		
		new Thread( new Runnable() {
			public void run()
			{
				try
				{
					String[] loginFile = (String[])DFile.loadObject( "data" + File.separator + "l.tmp" );
					StaticFiles.login( loginFile[0], loginFile[1] );
				}
				catch( Exception ex )
				{
					
				}
			}
		}).start();
		
		//gc.setMusicVolume( StaticFiles.options.getF( "slider.music" ) );
		//gc.setSoundVolume( StaticFiles.options.getF( "slider.sound" ) );
	}
	
	public static void main( String[] args )
	{
		try
		{
	        TacticClient tc = new TacticClient();
	 
	        // whether to use OpenGL ES 2.0
	        boolean useOpenGLES2 = true;
	 
	        // create the game
	        LwjglApplication app = new LwjglApplication( tc, "Tactic Shooter", StaticFiles.options.getI( "windowWidth" ), StaticFiles.options.getI( "windowHeight" ), useOpenGLES2 );
	        
		} catch( Exception ex )
		{
			ex.printStackTrace();
			
			PrintWriter pw = null;
			try {
				pw = new PrintWriter( "tmp/error.log" );
			} catch (FileNotFoundException e) {
				System.exit( 0 );
				e.printStackTrace();
			}
			ex.printStackTrace( pw );
			
			pw.flush();
			pw.close();
			
			System.exit( 1 );
		}
	}
}
