package com.deaded.pbdemo;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;

import org.jibble.pircbot.PircBot;

/**
 * Simple PircBot bot to demonstrate how to create a Timed message.
 * 
 * This example uses the Timer and TimerTask classes.
 * 
 * Run the bot and wait to see the timed messages appear.
 * 
 * NOTE: this requires pircbot.jar from www.jibble.org
 * 
 * @author DeadEd ( http://www.deaded.com ) @ 2006
 */
public class TimedMessageBot extends PircBot {
	
	Timer timer;
	
    public TimedMessageBot() {
        TimedMessageTask tmt1 = new TimedMessageTask("I am a timed message.  I repeat every minute");
        TimedMessageTask tmt2 = new TimedMessageTask("I am a timed message.  I repeat every 5 mins");
        TimedMessageTask tmt3 = new TimedMessageTask("I am another timed message.  I repeat every hour");

        timer = new Timer();
        timer.schedule(tmt1, 0, 60 * 1000);  // 1 min
        timer.schedule(tmt2, 0, 5 * 60 * 1000);  // 5 mins
        timer.schedule(tmt3, 0, 60 * 60 * 1000);  // 1 hour
    }
    
    public static void main(String[] args) throws Exception {
		Properties config = new Properties();
		try {
			config.load(new FileInputStream("pbdemo.properties"));
		} catch (IOException ioex) {
			System.err.println("Error loading config file: pbdemo.properties");
			System.exit(0);
		}
        TimedMessageBot bot = new TimedMessageBot();
		bot.setAutoNickChange(true);
        bot.setName(config.getProperty("nick", "pbdemo"));
        bot.setVerbose(true);
        bot.connect(config.getProperty("server", "irc.quakenet.org"));
        bot.joinChannel(config.getProperty("channel", "#deaded"));
    }

    class TimedMessageTask extends TimerTask {
        private String message = "";
        
        public TimedMessageTask(String message) {
            this.message = message;
        }
        
        public void run() {
        	String[] channels = getChannels();
        	for(int x=0; x < channels.length; x++) {
        		sendMessage(channels[x], message);
        	}
        }
    }

}
