package com.danwink.tacticshooter;

import java.util.ArrayList;
import java.util.HashMap;

import org.newdawn.slick.GameContainer;

import com.danwink.tacticshooter.gameobjects.Bullet;
import com.danwink.tacticshooter.gameobjects.Level;
import com.danwink.tacticshooter.gameobjects.Marker;
import com.danwink.tacticshooter.gameobjects.Player;
import com.danwink.tacticshooter.gameobjects.Unit;
import com.danwink.tacticshooter.screens.MultiplayerGameScreen;

public class ClientState {
	public HashMap<Integer, Unit> unitMap = new HashMap<Integer, Unit>();
	public ArrayList<Unit> units = new ArrayList<Unit>();

	public ArrayList<Bullet> bullets = new ArrayList<Bullet>();
	public ArrayList<Marker> markers = new ArrayList<Marker>();

	public Level l;

	public ArrayList<Integer> selected = new ArrayList<Integer>();

	public Player player;

	public float scrollx = 0;
	public float scrolly = 0;

	public float soundFadeDist = 1000;
	public Player[] players;
	public MultiplayerGameScreen mgs;

	public ClientState(MultiplayerGameScreen mgs) {
		this.mgs = mgs;
	}

	public void resetState() {
		unitMap.clear();
		units.clear();
		bullets.clear();
		l = null;
		selected.clear();
		scrollx = 0;
		scrolly = 0;
	}

	public float getSoundMag(GameContainer gc, float x, float y) {
		float dx = (scrollx + (gc.getWidth() / 2)) - x;
		float dy = (scrolly + (gc.getHeight() / 2)) - y;
		float dist = (float) Math.sqrt((dx * dx) + (dy * dy));
		return Math.max((float) ((soundFadeDist - dist) / soundFadeDist), 0);
	}
}
