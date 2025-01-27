package com.danwink.tacticshooter.renderer;

import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;

import com.danwink.tacticshooter.ClientState;
import com.danwink.tacticshooter.ExplodeParticle;
import com.danwink.tacticshooter.gameobjects.Bullet;
import com.danwink.tacticshooter.gameobjects.WallParticle;
import com.phyloa.dlib.particle.ParticleSystem;
import com.phyloa.dlib.util.DMath;

public class ParticleSystemRenderer {
	public ParticleSystem<Graphics> ps = new ParticleSystem<Graphics>();

	public void update(float d) {
		ps.update(d);
	}

	public void render(Graphics g) {
		ps.render(g);
	}

	public void createExplosion(float x, float y, ClientState cs) {
		for (int j = 0; j < 7; j++) {
			float magmax = DMath.randomf(.4f, 1);
			float heading = DMath.randomf(0, DMath.PI2F);
			for (float mag = .1f; mag < 1; mag += .1f) {
				float r = DMath.lerp(mag, .5f, 1f);
				float g = DMath.lerp(mag, .5f, .25f);
				float b = DMath.lerp(mag, .5f, 0f);
				ExplodeParticle p = new ExplodeParticle(x, y, DMath.cosf(heading) * 25 * mag * magmax,
						DMath.sinf(heading) * 25 * mag * magmax, 30);
				p.c = new Color(r, g, b);
				p.friction = .075f;
				p.im = cs.l.theme.smoke.slim();
				p.size = (1.f - mag) * magmax * 20;
				ps.add(p);
			}
		}
	}

	public void bulletImpact(Bullet b) {
		for (int i = 0; i < 10; i++) {
			ps.add(WallParticle.makeParticle(b.loc.x + b.dir.x * .5f, b.loc.y + b.dir.y * .5f));
		}
	}
}
