package com.danwink.tacticshooter.screens;

import java.util.ArrayList;

import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.geom.Rectangle;
import org.newdawn.slick.opengl.shader.ShaderProgram;
import org.newdawn.slick.util.pathfinding.AStarPathFinder;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Buttons;
import com.badlogic.gdx.Input.Keys;
import com.danwink.tacticshooter.Assets;
import com.danwink.tacticshooter.ClientState;
import com.danwink.tacticshooter.MessageType;
import com.danwink.tacticshooter.MusicQueuer;
import com.danwink.tacticshooter.StaticFiles;
import com.danwink.tacticshooter.ai.LevelAnalysis;
import com.danwink.tacticshooter.dal.DAL;
import com.danwink.tacticshooter.dal.DAL.DALColor;
import com.danwink.tacticshooter.dal.DAL.DALTexture;
import com.danwink.tacticshooter.gameobjects.Building;
import com.danwink.tacticshooter.gameobjects.Building.BuildingType;
import com.danwink.tacticshooter.gameobjects.Bullet;
import com.danwink.tacticshooter.gameobjects.Level;
import com.danwink.tacticshooter.gameobjects.Level.TileType;
import com.danwink.tacticshooter.gameobjects.Marker;
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

	DALTexture miniMap;

	int bottomOffset = 200;

	boolean running = false;

	Graphics btg;

	Image fog;

	DALTexture endMap;

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

	LevelAnalysis levelAnalysis;
	boolean showLevelAnalysis = false;

	public void init(DAL dal) {
		initializeUIElements(dui);

		for (int i = 0; i < 10; i++) {
			battleGroups[i] = new ArrayList<Integer>();
		}

		dui.rootPane.consumeMouseEvents = false;
		dui.addPassthroughKeyListener(this);
		dui.addPassthroughMouseListener(this);

		MusicQueuer.shuffleTracks("play1", "play2", "play3");

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

		chatPanel = new DPanel(dal.getWidth() / 2 - 200, dal.getHeight() / 2 - 50, 400, 100);
		chatBox = new DTextBox(0, 50, 400, 50);
		teamChat = new DCheckBox(10, 10, 30, 30);

		chatPanel.add(new DText("Team Chat", 50, 25));
		chatPanel.add(teamChat);
		chatPanel.add(chatBox);

		chatPanel.setVisible(false);

		selectedUnitsDisplay = new SelectedUnitsDisplay(RelativePosition.TOP_RIGHT, 4, 48);
		selectedUnitsDisplay.setClientState(cs);
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

	@Override
	public void update(DAL dal, float d) {
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
						Assets.getSound("ping1").play(cs.getSoundMag(u.x, u.y));
						tu = u;
					}
					tu.sync(u);
					break;
				case LOBBYLEVELINFO:
					throw new RuntimeException("Received lobby level info in game");
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
					(Math.random() > .5 ? Assets.getSound("bullet1") : Assets.getSound("bullet2")).play(
							cs.getSoundMag(b.loc.x, b.loc.y) * .2f);
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
					cs.l.setTile(tx, ty, change);
					mapChanged = true;
					break;
				}
				case PINGMAP:
					Point2i pingLoc = (Point2i) m.message;
					pings.add(new Vector3f(pingLoc.x, pingLoc.y, 100));
					Assets.getSound("ping1").play(1.f, 2.f, 0.f);
					break;
				case GAMEOVER:
					dsh.message("postgame", m.message);
					dsh.message("postgame", endMap);
					dsh.activate("postgame", dal, StaticFiles.getUpMenuOut(), StaticFiles.getUpMenuIn());
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

			boolean scrollUp = cs.camera.y > 0 && (Gdx.input.isKeyPressed(Keys.UP)
					|| (dal.isFullscreen() && Gdx.input.getY() < 10));

			boolean scrollDown = cs.camera.y < cs.l.height * Level.tileSize
					&& (Gdx.input.isKeyPressed(Keys.DOWN)
							|| (dal.isFullscreen() && Gdx.input.getY() > dal.getHeight() - 10));

			boolean scrollLeft = cs.camera.x > 0 && (Gdx.input.isKeyPressed(Keys.LEFT)
					|| (dal.isFullscreen() && Gdx.input.getX() < 10));

			boolean scrollRight = cs.camera.x < cs.l.width * Level.tileSize
					&& (Gdx.input.isKeyPressed(Keys.RIGHT)
							|| (dal.isFullscreen() && Gdx.input.getX() > dal.getWidth() - 10));

			float scrollMultiplier = (Gdx.input.isKeyPressed(Keys.SHIFT_LEFT)
					|| Gdx.input.isKeyPressed(Keys.SHIFT_RIGHT)) ? 2 : 1;

			if (scrollUp)
				cs.camera.y -= scrollSpeed * d * scrollMultiplier;
			if (scrollDown)
				cs.camera.y += scrollSpeed * d * scrollMultiplier;
			if (scrollLeft)
				cs.camera.x -= scrollSpeed * d * scrollMultiplier;
			if (scrollRight)
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
					Assets.getSound("explode1").play();
				} else {
					(Math.random() > .5 ? Assets.getSound("death1") : Assets.getSound("death2"))
							.play(cs.getSoundMag(u.x, u.y));
				}
				i--;
				continue;
			}
		}

		for (int i = 0; i < cs.bullets.size(); i++) {
			Bullet b = cs.bullets.get(i);
			b.clientUpdate(this, d);
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
		super.update(dal, d);

		if (cs.l != null && miniMap == null) {
			boolean xLarger = cs.l.width > cs.l.height;
			float xOffset = xLarger ? 0 : 100 - 100 * cs.l.width / (float) cs.l.height;
			float yOffset = !xLarger ? 0 : 100 - 100 * (float) cs.l.height / cs.l.width;
			float scale = 200.f / ((xLarger ? cs.l.width : cs.l.height) * Level.tileSize);
			miniMap = dal.generateRenderableTexture(200, 200);
			miniMap.getTextureRegion().flip(false, true);
			miniMap.renderTo(mg -> {
				mg.translate(xOffset, yOffset);
				mg.scale(scale, scale);

				cs.l.renderFloor(mg);
				cs.l.render(mg);
				mg.flush();
			});
		}

		gameRenderer.update(cs, d);

		cs.frame++;
	}

	@Override
	public void render(DAL dal) {
		if (!running)
			return;

		if (cs.l == null) {
			return;
		}

		var g = dal.getGraphics();

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
				endMap = dal.generateRenderableTexture(cs.l.width * Level.tileSize, cs.l.height * Level.tileSize);
				endMap.renderTo(emg -> {
					emg.setAntiAlias(true);

					gameRenderer.renderEndGameMap(dal.useGraphics(emg), cs);
				});
			}
		}

		if (mapChanged) {
			gameRenderer.updateWalls(cs);
			mapChanged = false;
		}

		gameRenderer.render(dal, cs, fogEnabled);

		cs.camera.start(g);

		if (showLevelAnalysis) {
			levelAnalysis.render(g);
		}

		if (selecting) {
			g.setColor(DALColor.blue);
			float x1 = Math.min(sx, sx2);
			float y1 = Math.min(sy, sy2);
			float x2 = Math.max(sx, sx2);
			float y2 = Math.max(sy, sy2);
			g.drawRect(x1, y1, x2 - x1, y2 - y1);
		}
		cs.camera.end(g);

		g.setColor(new DALColor(0, 0, 0, 128));
		g.fillRect(0, 0, dal.getWidth(), 30 * uiScale);
		g.setColor(DALColor.white);
		if (cs.player != null) {
			g.drawText("Money: " + cs.player.money, 100 * uiScale, 10 * uiScale);
			g.drawText("Selected: " + cs.selected.size(), 200 * uiScale, 10 * uiScale);
			g.setColor(DALColor.black);
			if (messages.size() > 0) {
				for (int i = messages.size() - 1; i >= Math.max(messages.size() - 12, 0); i--) {
					g.drawText(messages.get(i), 15 * uiScale,
							330 * uiScale - (messages.size() - 1 - i) * 25 * uiScale);
				}
			}
		}

		// Render UI
		super.render(dal);

		// Draw minimap
		g.withClip(g.getWidth() - 200, g.getHeight() - 200, 200, 200, () -> {
			boolean xLarger = cs.l.width > cs.l.height;
			float xOffset = xLarger ? 0 : 100 - 100 * cs.l.width / (float) cs.l.height;
			float yOffset = !xLarger ? 0 : 100 - 100 * (float) cs.l.height / cs.l.width;
			float scale = 200.f / ((xLarger ? cs.l.width : cs.l.height) * Level.tileSize);
			g.pushTransform();
			g.translate(g.getWidth() - 200, g.getHeight() - 200);
			g.setColor(DALColor.white);
			g.fillRect(0, 0, 200, 200);
			g.pushTransform();

			if (miniMap != null) {
				g.drawImage(miniMap, 0, 0);
			}
			g.translate(xOffset, yOffset);
			g.scale(scale, scale);
			cs.l.renderBuildings(dal.getGraphics(), false);
			for (int i = 0; i < cs.units.size(); i++) {
				Unit u = cs.units.get(i);
				u.renderMinimap(dal.getGraphics(), cs.player);
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
				g.setColor(DALColor.pink);
				g.fillOval(v.x - size / 2, v.y - size / 2, size, size);
				g.setColor(DALColor.black);
				g.drawOval(v.x - size / 2, v.y - size / 2, size, size);
			}

			// Draw window
			g.setColor(DALColor.blue);
			var topLeft = cs.camera.screenToWorld(0, 0, g);
			var bottomRight = cs.camera.screenToWorld(g.getWidth(), g.getHeight(), g);
			g.drawRect(xOffset + topLeft.x * scale, yOffset + topLeft.y * scale, (bottomRight.x - topLeft.x) * scale,
					(bottomRight.y - topLeft.y) * scale);

			g.setColor(DALColor.black);
			g.setLineWidth(2);
			g.drawRect(0, 0, 200, 300);
			g.setLineWidth(1);
			g.popTransform();

		});

		if (escapeMenu.isVisible()) {
			g.setColor(new DALColor(0, 0, 0, 128));
			// Left side
			g.fillRect(0, 0, dal.getWidth() / 2 - 100 * uiScale, dal.getHeight());

			// Right side
			g.fillRect(dal.getWidth() / 2 + 100 * uiScale, 0, dal.getWidth() / 2 - 100 * uiScale, dal.getHeight());

			// Top
			g.fillRect(dal.getWidth() / 2 - 100 * uiScale, 0, 200 * uiScale, dal.getHeight() / 2 - 100 * uiScale);

			// bottom
			g.fillRect(dal.getWidth() / 2 - 100 * uiScale, dal.getHeight() / 2 + 100 * uiScale, 200 * uiScale,
					dal.getHeight() / 2 - 100 * uiScale);
		}

		if (Gdx.input.isKeyPressed(Keys.TAB) && cs.players != null) {
			g.setColor(new DALColor(128, 128, 128, 200));
			g.fillRect(dal.getWidth() / 2 - 400, dal.getHeight() / 2 - 300, 800, 600);
			g.setColor(DALColor.black);
			g.drawRect(dal.getWidth() / 2 - 400, dal.getHeight() / 2 - 300, 800, 600);
			int red = 0, green = 0;
			for (int i = 0; i < cs.players.length; i++) {
				Player p = cs.players[i];
				boolean teamRed = p.team.id == Team.a.id;
				g.drawText(p.name + " - " + (teamRed ? "RED" : "GREEN"), dal.getWidth() / 2 - (teamRed ? 390 : -10),
						dal.getHeight() / 2 - 270 + (teamRed ? red : green) * 30);
				if (p.team.id == Team.a.id) {
					red++;
				} else {
					green++;
				}
			}
		}

		boolean writeScreenFrames = false;
		if (writeScreenFrames) {
			if (cs.frame % 200 == 0) {
				// TODO(slick2gdx): This is a little bit of code that allows for making animated
				// gifs of games. Need to convert it over to using LibGDX
				// DALTexture tex = gameRenderer.renderToTexture(cs.l.width * 8, cs.l.height *
				// 8, cs, dal);
				// FileOutputStream fos = new FileOutputStream("screenshots/tmp/" + cs.frame +
				// ".png");
				// ImageOut.write(im.getFlippedCopy(false, false), "png", fos);
				// fos.close();
				// im.destroy();
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
			cs.camera.x = destX;
			cs.camera.y = destY;
		}
	}

	public Rectangle getScreenBounds() {
		var topLeft = cs.camera.screenToWorld(0, 0, dal.getGraphics());
		var bottomRight = cs.camera.screenToWorld(dal.getWidth(), dal.getHeight(), dal.getGraphics());
		return new Rectangle(topLeft.x, topLeft.y, bottomRight.x - topLeft.x, bottomRight.y - topLeft.y);
	}

	@Override
	public boolean mousePressed(DMouseEvent e) {
		if (e.x > dal.getWidth() - 200 && e.y > dal.getHeight() - 200 && !selecting) {
			boolean xLarger = cs.l.width > cs.l.height;
			float xOffset = xLarger ? 0 : 100 - 100 * cs.l.width / (float) cs.l.height;
			float yOffset = !xLarger ? 0 : 100 - 100 * (float) cs.l.height / cs.l.width;
			float scale = 200.f / ((xLarger ? cs.l.width : cs.l.height) * Level.tileSize);
			float minimapX = (e.x - (dal.getWidth() - 200 + xOffset));
			float minimapY = (e.y - (dal.getHeight() - 200 + yOffset));
			float mapX = minimapX / scale;
			float mapY = minimapY / scale;

			if (Gdx.input.isKeyPressed(Keys.CONTROL_LEFT)) {

				ci.sendToServer(new Message(MessageType.MESSAGE, "/ping " + (Gdx.input.getX() - (dal.getWidth() - 200))
						+ " " + (Gdx.input.getY() - (dal.getHeight() - 200))));
			} else {
				if (e.button == Buttons.LEFT) {
					Rectangle screenBounds = getScreenBounds();
					cs.camera.x = DMath.bound(mapX, screenBounds.getMinX(), screenBounds.getMaxX());
					cs.camera.y = DMath.bound(mapY, screenBounds.getMinY(),
							screenBounds.getMaxY());
				} else if (e.button == Buttons.RIGHT) {
					int tx = cs.l.getTileX(((e.x - (dal.getWidth() - 200.f)) / 200.f) * cs.l.width * Level.tileSize);
					int ty = cs.l.getTileY(((e.y - (dal.getHeight() - 200.f)) / 200.f) * cs.l.height * Level.tileSize);
					ci.sendToServer(
							new Message(Gdx.input.isKeyPressed(Keys.CONTROL_LEFT) ? MessageType.SETATTACKPOINTCONTINUE
									: MessageType.SETATTACKPOINT, new Object[] { new Point2i(tx, ty), cs.selected }));
					this.waitingForMoveConfirmation = true;
				}
			}
		} else {
			var worldCoords = cs.camera.screenToWorld(e.x, e.y, dal.getGraphics());
			if (e.button == Buttons.LEFT) {
				sx = worldCoords.x;
				sy = worldCoords.y;
				sx2 = sx;
				sy2 = sy;
				selecting = true;
			} else if (e.button == Buttons.RIGHT) {
				int tx = (int) (worldCoords.x / Level.tileSize);
				int ty = (int) (worldCoords.y / Level.tileSize);
				if (Gdx.input.isKeyPressed(Keys.CONTROL_LEFT)) {
					ci.sendToServer(new Message(MessageType.SETATTACKPOINTCONTINUE,
							new Object[] { new Point2i(tx, ty), cs.selected }));
				} else if (Gdx.input.isKeyPressed(Keys.SHIFT_LEFT)) {
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
		if (e.button == Buttons.LEFT && selecting) {
			cs.clearSelected();

			var worldCoords = cs.camera.screenToWorld(e.x, e.y, dal.getGraphics());
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
					var topLeft = cs.camera.screenToWorld(0, 0, dal.getGraphics());
					var bottomRight = cs.camera.screenToWorld(dal.getWidth(), dal.getHeight(), dal.getGraphics());
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
	public boolean mouseDragged(DMouseEvent e) {
		if (e.x > dal.getWidth() - 200 && e.y > dal.getHeight() - 200 && !selecting) {
			if (Gdx.input.isButtonPressed(Buttons.LEFT) && !Gdx.input.isKeyPressed(Keys.CONTROL_LEFT)) {
				boolean xLarger = cs.l.width > cs.l.height;
				float xOffset = xLarger ? 0 : 100 - 100 * cs.l.width / (float) cs.l.height;
				float yOffset = !xLarger ? 0 : 100 - 100 * (float) cs.l.height / cs.l.width;
				float scale = 200.f / ((xLarger ? cs.l.width : cs.l.height) * Level.tileSize);
				float minimapX = (e.x - (dal.getWidth() - 200 + xOffset));
				float minimapY = (e.y - (dal.getHeight() - 200 + yOffset));
				float mapX = minimapX / scale;
				float mapY = minimapY / scale;

				Rectangle screenBounds = getScreenBounds();
				cs.camera.x = DMath.bound(mapX, screenBounds.getMinX(), screenBounds.getMaxX());
				cs.camera.y = DMath.bound(mapY, screenBounds.getMinY(), screenBounds.getMaxY());
			}
		} else {
			if (Gdx.input.isButtonPressed(Buttons.LEFT)) {
				var worldCoords = cs.camera.screenToWorld(e.x, e.y, dal.getGraphics());
				sx2 = worldCoords.x;
				sy2 = worldCoords.y;
			} else if (Gdx.input.isButtonPressed(Buttons.MIDDLE)) {
				cs.camera.x += (lastMouseX - e.x) / cs.camera.zoom;
				cs.camera.y += (lastMouseY - e.y) / cs.camera.zoom;
			}
		}

		lastMouseX = e.x;
		lastMouseY = e.y;

		return true;
	}

	@Override
	public void mouseMoved(DMouseEvent e) {

	}

	@Override
	public void mouseWheel(DMouseEvent e) {
		float zoomSpeed = 0.1f;

		if (e.wheel == 0) {
			return;
		}

		int dir = e.wheel > 0 ? 1 : -1;
		float zoom = dir * zoomSpeed;

		var mx = Gdx.input.getX();
		var my = Gdx.input.getY();

		var worldCoords = cs.camera.screenToWorld(mx, my, dal.getGraphics());

		cs.camera.zoom(zoom);

		var afterWorldCoords = cs.camera.screenToWorld(mx, my, dal.getGraphics());

		cs.camera.x += worldCoords.x - afterWorldCoords.x;
		cs.camera.y += worldCoords.y - afterWorldCoords.y;
	}

	@Override
	public void keyPressed(DKeyEvent e) {
		if (e.keyCode == Keys.ESCAPE) {
			escapeMenu.setVisible(!escapeMenu.isVisible());
			chatPanel.setVisible(false);
			chatBox.setText("");
		} else if (e.keyCode == Keys.ENTER) {
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
		} else if (e.keyCode >= Keys.NUM_1 && e.keyCode <= Keys.NUM_9) {
			ArrayList<Integer> bg = battleGroups[e.keyCode - Keys.NUM_1];
			if (Gdx.input.isKeyPressed(Keys.CONTROL_LEFT)) {
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
		} else if (e.keyCode == Keys.F8) {
			if (levelAnalysis == null) {
				levelAnalysis = new LevelAnalysis();
				levelAnalysis.build(cs.l, new AStarPathFinder(cs.l, 500, StaticFiles.options.getB("diagonalMove")));
			}

			showLevelAnalysis = !showLevelAnalysis;
		} else {
			DButton b = null;
			switch (e.keyCode) {
				case Keys.Q:
					b = buttonSlots[0][0];
					break;
				case Keys.W:
					b = buttonSlots[1][0];
					break;
				case Keys.E:
					b = buttonSlots[2][0];
					break;
				case Keys.A:
					b = buttonSlots[0][1];
					break;
				case Keys.S:
					b = buttonSlots[1][1];
					break;
				case Keys.D:
					b = buttonSlots[2][1];
					break;
				case Keys.Z:
					b = buttonSlots[0][2];
					break;
				case Keys.X:
					b = buttonSlots[1][2];
					break;
				case Keys.C:
					b = buttonSlots[2][2];
					break;
			}

			if (b != null) {
				dui.event(new DUIEvent(b, DButton.MOUSE_UP));
			}
		}
	}

	@Override
	public void keyTyped(DKeyEvent dke) {

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
				dsh.activate("home", dal, StaticFiles.getUpMenuOut(), StaticFiles.getUpMenuIn());
			} else if (e == returnToGame) {
				escapeMenu.setVisible(false);
			} else if (e.name.startsWith("userbutton")) {
				ci.sendToServer(new Message(MessageType.BUTTONPRESS,
						new Object[] { e.name, Gdx.input.isKeyPressed(Keys.SHIFT_LEFT) }));
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
