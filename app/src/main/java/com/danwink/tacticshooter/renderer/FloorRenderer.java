package com.danwink.tacticshooter.renderer;

import org.checkerframework.checker.units.qual.g;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.SlickException;

import com.danwink.tacticshooter.dal.DAL;
import com.danwink.tacticshooter.dal.DAL.DALTexture;
import com.danwink.tacticshooter.gameobjects.Level;

public class FloorRenderer {
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

	private void generateTexture(DAL dal, Level l) {
		texture = dal.generateRenderableTexture(l.width * Level.tileSize, l.height * Level.tileSize);
		texture.renderTo(g -> {
			g.setAntiAlias(false);
			l.renderFloor(g);
		});
	}

	public void redrawLevel(Level l) {
		texture.renderTo(g -> {
			g.clear();
			l.renderFloor(g);
			g.flush();
		});
	}
}
