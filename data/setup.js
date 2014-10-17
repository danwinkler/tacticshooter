var tickListeners = [];
var killListeners = [];

var stepListeners = {};
var stopListeners = {};
var buttonListeners = {};

function addTickListener( f ) {
	tickListeners.push( f );
}

function callTick( frame ) {
	for( var i in tickListeners ) {
		tickListeners[i]( frame );
	}
}

function addKillListener( f ) {
	killListeners.push( f );
}

function callKill( u ) {
	for( var i in killListeners ) {
		killListeners[i]( u );
	}
}

function addStepListener( building, f ) {
	if( !(building in stepListeners) ) {
		stepListeners[building] = [];
	}
	stepListeners[building].push( f );
}

function callStep( building, unit ) {
	if( building in stepListeners ) {
		var sl = stepListeners[building];
		for( i in sl ) {
			sl[i]( building, unit );
		}
	}
}

function addStopListener( building, f ) {
	if( !(building in stopListeners) ) {
		stopListeners[building] = [];
	}
	stopListeners[building].push( f );
}

function callStop( building, unit ) {
	if( building in stopListeners ) {
		var sl = stopListeners[building];
		for( i in sl ) {
			sl[i]( building, unit );
		}
	}
}

function callButton( id, player ) {
	if( id in buttonListeners ) {
		buttonListeners[id]( player );
	}
}

function setupDefaultButtons() {
	var unitTypes = [
		'LIGHT',
		'HEAVY',
		'SHOTGUN',
		'SCOUT',
		'SNIPER',
		'SABOTEUR'
	];
	var prices = {
		'LIGHT': 10,
		'HEAVY': 20,
		'SHOTGUN': 15,
		'SCOUT': 3,
		'SNIPER': 15,
		'SABOTEUR': 20
	};
	for( var i = 0; i < unitTypes.length; i++ ) {
		var unit = unitTypes[i];
		addButton( "BUILD " + unit + "\n" + prices[unit], i*2, 0, 2, 1,
			//This callback trickery is because javascript is a cunt
			function( unit ) {
				function callback( player ) {
					if( api.getPlayerMoney( player ) >= prices[unit] ) {
						api.createUnit( player, unit, api.getBaseX( player ), api.getBaseY( player ) );
						api.addPlayerMoney( player, -prices[unit] );
					}
				}
				return callback;
			}( unit )
		);
	}
}

function addButton( text, x, y, width, height, callback ) {
	var id = api.addButton( text, x, y, width, height );
	buttonListeners[id] = callback;
}
