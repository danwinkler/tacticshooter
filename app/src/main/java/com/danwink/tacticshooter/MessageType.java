package com.danwink.tacticshooter;

public enum MessageType {
	UNITUPDATE,
	LEVELUPDATE,
	BUILDINGUPDATE,
	BULLETUPDATE,
	CLIENTJOIN,
	SETATTACKPOINT,
	MOVESUCCESS,
	DISCONNECTED,
	CONNECTED,
	PLAYERUPDATE,
	SWITCHTEAMS,
	BUILDUNIT,
	PLAYERLIST,
	GAMEOVER,
	MESSAGE,
	TILEUPDATE,
	PINGMAP,
	SETATTACKPOINTCONTINUE,
	SERVERSTATE,
	KICK,
	SETBOT,
	SETPLAYTYPE,
	STARTGAME,
	LOOKTOWARD,
	TAKECONTROL,
	RELEASECONTROL,
	LOBBYLEVELINFO,
	FOGUPDATE,
	UNITMINIUPDATE,
	GAMETYPE,
	CREATEBUTTON,
	BUTTONPRESS,
	SETSPECTATOR,
	// Sent by the server to all clients at the beginning of the game to inform the
	// clients of the available unit definitions
	// Type: UnitDef[]
	UNITDEFS,
	// Sent by the server when a marker is created
	// Type: Marker
	MARKERCREATE,
	// Sent by the server when a marker is deleted
	// Type: int (id)
	MARKERDELETE;
}
