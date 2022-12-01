package com.danwink.tacticshooter.ai;

import org.dom4j.DocumentException;
import org.lwjgl.openal.OpenALException;
import org.newdawn.slick.AppGameContainer;
import org.newdawn.slick.BasicGame;
import org.newdawn.slick.Color;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.util.pathfinding.AStarPathFinder;

import com.danwink.tacticshooter.LevelFileHelper;
import com.danwink.tacticshooter.StaticFiles;
import com.danwink.tacticshooter.Theme;
import com.danwink.tacticshooter.ai.LevelAnalysis.Neighbor;
import com.danwink.tacticshooter.ai.LevelAnalysis.Zone;
import com.danwink.tacticshooter.gameobjects.Building;
import com.danwink.tacticshooter.gameobjects.Level;

public class MapPartitionTest extends BasicGame {
	Theme t;
	Level l;
	LevelAnalysis la;

	public MapPartitionTest() {
		super("Map Partition Test");
	}

	@Override
	public void init(GameContainer gc) throws SlickException {
		String[] levels = new String[] {
				"sidewinder",
				"battlefield2",
				"dan.Battlefield1",
				"dan.Big Booty Bitches",
				"dan.The Complex",
				"dan.The Adventure"
		};
		try {
			t = Theme.getTheme("desertrpg");
			l = LevelFileHelper.loadLevel(levels[5]);
			l.theme = t;

			la = new LevelAnalysis();
			la.build(l, new AStarPathFinder(l, 500, StaticFiles.options.getB("diagonalMove")));
		} catch (DocumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void update(GameContainer gc, int t) throws SlickException {

	}

	@Override
	public void render(GameContainer gc, Graphics g) throws SlickException {
		g.pushTransform();
		g.scale((float) gc.getWidth() / (l.width * Level.tileSize),
				(float) gc.getHeight() / (l.height * Level.tileSize));

		l.renderFloor(g);
		l.render(g);
		l.renderBuildings(g, false);

		la.render(g);

		g.setColor(Color.black);

		for (Zone z : la.zones) {
			Building a = z.b;
			for (Neighbor n : z.neighbors) {
				Building b = n.z.b;
				g.drawLine(a.x, a.y, b.x, b.y);
				g.drawString("" + n.distance, (a.x + b.x) * .5f, (a.y + b.y) * .5f);
			}
		}

		g.popTransform();
	}

	public static void main(String[] args) {
		try {
			AppGameContainer app = new AppGameContainer(new MapPartitionTest());
			app.setMultiSample(StaticFiles.advOptions.getI("multisample"));
			app.setDisplayMode(StaticFiles.options.getI("windowWidth"), StaticFiles.options.getI("windowHeight"),
					StaticFiles.options.getB("fullscreen"));
			app.setVSync(StaticFiles.options.getB("vsync"));
			app.setUpdateOnlyWhenVisible(false);
			app.setAlwaysRender(true);
			app.start();
		} catch (OpenALException ex) {
			// These seem to happen fairly often on macs, not quite sure what to do about
			// it.
			ex.printStackTrace();
			System.exit(1);
		} catch (SlickException e) {
			e.printStackTrace();
		}
	}
}
