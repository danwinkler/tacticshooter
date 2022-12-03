setupDefaultUnits();
setupDefaultButtons();

function pointCaptureTick(frame) {
	if (frame % 10 == 0) {

		//Give player unit if they have no unit
		for (var i in players) {
			var p = players[i];
			var hasUnit = false;
			for (var j in units) {
				var u = units[j];
				if (api.getUnitPlayer(u) == p) {
					hasUnit = true;
					break;
				}
			}

			if (!hasUnit) {
				api.createUnit(p, "LIGHT", api.getBaseX(p), api.getBaseY(p));
			}
		}

	}

	if (frame % 100 == 0) {

		//Give players money based on points owned
		for (var pi in players) {
			var p = players[pi];
			var bc = 0;
			for (bi in buildings) {
				var b = buildings[bi];
				if (api.getPlayerTeam(p) == api.getBuildingTeam(b)) {
					bc++;
				}
			}
			api.addPlayerMoney(p, bc);
		}

		//Check to see if game should be over
		var winTeam = -1;
		var won = true;
		for (var i in buildings) {
			var b = buildings[i];
			var t = api.getBuildingTeam(b);
			if (winTeam == -1) {
				winTeam = t;
			} else if (winTeam != t) {
				won = false;
				break;
			}
		}

		if (won) {
			api.endGame();
		}

	}

}
addTickListener(pointCaptureTick);
