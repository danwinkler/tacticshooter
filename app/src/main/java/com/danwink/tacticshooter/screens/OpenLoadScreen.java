package com.danwink.tacticshooter.screens;

import org.newdawn.slick.Color;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;

import com.danwink.tacticshooter.StaticFiles;
import com.danwink.tacticshooter.dal.DAL;
import com.phyloa.dlib.game.DScreen;
import com.phyloa.dlib.game.DScreenHandler;

public class OpenLoadScreen extends DScreen<GameContainer, DAL> {
	public void onActivate(GameContainer e, DScreenHandler<GameContainer, DAL> dsh) {
		StaticFiles.loadAll();
		StaticFiles.loopWhenReady("menu");
	}

	public void update(GameContainer gc, float delta) {
		if (StaticFiles.ready) {
			dsh.activate("home", gc);
		}
	}

	public void render(GameContainer gc, DAL dal) {
		var g = dal.getGraphics();

		g.setColor(new Color(0, 0, 0, 100));
		g.fillRect(0, 0, gc.getWidth(), gc.getHeight());

		g.setColor(Color.red);
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
