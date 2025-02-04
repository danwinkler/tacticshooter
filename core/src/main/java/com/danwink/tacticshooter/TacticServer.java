package com.danwink.tacticshooter;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import org.dom4j.DocumentException;
import org.newdawn.slick.util.pathfinding.AStarPathFinder;
import org.newdawn.slick.util.pathfinding.PathFinder;

import com.danwink.tacticshooter.ComputerPlayer.PlayType;
import com.danwink.tacticshooter.gameobjects.Building;
import com.danwink.tacticshooter.gameobjects.Building.BuildingType;
import com.danwink.tacticshooter.gameobjects.Bullet;
import com.danwink.tacticshooter.gameobjects.Level;
import com.danwink.tacticshooter.gameobjects.Level.SlotOption;
import com.danwink.tacticshooter.gameobjects.Level.SlotType;
import com.danwink.tacticshooter.gameobjects.Marker;
import com.danwink.tacticshooter.gameobjects.Player;
import com.danwink.tacticshooter.gameobjects.Team;
import com.danwink.tacticshooter.gameobjects.Unit;
import com.danwink.tacticshooter.gameobjects.Unit.UnitDef;
import com.danwink.tacticshooter.gameobjects.Unit.UnitState;
import com.danwink.tacticshooter.network.Message;
import com.danwink.tacticshooter.network.ServerInterface;
import com.danwink.tacticshooter.network.ServerNetworkInterface;
import com.danwink.tacticshooter.screens.LobbyScreen.Slot;
import com.phyloa.dlib.math.Point2i;
import com.phyloa.dlib.util.DMath;

/**
 * Handles all of the game logic, and runs the server
 * 
 * @author Daniel Winkler
 *
 */
public class TacticServer {
	public static final int UNITS_PER_TILE = 2;
	public ServerInterface si;

	public ArrayList<Unit> units = new ArrayList<Unit>();
	public ArrayList<Bullet> bullets = new ArrayList<Bullet>();
	public Unit[][] stoppedUnitGrid;
	public List<Unit>[][] unitGrid;
	public ArrayList<Marker> markers = new ArrayList<Marker>();

	public HashMap<Integer, Player> players = new HashMap<Integer, Player>();
	public Level l;
	public PathFinder finder;

	public boolean running = true;

	Thread t;

	public ServerLoop sl;

	long lastTick;
	int tick = 0;

	Team a = Team.a;
	Team b = Team.b;

	public GameStats gs = new GameStats();

	ArrayList<String> maps = new ArrayList<String>();

	ServerState state = ServerState.LOBBY;

	String gameType = "pointcapture";

	public ArrayList<ComputerPlayer> comps = new ArrayList<ComputerPlayer>();

	// LOBBY
	Slot[] slots = new Slot[16];
	int selectedMap = 0;
	boolean fogEnabled = false;

	// SCRIPT
	public JSAPI js;

	public TacticServer(ServerInterface si) {
		this.si = si;

		for (int i = 0; i < 16; i++) {
			slots[i] = new Slot();
		}
	}

	public void begin(boolean daemon) {
		File[] files = new File("levels").listFiles();
		if (files != null) {
			for (int i = 0; i < files.length; i++) {
				maps.add(files[i].getName().replace(".xml", ""));
			}
		}

		try {
			l = LevelFileHelper.loadLevel(maps.get(selectedMap));
		} catch (DocumentException e) {
			e.printStackTrace();
		}

		sl = new ServerLoop();
		t = new Thread(sl);
		t.setDaemon(daemon);
		t.start();
		lastTick = System.currentTimeMillis();

		for (int i = 7; i < 8; i++) {
			Player p = new Player();
			p.slot = i;
			String[] rnames = StaticFiles.names.split("\n");
			p.name = rnames[DMath.randomi(0, rnames.length)].split(" ")[0];
			p.playType = PlayType.DEFAULT;
			p.isBot = true;
			slots[i].p = p;
		}

		for (int i = 15; i < 16; i++) {
			Player p = new Player();
			p.slot = i;
			String[] rnames = StaticFiles.names.split("\n");
			p.name = rnames[DMath.randomi(0, rnames.length)].split(" ")[0];
			p.playType = PlayType.DEFAULT;
			p.isBot = true;
			slots[i].p = p;
		}
	}

