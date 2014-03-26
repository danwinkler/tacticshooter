function tick( frame ) {

	if( frame % 100 == 0 ) {
		
		//Give players money based on points owned
		for( var pi in players ) {
			var p = players[pi];
			var bc = 0;
			for( bi in buildings ) {
				var b = buildings[pi];
				if( api.getPlayerTeam( p ) == api.getBuildingTeam( b ) ) {
					bc++;
				}
			}
			api.addPlayerMoney( p, bc );
		}
		
	}
	
}