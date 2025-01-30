package com.danwink.tacticshooter;

import com.danwink.tacticshooter.dal.DAL;
import com.phyloa.dlib.renderer.DScreenTransition;
import com.phyloa.dlib.util.DMath;

public class DScreenSlideTransition implements DScreenTransition<DAL> {
	float x, y;
	int xdir;
	int ydir;

	boolean slideIn;

	float startX;
	float startY;
	float goalX;
	float goalY;

	float time;
	float duration;

	public DScreenSlideTransition(int xdir, int ydir, float duration, boolean slideIn) {
		this.xdir = xdir;
		this.ydir = ydir;
		this.duration = duration;
		this.slideIn = slideIn;
	}

	public void init(DAL dal) {
		if (slideIn) {
			startX = dal.getWidth() * -xdir;
			startY = dal.getHeight() * -ydir;
		} else {
			goalX = dal.getWidth() * xdir;
			goalY = dal.getHeight() * ydir;
		}

		float v = slideIn ? expOut(time, 0, 1, duration) : expIn(time, 0, 1, duration);

		x = DMath.lerp(v, startX, goalX);
		y = DMath.lerp(v, startY, goalY);
	}

	public void update(DAL dal, float d) {
		float v = slideIn ? expOut(time, 0, 1, duration) : expIn(time, 0, 1, duration);

		x = DMath.lerp(v, startX, goalX);
		y = DMath.lerp(v, startY, goalY);

		time += d;
	}

	public void renderPre(DAL dal) {
		var g = dal.getGraphics();
		g.pushTransform();
		g.translate(x, y);
	}

	public void renderPost(DAL dal) {
		var g = dal.getGraphics();
		g.popTransform();
	}

	public boolean isFinished() {
		return time >= duration;
	}

	private static float expIn(float t, float b, float c, float d) {
		return (t == 0) ? b : c * (float) Math.pow(2, 10 * (t / d - 1)) + b;
	}

	private static float expOut(float t, float b, float c, float d) {
		return (t == d) ? b + c : c * (-(float) Math.pow(2, -10 * t / d) + 1) + b;
	}
}
