package com.danwink.tacticshooter;

import java.io.FileNotFoundException;
import java.util.Random;

import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import com.danwink.tacticshooter.gameobjects.Building;
import com.danwink.tacticshooter.gameobjects.Building.BuildingType;
import com.danwink.tacticshooter.gameobjects.Level;
import com.danwink.tacticshooter.gameobjects.Player;
import com.danwink.tacticshooter.gameobjects.Team;
import com.danwink.tacticshooter.gameobjects.Unit;
import com.danwink.tacticshooter.gameobjects.Unit.UnitType;
import com.danwink.tacticshooter.network.Message;
import com.phyloa.dlib.util.DFile;

public class JSAPI {
	ScriptEngineManager mgr = new ScriptEngineManager();

	ScriptEngine engine = mgr.getEngineByName("JavaScript");

	public TacticServer ts;

	public Random random = new Random();

	public JSAPI(TacticServer ts) {
		this.ts = ts;

		String beginProgram = "";
		try {
			Bindings bindings = engine.getBindings(ScriptContext.ENGINE_SCOPE);

			bindStatic(bindings);

			beginProgram = DFile.loadText("data/setup.js");
			engine.eval(beginProgram);
		} catch (FileNotFoundException | ScriptException e) {
			e.printStackTrace();
		}
	}

	public void bindStatic(Bindings bindings) {
		bindings.put("api", this);

		int[] buildings = new int[ts.l.buildings.size()];
		for (int i = 0; i < ts.l.buildings.size(); i++) {
			buildings[i] = ts.l.buildings.get(i).id;
		}
		bindings.put("buildings", buildings);

		bindings.put("out", System.out);
	}

	public void tick(int frame) {
		try {
			Bindings bindings = engine.getBindings(ScriptContext.ENGINE_SCOPE);

			bindings.put("players", ts.players.keySet().toArray());

			int[] units = new int[ts.units.size()];
			for (int i = 0; i < ts.units.size(); i++) {
				units[i] = ts.units.get(i).id;
			}
			bindings.put("units", units);

			engine.eval("callTick( " + frame + " );");
		} catch (ScriptException e) {
			e.printStackTrace();
		}
	}

	public void step(Building b, Unit u) {
		try {
			engine.eval("callStep( " + b.id + ", " + u.id + " );");
		} catch (ScriptException e) {
			e.printStackTrace();
		}
	}

	public void stop(Building b, Unit u) {
		try {
			engine.eval("callStop( " + b.id + ", " + u.id + " );");
		} catch (ScriptException e) {
			e.printStackTrace();
		}
	}

	public void kill(Unit u) {
		try {
			engine.eval("callKill( { "
					+ "unit: " + u.id + ","
					+ "owner:" + u.owner.id + ","
					+ "type: '" + u.type.toString() + "'"
					+ "} );");
		} catch (ScriptException e) {
			e.printStackTrace();
		}
	}

	public void load(String code) {
		try {
			engine.eval(code);
		} catch (ScriptException e) {
			e.printStackTrace();
		}
	}

