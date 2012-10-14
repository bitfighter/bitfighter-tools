package org.bitfighter.logbot.socket;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.ArrayList;

import org.bitfighter.logbot.BitfighterLogBot;
import org.bitfighter.logbot.BotConfig;
import org.jibble.pircbot.PircBot;

public class CaptureCommitMessages extends SocketHandler {
	
	private static final long COMMIT_POST_DELAY = 1500;  // 1.5 seconds to prevent flood prevention

	private static final String COMMIT_BREAK = "BREAK";
	private static final String NEWLINE_REPLACEMENT = " ";
	
	@Override
	public void run(PircBot bot, Socket socket) {
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

			StringBuilder stringBuilder = new StringBuilder();
		    ArrayList<String> commitList = new ArrayList<String>();

		    String line;
		    while((line = reader.readLine()) != null) {
		    	
		    	// New commit has arrived in the stream
		    	if(line.equals(COMMIT_BREAK)) {
		    		// Add the last commit to the list
		    		commitList.add(stringBuilder.toString());
		    		
		    		// Clear and continue
		    		stringBuilder = new StringBuilder();
		    		continue;
		    	}
		    	
		        stringBuilder.append(line);
		        // Newlines are replaced so IRC doesn't thing they're a separate message
		        stringBuilder.append(NEWLINE_REPLACEMENT);  
		    }
		    
		    // Add in the final commit
    		commitList.add(stringBuilder.toString());
		    
		    // Now post each to the channel
		    for(String commit: commitList) {
				BitfighterLogBot logbot = (BitfighterLogBot) bot;
				BotConfig config = logbot.getConfig();
				
				logbot.sendAction(config.getChannel(), commit);
	        	
	        	// Now log this action, too.  'hostname' is null because getting my own 
				// hostname is not implemented
				logbot.onAction(bot.getNick(), bot.getLogin(), null, config.getChannel(), commit);
				
				Thread.sleep(COMMIT_POST_DELAY);
		    }
		} 
		catch (Exception e) {
			e.printStackTrace();
		}
	}

}
