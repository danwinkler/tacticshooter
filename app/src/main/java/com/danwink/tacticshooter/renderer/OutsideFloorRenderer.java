package com.danwink.tacticshooter.renderer;

import com.danwink.tacticshooter.ClientState;
import com.danwink.tacticshooter.dal.DAL;
import com.danwink.tacticshooter.dal.DAL.DALTexture;
import com.danwink.tacticshooter.dal.SlickDAL;
import com.danwink.tacticshooter.gameobjects.Level;

public class OutsideFloorRenderer {
	DALTexture texture;

	int lastScreenWidth, lastScreenHeight;

	public void render(DAL dal, ClientState cs) {
		if (texture == null || lastScreenWidth != dal.getWidth() || lastScreenHeight != dal.getHeight()) {
			if (cs.l != null) {
				generateTexture(dal, cs);
				lastScreenWidth = dal.getWidth();
				lastScreenHeight = dal.getHeight();
			} else {
				return;
			}
		}

		// TODO: this isn't going to work anymore with zoom
		float x = -Level.tileSize - (cs.camera.x - ((int) (cs.camera.x / Level.tileSize)) * Level.tileSize);
		float y = -Level.tileSize - (cs.camera.y - ((int) (cs.camera.y / Level.tileSize) * Level.tileSize));
		var g = dal.getGraphics();
		g.drawImage(texture, x, y);
	}

	private void generateTexture(DAL dal, ClientState cs) {
		texture = dal.generateRenderableTexture(dal.getWidth() + Level.tileSize * 2,
				dal.getHeight() + Level.tileSize * 2);

		texture.renderTo(g -> {
			for (int y = 0; y < dal.getHeight() + Level.tileSize * 2; y += Level.tileSize) {
				for (int x = 0; x < dal.getWidth() + Level.tileSize * 2; x += Level.tileSize) {
					g.drawImage(
							cs.l.theme.floor,
							x,
							y,
							x + Level.tileSize,
							y + Level.tileSize,
							cs.l.theme.floor.getWidth() / 3,
							0,
							cs.l.theme.floor.getWidth() / 3 * 2,
							cs.l.theme.floor.getHeight() / 4);
				}
			}
		});
	}
}
