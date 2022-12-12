package com.danwink.tacticshooter.screens;

import java.awt.event.KeyEvent;

import org.newdawn.slick.GameContainer;

import com.danwink.tacticshooter.StaticFiles;
import com.danwink.tacticshooter.ui.DUIScreen;
import com.phyloa.dlib.dui.DButton;
import com.phyloa.dlib.dui.DColumnPanel;
import com.phyloa.dlib.dui.DRowPanel;
import com.phyloa.dlib.dui.DTextBox;
import com.phyloa.dlib.dui.DUI;
import com.phyloa.dlib.dui.DUIElement;
import com.phyloa.dlib.dui.DUIEvent;
import com.phyloa.dlib.dui.RelativePosition;

public class MultiplayerSetupScreen extends DUIScreen {
	DTextBox address;
	DButton enter;
	DButton back;

	public void init(GameContainer e) {
		address = new DTextBox(0, 0, 0, 0);

		if (((HomeScreen) dsh.get("home")).server != null) {
			address.setText("localhost");
		}
	}

	public void createUIElements(DUI dui, float windowHeight) {
		DColumnPanel column = new DColumnPanel(0, 0, 0, 0);
		column.setRelativePosition(RelativePosition.CENTER, 0, 0);
		address.setSize(400 * uiScale, 100 * uiScale);
		DRowPanel row = new DRowPanel(0, 0, 0, 0);
		back = new DButton("Back", 0, 0, 200 * uiScale, 100 * uiScale);
		enter = new DButton("Join", 0, 0, 200 * uiScale, 100 * uiScale);
		row.add(back);
		row.add(enter);

		column.add(address);
		column.add(row);

		dui.add(column);

		dui.setFocus(address);
	}

	public void event(DUIEvent event) {
		DUIElement e = event.getElement();
		if (e instanceof DButton && event.getType() == DButton.MOUSE_UP) {
			if (e == enter) {
				dsh.message("connect", address.getText().trim());
				dsh.activate("connect", gc, StaticFiles.getDownMenuOut(), StaticFiles.getDownMenuIn());
			} else if (e == back) {
				dsh.activate("home", gc, StaticFiles.getDownMenuOut(), StaticFiles.getDownMenuIn());
			}
		} else if (e instanceof DTextBox) {
			if (event.getType() == KeyEvent.VK_ENTER) {
				dsh.message("connect", address.getText().trim());
				dsh.activate("connect", gc, StaticFiles.getDownMenuOut(), StaticFiles.getDownMenuIn());
			}
		}
	}

	@Override
	public void message(Object o) {
		// TODO Auto-generated method stub

	}

}
