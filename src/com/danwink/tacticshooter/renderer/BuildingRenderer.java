package com.danwink.tacticshooter.renderer;

import org.newdawn.slick.Graphics;

import com.danwink.tacticshooter.ClientState;

public class BuildingRenderer
{
	public void render( Graphics g, ClientState cs ) 
	{
		cs.l.renderBuildings( g );
	}
}