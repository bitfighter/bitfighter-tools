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
	private int listenerPort;
	
	private String commitRecordFile;
	private String outputDirectory;

	public BotConfig() throws Exception {
        Properties properties = new Properties();
        properties.load(new FileInputStream(new File("./config.ini")));
        
        server = properties.getProperty("Server", "irc.freenode.net");
        channel = properties.getProperty("Channel", "#bottest");
        nick = properties.getProperty("Nick", "LogBot2");
        joinMessage = properties.getProperty("JoinMessage", "This channel is logged.");
        commitRecordFile = properties.getProperty("CommitRecordFile", "./webhooks/commit_record.txt");
        outputDirectory = properties.getProperty("OutputDir", "./output/");
        listenerPort = Integer.parseInt(properties.getProperty("ListenerPort", "25959"));

        String debugOptionString = properties.getProperty("Debug", "false");

        debug = debugOptionString.equalsIgnoreCase("true") ? true : false;
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

	public String getOutputDirectory() {
		return outputDirectory;
	}

	public String getCommitRecordFile() {
		return commitRecordFile;
	}

	public int getListenerPort() {
		return listenerPort;
	}
	
}
