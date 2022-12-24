package com.danwink.tacticshooter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;
import org.newdawn.slick.SlickException;

import com.danwink.tacticshooter.gameobjects.Building;
import com.danwink.tacticshooter.gameobjects.Level;
import com.danwink.tacticshooter.gameobjects.Level.SlotOption;
import com.danwink.tacticshooter.gameobjects.Team;
import com.danwink.tacticshooter.gameobjects.Building.BuildingType;
import com.danwink.tacticshooter.gameobjects.Level.SlotType;
import com.danwink.tacticshooter.gameobjects.Level.TileType;

public class LevelFileHelper {
	public static String[] getLevelNames() {
		return Arrays.stream(new File("levels").listFiles()).map(f -> f.getName().replace(".xml", ""))
				.toArray(String[]::new);
	}

	public static Level loadLevel(String name) throws DocumentException {
		return loadLevel(new File("levels" + File.separator + name + ".xml"));
	}

	public static Level loadLevel(File file) throws DocumentException {
		SAXReader reader = new SAXReader();
		Document doc = reader.read(file);
		Node level = doc.selectSingleNode("//level");
		Level m = new Level(Integer.parseInt(level.valueOf("@width")), Integer.parseInt(level.valueOf("@height")));
		String theme = level.valueOf("@theme");
		if (theme.length() > 0)
			m.themeName = theme;

		Node map = level.selectSingleNode("map");
		List<? extends Node> rows = map.selectNodes("row");
		for (int y = 0; y < m.height; y++) {
			String[] vals = rows.get(y).getText().split(",");
			for (int x = 0; x < m.width; x++) {
				m.tiles[x][y] = TileType.getTile(Integer.parseInt(vals[x]));
			}
		}

		// Load Buildings
		List<? extends Node> buildings = doc.selectNodes("//level/buildings/building");
		for (Node n : buildings) {
			Building b = new Building(Integer.parseInt(n.valueOf("@x")), Integer.parseInt(n.valueOf("@y")),
					BuildingType.valueOf(n.valueOf("@bt")),
					n.valueOf("@team").equals("null") ? null : new Team(Integer.valueOf(n.valueOf("@team"))));
			String id = n.valueOf("@id");
			if (id != null && !id.equals("")) {
				b.id = Integer.parseInt(id);
			}
			String radius = n.valueOf("@radius");
			if (radius != null && !radius.equals("")) {
				b.radius = Float.parseFloat(radius);
			}
			b.name = n.valueOf("@name");
			m.buildings.add(b);
		}

		// Load Slots
		List<? extends Node> slots = doc.selectNodes("//level/slots/slot");
		if (slots != null) {
			for (int i = 0; i < slots.size(); i++) {
				Node n = slots.get(i);
				m.slotOptions[i].st = SlotType.valueOf(n.valueOf("@s"));
				m.slotOptions[i].bt = ComputerPlayer.PlayType.valueOf(n.valueOf("@b"));
			}
		}

		// Load Code
		Node code = level.selectSingleNode("code");
		if (code != null) {
			m.code = code.getText();
		}

		Node ums = level.selectSingleNode("ums");
		if (ums != null) {
			m.ums = ums.getText();
		}

		// Set theme
		try {
			m.theme = Theme.getTheme("desertrpg");
		} catch (SlickException e) {
			e.printStackTrace();
		}

		return m;
	}

	public static void saveLevel(String name, Level m) throws IOException {
		saveLevel(new File("levels" + File.separator + name + ".xml"), m);
	}

	public static void saveLevel(File file, Level m) throws IOException {
		Document doc = DocumentHelper.createDocument();
		Element level = doc.addElement("level");
		level.addAttribute("width", Integer.toString(m.width));
		level.addAttribute("height", Integer.toString(m.height));

		// ADD map
		Element layer = level.addElement("map");
		for (int y = 0; y < m.height; y++) {
			Element row = layer.addElement("row");
			StringBuilder rows = new StringBuilder();
			for (int x = 0; x < m.width; x++) {
				rows.append(m.getTile(x, y).data + ",");
			}
			row.setText(rows.toString());
		}

		// ADD buildings
		Element buildings = level.addElement("buildings");
		for (int i = 0; i < m.buildings.size(); i++) {
			Building b = m.buildings.get(i);
			Element building = buildings.addElement("building");
			building.addAttribute("x", Integer.toString(b.x));
			building.addAttribute("y", Integer.toString(b.y));
			building.addAttribute("bt", b.bt.name());
			building.addAttribute("team", b.t == null ? "null" : Integer.toString(b.t.id));
			building.addAttribute("id", Integer.toString(b.id));
			building.addAttribute("radius", Float.toString(b.radius));
			building.addAttribute("name", b.name);
		}

		// ADD slots
		Element slots = level.addElement("slots");
		for (int i = 0; i < m.slotOptions.length; i++) {
			SlotOption so = m.slotOptions[i];
			Element slot = slots.addElement("slot");
			slot.addAttribute("s", so.st.toString());
			slot.addAttribute("b", so.bt.toString());
		}

		// ADD code
		if (m.code != null) {
			Element code = level.addElement("code");
			code.setText(m.code);
		}

		if (m.ums != null) {
			Element code = level.addElement("ums");
			code.setText(m.ums);
		}

		OutputFormat format = OutputFormat.createPrettyPrint();
		XMLWriter writer = new XMLWriter(
				new FileWriter(file),
				format);
		writer.write(doc);
		writer.close();
	}
}
