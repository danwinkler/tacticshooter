package com.danwink.tacticshooter.renderer;

import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.SlickException;

import com.danwink.tacticshooter.ClientState;
import com.danwink.tacticshooter.gameobjects.Level;

public class FloorRenderer {
	public Image texture;

	public void render(Graphics g, Level l) {
		if (texture == null) {
			if (l != null) {
				generateTexture(l);
			} else {
				return;
			}
		}

		g.drawImage(texture, 0, 0);
	}

	private void generateTexture(Level l) {
		try {
			texture = new Image(l.width * Level.tileSize, l.height * Level.tileSize);
			var g = texture.getGraphics();
			g.setAntiAlias(false);
			l.renderFloor(g);
		} catch (SlickException e) {
			e.printStackTrace();
		}
	}

	public void redrawLevel(Level l) {
		try {
			var g = texture.getGraphics();
			g.clear();
			g.clearAlphaMap();
			l.renderFloor(g);
			g.flush();
		} catch (SlickException e) {
			e.printStackTrace();
		}
	}
}
