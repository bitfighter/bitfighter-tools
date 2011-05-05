package org.bitfighter.logbot;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;

public class BotConfig {
	
	private String server;
	private String channel;
	private String nick;
	private String joinMessage;
	private boolean debug;
	private boolean feed;
	private String feedUrlString;
	private String outputDirectory;

	BotConfig() throws Exception {
        Properties properties = new Properties();
        properties.load(new FileInputStream(new File("./config.ini")));
        
        server = properties.getProperty("Server", "irc.freenode.net");
        channel = properties.getProperty("Channel", "#bitfighter");
        nick = properties.getProperty("Nick", "LogBot2");
        joinMessage = properties.getProperty("JoinMessage", "This channel is logged.");
        feedUrlString = properties.getProperty("AtomFeed", "");
        outputDirectory = properties.getProperty("OutputDir", "./output/");

        String debugOptionString = properties.getProperty("Debug", "false");
        String feedOptionString = properties.getProperty("IncludeFeed", "false");

        debug = debugOptionString.equalsIgnoreCase("true") ? true : false;
        feed = feedOptionString.equalsIgnoreCase("true") ? true : false;
	}

	public String getServer() {
		return server;
	}

	public String getChannel() {
		return channel;
	}

	public String getNick() {
		return nick;
	}

	public String getJoinMessage() {
		return joinMessage;
	}

	public boolean isDebug() {
		return debug;
	}

	public boolean hasFeed() {
		return feed;
	}

	public String getFeedUrlString() {
		return feedUrlString;
	}

	public String getOutputDirectory() {
		return outputDirectory;
	}
	
}
