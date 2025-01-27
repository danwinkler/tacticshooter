package com.danwink.tacticshooter.renderer;

import com.danwink.tacticshooter.AutoTileDrawer;
import com.danwink.tacticshooter.dal.DAL;
import com.danwink.tacticshooter.dal.DAL.DALGraphics;
import com.danwink.tacticshooter.dal.DAL.DALTexture;
import com.danwink.tacticshooter.gameobjects.Level;
import com.danwink.tacticshooter.gameobjects.Level.TileType;

public class WallRenderer {
	public DALTexture texture;

	public void render(DAL dal, Level l) {
		if (texture == null) {
			if (l != null) {
				generateTexture(dal, l);
			} else {
				return;
			}
		}
		var g = dal.getGraphics();
		g.drawImage(texture, 0, 0);
	}

	public void redrawLevel(Level l) {
		if (texture == null)
			return;

		// Figure out lower wall stuff
		RWTile[][] rt = new RWTile[l.width][l.height];
		for (int y = 0; y < l.height; y++) {
			for (int x = 0; x < l.width; x++) {
				if (l.getTile(x, y) == TileType.WALL) {
					if (l.getTile(x, y + 1) != TileType.WALL) {
						TileType left = l.getTile(x - 1, y);
						TileType right = l.getTile(x + 1, y);
						if (left == TileType.WALL && right == TileType.WALL) {
							rt[x][y] = RWTile.WALL_BOTH;
						} else if (left == TileType.WALL) {
							rt[x][y] = RWTile.WALL_RIGHT;
						} else if (right == TileType.WALL) {
							rt[x][y] = RWTile.WALL_LEFT;
						} else {
							rt[x][y] = RWTile.WALL_MID;
						}

					} else {
						rt[x][y] = RWTile.ROOF;
					}
				}
			}
		}

		texture.renderTo(g -> {
			g.setAntiAlias(false);
			g.clear();

			g.setLineWidth(1);
			// draw walls

			for (int y = 0; y < l.height; y++) {
				for (int x = 0; x < l.width; x++) {
					drawTile(rt, l, x, y, g);
				}
			}
			g.setLineWidth(1);

			g.flush();
		});

	}

	public void drawTile(RWTile[][] rt, Level l, int x, int y, DALGraphics g) {
		switch (l.tiles[x][y]) {
			case DOOR:
				drawAutoTile(l, g, x, y, TileType.FLOOR, l.theme.floor);
				drawAutoTile(l, g, x, y, TileType.WALL, l.theme.wall);
				break;
			case GRATE:
				drawAutoTile(l, g, x, y, TileType.FLOOR, l.theme.floor);
				drawAutoTile(l, g, x, y, TileType.GRATE, l.theme.grate);
				break;
			case WALL:
				/*
				 * g.drawImage(
				 * l.theme.floor,
				 * x*Level.tileSize, y*Level.tileSize,
				 * x*Level.tileSize+Level.tileSize, y*Level.tileSize+Level.tileSize,
				 * l.theme.floor.getWidth()/3, 0,
				 * l.theme.floor.getWidth()/3 * 2, l.theme.floor.getHeight()/4
				 * );
				 */

				drawAutoTile(l, g, x, y, l.tiles[x][y], l.theme.wall);
				/*
				 * switch( rt[x][y] )
				 * {
				 * case ROOF:
				 * drawWallAutoTile( rt, l, g, x, y, l.tiles[x][y], l.theme.wall );
				 * case WALL_BOTH:
				 * break;
				 * case WALL_LEFT:
				 * break;
				 * case WALL_MID:
				 * g.drawImage( l.theme.wall, x*Level.tileSize, y*Level.tileSize,
				 * Level.tileSize, Level.tileSize*3, Level.tileSize, Level.tileSize );
				 * break;
				 * case WALL_RIGHT:
				 * //l.theme.wall.draw( x, y, );
				 * break;
				 * }
				 */
				break;
			case FLOOR:
				break;
			default:
				break;
		}
	}

	public RWTile getTile(RWTile[][] rt, int x, int y) {
		if (x < 0 || x >= rt.length || y < 0 || y >= rt[0].length) {
			return RWTile.ROOF;
		}
		return rt[x][y];
	}

	public void drawWallAutoTile(RWTile[][] rt, Level l, DALGraphics g, int x, int y, TileType autoTile,
			DALTexture tileImage) {
		g.pushTransform();
		g.translate(x * Level.tileSize, y * Level.tileSize);
		// g.scale( tileSize/32f, tileSize/32f );
		AutoTileDrawer.draw(g, tileImage, Level.tileSize, 0,
				getTile(rt, x - 1, y - 1) == RWTile.ROOF,
				getTile(rt, x, y - 1) == RWTile.ROOF,
				getTile(rt, x + 1, y - 1) == RWTile.ROOF,
				getTile(rt, x - 1, y) == RWTile.ROOF,
				getTile(rt, x + 1, y) == RWTile.ROOF,
				getTile(rt, x - 1, y + 1) == RWTile.ROOF,
				getTile(rt, x, y + 1) == RWTile.ROOF,
				getTile(rt, x + 1, y + 1) == RWTile.ROOF);
		g.popTransform();
	}

	public void drawAutoTile(Level l, DALGraphics g, int x, int y, TileType autoTile, DALTexture tileImage) {
		g.pushTransform();
		g.translate(x * Level.tileSize, y * Level.tileSize);
		// g.scale( tileSize/32f, tileSize/32f );
		AutoTileDrawer.draw(g, tileImage, Level.tileSize, 0,
				l.getTile(x - 1, y - 1).connectsTo(autoTile),
				l.getTile(x, y - 1).connectsTo(autoTile),
				l.getTile(x + 1, y - 1).connectsTo(autoTile),
				l.getTile(x - 1, y).connectsTo(autoTile),
				l.getTile(x + 1, y).connectsTo(autoTile),
				l.getTile(x - 1, y + 1).connectsTo(autoTile),
				l.getTile(x, y + 1).connectsTo(autoTile),
				l.getTile(x + 1, y + 1).connectsTo(autoTile));
		g.popTransform();
	}

	private void generateTexture(DAL dal, Level l) {
		texture = dal.generateRenderableTexture(l.width * Level.tileSize, l.height * Level.tileSize);
		redrawLevel(l);
	}

	public enum RWTile {
		WALL_LEFT,
		WALL_MID,
		WALL_RIGHT,
		WALL_BOTH,
		ROOF;

		int x, y;

		RWTile() {
		}

		RWTile(int x, int y) {
			this.x = x;
			this.y = y;
		}
	}
}
