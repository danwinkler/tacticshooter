package com.danwink.tacticshooter.screens;

import com.danwink.tacticshooter.StaticFiles;
import com.danwink.tacticshooter.dal.DAL;
import com.danwink.tacticshooter.slick.Slick2DEventMapper;
import com.danwink.tacticshooter.slick.Slick2DRenderer;
import com.phyloa.dlib.dui.DButton;
import com.phyloa.dlib.dui.DUI;
import com.phyloa.dlib.dui.DUIElement;
import com.phyloa.dlib.dui.DUIEvent;
import com.phyloa.dlib.dui.DUIListener;
import com.phyloa.dlib.game.DScreen;
import com.phyloa.dlib.game.DScreenHandler;

public class SettingsScreen extends DScreen<DAL> implements DUIListener {
	DUI dui;
	DButton toggleFullscreen;
	DButton options;
	DButton advOptions;
	DButton back;

	Slick2DRenderer r = new Slick2DRenderer();

	public void onActivate(DAL gc, DScreenHandler<DAL> dsh) {
		if (dui == null) {
			dui = new DUI(new Slick2DEventMapper(gc.getInput()));

			toggleFullscreen = new DButton("Toggle Fullscreen", gc.getWidth() / 2 - 100, gc.getHeight() / 2 - 200, 200,
					100);
			options = new DButton("Options", gc.getWidth() / 2 - 100, gc.getHeight() / 2 - 100, 200, 100);
			advOptions = new DButton("Advanced Options", gc.getWidth() / 2 - 100, gc.getHeight() / 2, 200, 100);
			back = new DButton("Back", gc.getWidth() / 2 - 100, gc.getHeight() / 2 + 100, 200, 100);

			dui.add(toggleFullscreen);
			dui.add(options);
			dui.add(advOptions);
			dui.add(back);

			dui.addDUIListener(this);
		}
		dui.setEnabled(true);
	}

	public void update(DAL gc, float delta) {
		dui.update();
	}

	public void render(DAL dal) {
		dui.render(DAL.getDUIRenderer(dal.getGraphics()));
	}

	public void onExit() {
		dui.setEnabled(false);
	}

	public void message(Object o) {

	}

	public void event(DUIEvent event) {
		DUIElement e = event.getElement();
		if (e instanceof DButton && event.getType() == DButton.MOUSE_UP) {
			if (e == toggleFullscreen) {
				dal.setFullscreen(!dal.isFullscreen());
			} else if (e == options) {
				dsh.activate("options", dal, StaticFiles.getDownMenuOut(), StaticFiles.getDownMenuIn());
			} else if (e == advOptions) {
				dsh.activate("advoptions", dal, StaticFiles.getDownMenuOut(), StaticFiles.getDownMenuIn());
			} else if (e == back) {
				dsh.activate("home", dal, StaticFiles.getUpMenuOut(), StaticFiles.getUpMenuIn());
			}
		}
	}

	public void onResize(int width, int height) {
	}
}
