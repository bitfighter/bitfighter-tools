package org.bitfighter.logbot;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Launcher for BitfighterLogBot
 * 
 * @author raptor
 *
 */
public class Run {
    
    public static void main(String[] args) throws Exception {
        
        BotConfig config = new BotConfig();
        
        /* Set up basic output structure and files */
        File outDir = new File(config.getOutputDirectory());
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
        writer.write("    $server = \"" + config.getServer() + "\";");
        writer.newLine();
        writer.write("    $channel = \"" + config.getChannel() + "\";");
        writer.newLine();
        writer.write("    $nick = \"" + config.getNick() + "\";");
        writer.newLine();
        writer.write("?>");
        writer.flush();
        writer.close();
        
        /* Initialize the bot and connect! */
        BitfighterLogBot bot = new BitfighterLogBot(config);
		bot.setAutoNickChange(true);
        bot.setVerbose(config.isDebug());
        bot.connect(config.getServer());
        bot.joinChannel(config.getChannel());
    }
    


	public static void copy(File source, File target) {
		BufferedInputStream input = null;
		BufferedOutputStream output = null;
		try {
			input = new BufferedInputStream(new FileInputStream(source));
			output = new BufferedOutputStream(new FileOutputStream(target));
			int bytesRead = 0;
			byte[] buffer = new byte[1024];
			while ((bytesRead = input.read(buffer, 0, buffer.length)) != -1) {
				output.write(buffer, 0, bytesRead);
			}
			output.flush();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			try {
				output.close();
				input.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}