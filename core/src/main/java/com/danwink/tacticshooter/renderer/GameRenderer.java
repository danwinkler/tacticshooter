package com.danwink.tacticshooter.renderer;

import java.util.Collections;
import java.util.concurrent.ConcurrentLinkedDeque;

import com.badlogic.gdx.Gdx;
import com.danwink.tacticshooter.ClientState;
import com.danwink.tacticshooter.dal.DAL;
import com.danwink.tacticshooter.dal.DAL.DALColor;
import com.danwink.tacticshooter.dal.DAL.DALGraphics;
import com.danwink.tacticshooter.dal.DAL.DALTexture;
import com.danwink.tacticshooter.gameobjects.Building;
import com.danwink.tacticshooter.gameobjects.Bullet;
import com.danwink.tacticshooter.gameobjects.Level;
import com.danwink.tacticshooter.gameobjects.Marker;
import com.danwink.tacticshooter.gameobjects.Unit;
import com.danwink.tacticshooter.gameobjects.Unit.UnitState;
import com.phyloa.dlib.math.Point2i;
import com.phyloa.dlib.util.DMath;

import jp.objectclub.vecmath.Point2f;
import jp.objectclub.vecmath.Vector2f;

public class GameRenderer {
	/*
	 * Order of Rendering:
	 * 1. Outside of level texture (Shifted by viewport distance from nearest tile
	 * rounded down)
	 * 
	 * 2. Translate to viewport
	 * 
	 * 3. Floor Texture (Regenerated on first load, and when map changes)
	 * 4. Blood/Explosion Texture (Modified whenever a unit dies/blows up)
	 * 5. Buildings
	 * 6. Unit bodies
	 * 7. Wall Texture (Regenerated on first load, and when map changes)
	 * 8. Bullets
	 * 9. Particles
	 * 10. Unit Info (Paths if selected, name if mouse is nearby)
	 * 11. Fog of War (Generated each frame)
	 */

	public OutsideFloorRenderer outsideFloor;
	public FloorRenderer floor;
	public BloodLayerRenderer bloodExplosion;
	public WallRenderer wall;
	public BuildingRenderer building;
	public UnitBodyRenderer unitBody;
	public BulletRenderer bullet;
	public ParticleSystemRenderer particle;
	public UnitInfoRenderer unitInfo;
	public FogRenderer fog;
	public FootprintLayerRenderer footprint;
	public GrassRenderer grass;

	ConcurrentLinkedDeque<Unit> unitsToKill = new ConcurrentLinkedDeque<>();

	DALTexture saveBuffer;

	public GameRenderer() {
		outsideFloor = new OutsideFloorRenderer();
		floor = new FloorRenderer();
		bloodExplosion = new BloodLayerRenderer(this);
		building = new BuildingRenderer();
		unitBody = new UnitBodyRenderer();
		wall = new WallRenderer();
		bullet = new BulletRenderer();
		particle = new ParticleSystemRenderer();
		unitInfo = new UnitInfoRenderer();
		fog = new FogRenderer();
		footprint = new FootprintLayerRenderer();
		// grass = new GrassRenderer();
	}

	public void update(ClientState cs, float d) {
		particle.update(d);
		// grass.update(cs);
	}

	public void render(DAL dal, ClientState cs, boolean fogEnabled) {
		// Sort units by y
		Collections.sort(cs.units, (Unit a, Unit b) -> {
			return ((int) a.y - (int) b.y);
		});

		// Prerendering to secondary textures
		while (!unitsToKill.isEmpty()) {
			Unit u = unitsToKill.removeLast();
			bloodExplosion.killUnit(u, cs, unitBody);

			if (u.type.explodesOnDeath) {
				createExplosion(u.x, u.y, cs);
			}
		}

		var g = dal.getGraphics();

		outsideFloor.render(dal, cs);

		cs.camera.start(g);
		// Main Rendering
		floor.render(dal, cs.l);
		footprint.render(dal, cs);
		bloodExplosion.render(dal, cs, unitBody);
		building.render(g, cs.l, false);
		// grass.render(g, cs);
		wall.render(dal, cs.l);
		renderMarkers(g, cs);
		unitBody.render(g, cs);
		bullet.render(g, cs);
		particle.render(g);
		unitInfo.render(g, cs);
		if (fogEnabled) {
			fog.render(dal, cs);
		}

		cs.camera.end(g);
	}

