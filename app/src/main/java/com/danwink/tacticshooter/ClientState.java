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
import com.danwink.tacticshooter.slick.Slick2DCamera;

public class ClientState {
	public HashMap<Integer, Unit> unitMap = new HashMap<Integer, Unit>();
	public ArrayList<Unit> units = new ArrayList<Unit>();

	public ArrayList<Bullet> bullets = new ArrayList<Bullet>();
	public ArrayList<Marker> markers = new ArrayList<Marker>();

	public Level l;

	public ArrayList<Integer> selected = new ArrayList<Integer>();

	public Player player;

	public Slick2DCamera camera = new Slick2DCamera();

	public float soundFadeDist = 1000;
	public Player[] players;
	public MultiplayerGameScreen mgs;

	public int frame = 0;
	public int[][] lastWalked;

	public ClientState(MultiplayerGameScreen mgs) {
		this.mgs = mgs;
	}

	public void resetState() {
		unitMap.clear();
		units.clear();
		bullets.clear();
		l = null;
		selected.clear();
		camera.reset();
		lastWalked = null;
		frame = 0;
	}

	public float getSoundMag(GameContainer gc, float x, float y) {
		float dx = (camera.x) - x;
		float dy = (camera.y) - y;
		float dist = (float) Math.sqrt((dx * dx) + (dy * dy));
		return Math.max((float) ((soundFadeDist - dist) / soundFadeDist), 0);
	}

	public void setWalked(float x, float y) {
		int ix = (int) (x / Level.tileSize);
		int iy = (int) (y / Level.tileSize);
		if (ix >= 0 && ix < l.width && iy >= 0 && iy < l.height) {
			lastWalked[ix][iy] = frame;
		}
	}
}