	public void setupLobby() {
		for (int i = 0; i < 16; i++) {
			if (slots[i].p != null && !slots[i].p.isBot) {
				slots[i].p = null;
			} else if (slots[i].p != null && slots[i].p.isBot) {
				slots[i].p.money = 0;
				slots[i].p.respawn = 0;
			}
		}

		selectedMap = (selectedMap + 1) % maps.size();

		try {
			l = LevelFileHelper.loadLevel(maps.get(selectedMap));
		} catch (DocumentException e) {
			e.printStackTrace();
		}

		state = ServerState.LOBBY;
	}

	public void setupServer() {
		try {
			l = LevelFileHelper.loadLevel(maps.get(selectedMap));
		} catch (DocumentException e) {
			e.printStackTrace();
		}

		stoppedUnitGrid = new Unit[l.width * UNITS_PER_TILE][l.height * UNITS_PER_TILE];
		unitGrid = new List[l.width][l.height];
		for (int x = 0; x < l.width; x++) {
			for (int y = 0; y < l.height; y++) {
				unitGrid[x][y] = new ArrayList<Unit>();
			}
		}

		gs.setup(a, b);
		comps.clear();
		for (int i = 0; i < 16; i++) {
			if (slots[i].p != null && !slots[i].p.spectator) {
				slots[i].p.team = i < 8 ? a : b;
				if (slots[i].p.isBot) {
					ComputerPlayer cp = null;

					// TODO: do something with these errors?
					try {
						cp = (ComputerPlayer) slots[i].p.playType.c.getConstructor().newInstance();
					} catch (InstantiationException | IllegalAccessException | IllegalArgumentException
							| InvocationTargetException | NoSuchMethodException | SecurityException e) {
						throw new RuntimeException(e);
					}

					cp.setup((ServerNetworkInterface) si);
					cp.player = slots[i].p;
					cp.l = l;
					comps.add(cp);
					slots[i].p.id = cp.fc.id;
					players.put(cp.fc.id, slots[i].p);
					Thread ct = new Thread(cp);
					ct.start();
				} else {
					players.put(slots[i].p.id, slots[i].p);
				}
			}
		}

		si.sendToAllClients(new Message(MessageType.FOGUPDATE, fogEnabled));
		si.sendToAllClients(new Message(MessageType.LEVELUPDATE, l));

		for (int i = 0; i < 16; i++) {
			if (slots[i].p != null) {
				si.sendToClient(slots[i].p.id, new Message(MessageType.PLAYERUPDATE, slots[i].p));
			}
		}

		js = new JSAPI(this);
		if (gameType.equals("UMS")) {
			js.load(l.ums);
		} else {
			js.loadFile("data/gamemodes/" + gameType + ".js");
		}

		si.sendToAllClients(new Message(MessageType.UNITDEFS, js.getUnitDefsArray()));

		finder = new AStarPathFinder(l, 500, StaticFiles.advOptions.getB("diagonalMove"));

		lastTick = System.currentTimeMillis();
		tick = 0;
		state = ServerState.PLAYING;
	}

	public void setBot(int line, boolean isBot, boolean update) {
		Player p = slots[line].p;

		if (p != null) {
			if (isBot) {
				si.sendToClient(p.id, new Message(MessageType.KICK, "Your slot turned into a bot."));
			} else if (!isBot && p.isBot) {
				slots[line].p = null;
			}
		}

		if (isBot) {
			p = new Player();
			p.slot = line;
			String[] rnames = StaticFiles.names.split("\n");
			p.name = rnames[DMath.randomi(0, rnames.length)].split(" ")[0];
			p.isBot = isBot;
			slots[line].p = p;
		}

		if (update) {
			si.sendToAllClients(new Message(MessageType.PLAYERUPDATE, new Object[] { line, slots[line] }));
		}
	}

	public void setPlayType(int line, PlayType pt) {
		Player p = slots[line].p;

		if (p != null) {
			p.playType = pt;
		}

		si.sendToAllClients(new Message(MessageType.PLAYERUPDATE, new Object[] { line, slots[line] }));
	}

	public void setSpectator(int line, boolean spectator) {
		Player p = slots[line].p;

		if (p != null) {
			p.spectator = spectator;
		}

		si.sendToAllClients(new Message(MessageType.PLAYERUPDATE, new Object[] { line, slots[line] }));
	}

