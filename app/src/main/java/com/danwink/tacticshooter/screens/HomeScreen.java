package com.danwink.tacticshooter.screens;

import java.awt.event.KeyEvent;
import java.io.File;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.newdawn.slick.Color;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.Input;
import org.newdawn.slick.SlickException;

import com.danwink.tacticshooter.StaticFiles;
import com.danwink.tacticshooter.TacticServer;
import com.danwink.tacticshooter.UIHelper;
import com.danwink.tacticshooter.editor.Editor;
import com.danwink.tacticshooter.network.ServerNetworkInterface;
import com.danwink.tacticshooter.ui.DUIScreen;
import com.phyloa.dlib.dui.DButton;
import com.phyloa.dlib.dui.DColumnPanel;
import com.phyloa.dlib.dui.DKeyEvent;
import com.phyloa.dlib.dui.DMouseEvent;
import com.phyloa.dlib.dui.DUI;
import com.phyloa.dlib.dui.DUIElement;
import com.phyloa.dlib.dui.DUIEvent;
import com.phyloa.dlib.dui.RelativePosition;
import com.phyloa.dlib.renderer.Renderer2D;
import com.phyloa.dlib.util.DMath;

public class HomeScreen extends DUIScreen {
	TacticServer server;

	DButton singlePlayer;
	DButton multiPlayer;
	DButton settings;
	DButton editor;
	DButton exit;

	List<String> ips;

	Image title;

	boolean openEditor = false;

	int frame;

	@Override
	public void init(GameContainer e) {
		try {
			title = new Image("data" + File.separator + "img" + File.separator + "title.png");
		} catch (SlickException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}

	public void createUIElements(DUI dui, float windowHeight) {
		DColumnPanel mainButtons = new DColumnPanel(0, 0, 0, 0);
		mainButtons.setRelativePosition(RelativePosition.CENTER, 0, 0);

		int uiScale = UIHelper.getUIScale(windowHeight);
		int buttonWidth = 400 * uiScale;
		int buttonHeight = 100 * uiScale;

		singlePlayer = new DButton("Start Local Server", 0, 0, buttonWidth, buttonHeight);
		multiPlayer = new DButton("Multiplayer", 0, 0, buttonWidth, buttonHeight);
		settings = new DButton("Settings", 0, 0, buttonWidth, buttonHeight);
		exit = new DButton("Exit", 0, 0, buttonWidth, buttonHeight);

		editor = new DButton("Editor", 50 * uiScale, 50 * uiScale, 200 * uiScale, 100 * uiScale);

		mainButtons.add(singlePlayer);
		mainButtons.add(multiPlayer);
		mainButtons.add(settings);
		mainButtons.add(exit);

		dui.add(mainButtons);

		dui.add(editor);

		var devKeyListener = new DevKeyListener();
		dui.add(devKeyListener);
	}

	public void render(GameContainer gc, Graphics g) {
		super.render(gc, g);
		if (ips != null) {
			g.setColor(Color.white);
			g.drawString("Server Address: ", 200 * uiScale, 15 * uiScale);
			for (int i = 0; i < ips.size(); i++) {
				g.drawString(ips.get(i), 340 * uiScale, 15 * uiScale + i * 25 * uiScale);
			}

		}

		// Title
		g.drawImage(title, gc.getWidth() / 2 - title.getWidth() / 2, gc.getHeight() / 6, new Color(0, 0, 0, 128));

		// Pulsing message
		g.pushTransform();
		g.setColor(Color.white);
		var message = "2022 Edition!";
		var textWidth = g.getFont().getWidth(message);
		g.translate(gc.getWidth() / 2 + title.getWidth() * .45f, gc.getHeight() / 6 + 20);
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
				if (gc.getInput().isKeyDown(Input.KEY_LCONTROL)) {
					openEditor = true;
				} else {
					dsh.activate("editor", gc, StaticFiles.getDownMenuOut(), StaticFiles.getDownMenuIn());
				}
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

	public class DevKeyListener extends DUIElement {
		List<Integer> lastPressedKeys = new ArrayList<>();

		@Override
		public void keyPressed(DKeyEvent dke) {
			lastPressedKeys.add(dke.keyCode);

			if (lastPressedKeys.size() > 3) {
				lastPressedKeys.remove(0);
			}

			if (lastPressedKeys.size() >= 3) {
				var k0 = lastPressedKeys.get(0) == KeyEvent.VK_D;
				var k1 = lastPressedKeys.get(1) == KeyEvent.VK_E;
				var k2 = lastPressedKeys.get(2) == KeyEvent.VK_V;

				if (k0 && k1 && k2) {
					dsh.activate("devmenu", gc);
				}
			}
		}

		@Override
		public void keyReleased(DKeyEvent dke) {
			// TODO Auto-generated method stub

		}

		@Override
		public void mouseEntered(DMouseEvent e) {
			// TODO Auto-generated method stub

		}

		@Override
		public void mouseExited(DMouseEvent e) {
			// TODO Auto-generated method stub

		}

		@Override
		public boolean mousePressed(DMouseEvent e) {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public boolean mouseReleased(DMouseEvent e) {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public void mouseMoved(DMouseEvent e) {
			// TODO Auto-generated method stub

		}

		@Override
		public boolean mouseDragged(DMouseEvent e) {
			return true;
		}

		@Override
		public void mouseWheel(DMouseEvent dme) {
			// TODO Auto-generated method stub

		}

		@Override
		public void render(Renderer2D<Image> r) {
			// TODO Auto-generated method stub

		}

		@Override
		public void update(DUI ui) {
			ui.setFocus(this);

		}
	}
}
