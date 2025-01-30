package com.danwink.tacticshooter;

import java.io.File;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.utils.ScreenUtils;
import com.danwink.tacticshooter.dal.DAL;
import com.danwink.tacticshooter.dal.GdxDAL;
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

/**
 * {@link com.badlogic.gdx.ApplicationListener} implementation shared by all
 * platforms.
 */
public class TacticClient extends ApplicationAdapter {
    static {
        Assets.defineMusic("menu", "data/sound/Deliberate Thought.ogg");
        Assets.defineMusic("play1", "data/sound/Decisions.ogg");
        Assets.defineMusic("play2", "data/sound/Finding the Balance.ogg");
        Assets.defineMusic("play3", "data/sound/Rising.ogg");

        Assets.defineSound("bullet1", "data/sound/bullet1.wav");
        Assets.defineSound("bullet2", "data/sound/bullet2.wav");
        Assets.defineSound("ping1", "data/sound/ping1.wav");
        Assets.defineSound("death1", "data/sound/death1.wav");
        Assets.defineSound("death2", "data/sound/death2.wav");
        Assets.defineSound("hit1", "data/sound/hit1.wav");
        Assets.defineSound("explode1", "data/sound/explode1.wav");
    }

    DScreenHandler<DAL> dsh = new DScreenHandler<>();

    int lastWindowWidth = 0;
    int lastWindowHeight = 0;

    GdxDAL dal;

    BitmapFont f;

    @Override
    public void create() {
        dal = new GdxDAL();
        dal.init();

        lastWindowWidth = dal.getWidth();
        lastWindowHeight = dal.getHeight();

        Theme.getTheme("desertrpg").load();
        Theme.getTheme("junglerpg").load();

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

        dsh.activate("openload", dal);

        // Dev screens
        dsh.register("devmenu", new DevMenu());
        dsh.register("spriteeditor", new SpriteEditor());
        dsh.register("sdlevelgen", new SDLevelGenScreen());
        dsh.register("sdimageparse", new SDImageParseScreen());

        dal.setMusicVolume(StaticFiles.options.getF("slider.music"));
        dal.setSoundVolume(StaticFiles.options.getF("slider.sound"));

        f = UIHelper.getFontForScale(UIHelper.getUIScale(lastWindowHeight));
    }

    @Override
    public void render() {
        // UPDATE
        ScreenUtils.clear(0.15f, 0.15f, 0.2f, 1f);

        float delta = Gdx.graphics.getDeltaTime() * 1000;

        dsh.update(dal, delta / 1000.f);

        var g = dal.getGraphics();

        g.preRender();

        // RENDER
        if (lastWindowWidth != dal.getWidth() || lastWindowHeight != dal.getHeight()) {
            lastWindowWidth = dal.getWidth();
            lastWindowHeight = dal.getHeight();
            f = UIHelper.getFontForScale(UIHelper.getUIScale(lastWindowHeight));
            dsh.get().onResize(lastWindowWidth, lastWindowHeight);
        }

        g.setAntiAlias(StaticFiles.advOptions.getB("antialias"));
        g.setFont(f);

        // Render background if not in a game
        if (!(dsh.get() instanceof MultiplayerGameScreen)) {
            StaticFiles.bgd.update((int) delta);
            StaticFiles.bgd.render(dal);
        }

        dsh.render(dal);

        g.postRender();
    }

    @Override
    public void dispose() {

    }
}
