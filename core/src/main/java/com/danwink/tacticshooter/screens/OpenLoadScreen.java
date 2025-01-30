package com.danwink.tacticshooter.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.danwink.tacticshooter.AssetManager;
import com.danwink.tacticshooter.AssetManager.AssetLoader;
import com.danwink.tacticshooter.StaticFiles;
import com.danwink.tacticshooter.Theme.GdxSpriteSheet;
import com.danwink.tacticshooter.Theme.TSSpriteSheet;
import com.danwink.tacticshooter.dal.DAL;
import com.danwink.tacticshooter.dal.DAL.DALColor;
import com.danwink.tacticshooter.dal.DAL.DALTexture;
import com.danwink.tacticshooter.dal.GdxDAL.GdxRegionTexture;
import com.phyloa.dlib.game.DScreen;
import com.phyloa.dlib.game.DScreenHandler;

public class OpenLoadScreen extends DScreen<DAL> {
	public void onActivate(DAL dal, DScreenHandler<DAL> dsh) {
		// TODO:
		StaticFiles.loadAll();
		// StaticFiles.loopWhenReady("menu");
		StaticFiles.ready = true; // DEBUG line, remove when fixed

		AssetManager.configureLoader(new AssetLoader() {
			public DALTexture loadTexture(String path) {
				var tr = new TextureRegion(new Texture(Gdx.files.internal(path)));
				tr.flip(false, true);
				return new GdxRegionTexture(tr);
			}

			public TSSpriteSheet loadSpriteSheet(String path, int tw, int th) {
				return new GdxSpriteSheet(new Texture(Gdx.files.internal(path)), tw, th);
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
