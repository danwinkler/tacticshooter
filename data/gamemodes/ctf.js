setupDefaultUnits();
setupDefaultButtons();

var points = [0, 0];
var flaggedUnit = -1;
var flag = -1;

var base0 = api.getBaseForTeam(0);
var base1 = api.getBaseForTeam(1);

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
        //Give players money - fixed income
        for (var pi in players) {
            var p = players[pi];
            api.addPlayerMoney(p, 10);
        }

        // Create flag if doesn't exist
        if (flag == -1 && flaggedUnit == -1) {
            var spawn = findFlagSpawn();
            flag = api.createMarker(spawn.x, spawn.y);
            api.sendMessage("New flag spawned!");
        }
    }
}
addTickListener(pointCaptureTick);

function findFlagSpawn() {
    var base0X = api.getBuildingX(base0);
    var base0Y = api.getBuildingY(base0);
    var base1X = api.getBuildingX(base1);
    var base1Y = api.getBuildingY(base1);

    var bestBuildingX = -1;
    var bestBuildingY = -1;
    var bestBuildingScore = -1;
    for (var i in buildings) {
        var b = buildings[i];
        var bx = api.getBuildingX(b);
        var by = api.getBuildingY(b);

        // If bx,by is == to base0X,base0Y or base1X,base1Y, skip
        if ((bx == base0X && by == base0Y) || (bx == base1X && by == base1Y)) {
            continue;
        }

        var score0 = api.getPathLength(base0X / tileSize, base0Y / tileSize, bx / tileSize, by / tileSize);
        var score1 = api.getPathLength(base1X / tileSize, base1Y / tileSize, bx / tileSize, by / tileSize);
        var score = score0 + score1;

        if (score > bestBuildingScore) {
            bestBuildingScore = score;
            bestBuildingX = bx;
            bestBuildingY = by;
        }
    }

    return {
        x: bestBuildingX,
        y: bestBuildingY
    };
}

addTouchMarkerListener(function (u, m) {
    flaggedUnit = u;
    flag = -1;
    api.deleteMarker(m);
    api.setUnitMarked(u, true);
});

addKillListener(function (u) {
    if (u.killer_owner >= 0) {
        api.addPlayerMoney(u.killer_owner, 2);
    }

    // If the unit is carrying a flag, drop it
    if (u.unit == flaggedUnit) {
        flaggedUnit = -1;
        flag = api.createMarker(u.x, u.y);
        api.sendMessage("Flag dropped!");
    }
});

function makeOnStopCallback(team, base) {
    function onStop(b, u) {
        if (u == flaggedUnit && b == base) {
            flaggedUnit = -1;
            api.setUnitMarked(u, false);
            points[team] += 1;
            api.sendMessage("Team " + team + " captured the flag!");
            api.sendMessage("Team 0: " + points[0] + " points");
            api.sendMessage("Team 1: " + points[1] + " points");
            if (points[team] >= 3) {
                api.sendMessage("Team " + team + " won!");
                api.endGame();
            }
        }
    }

    return onStop;
}

addStopListener(base0, makeOnStopCallback(0, base0));
addStopListener(base1, makeOnStopCallback(1, base1));