	@SuppressWarnings({ "incomplete-switch", "unchecked" })
	public void update() {
		if (state == ServerState.LOBBY) {
			while (si.hasServerMessages()) {
				Message m = si.getNextServerMessage();
				switch (m.messageType) {
					case DISCONNECTED:
						for (int i = 0; i < 16; i++) {
							if (slots[i].p != null && slots[i].p.id == m.sender) {
								si.sendToAllClients(
										new Message(MessageType.MESSAGE, slots[i].p.name + " left the game."));
								if (!slots[i].p.isBot) {
									slots[i].p = null;
								}
								si.sendToClient(m.sender,
										new Message(MessageType.PLAYERUPDATE, new Object[] { i, slots[i] }));
								break;
							}
						}
						break;
					case CONNECTED:
						si.sendToClient(m.sender, new Message(MessageType.SERVERSTATE, this.state));
						si.sendToClient(m.sender,
								new Message(MessageType.LOBBYLEVELINFO, new Object[] { selectedMap, maps, l }));
						break;
					case CLIENTJOIN:
						Player player = new Player(m.sender);
						if (m.message != null) {
							String name = (String) m.message;
							player.name = name;
						}

						boolean foundSlot = false;
						for (int i = 0; i < 16; i++) {
							if (slots[i].p == null && slots[i].type.allowPlayer()) {
								slots[i].p = player;
								player.slot = i;
								foundSlot = true;
								break;
							}
						}

						if (!foundSlot) {
							si.sendToClient(m.sender, new Message(MessageType.KICK, "Sorry, the server is full."));
							si.sendToAllClients(new Message(MessageType.MESSAGE,
									player.name + " tried to join the game but the game is full."));
						} else {
							for (int i = 0; i < 16; i++) {
								if (slots[i].p != null) {
									si.sendToClient(m.sender,
											new Message(MessageType.PLAYERUPDATE, new Object[] { i, slots[i] }));
								}
							}
							si.sendToClient(m.sender,
									new Message(MessageType.LOBBYLEVELINFO, new Object[] { selectedMap, maps, l }));
							si.sendToClient(m.sender, new Message(MessageType.FOGUPDATE, fogEnabled));
							si.sendToAllClients(new Message(MessageType.PLAYERUPDATE,
									new Object[] { player.slot, slots[player.slot] }));
							si.sendToAllClients(new Message(MessageType.MESSAGE, player.name + " joined."));
						}
						break;
					case SETBOT: {
						Object[] oa = (Object[]) m.message;
						int line = (Integer) oa[0];
						boolean isBot = (Boolean) oa[1];

						setBot(line, isBot, true);
						break;
					}
					case SETPLAYTYPE: {
						Object[] oa = (Object[]) m.message;
						int line = (Integer) oa[0];
						PlayType pt = (PlayType) oa[1];
						setPlayType(line, pt);
						break;
					}
					case SETSPECTATOR: {
						Object[] oa = (Object[]) m.message;
						int line = (Integer) oa[0];
						boolean spectator = (Boolean) oa[1];

						setSpectator(line, spectator);
						break;
					}
					case MESSAGE: {
						String text = (String) m.message;
						for (int i = 0; i < slots.length; i++) {
							if (slots[i].p != null && slots[i].p.id == m.sender) {
								si.sendToAllClients(new Message(MessageType.MESSAGE, slots[i].p.name + ": " + text));
								break;
							}
						}
						break;
					}
					case STARTGAME: {
						si.sendToAllClients(new Message(MessageType.STARTGAME, null));
						setupServer();
						break;
					}
					case LEVELUPDATE: {
						selectedMap = (Integer) m.message;
						// si.sendToAllClients(new Message(MessageType.LEVELUPDATE, new Object[] {
						// selectedMap, maps }));
						try {
							l = LevelFileHelper.loadLevel(maps.get(selectedMap));
							si.sendToAllClients(
									new Message(MessageType.LOBBYLEVELINFO, new Object[] { selectedMap, maps, l }));
						} catch (DocumentException e) {
							e.printStackTrace();
						}
						break;
					}
					case SWITCHTEAMS: {
						int target = (Integer) m.message;
						if (slots[target].type.allowPlayer()) {
							for (int i = 0; i < slots.length; i++) {
								if (slots[i].p != null && slots[i].p.id == m.sender) {
									if (target == i) {
										// Doesn't make sense to switch in the same slot
										break;
									}

									slots[target].p = slots[i].p;
									slots[target].p.slot = target;
									slots[i].p = null;
									si.sendToAllClients(
											new Message(MessageType.PLAYERUPDATE, new Object[] { i, slots[i] }));
									si.sendToAllClients(new Message(MessageType.PLAYERUPDATE,
											new Object[] { target, slots[target] }));
									break;
								}
							}
						}
						break;
					}
					case FOGUPDATE:
						fogEnabled = (Boolean) m.message;
						si.sendToAllClients(new Message(MessageType.FOGUPDATE, fogEnabled));
						break;
					case GAMETYPE:
						gameType = (String) m.message;
						si.sendToAllClients(new Message(MessageType.GAMETYPE, gameType));
						if (gameType.equals("UMS")) {
							for (int i = 0; i < l.slotOptions.length; i++) {
								SlotOption so = l.slotOptions[i];
								slots[i].type = so.st;
								switch (so.st) {
									case COMPUTER:
										setBot(i, true, false);
										setPlayType(i, so.bt);
										break;
									case PLAYER:
										setBot(i, false, true);
										break;
									case CLOSED:
										setBot(i, false, true);
										break;
								}
							}
						} else {
							for (int i = 0; i < slots.length; i++) {
								slots[i].type = SlotType.ANY;
								si.sendToAllClients(
										new Message(MessageType.PLAYERUPDATE, new Object[] { i, slots[i] }));
							}
						}
						break;
				}
			}
			return;
		}

		// This isn't used at the moment, but we should be using it
		// float d = (System.currentTimeMillis() - sl.lastTime) / 60.f;
		if (sl.lastTime - lastTick > 100) {
			lastTick += 100;
			tick++;

			if (state == ServerState.LOBBY)
				return;

			js.tick(tick);

			// Every 100 ticks
			// checking for l so we don't count points if the game is over
			if (tick % 100 == 0 && l != null) {

				// Count how many units and points for each team for postgame stats
				int apoints = 0;
				int bpoints = 0;
				int aunits = 0;
				int bunits = 0;
				for (Building bu : l.buildings) {
					if (bu.t != null && bu.t.id == a.id) {
						apoints++;
					} else if (bu.t != null && bu.t.id == b.id) {
						bpoints++;
					}
				}
				for (int i = 0; i < units.size(); i++) {
					Unit u = units.get(i);
					if (u.owner.team.id == a.id) {
						aunits++;
					} else {
						bunits++;
					}
				}

				gs.get(a).pointCount.add(apoints);
				gs.get(a).unitCount.add(aunits);
				gs.get(b).pointCount.add(bpoints);
				gs.get(b).unitCount.add(bunits);
				gs.totalPoints = l.buildings.size();

				Player[] playerArr = new Player[players.entrySet().size()];
				int pi = 0;
				for (Entry<Integer, Player> e : players.entrySet()) {
					Player p = e.getValue();
					playerArr[pi++] = p;
				}

				if (playerArr.length == 0)
					return;
				si.sendToAllClients(new Message(MessageType.PLAYERLIST, playerArr));
			}

			// Every tick
			for (int i = 0; i < l.buildings.size(); i++) {
				Building b = l.buildings.get(i);
				if (b.update(this)) {
					si.sendToAllClients(new Message(MessageType.BUILDINGUPDATE, b));
				}
			}

			for (var u : units) {
				u.tick(this);
			}
		}
		while (si.hasServerMessages()) {
			Message m = si.getNextServerMessage();
			switch (m.messageType) {
				case CONNECTED:
					si.sendToClient(m.sender, new Message(MessageType.SERVERSTATE, this.state));
					break;
				case CLIENTJOIN: {
					si.sendToClient(m.sender, new Message(MessageType.KICK, "Game is in progress."));
					break;
				}
				case SETATTACKPOINT: {
					Object[] oa = (Object[]) m.message;
					Point2i p = (Point2i) oa[0];
					ArrayList<Integer> selected = (ArrayList<Integer>) oa[1];
					for (Unit unit : units) {
						if (unit.owner.id == m.sender && selected.contains(unit.id)) {
							unit.pathTo(p.x, p.y, this);
						}
						si.sendToAllClients(new Message(MessageType.UNITUPDATE, unit));
					}
					si.sendToClient(m.sender, new Message(MessageType.MOVESUCCESS, null));
					break;
				}
				case SETATTACKPOINTCONTINUE: {
					Object[] oa = (Object[]) m.message;
					Point2i p = (Point2i) oa[0];
					ArrayList<Integer> selected = (ArrayList<Integer>) oa[1];
					for (Unit unit : units) {
						if (unit.owner.id == m.sender && selected.contains(unit.id)) {
							unit.pathToContinue(p.x, p.y, this);
							si.sendToClient(m.sender, new Message(MessageType.UNITUPDATE, unit));
						}
					}
					si.sendToClient(m.sender, new Message(MessageType.MOVESUCCESS, null));
					break;
				}
				case LOOKTOWARD: {
					Object[] oa = (Object[]) m.message;
					Point2i p = (Point2i) oa[0];
					ArrayList<Integer> selected = (ArrayList<Integer>) oa[1];
					for (Unit unit : units) {
						if (unit.owner.id == m.sender && selected.contains(unit.id)) {
							unit.turnToAngle = (float) Math.atan2(p.y - unit.y, p.x - unit.x);
							if (unit.state == Unit.UnitState.MOVING) {
								unit.turnOnStop = true;
								unit.pathTo(l.getTileX(unit.x), l.getTileY(unit.y), this);
								si.sendToClient(m.sender, new Message(MessageType.UNITUPDATE, unit));
							} else {
								unit.state = UnitState.TURNTO;
							}
						}
					}
					break;
				}
				case BUILDUNIT: {
					Player player = players.get(m.sender);
					String typeName = (String) m.message;
					UnitDef type = js.unitDefs.get(typeName);
					if (player.money >= type.price) {
						player.money -= type.price;
						Building base = null;
						for (Building bu : l.buildings) {
							if (bu.bt == BuildingType.CENTER && bu.t.id == player.team.id) {
								base = bu;
							}
						}
						if (base != null) {
							Unit u = new Unit(base.x, base.y, player);
							u.setType(type);
							u.stoppedAt = base;
							units.add(u);

							var tx = l.getTileX(u.x);
							var ty = l.getTileY(u.y);
							unitGrid[tx][ty].add(u);

							si.sendToAllClients(new Message(MessageType.UNITUPDATE, u));
							gs.get(u.owner.team).unitsCreated++;
							gs.getPlayerStats(player).unitsCreated++;
							u.pathTo(l.getTileX(u.x), l.getTileY(u.y), this);
						}
					}
					si.sendToClient(m.sender, new Message(MessageType.PLAYERUPDATE, player));
					break;
				}
				case DISCONNECTED: {
					Player player = players.get(m.sender);
					if (player != null) {
						Player chosenPlayer = null;
						for (int i = 0; i < slots.length; i++) {
							if (slots[i].p != null && slots[i].p.team == player.team && slots[i].p.id != player.id) {
								chosenPlayer = slots[i].p;
								break;
							}
						}
						for (int i = 0; i < units.size(); i++) {
							Unit u = units.get(i);
							if (chosenPlayer != null) {
								u.owner = chosenPlayer;
							} else {
								u.alive = false;
							}
						}
						players.remove(m.sender);
					}
					break;
				}
				case MESSAGE: {
					String text = (String) m.message;
					if (!text.startsWith("/")) {
						si.sendToAllClients(new Message(MessageType.MESSAGE, players.get(m.sender).name + ": " + text));
					} else {
						if (text.trim().startsWith("/ping")) {
							try {
								String[] commands = text.split(" ");
								Player tp = players.get(m.sender);
								for (Entry<Integer, Player> e : players.entrySet()) {
									Player p = e.getValue();
									if (p.team.id == tp.team.id) {
										si.sendToClient(p.id, new Message(MessageType.PINGMAP, new Point2i(
												Integer.parseInt(commands[1]), Integer.parseInt(commands[2]))));
									}
								}
							} catch (Exception ex) {
								si.sendToClient(m.sender,
										new Message(MessageType.MESSAGE, "SERVER: Malformed command."));
							}
						} else if (text.trim().startsWith("/team")) {
							try {
								String[] commands = text.split(" ", 2);
								Player tp = players.get(m.sender);
								for (Entry<Integer, Player> e : players.entrySet()) {
									Player p = e.getValue();
									if (p.team.id == tp.team.id) {
										si.sendToClient(p.id, new Message(MessageType.MESSAGE,
												"(TEAM)" + players.get(m.sender).name + ": " + commands[1]));
									}
								}
							} catch (Exception ex) {
								si.sendToClient(m.sender,
										new Message(MessageType.MESSAGE, "SERVER: Malformed command."));
							}
						}
					}
					break;
				}
				case UNITUPDATE: {
					Unit u = (Unit) m.message;
					Unit find = null;
					for (int i = 0; i < units.size(); i++) {
						Unit tu = units.get(i);
						if (tu.id == u.id) {
							si.sendToClient(m.sender, new Message(MessageType.UNITUPDATE, tu));
							find = tu;
							break;
						}
					}
					if (find == null) {
						u.alive = false;
						u.health = 0;
						si.sendToClient(m.sender, new Message(MessageType.UNITUPDATE, u));
					}
					break;
				}
				case BUTTONPRESS: {
					Object[] arr = (Object[]) m.message;
					String id = (String) arr[0];
					boolean shiftPressed = (boolean) arr[1];
					js.buttonPressed(id, m.sender, shiftPressed);
					break;
				}
			}
		}

		for (int i = 0; i < units.size(); i++) {
			Unit u = units.get(i);
			int update = u.update(this);
			if (update == 2) {
				si.sendToAllClients(new Message(MessageType.UNITUPDATE, u));
			} else if (update == 1) {
				si.sendToAllClients(new Message(MessageType.UNITMINIUPDATE,
						new Unit.UnitUpdate(u.id, u.x, u.y, u.heading, u.health)));
			}

			if (!u.alive) {
				gs.get(u.owner.team).unitsLost++;
				gs.getPlayerStats(u.owner).unitsLost++;
				// Saboteurs can kill themselves, so there won't be a killer
				if (u.killer != null) {
					gs.getPlayerStats(u.killer).kills++;
				}
				units.remove(i);
				js.kill(u);
				if (u.occupyX > -1) {
					stoppedUnitGrid[u.occupyX][u.occupyY] = null;
				}

				var tx = l.getTileX(u.x);
				var ty = l.getTileY(u.y);
				unitGrid[tx][ty].remove(u);

				i--;
			}
		}

		for (int i = 0; i < markers.size(); i++) {
			Marker m = markers.get(i);
			for (int unit_i = 0; unit_i < units.size(); unit_i++) {
				Unit u = units.get(unit_i);
				if (DMath.d2(m.x, m.y, u.x, u.y) < Level.tileSize * Level.tileSize) {
					js.touchMarker(u, m);
					break;
				}
			}
		}

		for (int i = 0; i < bullets.size(); i++) {
			Bullet b = bullets.get(i);
			b.update(this);
			if (!b.alive) {
				bullets.remove(i);
				i--;
			}
		}
	}

