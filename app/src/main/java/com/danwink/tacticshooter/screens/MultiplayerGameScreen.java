package com.danwink.tacticshooter.screens;

import java.util.ArrayList;
import java.awt.event.KeyEvent;

import org.newdawn.slick.Color;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.Input;
import org.newdawn.slick.Music;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.geom.Rectangle;
import org.newdawn.slick.opengl.shader.ShaderProgram;

import com.danwink.tacticshooter.ClientState;
import com.danwink.tacticshooter.MessageType;
import com.danwink.tacticshooter.MusicQueuer;
import com.danwink.tacticshooter.StaticFiles;
import com.danwink.tacticshooter.gameobjects.Building;
import com.danwink.tacticshooter.gameobjects.Building.BuildingType;
import com.danwink.tacticshooter.gameobjects.Bullet;
import com.danwink.tacticshooter.gameobjects.Level;
import com.danwink.tacticshooter.gameobjects.Marker;
import com.danwink.tacticshooter.gameobjects.Level.TileType;
import com.danwink.tacticshooter.gameobjects.Player;
import com.danwink.tacticshooter.gameobjects.Team;
import com.danwink.tacticshooter.gameobjects.Unit;
import com.danwink.tacticshooter.gameobjects.Unit.UnitDef;
import com.danwink.tacticshooter.network.ClientInterface;
import com.danwink.tacticshooter.network.Message;
import com.danwink.tacticshooter.renderer.GameRenderer;
import com.danwink.tacticshooter.ui.DUIScreen;
import com.danwink.tacticshooter.ui.SelectedUnitsDisplay;
import com.phyloa.dlib.dui.DButton;
import com.phyloa.dlib.dui.DCheckBox;
import com.phyloa.dlib.dui.DColumnPanel;
import com.phyloa.dlib.dui.DGrid;
import com.phyloa.dlib.dui.DKeyEvent;
import com.phyloa.dlib.dui.DKeyListener;
import com.phyloa.dlib.dui.DMouseEvent;
import com.phyloa.dlib.dui.DMouseListener;
import com.phyloa.dlib.dui.DPanel;
import com.phyloa.dlib.dui.DText;
import com.phyloa.dlib.dui.DTextBox;
import com.phyloa.dlib.dui.DUI;
import com.phyloa.dlib.dui.DUIElement;
import com.phyloa.dlib.dui.DUIEvent;
import com.phyloa.dlib.dui.RelativePosition;
import com.phyloa.dlib.math.Point2i;
import com.phyloa.dlib.util.DMath;

import jp.objectclub.vecmath.Vector3f;

public class MultiplayerGameScreen extends DUIScreen implements DKeyListener, DMouseListener {
	int gamemodeButtonWidth = 75;
	int gamemodeButtonHeight = 75;

	ClientInterface ci;
	public ClientState cs = new ClientState(this);

	boolean waitingForMoveConfirmation = false;

	float sx, sy, sx2, sy2;
	boolean selecting = false;

	DColumnPanel escapeMenu;
	DButton quit;
	DButton returnToGame;
	DButton switchTeams;

	DPanel chatPanel;
	DTextBox chatBox;
	DCheckBox teamChat;
	DGrid gamemodeButtons;
	SelectedUnitsDisplay selectedUnitsDisplay;

	public Input input;

	Image miniMap;

	int bottomOffset = 200;

	boolean running = false;

	Graphics btg;

	Image wallTexture;
	Image floorTexture;
	Image backgroundTexture;

	Image bloodTexture;
	Image fog;

	Image endMap;

	boolean fogEnabled;

	ArrayList<String> messages = new ArrayList<String>();

	ShaderProgram shader;

	boolean mapChanged = true;

	ArrayList<Vector3f> pings = new ArrayList<Vector3f>();

	@SuppressWarnings("unchecked")
	ArrayList<Integer>[] battleGroups = new ArrayList[10];

	public GameRenderer gameRenderer;

	long lastClick;

	public DButton[][] buttonSlots = new DButton[3][3];

	int lastMouseX, lastMouseY;

	public void init(GameContainer gc) {
		input = gc.getInput();
		initializeUIElements(dui);

		for (int i = 0; i < 10; i++) {
			battleGroups[i] = new ArrayList<Integer>();
		}

		dui.addPassthroughKeyListener(this);
		dui.addPassthroughMouseListener(this);

		Music music;
		int musicChoice = DMath.randomi(1, 3);
		while ((music = StaticFiles.getMusic("play" + musicChoice)) == null) {
		}
		;
		music.play();
		music.addListener(new MusicQueuer(DMath.randomi(0, 2), "play1", "play2", "play3"));

		gameRenderer = new GameRenderer();

		running = true;
	}

