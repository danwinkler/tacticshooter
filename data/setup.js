var tickListeners = [];
var killListeners = [];

var stepListeners = {};
var stopListeners = {};

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

function setupDefaultButtons( building, unit ) {
	
}