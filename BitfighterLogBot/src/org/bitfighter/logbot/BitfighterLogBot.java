package org.bitfighter.logbot;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Properties;
import java.util.SimpleTimeZone;
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

	// Color classes
	// Take a look at the CSS in the header for the real colors
	private static final String A = "a"; // green
	private static final String B = "b"; // black
	private static final String C = "c"; // brown
	private static final String D = "d"; // navy
	private static final String E = "e"; // brick
	private static final String F = "f"; // red
	
	private static final String COMMANDS_FILENAME = "./commands.ini";

	private static Calendar CALENDAR = Calendar.getInstance(new SimpleTimeZone(0, "GMT"));
	private static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
	private static SimpleDateFormat timeFormat = new SimpleDateFormat("H:mm:ss");

	private static KeepAliveThread keepAliveThread = null;
	private static FeedNotifierThread feedReaderThread = null;

	private static Properties botCommands;
	private static String commandList;
	
	private static BotConfig config;

	public BitfighterLogBot(BotConfig botConfig) {
		config = botConfig;
		setName(config.getNick());
		setLogin(config.getNick());
		
		dateFormat.setCalendar(CALENDAR);
		timeFormat.setCalendar(CALENDAR);
		
		populateResponses();
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
			CALENDAR.setTimeInMillis(System.currentTimeMillis());  // re-sync calendar
			String date = dateFormat.format(CALENDAR.getTime());
			String time = timeFormat.format(CALENDAR.getTime());
			File file = new File(config.getOutputDirectory(), date + ".log");
			BufferedWriter writer = new BufferedWriter(new FileWriter(file, true));
			String entry = "[" + time + "] <span class=\"" + color + "\">" + line + "</span>";
			writer.write(entry);
			writer.newLine();
			writer.flush();
			writer.close();
		}
		catch (IOException e) {
			System.out.println("Could not write to log:");
			e.printStackTrace();
		}
	}

	public void onAction(String sender, String login, String hostname, String target, String action) {
		append(E, "* " + sender + " " + action);
	}

	public void onJoin(String channel, String sender, String login, String hostname) {
		append(A, "* " + sender + " (" + login + "@" + hostname + ") has joined " + channel);
		if (sender.equals(getNick())) {
			sendNotice(channel, config.getJoinMessage());
		}
		else {
			sendNotice(sender, config.getJoinMessage());
		}
	}

	public void onMessage(String channel, String sender, String login, String hostname, String message) {
		append(B, "<" + sender + "> " + message);

		message = message.toLowerCase();
		
		if (message.equals(getNick().toLowerCase())) {
			sendAndLogMessage(channel, "I'm a real boy.... I think?");
			return;
		}
		
		if (message.equals("!log")) {
			sendAndLogMessage(channel, config.getJoinMessage());
			return;
		}
		
		if (message.substring(0, 1).equals("!") && botCommands.size() > 0) {
			if (message.substring(1).equals("commands")) {
				sendAndLogMessage(channel, "Commands: " + commandList);
				return;
			}

			String botResponse = botCommands.getProperty(message.substring(1));

			if (botResponse != null)
				sendAndLogMessage(channel, botResponse);
		}
	}
	
	public void sendAndLogMessage(String target, String message) {
		sendMessage(target, message);
		append(B, "<" + getNick() + "> " + message);
	}

	public void onMode(String channel, String sourceNick, String sourceLogin, String sourceHostname, String mode) {
		append(A, "* " + sourceNick + " sets mode " + mode);
	}

	public void onNickChange(String oldNick, String login, String hostname, String newNick) {
		append(A, "* " + oldNick + " is now known as " + newNick);
	}

	public void onNotice(String sourceNick, String sourceLogin, String sourceHostname, String target, String notice) {
		append(C, "-" + sourceNick + "- " + notice);
	}

	public void onPart(String channel, String sender, String login, String hostname) {
		append(A, "* " + sender + " (" + login + "@" + hostname + ") has left " + channel);
	}

	public void onPing(String sourceNick, String sourceLogin, String sourceHostname, String target, String pingValue) {
		append(F, "[" + sourceNick + " PING]");
		super.onPing(sourceNick, sourceLogin, sourceHostname, target, pingValue);
	}

	public void onPrivateMessage(String sender, String login, String hostname, String message) {
		append(B, "<- *" + sender + "* " + message);
	}

	public void onQuit(String sourceNick, String sourceLogin, String sourceHostname, String reason) {
		append(D, "* " + sourceNick + " (" + sourceLogin + "@" + sourceHostname + ") Quit (" + reason + ")");
	}

	public void onTime(String sourceNick, String sourceLogin, String sourceHostname, String target) {
		append(F, "[" + sourceNick + " TIME]");
	}

	public void onTopic(String channel, String topic, String setBy, long date, boolean changed) {
		if (changed) {
			append(A, "* " + setBy + " changes topic to '" + topic + "'");
		}
		else {
			append(A, "* Topic is '" + topic + "'");
			append(A, "* Set by " + setBy + " on " + new Date(date));
		}
	}

	public void onVersion(String sourceNick, String sourceLogin, String sourceHostname, String target) {
		append(F, "[" + sourceNick + " VERSION]");
	}

	public void onKick(String channel, String kickerNick, String kickerLogin, String kickerHostname, String recipientNick, String reason) {
		append(A, "* " + recipientNick + " was kicked from " + channel + " by " + kickerNick);
		if (recipientNick.equalsIgnoreCase(getNick())) {
			joinChannel(channel);
		}
	}

	public void onConnect() {
		startThreads();
	}

	public void onDisconnect() {
		append(D, "* Disconnected.");

		stopThreads();

		while (!isConnected()) {
			try {
				connect(config.getServer());
				joinChannel(config.getChannel());
			}
			catch (Exception e) {
				e.printStackTrace();
				try {
					Thread.sleep(10000);
				}
				catch (Exception ee) {
					ee.printStackTrace();
				}
			}
		}
	}
	
	private void startThreads() {

		if (keepAliveThread == null)
			keepAliveThread = new KeepAliveThread(this);

		keepAliveThread.start();
		
		if (config.hasFeed()) {
			if (feedReaderThread == null)
				feedReaderThread = new FeedNotifierThread(this, config.getChannel(), config.getFeedUrlString());
	
			feedReaderThread.start();
		}
	}

	
	private void stopThreads() {

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
	}
}