	public DALTexture renderToTexture(int width, int height, ClientState cs, DAL dal) {
		if (saveBuffer == null) {
			saveBuffer = dal.generateRenderableTexture(cs.l.width * Level.tileSize, cs.l.height * Level.tileSize);
		}

		DALTexture im = dal.generateRenderableTexture(width, height);
		im.renderTo(ig -> {
			ig.setAntiAlias(false);
		});
		saveBuffer.renderTo(g -> {
			g.setAntiAlias(false);
		});

		saveBuffer.renderTo(g -> {
			g.clear();

			var sbDAL = dal.useGraphics(g);

			floor.render(sbDAL, cs.l);
			footprint.render(sbDAL, cs);
			bloodExplosion.render(sbDAL, cs, unitBody);
			building.render(g, cs.l, false);
			wall.render(sbDAL, cs.l);
			renderMarkers(g, cs);
			unitBody.render(g, cs);
			bullet.render(g, cs);
			particle.render(g);

			g.flush();
		});

		im.renderTo(ig -> {
			ig.drawImage(saveBuffer, 0, 0, width, height, 0, 0, saveBuffer.getWidth(), saveBuffer.getHeight());
			ig.flush();
		});

		return im;
	}

	public void renderEndGameMap(DAL dal, ClientState cs) {
		// floor.render(dal, cs.l);
		cs.l.renderFloor(dal.getGraphics());
		bloodExplosion.render(dal, cs, unitBody);
		building.render(dal.getGraphics(), cs.l, true);
		wall.render(dal, cs.l);
	}

	public void killUnit(Unit u) {
		unitsToKill.addFirst(u);
	}

	public void drawBlood(float x, float y) {
		bloodExplosion.drawBlood(x, y);
	}

	public void createExplosion(float x, float y, ClientState cs) {
		particle.createExplosion(x, y, cs);
	}

	public void renderMarkers(DALGraphics g, ClientState cs) {
		for (int i = 0; i < cs.markers.size(); i++) {
			Marker m = cs.markers.get(i);
			int frame = (int) (System.currentTimeMillis() / 250) % 4;
			g.drawImage(cs.l.theme.flag.getSprite(0, 0), m.x - 16, m.y - 32, m.x + 16, m.y, frame * 16, 0,
					(frame + 1) * 16, 16);
		}
	}

	public class UnitInfoRenderer {
		public void render(DALGraphics g, ClientState cs) {
			for (int i = 0; i < cs.units.size(); i++) {
				Unit u = cs.units.get(i);
				renderInfo(g, cs, u);
			}
		}

		public void renderInfo(DALGraphics g, ClientState cs, Unit u) {
			var worldCoords = cs.camera.screenToWorld(Gdx.input.getX(), Gdx.input.getY(), g);
			float mx = worldCoords.x;
			float my = worldCoords.y;
			if (u.selected && u.state == UnitState.MOVING) {
				g.setColor(DALColor.black);
				g.setLineWidth(3);
				for (int i = Math.max(u.onStep - 2, 0); i < u.path.size() - 1; i++) {
					Point2i p1 = u.path.get(i);
					Point2i p2 = u.path.get(i + 1);
					g.drawLine((p1.x + .5f) * Level.tileSize, (p1.y + .5f) * Level.tileSize,
							(p2.x + .5f) * Level.tileSize, (p2.y + .5f) * Level.tileSize);
				}

				g.setColor(DALColor.lightGray);
				g.setLineWidth(1);
				for (int i = Math.max(u.onStep - 2, 0); i < u.path.size() - 1; i++) {
					Point2i p1 = u.path.get(i);
					Point2i p2 = u.path.get(i + 1);
					g.drawLine((p1.x + .5f) * Level.tileSize, (p1.y + .5f) * Level.tileSize,
							(p2.x + .5f) * Level.tileSize, (p2.y + .5f) * Level.tileSize);
				}
			}

			g.pushTransform();
			g.translate(u.x, u.y);

			if (u.marked) {
				g.setColor(DALColor.red);
				g.setLineWidth(3);
				g.drawOval(-12, -12, 24, 24);
				g.setLineWidth(1);
			}

			if (u.selected) {
				g.setColor(DALColor.blue);
				g.pushTransform();
				g.rotate(0, 0, (u.heading / DMath.PI2F * 360) + 45);
				g.drawArc(-16, -16, 32, 32, 0, 270);
				g.drawLine(0, -16, 16, -16);
				g.drawLine(16, 0, 16, -16);
				g.popTransform();

				if (u.type.providesBuff != null) {
					g.setColor(new DALColor(0, 1, 0, .5f));
					var rad = u.type.buffRadius * Level.tileSize;
					g.fillOval(-rad, -rad, rad * 2, rad * 2);
					g.setColor(DALColor.black);
					g.drawOval(-rad, -rad, rad * 2, rad * 2);
				}
			}

			int healthBarDist = 0;
			// TODO: define these in the gamemode file
			switch (u.type.name) {
				case "SCOUT":
				case "SHOTGUN":
				case "LIGHT":
				case "SNIPER":
				case "SABOTEUR":
					healthBarDist = -11;
					break;
				case "HEAVY":
					healthBarDist = -14;
					break;
			}

			if (u.selected) {
				g.setColor(DALColor.black);
				g.fillRect(-9, healthBarDist, (18.f * u.health / u.type.health), 4);

				g.setColor(new DALColor(DMath.bound(1.f - u.health / u.type.health, 0, 1),
						DMath.bound(u.health / u.type.health, 0, 1), 0));
				g.fillRect(-8, healthBarDist + 1, (16.f * u.health / u.type.health), 2);
			}

			float dmx = u.x - mx;
			float dmy = u.y - my;
			if (dmx * dmx + dmy * dmy < 100) {
				float strWidth = g.getTextWidth(u.owner.name);
				g.setColor(DALColor.black);
				g.drawText(u.owner.name, -strWidth / 2, 10);
			}

			g.popTransform();
		}
	}

