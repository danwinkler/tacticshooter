package com.danwink.tacticshooter.screens;

import java.io.File;
import java.net.InetAddress;
import java.net.UnknownHostException;

import org.newdawn.slick.Color;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.SlickException;
import com.danwink.tacticshooter.StaticFiles;
import com.danwink.tacticshooter.TacticServer;
import com.danwink.tacticshooter.editor.Editor;
import com.danwink.tacticshooter.network.ServerNetworkInterface;
import com.danwink.tacticshooter.slick.Slick2DEventMapper;
import com.danwink.tacticshooter.slick.Slick2DRenderer;
import com.phyloa.dlib.dui.DButton;
import com.phyloa.dlib.dui.DUI;
import com.phyloa.dlib.dui.DUIElement;
import com.phyloa.dlib.dui.DUIEvent;
import com.phyloa.dlib.dui.DUIListener;
import com.phyloa.dlib.game.DScreen;
import com.phyloa.dlib.game.DScreenHandler;
import com.phyloa.dlib.util.DMath;

public class HomeScreen extends DScreen<GameContainer, Graphics> implements DUIListener {
	TacticServer server;

	DUI dui;
	DButton singlePlayer;
	DButton multiPlayer;
	DButton settings;
	DButton editor;
	DButton exit;

	Slick2DRenderer r = new Slick2DRenderer();

	String ip;

	Image title;

	boolean openEditor = false;

	public void onActivate(GameContainer e, DScreenHandler<GameContainer, Graphics> dsh) {
		if (dui == null) {
			dui = new DUI(new Slick2DEventMapper(e.getInput()));

			singlePlayer = new DButton("Start Local Server", e.getWidth() / 2 - 200, e.getHeight() / 2 - 150, 400, 100);
			multiPlayer = new DButton("Multiplayer", e.getWidth() / 2 - 200, e.getHeight() / 2 - 50, 400, 100);
			settings = new DButton("Settings", e.getWidth() / 2 - 200, e.getHeight() / 2 + 50, 400, 100);
			exit = new DButton("Exit", e.getWidth() / 2 - 200, e.getHeight() / 2 + 150, 400, 100);

			editor = new DButton("Editor", 50, 50, 200, 100);

			dui.add(singlePlayer);
			dui.add(multiPlayer);
			dui.add(settings);
			dui.add(exit);

			dui.add(editor);

			dui.addDUIListener(this);
		}

		dui.setEnabled(true);

		try {
			title = new Image("data" + File.separator + "img" + File.separator + "title.png");
		} catch (SlickException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}

	public void update(GameContainer gc, float delta) {
		dui.update();
	}

	public void render(GameContainer gc, Graphics g) {
		dui.render(r.renderTo(g));
		if (ip != null) {
			g.setColor(Color.white);
			g.drawString("Server Address: " + ip, 200, 15);
		}

		// Title
		g.drawImage(title, gc.getWidth() / 2 - title.getWidth() / 2, 150, new Color(0, 0, 0, 128));

		// Pulsing message
		g.pushTransform();
		g.setColor(Color.white);
		var message = "2022 Edtion!";
		var textWidth = g.getFont().getWidth(message);
		g.translate(gc.getWidth() / 2 + title.getWidth() * .45f, 170);
		g.rotate(textWidth / 2, 0, 45);
		var t = (int) (gc.getTime() - 1400000000l);
		var scaleAmount = 2 + DMath.sinf(t * .0025f) * .75f;
		g.scale(scaleAmount, scaleAmount);
		g.drawString(message, -textWidth / 2, 0);
		g.popTransform();

		if (openEditor) {
			try {
				gc.setForceExit(false);
				gc.exit();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
			Editor.main(new String[] {});
		}
	}

	public void onExit() {
		dui.setEnabled(false);
	}

	public void event(DUIEvent event) {
		DUIElement e = event.getElement();
		if (e instanceof DButton && event.getType() == DButton.MOUSE_UP)
			if (e == singlePlayer) {
				if (server == null) {
					singlePlayer.setText("Stop Local Server");
					server = new TacticServer(new ServerNetworkInterface());
					server.begin();
					try {
						InetAddress thisIp = InetAddress.getLocalHost();
						ip = thisIp.getHostAddress();
					} catch (UnknownHostException e1) {
					}
				} else {
					singlePlayer.setText("Start Local Server");
					server.sl.running = false;
					server = null;
					ip = null;
				}
			} else if (e == multiPlayer) {
				dsh.activate("multiplayersetup", gc, StaticFiles.getDownMenuOut(), StaticFiles.getDownMenuIn());
			} else if (e == settings) {
				dsh.activate("settings", gc, StaticFiles.getDownMenuOut(), StaticFiles.getDownMenuIn());
			} else if (e == exit) {
				gc.exit();
			} else if (e == editor) {
				openEditor = true;
			}
	}

	@Override
	public void message(Object o) {
	}

	public void onResize(int width, int height) {
	}
}
