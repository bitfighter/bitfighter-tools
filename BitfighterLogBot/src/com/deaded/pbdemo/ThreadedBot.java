package com.deaded.pbdemo;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import org.jibble.pircbot.PircBot;

/**
 * Simple PircBot bot to demonstrate how to run things in a Thread from the bot.
 * 
 * Run the bot and type "!start" in the channel to see the output coming from the thread.
 * Type "!stop" to tell the thread to stop it's current activity.
 * 
 * NOTE: this requires pircbot.jar from www.jibble.org
 * 
 * @author DeadEd ( http://www.deaded.com ) @ 2008
 */
public class ThreadedBot extends PircBot {

	ActionThread ationThread = null;
	
	public ThreadedBot() {
	}

	public void onMessage(String channel, String sender, String login, String hostname, String message) {
		if(message.equalsIgnoreCase("!start")) {
			if(ationThread == null) { // only allow one ActionThread at a time
				ationThread = new ActionThread(this, channel, sender);
				new Thread(ationThread).start();
			}
		} else if(message.equalsIgnoreCase("!stop")) {
			if(ationThread != null) {
				ationThread.stop();
				ationThread = null;
			}
		}
	}
	
	public static void main(String[] args) throws Exception {
		Properties config = new Properties();
		try {
			config.load(new FileInputStream("pbdemo.properties"));
		} catch (IOException ioex) {
			System.err.println("Error loading config file: pbdemo.properties");
			System.exit(0);
		}
		ThreadedBot bot = new ThreadedBot();
		bot.setAutoNickChange(true);
        bot.setName(config.getProperty("nick", "pbdemo"));
        bot.setVerbose(true);
        bot.connect(config.getProperty("server", "irc.freenode.net"));
        bot.joinChannel(config.getProperty("channel", "#pircbot"));
	}

}
