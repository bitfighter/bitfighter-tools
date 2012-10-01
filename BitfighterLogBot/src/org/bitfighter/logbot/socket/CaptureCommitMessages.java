package org.bitfighter.logbot.socket;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.Socket;

import org.bitfighter.logbot.BitfighterLogBot;
import org.bitfighter.logbot.BotConfig;
import org.jibble.pircbot.PircBot;

public class CaptureCommitMessages extends SocketHandler {

	@Override
	public void run(PircBot bot, Socket socket) {
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

			StringBuilder stringBuilder = new StringBuilder();

		    String line;
		    while((line = reader.readLine()) != null) { 
		        stringBuilder.append(line);
		        stringBuilder.append(" ");  // New lines are now turned to spaces
		    }
		    
		    String wholeString = stringBuilder.toString();
			
			BitfighterLogBot logbot = (BitfighterLogBot) bot;
			BotConfig config = logbot.getConfig();
			
			logbot.sendAction(config.getChannel(), wholeString);
        	
        	// Now log this action, too.  'hostname' is null because getting my own 
			// hostname is not implemented yet
			logbot.onAction(bot.getNick(), bot.getLogin(), null, config.getChannel(), wholeString);
		} 
		catch (Exception e) {
			e.printStackTrace();
		}
	}

}
