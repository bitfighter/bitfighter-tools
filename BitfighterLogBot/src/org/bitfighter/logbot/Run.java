package org.bitfighter.logbot;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Properties;

/**
 * Launcher for BitfighterLogBot
 * 
 * @author raptor
 *
 */
public class Run {
    
    public static void main(String[] args) throws Exception {
        
        Properties properties = new Properties();
        properties.load(new FileInputStream(new File("./config.ini")));
        
        String server = properties.getProperty("Server", "irc.freenode.net");
        String channel = properties.getProperty("Channel", "#bitfighter");
        String nick = properties.getProperty("Nick", "LogBot2");
        String joinMessage = properties.getProperty("JoinMessage", "This channel is logged.");
        String debug = properties.getProperty("Debug", "false");
        
        boolean enableDebug = debug.equalsIgnoreCase("true") ? true : false;
        
        File outDir = new File(properties.getProperty("OutputDir", "./output/"));
        outDir.mkdirs();
        if (!outDir.isDirectory()) {
            System.out.println("Cannot make output directory (" + outDir + ")");
            System.exit(1);
        }

        copy(new File("html/header.inc.php"), new File(outDir, "header.inc.php"));
        copy(new File("html/footer.inc.php"), new File(outDir, "footer.inc.php"));
        copy(new File("html/index.php"), new File(outDir, "index.php"));
        
        BufferedWriter writer = new BufferedWriter(new FileWriter(new File(outDir, "config.inc.php")));
        writer.write("<?php");
        writer.newLine();
        writer.write("    $server = \"" + server + "\";");
        writer.newLine();
        writer.write("    $channel = \"" + channel + "\";");
        writer.newLine();
        writer.write("    $nick = \"" + nick + "\";");
        writer.newLine();
        writer.write("?>");
        writer.flush();
        writer.close();
        
        /* Initialize the bot and connect! */
        BitfighterLogBot bot = new BitfighterLogBot(server, channel, nick, outDir, joinMessage);
		bot.setAutoNickChange(true);
        bot.setVerbose(enableDebug);
        bot.connect(server);
        bot.joinChannel(channel);
    }
    


	public static void copy(File source, File target) throws IOException {
		BufferedInputStream input = new BufferedInputStream(new FileInputStream(source));
		BufferedOutputStream output = new BufferedOutputStream(new FileOutputStream(target));
		int bytesRead = 0;
		byte[] buffer = new byte[1024];
		while ((bytesRead = input.read(buffer, 0, buffer.length)) != -1) {
			output.write(buffer, 0, bytesRead);
		}
		output.flush();
		output.close();
		input.close();
	}
}