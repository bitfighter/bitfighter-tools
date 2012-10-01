package org.bitfighter.logbot.threads;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

import org.bitfighter.logbot.socket.SocketHandler;
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
public class SocketListenerThread extends Thread {

	private PircBot bot;
	private Class<? extends SocketHandler> clazz;
	
	private ServerSocket serverSocket;
	
	private boolean loop = true;

	private static final long INITIAL_SLEEP_DELAY = 15000l;  // 15 sec
	
	/**
	 * Open a socket and pass it off to a specified class to handle the data 
	 * @param bitfighterLogBot 
	 */
	public SocketListenerThread(PircBot bot, Class<? extends SocketHandler> clazz, int port) {
		this.bot = bot;
		this.clazz = clazz;
		
		try {
			// Bind to localhost only
			this.serverSocket = new ServerSocket(port, 0, InetAddress.getByName("localhost"));
		} catch (Exception e) {
			loop = false;
			e.printStackTrace();
		}
	}
	
	
	/**
	 * Thread class to handle a socket connection to the open socket on this server
	 */
	private class ClientThread extends Thread {
		private Socket socket;
		private Class<? extends SocketHandler> clazz;
		
		ClientThread(Socket clientSocket, Class<? extends SocketHandler> clazz) {
			this.socket = clientSocket;
			this.clazz = clazz;
		}
		
		// Only run once!
		public void run() {
			try {
				SocketHandler handler = clazz.newInstance();
				
				handler.run(bot, socket);
			} 
			catch (Exception e) {
				e.printStackTrace();
			} 
			finally {
				try {
					socket.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	
	public void run() {
		// Sleep for just a bit on start-up
		try {
			Thread.sleep(INITIAL_SLEEP_DELAY);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		while(loop) {
			try {
				Socket clientSocket = serverSocket.accept();
				
				// Send it off!
				new ClientThread(clientSocket, clazz).run();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		try {
			serverSocket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void finish() {
		loop = false;
	}
}
