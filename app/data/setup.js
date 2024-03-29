var tickListeners = [];
var killListeners = [];
var touchMarkerListeners = [];

var stepListeners = {};
var stopListeners = {};
var buttonListeners = {};


function addTickListener(f) {
	tickListeners.push(f);
}

function callTick(frame) {
	for (var i in tickListeners) {
		tickListeners[i](frame);
	}
}

function addKillListener(f) {
	killListeners.push(f);
}

function callKill(u) {
	for (var i in killListeners) {
		killListeners[i](u);
	}
}

function addStepListener(building, f) {
	if (!(building in stepListeners)) {
		stepListeners[building] = [];
	}
	stepListeners[building].push(f);
}

function callStep(building, unit) {
	if (building in stepListeners) {
		var sl = stepListeners[building];
		for (i in sl) {
			sl[i](building, unit);
		}
	}
}

function addStopListener(building, f) {
	if (!(building in stopListeners)) {
		stopListeners[building] = [];
	}
	stopListeners[building].push(f);
}

function callStop(building, unit) {
	if (building in stopListeners) {
		var sl = stopListeners[building];
		for (i in sl) {
			sl[i](building, unit);
		}
	}
}

function addTouchMarkerListener(f) {
	touchMarkerListeners.push(f);
}

function callTouchMarker(unit_id, marker_id) {
	for (var i in touchMarkerListeners) {
		touchMarkerListeners[i](unit_id, marker_id);
	}
}

function callButton(id, player, shiftPressed) {
	if (id in buttonListeners) {
		buttonListeners[id](player, shiftPressed);
	}
}

var defaultUnitTypes = {
	"LIGHT": { name: "LIGHT", speed: 3, timeBetweenBullets: 10, bulletSpread: .05, price: 10, health: 100, bulletsAtOnce: 1, damage: 10, explodesOnDeath: false },
	"HEAVY": { name: "HEAVY", speed: 1.5, timeBetweenBullets: 3, bulletSpread: .1, price: 20, health: 200, bulletsAtOnce: 1, damage: 10, explodesOnDeath: false },
	"SHOTGUN": { name: "SHOTGUN", speed: 3, timeBetweenBullets: 30, bulletSpread: .3, price: 15, health: 150, bulletsAtOnce: 6, damage: 10, explodesOnDeath: false },
	"SCOUT": { name: "SCOUT", speed: 6, timeBetweenBullets: 30, bulletSpread: .1, price: 3, health: 30, bulletsAtOnce: 1, damage: 10, explodesOnDeath: false },
	"SNIPER": { name: "SNIPER", speed: 2.5, timeBetweenBullets: 100, bulletSpread: 0, price: 15, health: 90, bulletsAtOnce: 1, damage: 100, explodesOnDeath: false },
	"SABOTEUR": { name: "SABOTEUR", speed: 4, timeBetweenBullets: 10000, bulletSpread: 0, price: 20, health: 150, bulletsAtOnce: 0, damage: 0, explodesOnDeath: true }
};

function setupDefaultButtons() {
	function makeButton(unit, x, y, image) {
		addButton(unit.name + "\n" + unit.price, x, y, image,
			function (unit) {
				function callback(player, shiftPressed) {
					var nTimes = shiftPressed ? 5 : 1;
					for (var i = 0; i < nTimes; i++) {
						if (api.getPlayerMoney(player) >= unit.price) {
							api.createUnit(player, unit.name, api.getBaseX(player), api.getBaseY(player));
							api.addPlayerMoney(player, -unit.price);
						}
					}
				}
				return callback;
			}(unit)
		);
	}

	makeButton(defaultUnitTypes.LIGHT, 0, 0, "LIGHT");
	makeButton(defaultUnitTypes.SCOUT, 1, 0, "SCOUT");
	makeButton(defaultUnitTypes.HEAVY, 0, 1, "HEAVY");
	makeButton(defaultUnitTypes.SHOTGUN, 1, 1, "SHOTGUN");
	makeButton(defaultUnitTypes.SNIPER, 0, 2, "SNIPER");
	makeButton(defaultUnitTypes.SABOTEUR, 1, 2, "SABOTEUR");
}

function setupDefaultUnits() {
	for (var unitTypeName in defaultUnitTypes) {
		var unit = defaultUnitTypes[unitTypeName];
		api.createUnitDef(
			unit.name,
			unit.speed,
			unit.timeBetweenBullets,
			unit.bulletSpread,
			unit.price,
			unit.health,
			unit.bulletsAtOnce,
			unit.damage,
			unit.explodesOnDeath
		)
	}
}

function addButton(text, x, y, image, callback) {
	var id = api.addButton(text, x, y, image);
	buttonListeners[id] = callback;
}