	public void endGame() {
		// send stats to everyone
		si.sendToAllClients(new Message(MessageType.GAMEOVER, gs));

		// Clear everything
		units.clear();
		bullets.clear();
		l = null;
		players.clear();
	}

	public void addBullet(Unit u, float angle) {
		Bullet b = new Bullet(u.x + DMath.cosf(angle) * (Unit.radius), u.y + DMath.sinf(angle) * (Unit.radius), angle);
		b.owner = u.owner;
		b.shooter = u;
		b.damage = u.type.damage;
		gs.get(b.owner.team).bulletsShot++;
		gs.getPlayerStats(b.owner).bulletsShot++;
		bullets.add(b);
		si.sendToAllClients(new Message(MessageType.BULLETUPDATE, b));
	}

	public void addBullet(Bullet b) {
		gs.get(b.owner.team).bulletsShot++;
		gs.getPlayerStats(b.owner).bulletsShot++;
		bullets.add(b);
		si.sendToAllClients(new Message(MessageType.BULLETUPDATE, b));
	}

	public void updatePlayer(int id) {
		si.sendToClient(id, new Message(MessageType.PLAYERUPDATE, players.get(id)));
	}

	public class ServerLoop implements Runnable {
		long lastTime;
		long frameTime = (1000 / 30);
		long timeDiff;
		public boolean running = true;

		public ServerLoop() {

		}

		public void run() {
			lastTime = System.currentTimeMillis();
			while (running) {
				try {
					update();
				} catch (Exception ex) {
					ex.printStackTrace();
				}
				long time = System.currentTimeMillis();
				timeDiff = (lastTime + frameTime) - time;
				if (timeDiff > 0) {
					try {
						Thread.sleep(timeDiff);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				lastTime = System.currentTimeMillis();
			}
			si.stop();
		}
	}

	public boolean isRunning() {
		return sl.running;
	}

	public void stop() {
		sl.running = false;
	}

	public enum ServerState {
		LOBBY,
		PLAYING;
	}

	public static void main(String[] args) {
		TacticServer ts = new TacticServer(new ServerNetworkInterface());
		ts.begin(false);
	}
}
