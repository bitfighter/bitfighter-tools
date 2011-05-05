package org.bitfighter.logbot;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jibble.pircbot.Colors;
import org.jibble.pircbot.PircBot;

/**
 * BitfighterLogBot
 * 
 * Uses the excellent IRC bot framework PircBot from:
 * http://www.jibble.org/pircbot.php
 * 
 * Modified LogBot from:
 * http://www.jibble.org/logbot/
 * 
 * TODO:
 *  - add mysql/sqlite integration if wanted
 * 
 * @author raptor
 *
 */
public class BitfighterLogBot extends PircBot {

	private static final Pattern urlPattern = Pattern.compile("(?i:\\b((http|https|ftp|irc)://[^\\s]+))");
	private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
	private static final SimpleDateFormat TIME_FORMAT = new SimpleDateFormat("H:mm");

	private static final String GREEN = "irc-green";
	private static final String BLACK = "irc-black";
	private static final String BROWN = "irc-brown";
	private static final String NAVY = "irc-navy";
	private static final String BRICK = "irc-brick";
	private static final String RED = "irc-red";
	
	private static final String COMMANDS_FILENAME = "./commands.ini";

	private static final String feed = "http://code.google.com/feeds/p/bitfighter/hgchanges/basic";

	private String server;
	private String channel;
	private File outDir;
	private String joinMessage;

	private static KeepAliveThread keepAliveThread = null;
	private static FeedReaderThread feedReaderThread = null;

	private static Properties botCommands;
	private static String commandList;

	public BitfighterLogBot(String server, String channel, String name, File outDir, String joinMessage) {
		setName(name);
		setLogin(name);
		
		populateResponses();

		this.server = server;
		this.channel = channel;
		this.outDir = outDir;
		this.joinMessage = joinMessage;
	}

	private void populateResponses() {
        try {
        	botCommands = new Properties();
        	botCommands.load(new FileInputStream(new File(COMMANDS_FILENAME)));
		} catch (FileNotFoundException e) {
			System.out.println(COMMANDS_FILENAME + " not found");
			e.printStackTrace();
		} catch (IOException e) {
			System.out.println("Problem reading input file" + COMMANDS_FILENAME);
			e.printStackTrace();
		}
		
		ArrayList<String> commandArray = new ArrayList<String>(botCommands.stringPropertyNames());
		commandArray.add("log");
		
		Collections.sort(commandArray);
		
		StringBuilder stringBuilder = new StringBuilder();
		for (String command: commandArray) {
			stringBuilder.append(command);
			stringBuilder.append(" ");
		}
		
		commandList = stringBuilder.toString();
	}

	public void append(String color, String line) {
		line = Colors.removeFormattingAndColors(line);

		line = line.replaceAll("&", "&amp;");
		line = line.replaceAll("<", "&lt;");
		line = line.replaceAll(">", "&gt;");

		Matcher matcher = urlPattern.matcher(line);
		line = matcher.replaceAll("<a href=\"$1\">$1</a>");


		try {
			Date now = new Date();
			String date = DATE_FORMAT.format(now);
			String time = TIME_FORMAT.format(now);
			File file = new File(outDir, date + ".log");
			BufferedWriter writer = new BufferedWriter(new FileWriter(file, true));
			String entry = "<span class=\"irc-date\">[" + time + "]</span> <span class=\"" + color + "\">" + line + "</span><br />";
			writer.write(entry);
			writer.newLine();
			writer.flush();
			writer.close();
		}
		catch (IOException e) {
			System.out.println("Could not write to log: " + e);
		}
	}

	public void onAction(String sender, String login, String hostname, String target, String action) {
		append(BRICK, "* " + sender + " " + action);
	}

	public void onJoin(String channel, String sender, String login, String hostname) {
		append(GREEN, "* " + sender + " (" + login + "@" + hostname + ") has joined " + channel);
		if (sender.equals(getNick())) {
			sendNotice(channel, joinMessage);
		}
		else {
			sendNotice(sender, joinMessage);
		}
	}

