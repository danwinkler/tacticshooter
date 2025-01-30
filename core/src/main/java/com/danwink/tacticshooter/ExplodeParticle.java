package com.danwink.tacticshooter;

import com.danwink.tacticshooter.dal.DAL.DALColor;
import com.danwink.tacticshooter.dal.DAL.DALGraphics;
import com.danwink.tacticshooter.dal.DAL.DALTexture;
import com.phyloa.dlib.particle.Particle;
import com.phyloa.dlib.util.DMath;

public class ExplodeParticle extends Particle<DALGraphics> {
	public DALColor c;
	float maxdur;
	public float size;
	public DALTexture im;

	public ExplodeParticle(float x, float y, float dx, float dy, float duration) {
		super(x, y, 0, dx, dy, 0, duration);
		maxdur = duration;
	}

	public void update(float d) {
		super.update(d);
		c.a = .5f * DMath.minf(((timeleft * 2) / maxdur), 1);
	}

	public void render(DALGraphics r) {
		// r.setColor( c );
		r.drawImage(im, pos.x - size, pos.y - size, pos.x + size, pos.y + size, 0, 0, 64, 64, c);
	}
}
