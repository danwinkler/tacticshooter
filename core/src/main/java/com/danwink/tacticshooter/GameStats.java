package com.danwink.tacticshooter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Objects;
import java.util.stream.Stream;

import com.danwink.tacticshooter.gameobjects.Player;
import com.danwink.tacticshooter.gameobjects.Team;

public class GameStats {
	public TeamStats[] teamStats;
	public PlayerStats[] playerStats;
	public int totalPoints;

	public static class TeamStats {
		public ArrayList<Integer> pointCount = new ArrayList<Integer>();
		public ArrayList<Integer> unitCount = new ArrayList<Integer>();
		public int bulletsShot;
		public int unitsCreated;
		public int unitsLost;
		public int pointsTaken;
		public int moneyEarned;
		public Team t;

		public TeamStats(Team t) {
			this.t = t;
		}

		public TeamStats() {

		}
	}

	public static class PlayerStats {
		public String playerName;
		public int kills;
		public int unitsLost;
		public int unitsCreated;
		public int bulletsShot;

		public PlayerStats(String playerName) {
			this.playerName = playerName;
		}

		public PlayerStats() {

		}
	}

	public TeamStats get(Team t) {
		return teamStats[t.id];
	}

	public PlayerStats getPlayerStats(Player player) {
		if (playerStats[player.slot] == null) {
			playerStats[player.slot] = new PlayerStats(player.name);
		}
		return playerStats[player.slot];
	}

	public void setup(Team... teams) {
		teamStats = new TeamStats[teams.length];
		playerStats = new PlayerStats[16];
		for (int i = 0; i < teams.length; i++) {
			Team t = teams[i];
			teamStats[t.id] = new TeamStats(t);
		}
	}

	public PlayerStats getMostKills() {
		return playerStream().max(Comparator.comparing(p -> p.kills)).get();
	}

	public PlayerStats getMostUnitsLost() {
		return playerStream().max(Comparator.comparing(p -> p.unitsLost)).get();
	}

	public PlayerStats getMostUnitsCreated() {
		return playerStream().max(Comparator.comparing(p -> p.unitsCreated)).get();
	}

	public PlayerStats getLeastUnitsLost() {
		return playerStream().min(Comparator.comparing(p -> p.unitsLost)).get();
	}

	public PlayerStats getMostBulletsShot() {
		return playerStream().max(Comparator.comparing(p -> p.bulletsShot)).get();
	}

	public Stream<PlayerStats> playerStream() {
		return Arrays.stream(playerStats).filter(Objects::nonNull);
	}
}
