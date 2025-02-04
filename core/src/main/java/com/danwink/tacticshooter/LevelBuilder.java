package com.danwink.tacticshooter;

import com.danwink.tacticshooter.gameobjects.Building;
import com.danwink.tacticshooter.gameobjects.Building.BuildingType;
import com.danwink.tacticshooter.gameobjects.Level;
import com.danwink.tacticshooter.gameobjects.Level.TileType;
import com.danwink.tacticshooter.gameobjects.Team;
import com.phyloa.dlib.util.DMath;

public class LevelBuilder {
	public static void addBorder(Level l) {
		for (int x = 0; x < l.width; x++) {
			l.setTile(x, 0, TileType.WALL);
			l.setTile(x, l.height - 1, TileType.WALL);
		}

		for (int y = 0; y < l.height; y++) {
			l.setTile(0, y, TileType.WALL);
			l.setTile(l.width - 1, y, TileType.WALL);
		}
	}

	public static void addWall(Level l, int xx1, int yy1, int xx2, int yy2, TileType val) {
		int x1 = Math.min(xx1, xx2);
		int y1 = Math.min(yy1, yy2);

		int x2 = Math.max(xx1, xx2);
		int y2 = Math.max(yy1, yy2);

		int dx = x2 - x1;
		if (dx == 0) {
			for (int y = y1; y <= y2; y++) {
				setTile(l, x1, y, val);
			}
			return;
		}
		int dy = y2 - y1;
		float error = 0;
		float derr = Math.abs((float) dy / (float) dx);
		int y = y1;
		for (int x = x1; x <= x2; x++) {
			setTile(l, x, y, val);
			if (y == y2 && x == x2)
				break;
			error += derr;
			while (error > .5f) {
				y++;
				error -= 1;
				if (error > .5f) {
					setTile(l, x, y, val);
				}
			}
		}
	}

	public static void addRAWall(Level l, int x, int y, int x2, int y2, TileType val, boolean dir) {
		if (dir) {
			addWall(l, x, y, x2, y, val);
			addWall(l, x2, y, x2, y2, val);
		} else {
			addWall(l, x, y, x, y2, val);
			addWall(l, x, y2, x2, y2, val);
		}
	}

	public static void addBox(Level l, int x, int y, int width, int height, TileType val) {
		addWall(l, x, y, x + width, y, val);
		addWall(l, x, y + height, x + width, y + height, val);
		addWall(l, x, y, x, y + height, val);
		addWall(l, x + width, y, x + width, y + height, val);
	}

	public static void fillBox(Level l, int x, int y, int width, int height, TileType val) {
		for (int yy = y; yy < y + height; yy++) {
			for (int xx = x; xx < x + width; xx++) {
				setTile(l, xx, yy, val);
			}
		}
	}

	public static void buildLevelA(Level l) {
		addBorder(l);

		addBox(l, 5, 5, l.width - 10, l.height - 10, TileType.WALL);
		l.setTile(l.width / 2, 5, TileType.FLOOR);
		l.setTile(l.width / 2, l.height - 5, TileType.FLOOR);
		l.setTile(5, l.height / 2, TileType.FLOOR);
		l.setTile(l.width - 5, l.height / 2, TileType.FLOOR);

		fillBox(l, 10, 10, l.width - 20, l.height - 20, TileType.WALL);
	}

	public static void setTile(Level l, int x, int y, TileType val) {
		if (x > 0 && x < l.width && y > 0 && y < l.height) {
			l.setTile(x, y, val);
		}
	}

	public static void buildLevelB(Level l, Team a, Team b) {
		l.buildings.clear();
		for (int y = 0; y < l.height; y++) {
			for (int x = 0; x < l.width; x++) {
				l.setTile(x, y, TileType.WALL);
			}
		}

		fillBox(l, 1, 1, 5, 5, TileType.FLOOR);
		fillBox(l, l.width - 6, l.height - 6, 5, 5, TileType.FLOOR);

		l.buildings.add(new Building(3 * Level.tileSize, 3 * Level.tileSize, BuildingType.CENTER, a));
		l.buildings.add(
				new Building((l.width - 3) * Level.tileSize, (l.height - 3) * Level.tileSize, BuildingType.CENTER, b));

		int roomcount = 5;

		int[][] boxes = new int[roomcount * 2][4];
		for (int i = 0; i < roomcount; i++) {
			boxes[i][2] = DMath.randomi(6, 15); // width
			boxes[i][3] = DMath.randomi(6, 15); // height
			boxes[i][0] = DMath.randomi(10, (l.width - boxes[i][2])); // x
			boxes[i][1] = DMath.randomi(10, (l.height - boxes[i][3])); // y

			l.buildings.add(new Building((boxes[i][0] + boxes[i][2] / 2) * Level.tileSize,
					(boxes[i][1] + boxes[i][3] / 2) * Level.tileSize, BuildingType.POINT, null));
		}
		for (int i = roomcount; i < roomcount * 2; i++) {
			boxes[i][2] = boxes[i - roomcount][2]; // width
			boxes[i][3] = boxes[i - roomcount][3]; // height
			boxes[i][0] = l.width - boxes[i - roomcount][0]; // x
			boxes[i][1] = l.height - boxes[i - roomcount][1]; // y

			l.buildings.add(new Building((boxes[i][0] + boxes[i][2] / 2) * Level.tileSize,
					(boxes[i][1] + boxes[i][3] / 2) * Level.tileSize, BuildingType.POINT, null));
		}

		for (int i = 0; i < boxes.length; i++) {
			fillBox(l, boxes[i][0], boxes[i][1], boxes[i][2], boxes[i][3], TileType.FLOOR);
			int j = i + 1;
			if (j < boxes.length) {
				for (int y = 0; y < 2; y++) {
					for (int x = 0; x < 2; x++) {
						int x1 = boxes[i][0] + boxes[i][2] / 2 + x;
						int y1 = boxes[i][1] + boxes[i][3] / 2 + y;
						int x2 = boxes[j][0] + boxes[j][2] / 2 + x;
						int y2 = boxes[j][1] + boxes[j][3] / 2 + y;
						// addWall( l, boxes[i][0] + boxes[i][2]/2 + x, boxes[i][1] + boxes[i][3]/2 + y,
						// boxes[j][0] + boxes[j][2]/2 + x, boxes[j][1] + boxes[j][3]/2 + y, 0 );
						if (Math.random() > .5)
							addRAWall(l, x1, y1, x2, y2, TileType.FLOOR, Math.random() > .5);
					}
				}
			}
			for (int y = 0; y < 2; y++) {
				for (int x = 0; x < 2; x++) {
					int x1 = boxes[i][0] + boxes[i][2] / 2 + x;
					int y1 = boxes[i][1] + boxes[i][3] / 2 + y;
					// addWall( l, boxes[i][0] + boxes[i][2]/2 + x, boxes[i][1] + boxes[i][3]/2 + y,
					// 3 + x, 3 + y, 0 );
					// addWall( l, boxes[i][0] + boxes[i][2]/2 + x, boxes[i][1] + boxes[i][3]/2 + y,
					// l.width-3 + x, l.height-3 + y, 0 );
					if (Math.random() > .5)
						addRAWall(l, x1, y1, x + 3, y + 3, TileType.FLOOR, Math.random() > .5);

					if (Math.random() > .5)
						addRAWall(l, x1, y1, l.width - 3 + x, l.height - 3 + y, TileType.FLOOR, Math.random() > .5);
				}
			}
		}
	}
}
