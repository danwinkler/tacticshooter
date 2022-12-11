package com.danwink.tacticshooter.renderer;

import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.SlickException;

import com.danwink.tacticshooter.ClientState;
import com.danwink.tacticshooter.gameobjects.Level;

public class OutsideFloorRenderer {
	Image texture;

	int lastScreenWidth, lastScreenHeight;

	public void render(Graphics g, ClientState cs, GameContainer gc) {
		if (texture == null || lastScreenWidth != gc.getWidth() || lastScreenHeight != gc.getHeight()) {
			if (cs.l != null) {
				generateTexture(cs, gc);
				lastScreenWidth = gc.getWidth();
				lastScreenHeight = gc.getHeight();
			} else {
				return;
			}
		}

		// TODO: this isn't going to work anymore with zoom
		float x = -Level.tileSize - (cs.camera.x - ((int) (cs.camera.x / Level.tileSize)) * Level.tileSize);
		float y = -Level.tileSize - (cs.camera.y - ((int) (cs.camera.y / Level.tileSize) * Level.tileSize));
		g.drawImage(texture, x, y);
	}

	private void generateTexture(ClientState cs, GameContainer gc) {
		try {
			texture = new Image(gc.getWidth() + Level.tileSize * 2, gc.getHeight() + Level.tileSize * 2);

			Graphics bgg = texture.getGraphics();
			for (int y = 0; y < gc.getHeight() + Level.tileSize * 2; y += Level.tileSize) {
				for (int x = 0; x < gc.getWidth() + Level.tileSize * 2; x += Level.tileSize) {
					bgg.pushTransform();
					bgg.drawImage(
							cs.l.theme.floor,
							x,
							y,
							x + Level.tileSize,
							y + Level.tileSize,
							cs.l.theme.floor.getWidth() / 3,
							0,
							cs.l.theme.floor.getWidth() / 3 * 2,
							cs.l.theme.floor.getHeight() / 4);
					bgg.popTransform();
				}
			}
		} catch (SlickException e) {
			e.printStackTrace();
		}
	}
}
