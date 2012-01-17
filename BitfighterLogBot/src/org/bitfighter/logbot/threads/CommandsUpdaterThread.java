package org.bitfighter.logbot.threads;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Properties;

/**
 * @author raptor
 *
 * Class to update the commands every so often so stopping/starting the logbot isn't necessary
 * 
 */
public class CommandsUpdaterThread extends Thread {
	
	private static final String COMMANDS_FILENAME = "./commands.ini";
	
	private boolean loop = true;
	private static final long SCAN_DELAY = 300000l;

	public static Properties BotCommands;
	public static String CommandList;

	public CommandsUpdaterThread() {
		BotCommands = new Properties();
		CommandList = "";
	}
	
	public void run() {
		while(loop) {
			loadCommands();
			
			try {
				Thread.sleep(SCAN_DELAY);
			} catch (InterruptedException e) {
				e.printStackTrace();
				continue;
			}
		}
	}

	
	private void loadCommands() {
		Properties botCommands = null;
		try {
			botCommands = new Properties();
			botCommands.load(new FileInputStream(new File(COMMANDS_FILENAME)));
		} catch (FileNotFoundException e) {
			System.out.println(COMMANDS_FILENAME + " not found");
			e.printStackTrace();
			return;
		} catch (IOException e) {
			System.out.println("Problem reading input file" + COMMANDS_FILENAME);
			e.printStackTrace();
			return;
		}

		BotCommands.clear();
		BotCommands = botCommands;

		// We made it!
		ArrayList<String> commandArray = new ArrayList<String>(BotCommands.stringPropertyNames());
		commandArray.add("log");

		Collections.sort(commandArray);

		StringBuilder stringBuilder = new StringBuilder();
		for (String command: commandArray) {
			stringBuilder.append(command);
			stringBuilder.append(" ");
		}

		CommandList = stringBuilder.toString();
	}
	

	public void finish() {
		loop = false;
	}
	

	public static Properties getBotCommands() {
		return BotCommands;
	}

	public static String getCommandList() {
		return CommandList;
	}
}
