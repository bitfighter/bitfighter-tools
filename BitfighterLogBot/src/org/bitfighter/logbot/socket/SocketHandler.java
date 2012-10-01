package org.bitfighter.logbot.socket;

import java.net.Socket;

import org.jibble.pircbot.PircBot;

public abstract class SocketHandler {
	public abstract void run(PircBot bot, Socket socket);
}