	public void initializeUIElements(DUI dui) {
		escapeMenu = new DColumnPanel(0, 0, 200, 300);
		quit = new DButton("Quit Game", 0, 0, 200, 100);
		escapeMenu.add(quit);
		returnToGame = new DButton("Return to Game", 0, 100, 200, 100);
		escapeMenu.add(returnToGame);
		escapeMenu.setVisible(false);
		gamemodeButtons = new DGrid(0, 0, gamemodeButtonWidth * 3 * uiScale, gamemodeButtonHeight * 3 * uiScale, 3, 3);
		gamemodeButtons.setRelativePosition(RelativePosition.BOTTOM_LEFT, 0, 0);

		chatPanel = new DPanel(gc.getWidth() / 2 - 200, gc.getHeight() / 2 - 50, 400, 100);
		chatBox = new DTextBox(0, 50, 400, 50);
		teamChat = new DCheckBox(10, 10, 30, 30);

		chatPanel.add(new DText("Team Chat", 50, 25));
		chatPanel.add(teamChat);
		chatPanel.add(chatBox);

		chatPanel.setVisible(false);

		selectedUnitsDisplay = new SelectedUnitsDisplay(RelativePosition.TOP_RIGHT, 4, 48);
		selectedUnitsDisplay.setClientState(cs);
		selectedUnitsDisplay.setInput(input);
	}

	public void createUIElements(DUI dui, float screenHeight) {
		escapeMenu.setRelativePosition(RelativePosition.CENTER, 0, 0);
		quit.setSize(200 * uiScale, 100 * uiScale);
		returnToGame.setSize(200 * uiScale, 100 * uiScale);

		gamemodeButtons.setSize(gamemodeButtonWidth * 3 * uiScale, gamemodeButtonHeight * 3 * uiScale);

		selectedUnitsDisplay.setRelativePosition(RelativePosition.TOP_RIGHT, -8 * uiScale, 100 * uiScale);
		selectedUnitsDisplay.setPortraitSize(48 * uiScale);

		dui.add(chatPanel);
		dui.add(escapeMenu);
		dui.add(gamemodeButtons);
		dui.add(selectedUnitsDisplay);
	}

