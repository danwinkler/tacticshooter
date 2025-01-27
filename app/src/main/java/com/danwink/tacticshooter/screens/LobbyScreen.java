package com.danwink.tacticshooter.screens;

import java.awt.event.KeyEvent;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;

import org.newdawn.slick.Color;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.SlickException;

import com.danwink.tacticshooter.ComputerPlayer;
import com.danwink.tacticshooter.ComputerPlayer.PlayType;
import com.danwink.tacticshooter.dal.DAL;
import com.danwink.tacticshooter.dal.SlickDAL;
import com.danwink.tacticshooter.MessageType;
import com.danwink.tacticshooter.StaticFiles;
import com.danwink.tacticshooter.Theme;
import com.danwink.tacticshooter.UIHelper;
import com.danwink.tacticshooter.gameobjects.Level;
import com.danwink.tacticshooter.gameobjects.Level.SlotType;
import com.danwink.tacticshooter.gameobjects.Player;
import com.danwink.tacticshooter.network.ClientInterface;
import com.danwink.tacticshooter.network.Message;
import com.danwink.tacticshooter.screens.LobbyScreen.Slot;
import com.danwink.tacticshooter.ui.DUIScreen;
import com.phyloa.dlib.dui.DButton;
import com.phyloa.dlib.dui.DCheckBox;
import com.phyloa.dlib.dui.DColumnPanel;
import com.phyloa.dlib.dui.DDropDown;
import com.phyloa.dlib.dui.DPanel;
import com.phyloa.dlib.dui.DRowPanel;
import com.phyloa.dlib.dui.DSpacer;
import com.phyloa.dlib.dui.DTextBox;
import com.phyloa.dlib.dui.DUI;
import com.phyloa.dlib.dui.DUIEvent;
import com.phyloa.dlib.dui.RelativePosition;

public class LobbyScreen extends DUIScreen {
	ClientInterface ci;

	DButton[] names = new DButton[16];
	DDropDown[] humanOrBot = new DDropDown[16];
	DDropDown[] botType = new DDropDown[16];
	DDropDown[] spectator = new DDropDown[16];
	DDropDown maps;
	DDropDown gameType;
	DTextBox chatBox;
	DButton startGame;
	DButton leaveGame;
	DCheckBox fog;
	DPanel chatBackground;
	DButton fillBots;

	Theme theme;
	Level level;
	Image miniMap;

	Slot[] slots = new Slot[16];

	ArrayList<String> messages = new ArrayList<String>();

	public void init(GameContainer gc) {
		initializeUIElements(dui);

		messages.clear();
		ci.sendToServer(new Message(MessageType.CLIENTJOIN, StaticFiles.getUsername()));
	}

	/*
	 * Creating all the UI elements here one time, and then in createUIElements I'll
	 * place them in the DUI and resize them
	 */
	public void initializeUIElements(DUI dui) {
		for (int i = 0; i < 16; i++) {
			int baseHeight = i < 8 ? 160 : 200;
			names[i] = new DButton("Open", 20, baseHeight + i * 30, 170, 25);
			names[i].setName("na " + i);

			humanOrBot[i] = new DDropDown(200, baseHeight + i * 30, 100, 25);
			humanOrBot[i].name = "hb " + i;
			humanOrBot[i].addItems("HUMAN", "BOT");

			botType[i] = new DDropDown(310, baseHeight + i * 30, 100, 25);
			botType[i].name = "bt " + i;
			botType[i].setVisible(false);
			for (ComputerPlayer.PlayType pt : ComputerPlayer.PlayType.values()) {
				botType[i].addItems(pt.name());
			}

			spectator[i] = new DDropDown(420, baseHeight + i * 30, 90, 25);
			spectator[i].name = "sp " + i;
			spectator[i].setVisible(false);
			spectator[i].addItems("Player", "Spec");
		}

		maps = new DDropDown(20, 100, 500, 25);

		gameType = new DDropDown(20, 130, 500, 25);
		gameType.addItems(StaticFiles.getGameTypes());
		gameType.setSelected(gameType.items.indexOf("pointcapture"));

		leaveGame = new DButton("Leave", 105, 700, 90, 50);

		startGame = new DButton("Start", 205, 700, 90, 50);

		chatBox = new DTextBox(gc.getWidth() - 600, gc.getHeight() - 200, 500, 50);

		fog = new DCheckBox(gc.getWidth() - 600, gc.getHeight() - 130, 20, 20);

		fillBots = new DButton("Fill Bots", 0, 0, 90, 50);
	}

