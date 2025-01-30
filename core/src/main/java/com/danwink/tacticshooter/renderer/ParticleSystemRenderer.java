package com.danwink.tacticshooter.renderer;

import com.danwink.tacticshooter.ClientState;
import com.danwink.tacticshooter.ExplodeParticle;
import com.danwink.tacticshooter.dal.DAL.DALColor;
import com.danwink.tacticshooter.dal.DAL.DALGraphics;
import com.danwink.tacticshooter.gameobjects.Bullet;
import com.danwink.tacticshooter.gameobjects.WallParticle;
import com.phyloa.dlib.particle.ParticleSystem;
import com.phyloa.dlib.util.DMath;

public class ParticleSystemRenderer {
	public ParticleSystem<DALGraphics> ps = new ParticleSystem<>();

	public void update(float d) {
		ps.update(d);
	}

	public void render(DALGraphics g) {
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
				p.c = new DALColor(r, g, b, 1.f);
				p.friction = .075f;
				p.im = cs.l.theme.smoke;
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
