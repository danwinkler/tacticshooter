package com.danwink.tacticshooter.renderer;

import java.util.concurrent.ConcurrentLinkedDeque;

import com.danwink.tacticshooter.ClientState;
import com.danwink.tacticshooter.dal.DAL;
import com.danwink.tacticshooter.dal.DAL.DALColor;
import com.danwink.tacticshooter.dal.DAL.DALGraphics;
import com.danwink.tacticshooter.dal.DAL.DALTexture;
import com.danwink.tacticshooter.gameobjects.Level;
import com.danwink.tacticshooter.gameobjects.Unit;
import com.phyloa.dlib.util.DMath;

import jp.objectclub.vecmath.Point2f;

/**
 * Draws blood and craters to a initially transparent texture to be rendered
 * above the floor
 */
public class BloodLayerRenderer {
	BloodLayerRenderer(GameRenderer gameRenderer) {
	}

	public DALColor bloodColor = new DALColor(1.f, 0, 0, 1.f);

	public DALTexture texture;

	private ConcurrentLinkedDeque<Point2f> bloodToDraw = new ConcurrentLinkedDeque<>();

	public void render(DAL dal, ClientState cs, UnitBodyRenderer ubr) {
		if (texture == null) {
			if (cs.l != null) {
				generateTexture(dal, cs);
			} else {
				return;
			}
		}

		if (!bloodToDraw.isEmpty()) {
			internalDrawBlood(cs);
		}

		var g = dal.getGraphics();

		g.drawImage(texture, 0, 0);
	}

	public void generateTexture(DAL dal, ClientState cs) {
		texture = dal.generateRenderableTexture(cs.l.width * Level.tileSize, cs.l.height * Level.tileSize);
		texture.getTextureRegion().flip(false, true);

		texture.renderTo(g -> {
			g.clear();
			g.setAntiAlias(false);
		});
	}

	public void killUnit(Unit u, ClientState cs, UnitBodyRenderer ubr) {
		texture.renderTo(tg -> {
			ubr.drawDeadUnit(tg, u, cs);

			for (int j = 0; j < 10; j++) {
				internalDrawBlood(tg, cs, u.x, u.y);
			}

			if (u.type.explodesOnDeath) {
				tg.drawImage(cs.l.theme.crater, u.x - 16, u.y - 16, u.x + 16, u.y + 16, 0, 0, 32, 32);
				tg.flush();
			}
		});
	}

	public void drawBlood(float x, float y) {
		bloodToDraw.addFirst(new Point2f(x, y));
	}

	private void internalDrawBlood(DALGraphics tg, ClientState cs, float x, float y) {
		var drawX = x + DMath.randomf(-8, 8);
		var drawY = y + DMath.randomf(-8, 8);
		// tg.drawImage(cs.l.theme.smoke, drawX - 4, drawY - 4, drawX + 4, drawY + 4, 0,
		// 0, 64, 64, bloodColor);
		tg.drawImage(cs.l.theme.smoke, drawX - 4, drawY - 4, 8, 8, bloodColor);
	}

	private void internalDrawBlood(ClientState cs) {
		texture.renderTo(tg -> {
			while (!bloodToDraw.isEmpty()) {
				var p = bloodToDraw.removeLast();
				internalDrawBlood(tg, cs, p.x, p.y);
			}
			tg.flush();
		});
	}
}
