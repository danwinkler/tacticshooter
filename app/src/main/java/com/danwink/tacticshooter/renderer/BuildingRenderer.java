package com.danwink.tacticshooter.renderer;

import org.newdawn.slick.Graphics;

import com.danwink.tacticshooter.ClientState;
import com.danwink.tacticshooter.gameobjects.Level;

public class BuildingRenderer {
	public void render(Graphics g, Level l, boolean endGame) {
		l.renderBuildings(g, endGame);
	}
}
