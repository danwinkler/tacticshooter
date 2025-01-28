package com.danwink.tacticshooter.screens;

import org.newdawn.slick.Image;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.SpriteSheet;

import com.danwink.tacticshooter.AssetManager;
import com.danwink.tacticshooter.AssetManager.AssetLoader;
import com.danwink.tacticshooter.StaticFiles;
import com.danwink.tacticshooter.Theme.SlickTSSpriteSheet;
import com.danwink.tacticshooter.Theme.TSSpriteSheet;
import com.danwink.tacticshooter.dal.DAL;
import com.danwink.tacticshooter.dal.DAL.DALColor;
import com.danwink.tacticshooter.dal.DAL.DALTexture;
import com.danwink.tacticshooter.dal.SlickDAL.SlickTexture;
import com.phyloa.dlib.game.DScreen;
import com.phyloa.dlib.game.DScreenHandler;

public class OpenLoadScreen extends DScreen<DAL> {
	public void onActivate(DAL dal, DScreenHandler<DAL> dsh) {
		StaticFiles.loadAll();
		StaticFiles.loopWhenReady("menu");
		AssetManager.configureLoader(new AssetLoader() {
			public DALTexture loadTexture(String path) {
				try {
					return new SlickTexture(new Image(path));
				} catch (SlickException e) {
					throw new RuntimeException(e);
				}
			}

			public TSSpriteSheet loadSpriteSheet(String path, int tw, int th) {
				try {
					return new SlickTSSpriteSheet(new SpriteSheet(path, tw, th));
				} catch (SlickException e) {
					throw new RuntimeException(e);
				}
			}
		});
		AssetManager.load();
	}

	public void update(DAL dal, float delta) {
		if (StaticFiles.ready) {
			dsh.activate("home", dal);
		}
	}

	public void render(DAL dal) {
		var g = dal.getGraphics();

		g.setColor(new DALColor(0, 0, 0, 100));
		g.fillRect(0, 0, dal.getWidth(), dal.getHeight());

		g.setColor(DALColor.red);
		g.drawText(StaticFiles.status, 50, 50);
	}

	public void onExit() {

	}

	public void message(Object o) {
		// TODO Auto-generated method stub

	}

	public void onResize(int width, int height) {
	}
}
