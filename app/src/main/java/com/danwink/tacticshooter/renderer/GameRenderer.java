package com.danwink.tacticshooter.renderer;

import java.util.Collections;
import java.util.concurrent.ConcurrentLinkedDeque;

import jp.objectclub.vecmath.Point2f;
import jp.objectclub.vecmath.Vector2f;

import org.newdawn.slick.Color;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.Input;
import org.newdawn.slick.SlickException;

import com.danwink.tacticshooter.ClientState;
import com.danwink.tacticshooter.dal.DAL;
import com.danwink.tacticshooter.dal.SlickDAL;
import com.danwink.tacticshooter.gameobjects.Building;
import com.danwink.tacticshooter.gameobjects.Bullet;
import com.danwink.tacticshooter.gameobjects.Level;
import com.danwink.tacticshooter.gameobjects.Marker;
import com.danwink.tacticshooter.gameobjects.Unit;
import com.danwink.tacticshooter.gameobjects.Unit.UnitState;
import com.phyloa.dlib.math.Point2i;
import com.phyloa.dlib.util.DMath;

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

	Image saveBuffer;

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

		var slickDAL = (SlickDAL) dal;
		GameContainer gc = slickDAL.gc;
		Graphics g = slickDAL.g;

		outsideFloor.render(dal, cs);

		cs.camera.start(gc, g);
		// Main Rendering
		floor.render(dal, cs.l);
		footprint.render(dal, cs);
		bloodExplosion.render(dal, cs, unitBody);
		building.render(dal.getGraphics(), cs.l, false);
		// grass.render(g, cs);
		wall.render(dal, cs.l);
		renderMarkers(g, cs);
		unitBody.render(dal.getGraphics(), cs);
		bullet.render(dal.getGraphics(), cs);
		particle.render(dal.getGraphics());
		unitInfo.render(g, gc, cs, gc.getInput());
		if (fogEnabled)
			fog.render(g, cs);

		cs.camera.end(g);
	}

	public Image renderToTexture(int width, int height, ClientState cs, GameContainer gc) {
		if (saveBuffer == null) {
			try {
				saveBuffer = new Image(cs.l.width * Level.tileSize, cs.l.height * Level.tileSize);
			} catch (SlickException e) {
				// TODO Auto-generated catch block
				throw new RuntimeException(e);
			}
		}

		Image im;
		Graphics ig = null;
		Graphics g = null;
		try {
			im = new Image(width, height);
			ig = im.getGraphics();
			ig.setAntiAlias(false);
			g = saveBuffer.getGraphics();
			g.setAntiAlias(false);
		} catch (SlickException e) {
			throw new RuntimeException(e);
		}

		g.clear();
		g.clearAlphaMap();

		DAL dal = new SlickDAL(gc, g);

		floor.render(dal, cs.l);
		footprint.render(dal, cs);
		bloodExplosion.render(dal, cs, unitBody);
		building.render(dal.getGraphics(), cs.l, false);
		wall.render(dal, cs.l);
		renderMarkers(g, cs);
		unitBody.render(dal.getGraphics(), cs);
		bullet.render(dal.getGraphics(), cs);
		particle.render(dal.getGraphics());

		g.flush();

		ig.drawImage(saveBuffer, 0, 0, width, height, 0, 0, saveBuffer.getWidth(), saveBuffer.getHeight());
		ig.flush();

		return im;
	}

	public void renderEndGameMap(DAL dal, ClientState cs) {
		floor.render(dal, cs.l);
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

	public void renderMarkers(Graphics g, ClientState cs) {
		for (int i = 0; i < cs.markers.size(); i++) {
			Marker m = cs.markers.get(i);
			int frame = (int) (System.currentTimeMillis() / 250) % 4;
			g.drawImage(cs.l.theme.flag.slim(), m.x - 16, m.y - 32, m.x + 16, m.y, frame * 16, 0, (frame + 1) * 16, 16);
		}
	}

	public class UnitInfoRenderer {
		public void render(Graphics g, GameContainer gc, ClientState cs, Input input) {
			for (int i = 0; i < cs.units.size(); i++) {
				Unit u = cs.units.get(i);
				renderInfo(g, gc, cs, input, u);
			}
		}

		public void renderInfo(Graphics g, GameContainer gc, ClientState cs, Input input, Unit u) {
			var worldCoords = cs.camera.screenToWorld(input.getMouseX(), input.getMouseY(), gc);
			float mx = worldCoords.x;
			float my = worldCoords.y;
			if (u.selected && u.state == UnitState.MOVING) {
				g.setColor(Color.black);
				g.setLineWidth(3);
				for (int i = Math.max(u.onStep - 2, 0); i < u.path.size() - 1; i++) {
					Point2i p1 = u.path.get(i);
					Point2i p2 = u.path.get(i + 1);
					g.drawLine((p1.x + .5f) * Level.tileSize, (p1.y + .5f) * Level.tileSize,
							(p2.x + .5f) * Level.tileSize, (p2.y + .5f) * Level.tileSize);
				}

				g.setColor(Color.lightGray);
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
				g.setColor(Color.red);
				g.setLineWidth(3);
				g.drawOval(-12, -12, 24, 24);
				g.setLineWidth(1);
			}

			if (u.selected) {
				g.setColor(Color.blue);
				g.pushTransform();
				g.rotate(0, 0, (u.heading / DMath.PI2F * 360) + 45);
				g.drawArc(-16, -16, 32, 32, 0, 270);
				g.drawLine(0, -16, 16, -16);
				g.drawLine(16, 0, 16, -16);
				g.popTransform();
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
				g.setColor(Color.black);
				g.fillRect(-9, healthBarDist, (18.f * u.health / u.type.health), 4);

				g.setColor(new Color(DMath.bound(1.f - u.health / u.type.health, 0, 1),
						DMath.bound(u.health / u.type.health, 0, 1), 0));
				g.fillRect(-8, healthBarDist + 1, (16.f * u.health / u.type.health), 2);
			}

			float dmx = u.x - mx;
			float dmy = u.y - my;
			if (dmx * dmx + dmy * dmy < 100) {
				float strWidth = g.getFont().getWidth(u.owner.name);
				g.setColor(Color.black);
				g.drawString(u.owner.name, -strWidth / 2, 10);
			}

			g.popTransform();
		}
	}

	public class FogRenderer {
		Image texture;

		public void render(Graphics g, ClientState cs) {
			if (texture == null) {
				if (cs.l != null) {
					generateTexture(cs);
				} else {
					return;
				}
			}

			try {
				Graphics fogG = texture.getGraphics();

				fogG.setColor(Color.black);
				fogG.fillRect(0, 0, texture.getWidth(), texture.getHeight());

				for (int i = 0; i < cs.l.buildings.size(); i++) {
					Building b = cs.l.buildings.get(i);
					if (b.t != null && b.t.id == cs.player.team.id) {
						fogG.setColor(Color.white);
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

				g.setColor(Color.darkGray);

				g.setDrawMode(Graphics.MODE_COLOR_MULTIPLY);
				g.drawImage(texture, 0, 0);
				g.setDrawMode(Graphics.MODE_NORMAL);
			} catch (SlickException e) {
				e.printStackTrace();
			}
		}

		private void generateTexture(ClientState cs) {
			try {
				texture = new Image(cs.l.width * Level.tileSize, cs.l.height * Level.tileSize);
			} catch (SlickException e) {
				e.printStackTrace();
			}
		}
	}

	public void bulletImpact(Bullet b) {
		particle.bulletImpact(b);
	}

	public void updateWalls(ClientState cs) {
		wall.redrawLevel(cs.l);
	}
}