	private DRowPanel createSlotRow(int i) {
		DRowPanel row = new DRowPanel(0, 0, 0, 0);
		names[i].setSize(180 * uiScale, 25 * uiScale);
		row.add(names[i]);
		row.add(new DSpacer(10 * uiScale, 0));
		humanOrBot[i].setSize(100 * uiScale, 25 * uiScale);
		row.add(humanOrBot[i]);
		row.add(new DSpacer(10 * uiScale, 0));
		botType[i].setSize(100 * uiScale, 25 * uiScale);
		row.add(botType[i]);
		row.add(new DSpacer(10 * uiScale, 0));
		spectator[i].setSize(90 * uiScale, 25 * uiScale);
		row.add(spectator[i]);

		return row;
	}

	@Override
	public void createUIElements(DUI dui, float windowHeight) {
		int uiScale = UIHelper.getUIScale(windowHeight);

		DColumnPanel leftColumn = new DColumnPanel(0, 0, 0, 0);
		leftColumn.setRelativePosition(RelativePosition.CENTER_LEFT, 20 * uiScale, 0);

		maps.setSize(500 * uiScale, 25 * uiScale);
		leftColumn.add(maps);
		gameType.setSize(500 * uiScale, 25 * uiScale);
		leftColumn.add(gameType);
		leftColumn.add(new DSpacer(0, 10 * uiScale));

		// TEAM A
		DColumnPanel teamAColumn = new DColumnPanel(0, 0, 0, 0);
		for (int i = 0; i < 8; i++) {
			var row = createSlotRow(i);
			teamAColumn.add(row);
		}
		leftColumn.add(teamAColumn);
		leftColumn.add(new DSpacer(0, 30 * uiScale));

		// TEAM B
		DColumnPanel teamBColumn = new DColumnPanel(0, 0, 0, 0);
		for (int i = 8; i < 16; i++) {
			var row = createSlotRow(i);
			teamBColumn.add(row);
		}
		leftColumn.add(teamBColumn);
		leftColumn.add(new DSpacer(0, 30 * uiScale));

		// MISC Buttons
		DRowPanel miscRow = new DRowPanel(0, 0, 0, 0);
		fillBots.setSize(90 * uiScale, 50 * uiScale);
		miscRow.add(fillBots);
		leftColumn.add(miscRow);
		leftColumn.add(new DSpacer(0, 30 * uiScale));

		// LEAVE AND START
		DRowPanel startLeaveRow = new DRowPanel(0, 0, 0, 0);
		leaveGame.setSize(90 * uiScale, 50 * uiScale);
		startLeaveRow.add(leaveGame);
		startLeaveRow.add(new DSpacer(10 * uiScale, 0));
		startGame.setSize(90 * uiScale, 50 * uiScale);
		startLeaveRow.add(startGame);
		leftColumn.add(startLeaveRow);

		dui.add(leftColumn);

		// dui.add(new DText("Enable Fog", gc.getWidth() - 570, gc.getHeight() - 126));

		DColumnPanel rightColumn = new DColumnPanel(0, 0, 0, 0);
		rightColumn.setRelativePosition(RelativePosition.TOP_RIGHT, -20 * uiScale, 130 * uiScale);

		chatBackground = new DPanel(0, 0, 500 * uiScale, gc.getHeight() - 310 * uiScale);
		chatBackground.setDrawBackground(true);
		rightColumn.add(chatBackground);
		rightColumn.add(new DSpacer(0, uiScale * 10));
		chatBox.setSize(500 * uiScale, 50 * uiScale);
		rightColumn.add(chatBox);

		dui.add(rightColumn);
	}

