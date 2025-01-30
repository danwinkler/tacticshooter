package com.danwink.tacticshooter.screens;

import com.danwink.tacticshooter.Assets;
import com.danwink.tacticshooter.MusicQueuer;
import com.danwink.tacticshooter.StaticFiles;
import com.danwink.tacticshooter.dal.DAL;
import com.danwink.tacticshooter.dal.DAL.DALColor;
import com.phyloa.dlib.game.DScreen;
import com.phyloa.dlib.game.DScreenHandler;

public class OpenLoadScreen extends DScreen<DAL> {
	public void onActivate(DAL dal, DScreenHandler<DAL> dsh) {
		// TODO:
		StaticFiles.loadAll();
		// StaticFiles.loopWhenReady("menu");
		StaticFiles.ready = true; // DEBUG line, remove when fixed
	}

	public void update(DAL dal, float delta) {
		boolean assetManagerDone = Assets.manager.update();

		if (StaticFiles.ready && assetManagerDone) {
			MusicQueuer.loopTracks("menu");
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