	public void loadFile(String path) {
		try {
			load(DFile.loadText(path));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	public void buttonPressed(String id, int player) {
		try {
			engine.eval("callButton( '" + id + "', " + player + " );");
		} catch (ScriptException e) {
			e.printStackTrace();
		}
	}

	// ---------------------------------
	// UI
	// ---------------------------------

	public String addButton(String text, int x, int y, int width, int height) {
		String id = "userbutton" + Integer.toString(random.nextInt());
		ts.si.sendToAllClients(new Message(MessageType.CREATEBUTTON, new Object[] { id, text, x, y, width, height }));
		return id;
	}

	// ---------------------------------
	// PLAYER
	// ---------------------------------

	public int getPlayerMoney(int id) {
		return ts.players.get(id).money;
	}

	public void setPlayerMoney(int id, int money) {
		ts.players.get(id).money = money;
		ts.updatePlayer(id);
	}

	public void addPlayerMoney(int id, int money) {
		ts.players.get(id).money += money;
		if (money > 0) {
			ts.gs.teamStats[ts.players.get(id).team.id].moneyEarned += money;
		}
		ts.updatePlayer(id);
	}

	public int getPlayerTeam(int id) {
		Team t = ts.players.get(id).team;
		return t == null ? -1 : t.id;
	}

	public int getPlayerBySlot(int slot) {
		if (slot < 0 || slot >= ts.slots.length)
			return -1;
		Player p = ts.slots[slot].p;
		return p == null ? -1 : p.id;
	}

	// ---------------------------------
	// BUILDING
	// ---------------------------------

	public int getBuildingTeam(int id) {
		for (Building b : ts.l.buildings) {
			if (b.id == id) {
				return b.t != null ? b.t.id : -1;
			}
		}
		return -1;
	}

	public int getBaseX(int id) {
		Player p = ts.players.get(id);
		Building base = null;
		for (Building bu : ts.l.buildings) {
			if (bu.bt == BuildingType.CENTER && bu.t.id == p.team.id) {
				base = bu;
			}
		}
		if (base != null) {
			return base.x;
		}
		return -1;
	}

	public int getBaseY(int id) {
		Player p = ts.players.get(id);
		Building base = null;
		for (Building bu : ts.l.buildings) {
			if (bu.bt == BuildingType.CENTER && bu.t.id == p.team.id) {
				base = bu;
			}
		}
		if (base != null) {
			return base.y;
		}
		return -1;
	}

	public int getBuildingByName(String name) {
		for (Building bu : ts.l.buildings) {
			if (bu.name.equals(name)) {
				return bu.id;
			}
		}
		return -1;
	}

	public int getBuildingX(int id) {
		for (Building bu : ts.l.buildings) {
			if (bu.id == id) {
				return bu.x;
			}
		}
		return -1;
	}

	public int getBuildingY(int id) {
		for (Building bu : ts.l.buildings) {
			if (bu.id == id) {
				return bu.y;
			}
		}
		return -1;
	}

	// ---------------------------------
	// UNIT
	// ---------------------------------

	public int getUnitPlayer(int id) {
		for (Unit u : ts.units) {
			if (u.id == id) {
				return u.owner.id;
			}
		}
		return -1;
	}

	public int createUnit(int player, String type, float x, float y) {
		Player p = ts.players.get(player);

		Unit u = new Unit(x, y, p);
		u.setType(UnitType.valueOf(type));

		for (int i = 0; i < ts.l.buildings.size(); i++) {
			Building tb = ts.l.buildings.get(i);
			float dx = u.x - tb.x;
			float dy = u.y - tb.y;
			if ((dx * dx + dy * dy) < tb.bt.bu.getRadius() * tb.bt.bu.getRadius()) {
				u.stoppedAt = tb;
				break;
			}
		}

		ts.units.add(u);
		u.pathTo((int) (x / Level.tileSize), (int) (y / Level.tileSize), ts);
		ts.si.sendToAllClients(new Message(MessageType.UNITUPDATE, u));
		ts.gs.get(u.owner.team).unitsCreated++;
		return u.id;
	}

	public void moveUnit(int u, float x, float y) {
		for (Unit unit : ts.units) {
			if (unit.id == u) {
				unit.pathTo((int) (x / Level.tileSize), (int) (y / Level.tileSize), ts);
				break;
			}
		}
	}

	public void killUnit(int u) {
		for (Unit unit : ts.units) {
			if (unit.id == u) {
				unit.health = 0;
				break;
			}
		}
	}

	// ---------------------------------
	// GAME
	// ---------------------------------

	public void endGame() {
		ts.endGame();
		ts.setupLobby();
	}

	public void sendMessage(String text) {
		ts.si.sendToAllClients(new Message(MessageType.MESSAGE, text));
	}
}
