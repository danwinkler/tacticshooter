package tacticshooter;

import java.awt.Font;
import java.awt.FontFormatException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;

import org.newdawn.slick.AngelCodeFont;
import org.newdawn.slick.AppGameContainer;
import org.newdawn.slick.BasicGame;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.TrueTypeFont;
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

public class TacticClient extends BasicGame
{
	DScreenHandler<GameContainer, Graphics> dsh = new DScreenHandler<GameContainer, Graphics>();
	
	org.newdawn.slick.Font f;
	
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
		dsh.register( "options", new OptionsScreen( "options.txt", "settings" ) );
		dsh.register( "advoptions", new OptionsScreen( "data" + File.separator + "advoptions.txt", "settings" ) );
		
		dsh.activate( "home", gc );
		
		
		f = new AngelCodeFont( "data" + File.separator + "pixelfont1_16px.fnt", "data" + File.separator + "pixelfont1_16px_0.png" );
		
	}
	
	public void update( GameContainer gc, int delta ) throws SlickException
	{
		dsh.update( gc, delta );
		
		if( !(dsh.get() instanceof MultiplayerGameScreen) && !(dsh.get() instanceof LevelEditor) )
		{
			StaticFiles.bgd.update( delta );
		}
	}

	public void render( GameContainer gc, Graphics g ) throws SlickException 
	{
		g.setFont( f );
		
		if( !(dsh.get() instanceof MultiplayerGameScreen) && !(dsh.get() instanceof LevelEditor) )
		{
			StaticFiles.bgd.render( gc, g );
		}
		
		dsh.render( gc, g );
	}
	
	public static void main( String[] args )
	{
		try
		{
			//Attempt to avoid sealed exception errors on zoe's mac
			Class.forName( "javax.vecmath.Point2i" );
			
			AppGameContainer app = new AppGameContainer( new TacticClient() );
			app.setDisplayMode( StaticFiles.options.getI( "windowWidth" ), StaticFiles.options.getI( "windowHeight" ), StaticFiles.options.getB( "fullscreen" ) );
			app.setVSync( StaticFiles.options.getB( "vsync" ) );
			app.setUpdateOnlyWhenVisible( false );
			app.setAlwaysRender( true );
			app.start();
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
