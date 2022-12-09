package com.danwink.tacticshooter.screens;

import java.io.File;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.List;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

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
import com.phyloa.dlib.dui.DColumnPanel;
import com.phyloa.dlib.dui.DUI;
import com.phyloa.dlib.dui.DUIElement;
import com.phyloa.dlib.dui.DUIEvent;
import com.phyloa.dlib.dui.DUIListener;
import com.phyloa.dlib.dui.RelativePosition;
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

	List<String> ips;

	Image title;

	boolean openEditor = false;

	int frame;

	public void onActivate(GameContainer e, DScreenHandler<GameContainer, Graphics> dsh) {
		if (dui == null) {
			dui = new DUI(new Slick2DEventMapper(e.getInput()), 0, 0, gc.getWidth(), gc.getHeight());

			DColumnPanel mainButtons = new DColumnPanel(0, 0, 0, 0);
			mainButtons.setRelativePosition(RelativePosition.CENTER, 0, 0);

			singlePlayer = new DButton("Start Local Server", 0, 0, 400, 100);
			multiPlayer = new DButton("Multiplayer", 0, 0, 400, 100);
			settings = new DButton("Settings", 0, 0, 400, 100);
			exit = new DButton("Exit", 0, 0, 400, 100);

			editor = new DButton("Editor", 50, 50, 200, 100);

			mainButtons.add(singlePlayer);
			mainButtons.add(multiPlayer);
			mainButtons.add(settings);
			mainButtons.add(exit);

			dui.add(mainButtons);

			dui.add(editor);

			dui.addDUIListener(this);

			dui.doLayout();
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
		if (ips != null) {
			g.setColor(Color.white);
			g.drawString("Server Address: ", 200, 15);
			for (int i = 0; i < ips.size(); i++) {
				g.drawString(ips.get(i), 340, 15 + i * 25);
			}

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
		var scaleAmount = 2 + DMath.sinf(frame * .05f) * .75f;
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

		frame++;
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
					ips = getINetAddresses();
				} else {
					singlePlayer.setText("Start Local Server");
					server.sl.running = false;
					server = null;
					ips = null;
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

	public List<String> getINetAddresses() {
		try {
			var stream = StreamSupport
					.stream(Spliterators.spliteratorUnknownSize(NetworkInterface.getNetworkInterfaces().asIterator(),
							Spliterator.ORDERED), false);
			return stream.filter(ifc -> {
				try {
					return ifc.isUp();
				} catch (SocketException e) {
					e.printStackTrace();
					return false;
				}
			}).flatMap(ifc -> ifc.getInterfaceAddresses().stream())
					.filter(addr -> addr.getAddress().isSiteLocalAddress())
					.map(addr -> addr.getAddress().getHostAddress())
					.collect(Collectors.toList());
		} catch (SocketException e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public void message(Object o) {
	}

	public void onResize(int width, int height) {
		dui.resize(width, height);
		dui.doLayout();
	}
}
