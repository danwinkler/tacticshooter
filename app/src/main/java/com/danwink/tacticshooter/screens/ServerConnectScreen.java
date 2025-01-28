package com.danwink.tacticshooter.screens;

import java.io.IOException;

import com.danwink.tacticshooter.MessageType;
import com.danwink.tacticshooter.TacticServer.ServerState;
import com.danwink.tacticshooter.dal.DAL;
import com.danwink.tacticshooter.network.ClientNetworkInterface;
import com.danwink.tacticshooter.network.Message;
import com.phyloa.dlib.game.DScreen;
import com.phyloa.dlib.game.DScreenHandler;

public class ServerConnectScreen extends DScreen<DAL> {
	String address;
	ClientNetworkInterface ci;

	public void onActivate(DAL dal, DScreenHandler<DAL> dsh) {
		try {
			ci = new ClientNetworkInterface(address);
		} catch (IOException e) {
			e.printStackTrace();
		}

		ci.sendToServer(new Message(MessageType.CONNECTED, null));
	}

	public void update(DAL gc, float delta) {
		while (ci.hasClientMessages()) {
			Message m = ci.getNextClientMessage();
			if (m.messageType == MessageType.SERVERSTATE) {
				ServerState s = (ServerState) m.message;
				switch (s) {
					case LOBBY:
						dsh.message("lobby", ci);
						dsh.activate("lobby", gc);
						return;
					case PLAYING:
						dsh.message("message", "Cannot join game in progress.");
						dsh.activate("message", gc);
						return;
				}
			}
		}
	}

	public void render(DAL dal) {

	}

	public void onExit() {

	}

	public void message(Object o) {
		if (o instanceof String) {
			address = (String) o;
		}
	}

	public void onResize(int width, int height) {
	}
}
