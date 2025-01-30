package com.danwink.tacticshooter.gameobjects;

import jp.objectclub.vecmath.Point2f;
import jp.objectclub.vecmath.Vector2f;

import com.danwink.tacticshooter.Assets;
import com.danwink.tacticshooter.ClientState;
import com.danwink.tacticshooter.TacticServer;
import com.danwink.tacticshooter.dal.DAL.DALGraphics;
import com.danwink.tacticshooter.screens.MultiplayerGameScreen;
import com.phyloa.dlib.util.DMath;

public class Bullet {
	public static float bulletSpeed = 35;

	public int id = (int) (Math.random() * Integer.MAX_VALUE);

	public Point2f loc;
	public Point2f lastLoc;
	public Vector2f dir;

	public int damage = 10;

	public boolean alive = true;
	public boolean isRicochet = false;

	public Player owner;
	public Unit shooter;

	Point2f p = new Point2f();

	int age = 0;

	public Bullet() {

	}

	public Bullet(float x, float y, float angle) {
		loc = new Point2f(x, y);
		dir = new Vector2f(DMath.cosf(angle) * bulletSpeed, DMath.sinf(angle) * bulletSpeed);
		lastLoc = new Point2f(x - dir.x, y - dir.y);
	}

	public Bullet(float x, float y, float dx, float dy) {
		loc = new Point2f(x, y);
		dir = new Vector2f(dx, dy);
		dir.normalize();
		dir.scale(bulletSpeed);
		lastLoc = new Point2f(x - dir.x, y - dir.y);
	}

	public void update(TacticServer ts) {
		lastLoc.set(loc);
		loc.add(dir);

		Level l = ts.l;

		for (Unit u : ts.units) {
			if (u.owner.team.id != owner.team.id && u.alive) {
				p.x = u.x;
				p.y = u.y;
				if (DMath.pointToLineSegment(lastLoc, dir, p).lengthSquared() < Unit.radius * Unit.radius) {
					u.hit(this, ts);
					alive = false;
					break;
				}
			}
		}

		// Test if hits wall
		Point2f hitPoint = new Point2f();
		if (l.hitwall(lastLoc, dir, hitPoint)) {
			alive = false;

			Bullet b = null;
			if (Math.abs(hitPoint.x - Math.round(hitPoint.x)) < .001f) {
				// hit vertical
				if (Math.random() * 2.0f < Math.abs(dir.y) / Math.abs(dir.x)) {
					b = new Bullet(hitPoint.x - (dir.x * .01f), hitPoint.y, -dir.x, dir.y);
					b.age = this.age + 1;
				}
			} else if (Math.abs(hitPoint.y - Math.round(hitPoint.y)) < .001f) {
				// hit horizontal
				if (Math.random() * 2.0f < Math.abs(dir.x) / Math.abs(dir.y)) {
					b = new Bullet(hitPoint.x, hitPoint.y - (dir.y * .01f), dir.x, -dir.y);
					b.age = this.age + 1;
				}
			}

			if (b != null && b.age < 5) {
				b.owner = this.owner;
				b.shooter = this.shooter;
				b.damage = this.damage;
				b.isRicochet = true;
				ts.addBullet(b);
			}

			return;
		}

		if (loc.x > l.width * Level.tileSize || loc.x < 0 || loc.y > l.height * Level.tileSize || loc.y < 0) {
			alive = false;
		}
	}

	public void clientUpdate(MultiplayerGameScreen cs, float d) {
		ClientState ts = cs.cs;
		Level l = ts.l;
		lastLoc.set(loc);
		loc.x += dir.x * d * 1.4f;
		loc.y += dir.y * d * 1.4f;

		// Test if hits wall
		if (l.hitwall(lastLoc, dir)) {
			alive = false;
			cs.bulletImpact(this);

			return;
		}

		for (Unit u : ts.units) {
			if (u.owner.team.id != owner.team.id && u.alive) {
				p.x = u.x;
				p.y = u.y;
				Vector2f sect = DMath.pointToLineSegment(lastLoc, dir, p);
				if (sect.lengthSquared() < Unit.radius * Unit.radius) {
					alive = false;

					Assets.getSound("hit1").play(ts.getSoundMag(loc.x, loc.y) * .3f, DMath.randomf(.9f, 1.1f), 0.f);
					cs.drawBlood(loc.x, loc.y);
					break;
				}
			}
		}

		if (loc.x > l.width * Level.tileSize || loc.x < 0 || loc.y > l.height * Level.tileSize || loc.y < 0) {
			alive = false;
			cs.bulletImpact(this);
		}
	}

	public void render(DALGraphics g) {
		// g.setColor( Color.white );
		g.drawLine(loc.x, loc.y, loc.x + dir.x * .5f, loc.y + dir.y * .5f);
	}
}
