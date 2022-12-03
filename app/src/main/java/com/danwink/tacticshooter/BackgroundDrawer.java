package com.danwink.tacticshooter;

import java.io.File;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.SlickException;

import com.phyloa.dlib.util.DMath;
import com.phyloa.dlib.util.ImprovedNoise;

public class BackgroundDrawer {
	Theme theme;

	float scrollx;
	float scrolly;
	ImprovedNoise n;

	File themePath;

	public BackgroundDrawer() {
		File[] files = new File("data/themes").listFiles();
		// TODO: Throw Error if no files in folder
		themePath = files[DMath.randomi(0, files.length)];
		n = new ImprovedNoise(System.currentTimeMillis());
	}

	public void update(int delta) {
		scrollx += delta * .03f;
		scrolly += delta * .03f;
	}

	public void render(GameContainer gc, Graphics g) {
		if (theme == null) {
			try {
				theme = Theme.getTheme(themePath.getName());
			} catch (SlickException e) {
				e.printStackTrace();
			}
		}

		g.pushTransform();
		g.setAntiAlias(true);
		g.translate(-scrollx, -scrolly);

		int tileSize = 40;

		int scrollxTile = (int) (scrollx / tileSize);
		int scrollyTile = (int) (scrolly / tileSize);

		for (int y = scrollyTile; y < scrollyTile + gc.getHeight() / tileSize + 3; y++) {
			for (int x = scrollxTile; x < scrollxTile + gc.getWidth() / tileSize + 3; x++) {
				g.pushTransform();
				g.translate(x * tileSize, y * tileSize);
				Image here = getTile(x, y);
				if (here == theme.wall) {
					g.drawImage(theme.floor, 0, 0, tileSize, tileSize, theme.floor.getWidth() / 3, 0,
							theme.floor.getWidth() / 3 * 2, theme.floor.getHeight() / 4);
				}
				AutoTileDrawer.draw(g, here, tileSize, 0,
						getTile(x - 1, y - 1) == here,
						getTile(x, y - 1) == here,
						getTile(x + 1, y - 1) == here,
						getTile(x - 1, y) == here,
						getTile(x + 1, y) == here,
						getTile(x - 1, y + 1) == here,
						getTile(x, y + 1) == here,
						getTile(x + 1, y + 1) == here);
				g.popTransform();
			}
		}

		g.popTransform();
	}

	public Image getTile(int x, int y) {
		return n.noise(x * .1, y * .1, 0) > .2f ? theme.wall : theme.floor;
	}
}