	public void update(GameContainer gc, float delta) {
		super.update(gc, delta);

		while (ci.hasClientMessages()) {
			Message m = ci.getNextClientMessage();
			switch (m.messageType) {
				case PLAYERUPDATE: {
					Object[] oa = (Object[]) m.message;
					Slot s = (Slot) oa[1];
					int slot = (Integer) oa[0];
					if (s.type == SlotType.CLOSED) {
						names[slot].setText("CLOSED");
						botType[slot].setVisible(false);
						humanOrBot[slot].setVisible(false);
						spectator[slot].setVisible(false);
					} else if (s.p == null) {
						names[slot].setText("Open");
						humanOrBot[slot].setVisible(true);
						humanOrBot[slot].setSelected(0);
						botType[slot].setVisible(false);
						spectator[slot].setVisible(false);
					} else {
						names[s.p.slot].setText(s.p.name);
						humanOrBot[slot].setVisible(true);
						humanOrBot[s.p.slot].setSelected(s.p.isBot ? 1 : 0);
						botType[s.p.slot].setVisible(s.p.isBot);
						botType[s.p.slot].setSelected(s.p.playType.ordinal());
						spectator[slot].setVisible(true);
						spectator[slot].setSelected(s.p.spectator ? 1 : 0);
					}
					break;
				}
				case KICK:
					ci.stop();
					dsh.message("message", m.message);
					dsh.activate("message", gc, StaticFiles.getUpMenuOut(), StaticFiles.getUpMenuIn());
					break;
				case MESSAGE:
					messages.add((String) m.message);
					break;
				case STARTGAME:
					dsh.message("multiplayergame", ci);
					dsh.activate("multiplayergame", gc, StaticFiles.getDownMenuOut(), StaticFiles.getDownMenuIn());
					return; // YES, RETURN! We don't want lobby to handle any more of the messages from the
							// server
				case LEVELUPDATE: {
					Object[] oa = (Object[]) m.message;
					@SuppressWarnings("unchecked")
					ArrayList<String> mapList = (ArrayList<String>) oa[1];
					int selectedMap = (Integer) oa[0];
					maps.clearItems();
					for (String s : mapList) {
						maps.addItems(s);
					}
					maps.setSelected(selectedMap);
					level = (Level) oa[2];
					if (level != null) {
						level.loadTextures();
						miniMap = null;
					}
					break;
				}
				case FOGUPDATE:
					fog.checked = (Boolean) m.message;
					break;
				case GAMETYPE:
					gameType.setSelected(gameType.items.indexOf(m.message));
					break;
			}
		}
	}

	public void render(GameContainer gc, DAL dal) {
		super.render(gc, dal);

		var g = ((SlickDAL) dal).g;

		int count = 0;

		var chatPos = chatBackground.getScreenLocation();

		for (int i = messages.size() - 1; i >= Math.max(messages.size() - 10, 0); i--) {
			g.drawString(messages.get(i), chatPos.x + 10,
					chatPos.y + chatBackground.height + (count + 1) * -20 * uiScale);
			count++;
		}

		g.setColor(Color.white);
		String hostname = ci.getServerAddr();
		if (hostname.equals("localhost") || hostname.equals("127.0.0.1")) {
			try {
				InetAddress thisIp = InetAddress.getLocalHost();
				hostname = thisIp.getHostAddress();
			} catch (UnknownHostException e1) {
			}
		}
		g.drawString("Server Address: " + hostname, 30, 30);

		// Draw the level between the maps dropdown and the chatbackground
		if (level != null) {
			var mapsLoc = maps.getScreenLocation();
			var chatLoc = chatBackground.getScreenLocation();

			var lx = mapsLoc.x + maps.width + 20;
			var ly = mapsLoc.y;
			var boundingWidth = chatLoc.x - lx - 20;
			var boundingHeight = chatBackground.height;

			float mapWidthPx = level.width * Level.tileSize;
			float mapHeightPx = level.height * Level.tileSize;

			// Figure out which dimension "dominates" and scale accordingly
			float mapAspect = mapWidthPx / mapHeightPx;
			float boundingAspect = (float) boundingWidth / (float) boundingHeight;

			float scale;
			float xOffset = 0;
			float yOffset = 0;

			if (mapAspect > boundingAspect) {
				// Map is relatively wider than the bounding area
				// Fit to width, adjust vertical offset to center
				scale = boundingWidth / mapWidthPx;
				float newHeight = mapHeightPx * scale;
				yOffset = (boundingHeight - newHeight) * 0.5f;
			} else {
				// Map is relatively taller (or same aspect); fit to height, adjust horizontal
				// offset
				scale = boundingHeight / mapHeightPx;
				float newWidth = mapWidthPx * scale;
				xOffset = (boundingWidth - newWidth) * 0.5f;
			}

			if (miniMap == null) {

				try {
					// Now create the image for the minimap and render
					miniMap = new Image(level.width * Level.tileSize, level.height * Level.tileSize);
					Graphics mg = miniMap.getGraphics();
					mg.clearAlphaMap();
					// mg.setDrawMode(Graphics.MODE_NORMAL);
					mg.setColor(Color.white);

					mg.fillRect(0, 0, level.width * Level.tileSize, level.height * Level.tileSize);

					SlickDAL mgDal = new SlickDAL();
					mgDal.gc = gc;
					mgDal.g = mg;

					// Render level floor, walls, etc.
					level.renderFloor(mgDal.getGraphics());
					level.render(mgDal.getGraphics());
					level.renderBuildings(mg, false);

					mg.flush();
				} catch (SlickException e) {
					throw new RuntimeException(e);
				}
			}

			if (miniMap != null) {
				g.setColor(Color.white);
				g.drawImage(miniMap, lx + xOffset, ly + yOffset, lx + xOffset + miniMap.getWidth() * scale,
						ly + yOffset + miniMap.getHeight() * scale, 0, 0, miniMap.getWidth(), miniMap.getHeight());
			}
		}
	}

