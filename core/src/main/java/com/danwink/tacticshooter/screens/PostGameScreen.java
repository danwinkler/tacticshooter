package com.danwink.tacticshooter.screens;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.imageout.ImageOut;

import com.danwink.tacticshooter.GameStats;
import com.danwink.tacticshooter.GameStats.TeamStats;
import com.danwink.tacticshooter.dal.DAL;
import com.danwink.tacticshooter.dal.DAL.DALColor;
import com.danwink.tacticshooter.dal.SlickDAL.SlickTexture;
import com.danwink.tacticshooter.StaticFiles;
import com.danwink.tacticshooter.slick.Slick2DRenderer;
import com.danwink.tacticshooter.ui.DUIScreen;
import com.phyloa.dlib.dui.DButton;
import com.phyloa.dlib.dui.DColumnPanel;
import com.phyloa.dlib.dui.DLinePlot;
import com.phyloa.dlib.dui.DRowPanel;
import com.phyloa.dlib.dui.DText;
import com.phyloa.dlib.dui.DUI;
import com.phyloa.dlib.dui.DUIElement;
import com.phyloa.dlib.dui.DUIEvent;
import com.phyloa.dlib.dui.DUITheme;
import com.phyloa.dlib.dui.RelativePosition;
import com.phyloa.dlib.util.DUtil;

public class PostGameScreen extends DUIScreen {
	DUI dui;
	DButton okay;
	DButton rejoin;
	DButton saveImage;

	Slick2DRenderer r = new Slick2DRenderer();

	GameStats stats;
	Image endMap;

	boolean rd = false;

	public void init(DAL dal) {
		// TODO:
		// StaticFiles.getMusic("menu").loop();
		rd = false;
	}

	public void createUIElements(DUI dui, float windowHeight) {
		dui.setTheme(new DUITheme(DUITheme.defaultTheme) {
			{
				borderColor = new java.awt.Color(64, 170, 255);
			}
		});

		var centerColumn = new DColumnPanel(0, 0, 0, 0);
		centerColumn.setRelativePosition(RelativePosition.CENTER, 0, 0);

		centerColumn.add(new DText("Post Game Stats", false).setSize(0, 30 * uiScale));

		DRowPanel statsRow = new DRowPanel(0, 0, 0, 0);

		var statsTextWidth = 300 * uiScale;
		var statsTextHeight = 20 * uiScale;

		for (TeamStats ts : stats.teamStats) {
			DColumnPanel teamStatsCol = new DColumnPanel(0, 0, 0, 0);
			teamStatsCol.add(new DText("Team: " + ts.t.id, false).setSize(statsTextWidth, statsTextHeight));
			teamStatsCol
					.add(new DText("Bullets Shot: " + ts.bulletsShot, false).setSize(statsTextWidth, statsTextHeight));
			teamStatsCol
					.add(new DText("Money Earned: " + ts.moneyEarned, false).setSize(statsTextWidth, statsTextHeight));
			teamStatsCol
					.add(new DText("Points Taken: " + ts.pointsTaken, false).setSize(statsTextWidth, statsTextHeight));
			teamStatsCol.add(
					new DText("Units Created: " + ts.unitsCreated, false).setSize(statsTextWidth, statsTextHeight));
			teamStatsCol.add(new DText("Units Lost: " + ts.unitsLost, false).setSize(statsTextWidth, statsTextHeight));

			statsRow.add(teamStatsCol);
		}

		// Top stats
		var topStatsCol = new DColumnPanel(0, 0, 0, 0);
		var mostKills = stats.getMostKills();
		topStatsCol.add(new DText("Most Kills: " + mostKills.playerName + " with " + mostKills.kills + " kills",
				false).setSize(statsTextWidth, statsTextHeight));

		var mostUnitsCreated = stats.getMostUnitsCreated();
		topStatsCol.add(new DText("Most Units Created: " + mostUnitsCreated.playerName + " with "
				+ mostUnitsCreated.unitsCreated + " units created", false).setSize(statsTextWidth, statsTextHeight));

		var mostUnitsLost = stats.getMostUnitsLost();
		topStatsCol.add(new DText("Most Units Lost: " + mostUnitsLost.playerName + " with "
				+ mostUnitsLost.unitsLost + " units lost", false).setSize(statsTextWidth, statsTextHeight));

		var leastUnitsLost = stats.getLeastUnitsLost();
		topStatsCol.add(new DText("Least Units Lost: " + leastUnitsLost.playerName + " with "
				+ leastUnitsLost.unitsLost + " units lost", false).setSize(statsTextWidth, statsTextHeight));

		var mostBulletShot = stats.getMostBulletsShot();
		topStatsCol.add(new DText("Most Bullets Shot: " + mostBulletShot.playerName + " with "
				+ mostBulletShot.bulletsShot + " bullets shot", false).setSize(statsTextWidth, statsTextHeight));

		statsRow.add(topStatsCol);

		centerColumn.add(statsRow);
		centerColumn.addSpacer(30 * uiScale);

		// Test and see if extraneous data got added on to end :/
		ArrayList<Integer> lastPointA = stats.teamStats[0].pointCount;
		ArrayList<Integer> lastPointB = stats.teamStats[1].pointCount;
		if (lastPointA.get(lastPointA.size() - 1) + lastPointB.get(lastPointB.size() - 1) == 0) {
			for (TeamStats ts : stats.teamStats) {
				ts.pointCount.remove(ts.pointCount.size() - 1);
				ts.unitCount.remove(ts.unitCount.size() - 1);
			}
		}

		var pointsCol = new DColumnPanel(0, 0, 0, 0);
		pointsCol.add(new DText("Points:", 0, 0).setSize(0, 30 * uiScale));
		DLinePlot pointPlot = new DLinePlot(0, 0, 800 * uiScale, 100 * uiScale);
		for (TeamStats ts : stats.teamStats) {
			DALColor c = ts.t.getColor();
			pointPlot.addLine(DUtil.integerArrayListToIntArray(ts.pointCount), new java.awt.Color(c.r, c.g, c.b, .8f));
		}
		pointsCol.add(pointPlot);
		pointsCol.setRelativePosition(RelativePosition.CENTER, 0, 0);
		centerColumn.add(pointsCol);
		centerColumn.addSpacer(30 * uiScale);

		var unitsCol = new DColumnPanel(0, 0, 0, 0);
		unitsCol.add(new DText("Units:", 0, 0).setSize(0, 30 * uiScale));
		DLinePlot unitPlot = new DLinePlot(0, 0, 800 * uiScale, 100 * uiScale);
		for (TeamStats ts : stats.teamStats) {
			DALColor c = ts.t.getColor();
			unitPlot.addLine(DUtil.integerArrayListToIntArray(ts.unitCount), new java.awt.Color(c.r, c.g, c.b, .8f));
		}
		unitsCol.add(unitPlot);
		unitsCol.setRelativePosition(RelativePosition.CENTER, 0, 0);
		centerColumn.add(unitsCol);
		centerColumn.addSpacer(30 * uiScale);

		var buttonsRow = new DRowPanel(0, 0, 0, 0);
		buttonsRow.setRelativePosition(RelativePosition.CENTER, 0, 0);

		okay = new DButton("Okay", 0, 0, 200 * uiScale, 100 * uiScale);
		rejoin = new DButton("Rejoin", 0, 0, 200 * uiScale, 100 * uiScale);
		saveImage = new DButton("Save Image of\n End Map", 0, 0, 200 * uiScale, 100 * uiScale);

		buttonsRow.add(okay);
		buttonsRow.add(rejoin);
		buttonsRow.add(saveImage);

		centerColumn.add(buttonsRow);

		dui.add(centerColumn);
	}

