package com.danwink.tacticshooter.screens;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Map.Entry;

import com.danwink.tacticshooter.StaticFiles;
import com.danwink.tacticshooter.dal.DAL;
import com.danwink.tacticshooter.slick.Slick2DRenderer;
import com.phyloa.dlib.dui.DButton;
import com.phyloa.dlib.dui.DScrollPane;
import com.phyloa.dlib.dui.DSlider;
import com.phyloa.dlib.dui.DText;
import com.phyloa.dlib.dui.DTextBox;
import com.phyloa.dlib.dui.DUI;
import com.phyloa.dlib.dui.DUIElement;
import com.phyloa.dlib.dui.DUIEvent;
import com.phyloa.dlib.dui.DUIListener;
import com.phyloa.dlib.game.DScreen;
import com.phyloa.dlib.game.DScreenHandler;
import com.phyloa.dlib.util.DFile;
import com.phyloa.dlib.util.DOptions;

public class OptionsScreen extends DScreen<DAL> implements DUIListener {
	DUI dui;

	DScrollPane scrollPane;

	DButton back;

	Slick2DRenderer r = new Slick2DRenderer();

	ArrayList<DUIElement> boxes = new ArrayList<DUIElement>();

	private String optionsFile;

	private String screenToReturn;

	public OptionsScreen(String optionsFile, String screenToReturn) {
		this.optionsFile = optionsFile;
		this.screenToReturn = screenToReturn;
	}

	public void onActivate(DAL gc, DScreenHandler<DAL> dsh) {
		dui = new DUI(gc.getEventMapper());

		DOptions options = new DOptions(optionsFile);

		scrollPane = new DScrollPane(gc.getWidth() / 2 - 200, 50, 410, 500);

		int i = 0;
		for (Entry<String, String> e : options.options.entrySet()) {
			scrollPane.add(new DText(e.getKey(), 10, i * 50 + 10));
			if (e.getKey().startsWith("slider.")) {
				DSlider box = new DSlider(200, i * 50, 200, 50, 0, 1, Float.parseFloat(e.getValue()));
				box.setName(e.getKey());
				boxes.add(box);
				scrollPane.add(box);
			} else {
				DTextBox box = new DTextBox(200, i * 50, 200, 50);
				box.setText(e.getValue());
				box.setName(e.getKey());
				boxes.add(box);
				scrollPane.add(box);
			}
			i++;
		}
		scrollPane.setInnerPaneHeight(options.options.entrySet().size() * 50);

		back = new DButton("Back", gc.getWidth() / 2 - 100, gc.getHeight() - 150, 200, 100);

		dui.add(scrollPane);
		dui.add(back);

		dui.addDUIListener(this);

		dui.setEnabled(true);
	}

	public void update(DAL dal, float delta) {
		dui.update();
	}

	public void render(DAL g) {
		dui.render(DAL.getDUIRenderer(g.getGraphics()));
	}

	public void onExit() {
		dui.setEnabled(false);
		dui = null;
		boxes.clear();
	}

	public void message(Object o) {

	}

	public void onResize(int width, int height) {
	}

	public void event(DUIEvent event) {
		DUIElement e = event.getElement();
		if (e instanceof DButton && event.getType() == DButton.MOUSE_UP) {
			if (e == back) {
				StringBuilder mapList = new StringBuilder();
				for (int i = 0; i < boxes.size(); i++) {
					DUIElement element = boxes.get(i);
					if (element instanceof DTextBox) {
						DTextBox b = (DTextBox) element;
						mapList.append(b.getName());
						mapList.append(" ");
						mapList.append(b.getText().trim());
						mapList.append("\n");
					} else if (element instanceof DSlider) {
						DSlider d = (DSlider) element;
						mapList.append(d.getName());
						mapList.append(" ");
						mapList.append(d.getPosition());
						mapList.append("\n");
					}
				}
				try {
					DFile.saveText(optionsFile, mapList.toString());
				} catch (FileNotFoundException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				StaticFiles.options = new DOptions("options.txt");
				StaticFiles.advOptions = new DOptions("data" + File.separator + "advoptions.txt");

				dal.setMusicVolume(StaticFiles.options.getF("slider.music"));
				dal.setSoundVolume(StaticFiles.options.getF("slider.sound"));
				dal.setVSync(StaticFiles.options.getB("vsync"));

				dsh.activate(screenToReturn, dal, StaticFiles.getUpMenuOut(), StaticFiles.getUpMenuIn());
			}
		}
	}
}