	public void message(Object o) {
		if (o instanceof ClientInterface) {
			ci = (ClientInterface) o;
		}
	}

	public void event(DUIEvent event) {
		if (event.getElement() instanceof DDropDown) {
			DDropDown el = (DDropDown) event.getElement();
			if (el == maps) {
				ci.sendToServer(new Message(MessageType.LEVELUPDATE, el.getSelectedOrdinal()));
			} else if (el == gameType) {
				ci.sendToServer(new Message(MessageType.GAMETYPE, el.getSelected()));
			} else {
				String[] name = el.name.split(" ");
				if (name.length == 2) {
					int line = Integer.parseInt(name[1]);
					if (name[0].equals("hb")) {
						boolean isBot = el.getSelected().equals("BOT");
						ci.sendToServer(new Message(MessageType.SETBOT, new Object[] { line, isBot }));
					} else if (name[0].equals("bt")) {
						PlayType pt = PlayType.values()[el.getSelectedOrdinal()];
						ci.sendToServer(new Message(MessageType.SETPLAYTYPE, new Object[] { line, pt }));
					} else if (name[0].equals("sp")) {
						ci.sendToServer(new Message(MessageType.SETSPECTATOR,
								new Object[] { line, (el.getSelectedOrdinal() == 1) }));
					}
				}
			}
		} else if (event.getElement() instanceof DTextBox) {
			DTextBox b = (DTextBox) event.getElement();
			if (event.getType() == KeyEvent.VK_ENTER) {
				String text = b.getText().trim();
				if (text.length() > 0) {
					ci.sendToServer(new Message(MessageType.MESSAGE, b.getText().trim()));
				}
				b.setText("");
			}
		} else if (event.getElement() instanceof DButton) {
			DButton b = (DButton) event.getElement();
			if (event.getType() == DButton.MOUSE_UP) {
				if (b == startGame) {
					ci.sendToServer(new Message(MessageType.STARTGAME, null));
				} else if (b == leaveGame) {
					ci.stop();
					dsh.activate("home", gc, StaticFiles.getUpMenuOut(), StaticFiles.getUpMenuIn());
				} else if (b == fillBots) {
					for (int i = 0; i < 16; i++) {
						if (names[i].getText().equals("Open")) {
							ci.sendToServer(new Message(MessageType.SETBOT, new Object[] { i, true }));
						}
					}
				} else {
					String[] name = b.name.split(" ");
					if (name.length == 2) {
						int line = Integer.parseInt(name[1]);
						if (name[0].equals("na")) {
							ci.sendToServer(new Message(MessageType.SWITCHTEAMS, line));
						}
					}
				}
			}
		} else if (event.getElement() instanceof DCheckBox) {
			if (event.getElement() == fog) {
				ci.sendToServer(new Message(MessageType.FOGUPDATE, fog.checked));
			}
		}
	}

	@Override
	public void onResize(int width, int height) {
		super.onResize(width, height);
		miniMap = null;
	}

	public static class Slot {
		public SlotType type;
		public Player p;

		public Slot() {
			type = SlotType.ANY;
		}

		public Slot(SlotType type, Player p) {
			this.type = type;
			this.p = p;
		}
	}
}