	public void update(GameContainer gc, float d) {
		if (!running)
			return;

		// This is so fucked. I did all the tuning with the wrong timestep scale. So now
		// we do this shit.
		d = (d * 1000f) / 60f;

		while (ci.hasClientMessages()) {
			Message m = ci.getNextClientMessage();
			switch (m.messageType) {
				case UNITUPDATE:
					Unit u = (Unit) m.message;
					Unit tu = cs.unitMap.get(u.id);
					if (tu == null) {
						cs.unitMap.put(u.id, u);
						cs.units.add(u);
						StaticFiles.getSound("ping1").play(1.f, cs.getSoundMag(gc, u.x, u.y));
						tu = u;
					}
					tu.sync(u);
					break;
				case LEVELUPDATE:
					boolean newLevel = cs.l == null;
					cs.l = (Level) m.message;
					cs.l.loadTextures();

					if (newLevel) {
						cs.lastWalked = new int[cs.l.width][cs.l.height];

						if (cs.player != null) {
							scrollToTeamBase(cs.player.team);
						}
					}
					break;
				case BULLETUPDATE:
					Bullet b = (Bullet) m.message;
					cs.bullets.add(b);
					(Math.random() > .5 ? StaticFiles.getSound("bullet1") : StaticFiles.getSound("bullet2")).play(1.f,
							cs.getSoundMag(gc, b.loc.x, b.loc.y) * .2f);
					break;
				case MOVESUCCESS:
					this.waitingForMoveConfirmation = false;
					break;
				case PLAYERUPDATE:
					Player newPlayer = (Player) m.message;
					if ((cs.player == null || newPlayer.team.id != cs.player.team.id) && cs.l != null
							&& !newPlayer.spectator) {
						scrollToTeamBase(newPlayer.team);
					}
					this.cs.player = newPlayer;
					break;
				case BUILDINGUPDATE:
					if (cs.l != null) {
						Building building = (Building) m.message;
						for (int i = 0; i < cs.l.buildings.size(); i++) {
							Building bt = cs.l.buildings.get(i);
							if (bt.id == building.id) {
								cs.l.buildings.set(i, building);
							}
						}
					}
					break;
				case PLAYERLIST:
					cs.players = (Player[]) m.message;
					break;
				case MESSAGE:
					String mess = (String) m.message;
					int lineLength = 60;
					do {
						String p1 = mess.substring(0, Math.min(lineLength, mess.length()));
						mess = mess.substring(Math.min(lineLength, mess.length()), mess.length());
						messages.add(p1 + (mess.length() > 0 ? "-" : ""));
					} while (mess.length() > 0);
					break;
				case TILEUPDATE: {
					Object[] arr = (Object[]) m.message;
					int tx = (Integer) arr[0];
					int ty = (Integer) arr[1];
					TileType change = (TileType) arr[2];
					cs.l.tiles[tx][ty] = change;
					mapChanged = true;
					break;
				}
				case PINGMAP:
					Point2i pingLoc = (Point2i) m.message;
					pings.add(new Vector3f(pingLoc.x, pingLoc.y, 100));
					StaticFiles.getSound("ping1").play(2.f, 1.f);
					break;
				case GAMEOVER:
					dsh.message("postgame", m.message);
					dsh.message("postgame", endMap);
					dsh.activate("postgame", gc, StaticFiles.getUpMenuOut(), StaticFiles.getUpMenuIn());
					return;
				case FOGUPDATE:
					fogEnabled = (Boolean) m.message;
					break;
				case UNITMINIUPDATE: {
					Unit.UnitUpdate uu = (Unit.UnitUpdate) m.message;
					Unit unit = cs.unitMap.get(uu.id);
					if (unit != null) {
						unit.health = uu.health;
						unit.sheading = uu.heading;
						unit.sx = uu.x;
						unit.sy = uu.y;
					}
					break;
				}
				case CREATEBUTTON: {
					assert (cs.player != null);
					if (cs.player.spectator)
						break;
					Object[] arr = (Object[]) m.message;
					String id = (String) arr[0];
					String text = (String) arr[1];
					int xSlot = (int) arr[2];
					int ySlot = (int) arr[3];
					String imageKey = (String) arr[4];
					DButton button = new DButton(text,
							xSlot * gamemodeButtonWidth,
							ySlot * gamemodeButtonHeight,
							gamemodeButtonWidth,
							gamemodeButtonHeight);
					var backgroundImage = cs.l.theme.getPortrait(imageKey);
					if (backgroundImage != null) {
						button.setBackground(backgroundImage);
					}
					button.name = id;
					gamemodeButtons.add(button, xSlot, ySlot);
					buttonSlots[xSlot][ySlot] = button;
					break;
				}
				case MARKERCREATE: {
					Marker marker = (Marker) m.message;
					cs.markers.add(marker);
					break;
				}
				case MARKERDELETE: {
					Integer id = (Integer) m.message;
					cs.markers.removeIf(marker -> marker.id == id);
					break;
				}
			}
		}

		if (cs.l == null) {
			return;
		}

		if (!chatPanel.isVisible() && !escapeMenu.isVisible()) {
			float scrollSpeed = 20;
			Rectangle screenBounds = getScreenBounds();

			boolean scrollup = cs.camera.y > screenBounds.getMinY() && (input.isKeyDown(Input.KEY_UP)
					|| (gc.isFullscreen() && input.getMouseY() < 10));
			boolean scrolldown = cs.camera.y + gc.getHeight() < screenBounds.getMaxY()
					&& (input.isKeyDown(Input.KEY_DOWN)
							|| (gc.isFullscreen() && input.getMouseY() > gc.getHeight() - 10));
			boolean scrollleft = cs.camera.x > screenBounds.getMinX() && (input.isKeyDown(Input.KEY_LEFT)
					|| (gc.isFullscreen() && input.getMouseX() < 10));
			boolean scrollright = cs.camera.x + gc.getWidth() < screenBounds.getMaxX()
					&& (input.isKeyDown(Input.KEY_RIGHT)
							|| (gc.isFullscreen() && input.getMouseX() > gc.getWidth() - 10));

			float scrollMultiplier = (input.isKeyDown(Input.KEY_LSHIFT) || input.isKeyDown(Input.KEY_RSHIFT)) ? 2 : 1;

			if (scrollup)
				cs.camera.y -= scrollSpeed * d * scrollMultiplier;
			if (scrolldown)
				cs.camera.y += scrollSpeed * d * scrollMultiplier;
			if (scrollleft)
				cs.camera.x -= scrollSpeed * d * scrollMultiplier;
			if (scrollright)
				cs.camera.x += scrollSpeed * d * scrollMultiplier;
		}

		for (int i = 0; i < cs.units.size(); i++) {
			Unit u = cs.units.get(i);
			u.clientUpdate(cs, d);
			if (u.timeSinceUpdate > 100) {
				ci.sendToServer(new Message(MessageType.UNITUPDATE, u));
				u.timeSinceUpdate = 0;
			}
			if (!u.alive) {
				cs.units.remove(i);
				cs.unitMap.remove(u.id);
				cs.selected.remove((Object) u.id);
				// Trigger recalc of selected units display
				dui.doLayout();
				gameRenderer.killUnit(u);
				if (u.type.explodesOnDeath) {
					StaticFiles.getSound("explode1").play();
				} else {
					(Math.random() > .5 ? StaticFiles.getSound("death1") : StaticFiles.getSound("death2")).play(1.f,
							cs.getSoundMag(gc, u.x, u.y));
				}
				i--;
				continue;
			}
		}

		for (int i = 0; i < cs.bullets.size(); i++) {
			Bullet b = cs.bullets.get(i);
			b.clientUpdate(this, d, gc);
			if (!b.alive) {
				cs.bullets.remove(i);
				i--;
				continue;
			}
		}

		for (int i = 0; i < pings.size(); i++) {
			Vector3f v = pings.get(i);
			v.z -= d;
			if (v.z < 0) {
				pings.remove(i);
				i--;
			}
		}

		// Update UI
		super.update(gc, d);

		if (cs.l != null && miniMap == null) {
			try {
				boolean xLarger = cs.l.width > cs.l.height;
				float xOffset = xLarger ? 0 : 100 - 100 * cs.l.width / (float) cs.l.height;
				float yOffset = !xLarger ? 0 : 100 - 100 * (float) cs.l.height / cs.l.width;
				float scale = 200.f / ((xLarger ? cs.l.width : cs.l.height) * Level.tileSize);
				miniMap = new Image(200, 200);
				Graphics mg = miniMap.getGraphics();
				mg.translate(xOffset, yOffset);
				mg.scale(scale, scale);
				cs.l.renderFloor(mg);
				cs.l.render(mg);
				mg.flush();

			} catch (SlickException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		gameRenderer.update(cs, d);

		cs.frame++;
	}

	public void render(GameContainer gc, Graphics g) {
		if (!running)
			return;

		if (cs.l == null) {
			return;
		}

		// TODO: faster way to know when game is over, but server hasn't yet send
		// GAMEOVER command?
		if (endMap == null) {
			// Render endMap if game is over
			// Find out if game is over
			int teamA = -1;
			boolean gameOver = false;
			for (int i = 0; i < cs.l.buildings.size(); i++) {
				Building b = cs.l.buildings.get(i);
				if (b.t != null) {
					if (teamA != -1 && b.t.id != teamA) {
						gameOver = true;
						break;
					} else {
						teamA = b.t.id;
					}
				}
			}
			if (gameOver) {
				try {
					endMap = new Image(cs.l.width * Level.tileSize, cs.l.height * Level.tileSize);
					Graphics emg = endMap.getGraphics();
					emg.setAntiAlias(true);
					gameRenderer.renderEndGameMap(emg, cs);
				} catch (SlickException e) {
					e.printStackTrace();
				}

			}
		}

		if (mapChanged) {
			gameRenderer.updateWalls(cs);
			mapChanged = false;
		}

		gameRenderer.render(g, cs, gc, fogEnabled);

		cs.camera.start(gc, g);
		if (selecting) {
			g.setColor(Color.blue);
			float x1 = Math.min(sx, sx2);
			float y1 = Math.min(sy, sy2);
			float x2 = Math.max(sx, sx2);
			float y2 = Math.max(sy, sy2);
			g.drawRect(x1, y1, x2 - x1, y2 - y1);
		}
		cs.camera.end(g);

		g.setColor(new Color(0, 0, 0, 128));
		g.fillRect(0, 0, gc.getWidth(), 30 * uiScale);
		g.setColor(Color.white);
		if (cs.player != null) {
			g.drawString("Money: " + cs.player.money, 100 * uiScale, 10 * uiScale);
			g.drawString("Selected: " + cs.selected.size(), 200 * uiScale, 10 * uiScale);
			g.setColor(Color.black);
			if (messages.size() > 0) {
				for (int i = messages.size() - 1; i >= Math.max(messages.size() - 12, 0); i--) {
					g.drawString(messages.get(i), 15 * uiScale,
							330 * uiScale - (messages.size() - 1 - i) * 25 * uiScale);
				}
			}
		}

		// Render UI
		super.render(gc, g);

		// Draw minimap
		g.setClip(gc.getWidth() - 200, gc.getHeight() - 200, 200, 200);
		boolean xLarger = cs.l.width > cs.l.height;
		float xOffset = xLarger ? 0 : 100 - 100 * cs.l.width / (float) cs.l.height;
		float yOffset = !xLarger ? 0 : 100 - 100 * (float) cs.l.height / cs.l.width;
		float scale = 200.f / ((xLarger ? cs.l.width : cs.l.height) * Level.tileSize);
		g.pushTransform();
		g.translate(gc.getWidth() - 200, gc.getHeight() - 200);
		g.setColor(Color.white);
		g.fillRect(0, 0, 200, 200);
		g.pushTransform();

		if (miniMap != null) {
			g.drawImage(miniMap, 0, 0);
		}
		g.translate(xOffset, yOffset);
		g.scale(scale, scale);
		cs.l.renderBuildings(g, false);
		for (int i = 0; i < cs.units.size(); i++) {
			Unit u = cs.units.get(i);
			u.renderMinimap(g, cs.player);
		}

		if (fogEnabled) {
			// g.setDrawMode( Graphics.MODE_COLOR_MULTIPLY );
			// g.drawImage( fog, 0, 0 );
			// g.setDrawMode( Graphics.MODE_NORMAL );
		}

		g.popTransform();

		for (int i = 0; i < pings.size(); i++) {
			Vector3f v = pings.get(i);
			float size = (v.z / 100.f) * 10;
			g.setColor(Color.pink);
			g.fillOval(v.x - size / 2, v.y - size / 2, size, size);
			g.setColor(Color.black);
			g.drawOval(v.x - size / 2, v.y - size / 2, size, size);
		}

		// Draw window
		g.setColor(Color.blue);
		var topLeft = cs.camera.screenToWorld(0, 0, gc);
		var bottomRight = cs.camera.screenToWorld(gc.getWidth(), gc.getHeight(), gc);
		g.drawRect(xOffset + topLeft.x * scale, yOffset + topLeft.y * scale, (bottomRight.x - topLeft.x) * scale,
				(bottomRight.y - topLeft.y) * scale);

		g.setColor(Color.black);
		g.setLineWidth(2);
		g.drawRect(0, 0, 200, 300);
		g.setLineWidth(1);
		g.popTransform();

		g.clearClip();

		if (escapeMenu.isVisible()) {
			g.setColor(new Color(0, 0, 0, 128));
			// Left side
			g.fillRect(0, 0, gc.getWidth() / 2 - 100 * uiScale, gc.getHeight());

			// Right side
			g.fillRect(gc.getWidth() / 2 + 100 * uiScale, 0, gc.getWidth() / 2 - 100 * uiScale, gc.getHeight());

			// Top
			g.fillRect(gc.getWidth() / 2 - 100 * uiScale, 0, 200 * uiScale, gc.getHeight() / 2 - 100 * uiScale);

			// bottom
			g.fillRect(gc.getWidth() / 2 - 100 * uiScale, gc.getHeight() / 2 + 100 * uiScale, 200 * uiScale,
					gc.getHeight() / 2 - 100 * uiScale);
		}

		if (input.isKeyDown(Input.KEY_TAB) && cs.players != null) {
			g.setColor(new Color(128, 128, 128, 200));
			g.fillRect(gc.getWidth() / 2 - 400, gc.getHeight() / 2 - 300, 800, 600);
			g.setColor(Color.black);
			g.drawRect(gc.getWidth() / 2 - 400, gc.getHeight() / 2 - 300, 800, 600);
			int red = 0, green = 0;
			for (int i = 0; i < cs.players.length; i++) {
				Player p = cs.players[i];
				boolean teamRed = p.team.id == Team.a.id;
				g.drawString(p.name + " - " + (teamRed ? "RED" : "GREEN"), gc.getWidth() / 2 - (teamRed ? 390 : -10),
						gc.getHeight() / 2 - 270 + (teamRed ? red : green) * 30);
				if (p.team.id == Team.a.id) {
					red++;
				} else {
					green++;
				}
			}
		}
	}

	public void onExit() {
		running = false;
		if (ci != null) {
			ci.stop();
		}
		cs.resetState();
		miniMap = null;
		bloodTexture = null;
		wallTexture = null;
		mapChanged = true;
		endMap = null;
		buttonSlots = new DButton[3][3];
		dui.setEnabled(false);
		messages.clear();
	}

	public void scrollToTeamBase(Team t) {
		int destX = 0;
		int destY = 0;
		boolean found = false;
		for (int i = 0; i < cs.l.buildings.size(); i++) {
			Building b = cs.l.buildings.get(i);
			if (b.bt == BuildingType.CENTER && b.t != null && b.t.id == t.id) {
				destX = b.x;
				destY = b.y;
				found = true;
				break;
			}
		}

		if (found) {
			Rectangle screenBounds = getScreenBounds();
			cs.camera.x = DMath.bound(destX, screenBounds.getMinX(), screenBounds.getMaxX());
			cs.camera.y = DMath.bound(destY, screenBounds.getMinY(), screenBounds.getMaxY());
		}
	}

	public Rectangle getScreenBounds() {
		var topLeft = cs.camera.screenToWorld(0, 0, gc);
		var bottomRight = cs.camera.screenToWorld(gc.getWidth(), gc.getHeight(), gc);
		return new Rectangle(topLeft.x, topLeft.y, bottomRight.x - topLeft.x, bottomRight.y - topLeft.y);
	}

	@Override
	public boolean mousePressed(DMouseEvent e) {
		if (e.x > gc.getWidth() - 200 && e.y > gc.getHeight() - 200 && !selecting) {
			boolean xLarger = cs.l.width > cs.l.height;
			float xOffset = xLarger ? 0 : 100 - 100 * cs.l.width / (float) cs.l.height;
			float yOffset = !xLarger ? 0 : 100 - 100 * (float) cs.l.height / cs.l.width;
			float scale = 200.f / ((xLarger ? cs.l.width : cs.l.height) * Level.tileSize);
			float minimapX = (e.x - (gc.getWidth() - 200 + xOffset));
			float minimapY = (e.y - (gc.getHeight() - 200 + yOffset));
			float mapX = minimapX / scale;
			float mapY = minimapY / scale;

			if (input.isKeyDown(Input.KEY_LCONTROL)) {
				ci.sendToServer(new Message(MessageType.MESSAGE, "/ping " + (input.getMouseX() - (gc.getWidth() - 200))
						+ " " + (input.getMouseY() - (gc.getHeight() - 200))));
			} else {
				if (e.button == Input.MOUSE_LEFT_BUTTON) {
					Rectangle screenBounds = getScreenBounds();
					cs.camera.x = DMath.bound(mapX, screenBounds.getMinX(), screenBounds.getMaxX());
					cs.camera.y = DMath.bound(mapY, screenBounds.getMinY(),
							screenBounds.getMaxY());
				} else if (e.button == Input.MOUSE_RIGHT_BUTTON) {
					int tx = cs.l.getTileX(((e.x - (gc.getWidth() - 200.f)) / 200.f) * cs.l.width * Level.tileSize);
					int ty = cs.l.getTileY(((e.y - (gc.getHeight() - 200.f)) / 200.f) * cs.l.height * Level.tileSize);
					ci.sendToServer(new Message(input.isKeyDown(Input.KEY_LCONTROL) ? MessageType.SETATTACKPOINTCONTINUE
							: MessageType.SETATTACKPOINT, new Object[] { new Point2i(tx, ty), cs.selected }));
					this.waitingForMoveConfirmation = true;
				}
			}
		} else {
			var worldCoords = cs.camera.screenToWorld(e.x, e.y, gc);
			if (e.button == Input.MOUSE_LEFT_BUTTON) {
				sx = worldCoords.x;
				sy = worldCoords.y;
				sx2 = sx;
				sy2 = sy;
				selecting = true;
			} else if (e.button == Input.MOUSE_RIGHT_BUTTON) {
				int tx = (int) (worldCoords.x / Level.tileSize);
				int ty = (int) (worldCoords.y / Level.tileSize);
				if (input.isKeyDown(Input.KEY_LCONTROL)) {
					ci.sendToServer(new Message(MessageType.SETATTACKPOINTCONTINUE,
							new Object[] { new Point2i(tx, ty), cs.selected }));
				} else if (input.isKeyDown(Input.KEY_LSHIFT)) {
					ci.sendToServer(new Message(MessageType.LOOKTOWARD,
							new Object[] { new Point2i(tx * Level.tileSize, ty * Level.tileSize), cs.selected }));
				} else {
					ci.sendToServer(
							new Message(MessageType.SETATTACKPOINT, new Object[] { new Point2i(tx, ty), cs.selected }));
				}

				this.waitingForMoveConfirmation = true;
			}
		}
		lastMouseX = e.x;
		lastMouseY = e.y;
		return false;
	}

	@Override
	public boolean mouseReleased(DMouseEvent e) {
		if (e.button == Input.MOUSE_LEFT_BUTTON && selecting) {
			cs.clearSelected();

			var worldCoords = cs.camera.screenToWorld(e.x, e.y, gc);
			float x1 = Math.min(sx, worldCoords.x);
			float y1 = Math.min(sy, worldCoords.y);
			float x2 = Math.max(sx, worldCoords.x);
			float y2 = Math.max(sy, worldCoords.y);

			if (x2 - x1 > 2 || y2 - y1 > 2) {
				for (Unit u : cs.units) {
					u.selected = u.owner.id == this.cs.player.id && u.x > x1 && u.x < x2 && u.y > y1 && u.y < y2;
					if (u.selected) {
						cs.selected.add(u.id);
					}
				}
			} else {
				UnitDef matchType = null;
				for (Unit u : cs.units) {
					float dx = x2 - u.x;
					float dy = y2 - u.y;
					if (u.owner.id == this.cs.player.id && dx * dx + dy * dy < Unit.radius * Unit.radius) {
						u.selected = true;
						cs.selected.add(u.id);
						matchType = u.type;
						break;
					} else {
						u.selected = false;
					}
				}

				// Double click select same type units
				long timeDiff = System.currentTimeMillis() - lastClick;
				if (matchType != null && cs.selected.size() == 1 && timeDiff > 100 && timeDiff < 500) {
					var topLeft = cs.camera.screenToWorld(0, 0, gc);
					var bottomRight = cs.camera.screenToWorld(gc.getWidth(), gc.getHeight(), gc);
					for (Unit u : cs.units) {
						if (u.type.name.equals(matchType.name)
								&& u.owner.id == this.cs.player.id
								&& u.x > topLeft.x
								&& u.y > topLeft.y
								&& u.x < bottomRight.x
								&& u.y < bottomRight.y
								&& !cs.selected.contains(u.id)) {
							u.selected = true;
							cs.selected.add(u.id);
						}
					}
				}
			}
			selecting = false;
			// Trigger recalc of selected units display
			dui.doLayout();
		}
		if (cs.selected.size() > 0) {
			lastClick = System.currentTimeMillis();
		}
		return false;
	}

	@Override
	public void mouseDragged(DMouseEvent e) {
		if (e.x > gc.getWidth() - 200 && e.y > gc.getHeight() - 200 && !selecting) {
			if (input.isMouseButtonDown(Input.MOUSE_LEFT_BUTTON) && !input.isKeyDown(Input.KEY_LCONTROL)) {
				boolean xLarger = cs.l.width > cs.l.height;
				float xOffset = xLarger ? 0 : 100 - 100 * cs.l.width / (float) cs.l.height;
				float yOffset = !xLarger ? 0 : 100 - 100 * (float) cs.l.height / cs.l.width;
				float scale = 200.f / ((xLarger ? cs.l.width : cs.l.height) * Level.tileSize);
				float minimapX = (e.x - (gc.getWidth() - 200 + xOffset));
				float minimapY = (e.y - (gc.getHeight() - 200 + yOffset));
				float mapX = minimapX / scale;
				float mapY = minimapY / scale;

				Rectangle screenBounds = getScreenBounds();
				cs.camera.x = DMath.bound(mapX, screenBounds.getMinX(), screenBounds.getMaxX());
				cs.camera.y = DMath.bound(mapY, screenBounds.getMinY(), screenBounds.getMaxY());
			}
		} else {
			if (input.isMouseButtonDown(Input.MOUSE_LEFT_BUTTON)) {
				var worldCoords = cs.camera.screenToWorld(e.x, e.y, gc);
				sx2 = worldCoords.x;
				sy2 = worldCoords.y;
			} else if (input.isMouseButtonDown(Input.MOUSE_MIDDLE_BUTTON)) {
				cs.camera.x += (lastMouseX - e.x) / cs.camera.zoom;
				cs.camera.y += (lastMouseY - e.y) / cs.camera.zoom;
			}
		}

		lastMouseX = e.x;
		lastMouseY = e.y;
	}

	@Override
	public void mouseMoved(DMouseEvent e) {

	}

	@Override
	public void mouseWheel(DMouseEvent e) {
		float zoomSpeed = 0.1f;
		int dir = e.wheel > 0 ? 1 : -1;
		float zoom = dir * zoomSpeed;

		var mx = input.getMouseX();
		var my = input.getMouseY();

		var worldCoords = cs.camera.screenToWorld(mx, my, gc);

		cs.camera.zoom(zoom);

		var afterWorldCoords = cs.camera.screenToWorld(mx, my, gc);

		cs.camera.x += worldCoords.x - afterWorldCoords.x;
		cs.camera.y += worldCoords.y - afterWorldCoords.y;
	}

	@Override
	public void keyPressed(DKeyEvent e) {
		if (e.keyCode == KeyEvent.VK_ESCAPE) {
			escapeMenu.setVisible(!escapeMenu.isVisible());
			chatPanel.setVisible(false);
			chatBox.setText("");
		} else if (e.keyCode == KeyEvent.VK_ENTER) {
			if (chatPanel.isVisible()) {
				if (chatBox.getText().trim().length() > 0) {
					ci.sendToServer(new Message(MessageType.MESSAGE,
							(teamChat.checked ? "/team " : "") + chatBox.getText().trim()));
				}
				chatPanel.setVisible(false);
				chatBox.setText("");
			} else {
				chatBox.setText("");
				chatPanel.setVisible(true);
				dui.setFocus(chatBox);
			}
		} else if (e.keyCode >= KeyEvent.VK_1 && e.keyCode <= KeyEvent.VK_9) {
			ArrayList<Integer> bg = battleGroups[e.keyCode - KeyEvent.VK_1];
			if (input.isKeyPressed(Input.KEY_LCONTROL)) {
				bg.clear();
				for (int i = 0; i < cs.selected.size(); i++) {
					bg.add(cs.selected.get(i));
				}
			} else {
				// Deselect all units
				for (int i = 0; i < cs.selected.size(); i++) {
					cs.unitMap.get(cs.selected.get(i)).selected = false;
				}
				cs.selected.clear();

				// Select units in battlegroup
				for (int i = 0; i < bg.size(); i++) {
					Unit u = cs.unitMap.get(bg.get(i));
					if (u != null && u.alive) {
						u.selected = true;
						cs.selected.add(bg.get(i));
					}
				}
				// Trigger recalc of selected units display
				dui.doLayout();
			}
		} else {
			DButton b = null;
			switch (e.keyCode) {
				case KeyEvent.VK_Q:
					b = buttonSlots[0][0];
					break;
				case KeyEvent.VK_W:
					b = buttonSlots[1][0];
					break;
				case KeyEvent.VK_E:
					b = buttonSlots[2][0];
					break;
				case KeyEvent.VK_A:
					b = buttonSlots[0][1];
					break;
				case KeyEvent.VK_S:
					b = buttonSlots[1][1];
					break;
				case KeyEvent.VK_D:
					b = buttonSlots[2][1];
					break;
				case KeyEvent.VK_Z:
					b = buttonSlots[0][2];
					break;
				case KeyEvent.VK_X:
					b = buttonSlots[1][2];
					break;
				case KeyEvent.VK_C:
					b = buttonSlots[2][2];
					break;
			}

			if (b != null) {
				dui.event(new DUIEvent(b, DButton.MOUSE_UP));
			}
		}
	}

	@Override
	public void message(Object o) {
		this.ci = (ClientInterface) o;
	}

	public void event(DUIEvent event) {
		DUIElement e = event.getElement();
		if (e instanceof DButton && event.getType() == DButton.MOUSE_UP) {
			if (e == switchTeams) {
				ci.sendToServer(new Message(MessageType.SWITCHTEAMS, cs.player.team));
			} else if (e == quit) {
				running = false;
				escapeMenu.setVisible(false);
				dsh.activate("home", gc, StaticFiles.getUpMenuOut(), StaticFiles.getUpMenuIn());
			} else if (e == returnToGame) {
				escapeMenu.setVisible(false);
			} else if (e.name.startsWith("userbutton")) {
				ci.sendToServer(new Message(MessageType.BUTTONPRESS,
						new Object[] { e.name, input.isKeyDown(Input.KEY_LSHIFT) }));
			}
		}
	}

	public void drawBlood(float x, float y) {
		gameRenderer.drawBlood(x, y);
	}

	public void bulletImpact(Bullet bullet) {
		gameRenderer.bulletImpact(bullet);
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
	public void keyReleased(DKeyEvent dke) {
		// TODO Auto-generated method stub

	}
}