	@Override
	public void render(DAL dal) {
		var g = dal.getGraphics();

		g.setColor(new DALColor(0, 0, 0, 200));
		g.fillRect(0, 0, dal.getWidth(), dal.getHeight());

		// Render UI
		super.render(dal);

		// SO yeah I have to render here in order to get the blood to show up :/
		if (!rd) {
			// TODO:
			// Graphics emg = endMap.getGraphics();
			// MultiplayerGameScreen mgs = ((MultiplayerGameScreen)
			// dsh.get("multiplayergame"));
			// endMap.getGraphics().drawImage(mgs.gameRenderer.bloodExplosion.texture.slim(),
			// 0, 0);
			// var wallImage = ((SlickTexture) mgs.gameRenderer.wall.texture).image;
			// endMap.getGraphics().drawImage(wallImage, 0, 0);

			rd = true;
		}
	}

	public void event(DUIEvent event) {
		DUIElement e = event.getElement();
		if (e instanceof DButton && event.getType() == DButton.MOUSE_UP) {
			if (e == okay) {
				dsh.activate("home", dal, StaticFiles.getUpMenuOut(), StaticFiles.getUpMenuIn());
			} else if (e == rejoin) {
				dsh.message("connect", ((MultiplayerSetupScreen) dsh.get("multiplayersetup")).address.getText().trim());
				dsh.activate("connect", dal, StaticFiles.getUpMenuOut(), StaticFiles.getUpMenuIn());
			} else if (e == saveImage) {
				if (endMap == null)
					return;
				saveImage.setText("Image Saved!");
				try {
					// We have to create the FileOutputStream ourselves, as the ImageOut utility
					// will never close it!
					FileOutputStream fos = new FileOutputStream("screenshots/" + System.currentTimeMillis() + ".png");
					ImageOut.write(endMap.getFlippedCopy(false, false), "png", fos);
					fos.close();
					endMap.destroy();
					endMap = null;
				} catch (SlickException | IOException e1) {
					e1.printStackTrace();
				}
			}
		}

	}

	@Override
	public void message(Object o) {
		if (o instanceof GameStats) {
			stats = (GameStats) o;
		} else if (o instanceof Image) {
			endMap = (Image) o;
		}
	}
}
