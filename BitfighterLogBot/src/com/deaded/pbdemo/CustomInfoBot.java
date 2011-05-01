package com.deaded.pbdemo;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import org.jibble.pircbot.PircBot;

/**
 * Simple PircBot bot to set your own internal bot name, whois info, etc.
 * 
 * Run the bot and look at:
 * - whois info (host mask and version info)
 * - CTCP version
 * - CTCP finger
 * 
 * NOTE: this requires pircbot.jar from www.jibble.org
 * 
 * @author DeadEd ( http://www.deaded.com ) @ 2006
 */
public class CustomInfoBot extends PircBot {
	
	public CustomInfoBot() {
		setVersion("CI-Bot v1.0"); // visible to whois and CTCP version
		setFinger("Get your hand off of me!");  // visible on CTCP finger
		setLogin("CIBot");  // the user login in the host mask
	}

	public static void main(String[] args) throws Exception {
		Properties config = new Properties();
		try {
			config.load(new FileInputStream("pbdemo.properties"));
		} catch (IOException ioex) {
			System.err.println("Error loading config file: pbdemo.properties");
			System.exit(0);
		}
		CustomInfoBot bot = new CustomInfoBot();
		bot.setAutoNickChange(true);
        bot.setName(config.getProperty("nick", "pbdemo"));
        bot.setVerbose(true);
        bot.connect(config.getProperty("server", "irc.freenode.net"));
        bot.joinChannel(config.getProperty("channel", "#pircbot"));
	}
}
