package com.danwink.tacticshooter.gameobjects;

import com.danwink.tacticshooter.ComputerPlayer;

public class Player {
	public static final int MAX_RESPAWN = 6;

	public int id;
	public Team team;
	public int money;
	public int respawn = 0;
	public boolean isBot = false;
	public ComputerPlayer.PlayType playType = ComputerPlayer.PlayType.DEFAULT;
	public String name;
	public int slot;
	public boolean spectator;

	public Player() {
		reset();
	}

	public Player(int id) {
		this();
		this.id = id;
	}

	public void reset() {
		money = 0;
	}
}