	public class FogRenderer {
		DALTexture texture;

		public void render(DAL dal, ClientState cs) {
			if (texture == null) {
				if (cs.l != null) {
					generateTexture(dal, cs);
				} else {
					return;
				}
			}

			texture.renderTo(fogG -> {

				fogG.setColor(DALColor.black);
				fogG.fillRect(0, 0, texture.getWidth(), texture.getHeight());

				for (int i = 0; i < cs.l.buildings.size(); i++) {
					Building b = cs.l.buildings.get(i);
					if (b.t != null && b.t.id == cs.player.team.id) {
						fogG.setColor(DALColor.white);
						fogG.fillOval(b.x - b.bt.bu.getRadius(), b.y - b.bt.bu.getRadius(), b.bt.bu.getRadius() * 2,
								b.bt.bu.getRadius() * 2);
					}
				}

				for (int y = 0; y < cs.l.height; y++) {
					for (int x = 0; x < cs.l.width; x++) {
						if (!cs.l.tiles[x][y].isShootable()) {
							fogG.fillRect(x * Level.tileSize, y * Level.tileSize, Level.tileSize, Level.tileSize);
						}
					}
				}

				for (int i = 0; i < cs.units.size(); i++) {
					Unit u = cs.units.get(i);
					if (u.owner.team.id == cs.player.team.id) {
						// fogG.setColor( Color.white );
						int lx = cs.l.getTileX(u.x);
						int ly = cs.l.getTileY(u.y);

						Point2f loc = new Point2f(u.x, u.y);
						Vector2f v = new Vector2f();
						Point2f result = new Point2f();

						int xmin = Math.max(lx - 10, 0);
						int ymin = Math.max(ly - 10, 0);
						int xmax = Math.min(lx + 10, cs.l.width - 1);
						int ymax = Math.min(ly + 10, cs.l.height - 1);

						float maxView = 10 * Level.tileSize;
						float maxView2 = maxView * maxView;

						for (int xx = xmin; xx <= xmax; xx++) {
							for (int yy = ymin; yy < ymax; yy++) {
								v.set(xx * Level.tileSize - u.x + (Level.tileSize * .5f),
										yy * Level.tileSize - u.y + (Level.tileSize * .5f));
								if (v.lengthSquared() < maxView2 && !cs.l.hitwall(loc, v, result)) {
									fogG.fillOval(xx * Level.tileSize - (Level.tileSize * .5f),
											yy * Level.tileSize - (Level.tileSize * .5f), Level.tileSize * 2,
											Level.tileSize * 2);
								}
							}
						}
					}
				}
			});

			var g = dal.getGraphics();

			g.setColor(DALColor.darkGray);

			g.setDrawMode(DALGraphics.MODE_COLOR_MULTIPLY);
			g.drawImage(texture, 0, 0);
			g.setDrawMode(DALGraphics.MODE_NORMAL);
		}

		private void generateTexture(DAL dal, ClientState cs) {
			texture = dal.generateRenderableTexture(cs.l.width * Level.tileSize, cs.l.height * Level.tileSize);
		}
	}

	public void bulletImpact(Bullet b) {
		particle.bulletImpact(b);
	}

	public void updateWalls(ClientState cs) {
		wall.redrawLevel(cs.l);
	}
}
