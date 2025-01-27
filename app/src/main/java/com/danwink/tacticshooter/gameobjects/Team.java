package com.danwink.tacticshooter.gameobjects;

import com.danwink.tacticshooter.TacticServer;
import com.danwink.tacticshooter.dal.DAL.DALColor;

public class Team {
	public static Team a = new Team(0);
	public static Team b = new Team(1);

	public static final DALColor[] teamColors = {
			DALColor.yellow,
			new DALColor(1.f, 0, 1f, 1.f),
			DALColor.blue,
			DALColor.orange
	};

	public int id;

	public int cash = 1000;

	public Team() {

	}

	public Team(int id) {
		this.id = id;
	}

	public void update(TacticServer ts) {

	}

	public boolean equals(Object o) {
		if (!(o instanceof Team))
			return false;
		return this.id == ((Team) o).id;
	}

	public int hashCode() {
		return this.id;
	}

	public DALColor getColor() {
		return teamColors[id];
	}

	public String toString() {
		return "Team " + (id == 0 ? "a" : "b");
	}
}
