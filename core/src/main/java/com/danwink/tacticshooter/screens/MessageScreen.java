package com.danwink.tacticshooter.screens;

import com.danwink.tacticshooter.StaticFiles;
import com.danwink.tacticshooter.dal.DAL;
import com.phyloa.dlib.dui.DButton;
import com.phyloa.dlib.dui.DText;
import com.phyloa.dlib.dui.DUI;
import com.phyloa.dlib.dui.DUIElement;
import com.phyloa.dlib.dui.DUIEvent;
import com.phyloa.dlib.dui.DUIListener;
import com.phyloa.dlib.game.DScreen;
import com.phyloa.dlib.game.DScreenHandler;

public class MessageScreen extends DScreen<DAL> implements DUIListener {
	DUI dui;
	DButton okay;
	DText text;

	String message;

	public void onActivate(DAL dal, DScreenHandler<DAL> dsh) {
		dui = new DUI(dal.getEventMapper());

		okay = new DButton("Okay", dal.getWidth() / 2 - 100, dal.getHeight() / 2, 200, 100);
		text = new DText(message, dal.getWidth() / 2 - 100, dal.getHeight() / 2 - 100);
		text.setCentered(true);

		dui.add(okay);
		dui.add(text);

		dui.addDUIListener(this);

		dui.setEnabled(true);
	}

	public void update(DAL dal, float delta) {
		dui.update();
	}

	public void render(DAL dal) {
		// g.setColor( new Color( 0, 0, 0, 100 ) );
		// g.fillRect( 0, 0, gc.getWidth(), gc.getHeight() );

		dui.render(DAL.getDUIRenderer(dal.getGraphics()));
	}

	public void onExit() {
		dui.setEnabled(false);
		dui = null;
	}

	public void event(DUIEvent event) {
		DUIElement e = event.getElement();
		if (e instanceof DButton && event.getType() == DButton.MOUSE_UP)
			if (e == okay) {
				dsh.activate("home", dal, StaticFiles.getUpMenuOut(), StaticFiles.getUpMenuIn());
			}
	}

	@Override
	public void message(Object o) {
		message = (String) o;
	}

	public void onResize(int width, int height) {
	}
}
