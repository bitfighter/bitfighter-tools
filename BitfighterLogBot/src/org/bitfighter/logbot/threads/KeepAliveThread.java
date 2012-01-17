package org.bitfighter.logbot.threads;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.jibble.pircbot.PircBot;

/**
 * @author raptor
 *
 * Class to keep the logbot alive.
 * 
 * This is a thread that is spawned to send a PING request to the IRC server
 * once a minute thereby keeping the connection alive and staving off those pesky 
 * 'Ping timeout' problems
 */
public class KeepAliveThread extends Thread {

	private PircBot bot;
	private boolean loop = true;
	
	private static final SimpleDateFormat FORMAT = new SimpleDateFormat("hhmmss");
	private static final Calendar CALENDAR = Calendar.getInstance();
	
	private static final long PING_DELAY = 60000l;
	
	
	public KeepAliveThread(PircBot bot) {
		this.bot = bot;
	}
	
	public void run() {
		while(loop) {
			bot.sendRawLine("PING LAG" + FORMAT.format(CALENDAR.getTime()));
			
			try {
				Thread.sleep(PING_DELAY);
			} catch (InterruptedException e) {
				e.printStackTrace();
				continue;
			}
		}
	}

	public void finish() {
		loop = false;
	}
}