	public void onMessage(String channel, String sender, String login, String hostname, String message) {
		append(BLACK, "<" + sender + "> " + message);

		message = message.toLowerCase();
		
		if (message.equals(getNick().toLowerCase())) {
			sendMessage(channel, "I'm a real boy.... I think?");
			return;
		}
		
		if (message.equals("!log")) {
			sendMessage(channel, joinMessage);
			return;
		}
		
		if (message.substring(0, 1).equals("!") && botCommands.size() > 0) {
			if (message.substring(1).equals("commands")) {
				sendMessage(channel, "Commands: " + commandList);
				return;
			}

			String botResponse = botCommands.getProperty(message.substring(1));

			if (botResponse != null)
				sendMessage(channel, botResponse);
		}
	}

	public void onMode(String channel, String sourceNick, String sourceLogin, String sourceHostname, String mode) {
		append(GREEN, "* " + sourceNick + " sets mode " + mode);
	}

	public void onNickChange(String oldNick, String login, String hostname, String newNick) {
		append(GREEN, "* " + oldNick + " is now known as " + newNick);
	}

	public void onNotice(String sourceNick, String sourceLogin, String sourceHostname, String target, String notice) {
		append(BROWN, "-" + sourceNick + "- " + notice);
	}

	public void onPart(String channel, String sender, String login, String hostname) {
		append(GREEN, "* " + sender + " (" + login + "@" + hostname + ") has left " + channel);
	}

	public void onPing(String sourceNick, String sourceLogin, String sourceHostname, String target, String pingValue) {
		append(RED, "[" + sourceNick + " PING]");
		super.onPing(sourceNick, sourceLogin, sourceHostname, target, pingValue);
	}

	public void onPrivateMessage(String sender, String login, String hostname, String message) {
		append(BLACK, "<- *" + sender + "* " + message);
	}

	public void onQuit(String sourceNick, String sourceLogin, String sourceHostname, String reason) {
		append(NAVY, "* " + sourceNick + " (" + sourceLogin + "@" + sourceHostname + ") Quit (" + reason + ")");
	}

	public void onTime(String sourceNick, String sourceLogin, String sourceHostname, String target) {
		append(RED, "[" + sourceNick + " TIME]");
	}

	public void onTopic(String channel, String topic, String setBy, long date, boolean changed) {
		if (changed) {
			append(GREEN, "* " + setBy + " changes topic to '" + topic + "'");
		}
		else {
			append(GREEN, "* Topic is '" + topic + "'");
			append(GREEN, "* Set by " + setBy + " on " + new Date(date));
		}
	}

	public void onVersion(String sourceNick, String sourceLogin, String sourceHostname, String target) {
		append(RED, "[" + sourceNick + " VERSION]");
	}

	public void onKick(String channel, String kickerNick, String kickerLogin, String kickerHostname, String recipientNick, String reason) {
		append(GREEN, "* " + recipientNick + " was kicked from " + channel + " by " + kickerNick);
		if (recipientNick.equalsIgnoreCase(getNick())) {
			joinChannel(channel);
		}
	}

	public void onConnect() {
		if (keepAliveThread == null)
			keepAliveThread = new KeepAliveThread(this);

		keepAliveThread.start();
		
		if (feedReaderThread == null)
			feedReaderThread = new FeedReaderThread(this, channel, feed);

		feedReaderThread.start();
	}

	public void onDisconnect() {
		append(NAVY, "* Disconnected.");

		if (keepAliveThread != null) {
			keepAliveThread.finish();
			keepAliveThread.interrupt();
			keepAliveThread = null;
		}

		if (feedReaderThread != null) {
			feedReaderThread.finish();
			feedReaderThread.interrupt();
			feedReaderThread = null;
		}

		while (!isConnected()) {
			try {
				connect(server);
				joinChannel(channel);
			}
			catch (Exception e) {
				e.printStackTrace();
				try {
					Thread.sleep(10000);
				}
				catch (Exception anye) {
					// Do nothing.
				}
			}
		}
	}

}