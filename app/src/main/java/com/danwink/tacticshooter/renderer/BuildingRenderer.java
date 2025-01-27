package com.danwink.tacticshooter.renderer;

import com.danwink.tacticshooter.dal.DAL.DALGraphics;
import com.danwink.tacticshooter.gameobjects.Level;

public class BuildingRenderer {
	public void render(DALGraphics g, Level l, boolean endGame) {
		l.renderBuildings(g, endGame);
	}
}
