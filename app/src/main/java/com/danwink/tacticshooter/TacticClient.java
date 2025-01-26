package com.danwink.tacticshooter;

import java.io.File;

import org.lwjgl.openal.OpenALException;
import org.newdawn.slick.AppGameContainer;
import org.newdawn.slick.BasicGame;
import org.newdawn.slick.Font;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.SlickException;

import com.danwink.tacticshooter.dal.DAL;
import com.danwink.tacticshooter.dal.SlickDAL;
import com.danwink.tacticshooter.editor.Editor;
import com.danwink.tacticshooter.screens.HomeScreen;
import com.danwink.tacticshooter.screens.LobbyScreen;
import com.danwink.tacticshooter.screens.MessageScreen;
import com.danwink.tacticshooter.screens.MultiplayerGameScreen;
import com.danwink.tacticshooter.screens.MultiplayerSetupScreen;
import com.danwink.tacticshooter.screens.OpenLoadScreen;
import com.danwink.tacticshooter.screens.OptionsScreen;
import com.danwink.tacticshooter.screens.PostGameScreen;
import com.danwink.tacticshooter.screens.ServerConnectScreen;
import com.danwink.tacticshooter.screens.SettingsScreen;
import com.danwink.tacticshooter.screens.dev.DevMenu;
import com.danwink.tacticshooter.screens.dev.SDImageParseScreen;
import com.danwink.tacticshooter.screens.dev.SDLevelGenScreen;
import com.danwink.tacticshooter.screens.dev.SpriteEditor;
import com.danwink.tacticshooter.screens.editor.EditorScreen;
import com.phyloa.dlib.game.DScreenHandler;

public class TacticClient extends BasicGame {
	DScreenHandler<GameContainer, Graphics> dsh = new DScreenHandler<GameContainer, Graphics>();

	Font f;

	int lastWindowWidth = 0;
	int lastWindowHeight = 0;

	DAL dal;

	public TacticClient() {
		super("Tactic Shooter Client");
	}

	public void init(GameContainer gc) throws SlickException {
		lastWindowWidth = gc.getWidth();
		lastWindowHeight = gc.getHeight();

		dsh.register("openload", new OpenLoadScreen());

		dsh.register("home", new HomeScreen());

		dsh.register("multiplayersetup", new MultiplayerSetupScreen());
		dsh.register("multiplayergame", new MultiplayerGameScreen());
		dsh.register("connect", new ServerConnectScreen());
		dsh.register("lobby", new LobbyScreen());

		dsh.register("message", new MessageScreen());
		dsh.register("postgame", new PostGameScreen());

		dsh.register("settings", new SettingsScreen());
		dsh.register("options", new OptionsScreen("options.txt", "settings"));
		dsh.register("advoptions", new OptionsScreen("data" + File.separator + "advoptions.txt", "settings"));
		dsh.register("editor", new EditorScreen());

		dsh.activate("openload", gc);

		// Dev screens
		dsh.register("devmenu", new DevMenu());
		dsh.register("spriteeditor", new SpriteEditor());
		dsh.register("sdlevelgen", new SDLevelGenScreen());
		dsh.register("sdimageparse", new SDImageParseScreen());

		gc.setMusicVolume(StaticFiles.options.getF("slider.music"));
		gc.setSoundVolume(StaticFiles.options.getF("slider.sound"));

		f = UIHelper.getFontForScale(UIHelper.getUIScale(lastWindowHeight));
	}

	public void update(GameContainer gc, int delta) throws SlickException {
		dsh.update(gc, delta / 1000.f);

		// Render background if not in a game
		if (!(dsh.get() instanceof MultiplayerGameScreen)) {
			StaticFiles.bgd.update(delta);
		}
	}

	public void render(GameContainer gc, Graphics g) throws SlickException {
		if (dal == null) {
			dal = new SlickDAL(gc, g);
		}

		if (lastWindowWidth != gc.getWidth() || lastWindowHeight != gc.getHeight()) {
			lastWindowWidth = gc.getWidth();
			lastWindowHeight = gc.getHeight();
			f = UIHelper.getFontForScale(UIHelper.getUIScale(lastWindowHeight));
			dsh.get().onResize(lastWindowWidth, lastWindowHeight);
		}

		g.setAntiAlias(StaticFiles.advOptions.getB("antialias"));
		g.setFont(f);

		// Render background if not in a game
		if (!(dsh.get() instanceof MultiplayerGameScreen)) {
			StaticFiles.bgd.render(dal);
		}

		dsh.render(gc, g);
	}

	public static void main(String[] args) {
		SharedLibraryLoader.load();

		if (args.length > 0 && args[1].equals("--editor")) {
			Editor.main(args);
		} else {
			try {
				AppGameContainer app = new AppGameContainer(new TacticClient());
				app.setMultiSample(StaticFiles.advOptions.getI("multisample"));
				app.setDisplayMode(StaticFiles.options.getI("windowWidth"), StaticFiles.options.getI("windowHeight"),
						StaticFiles.options.getB("fullscreen"));
				app.setVSync(StaticFiles.options.getB("vsync"));
				app.setUpdateOnlyWhenVisible(false);
				app.setAlwaysRender(true);
				app.setResizable(true);
				app.start();
			} catch (OpenALException ex) {
				// These seem to happen fairly often on macs, not quite sure what to do about
				// it.
				ex.printStackTrace();
				System.exit(1);
			} catch (SlickException e) {
				e.printStackTrace();
			}
		}
	}
}
