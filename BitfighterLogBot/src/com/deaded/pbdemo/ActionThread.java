package com.deaded.pbdemo;

public class ActionThread implements Runnable {

	private ThreadedBot bot = null;
	private String channel = "";
	private String sender = "";
	private boolean display = true;
	
	public ActionThread(ThreadedBot bot, String channel, String sender) {
		this.bot = bot;
		this.channel = channel;
		this.sender = sender;
	}
	
	public void run() {
		while(display) {
			bot.sendMessage(channel, sender +" triggered me.");
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// not going to do anything
			}
		}
	}

	public void stop() {
		display = false;
	}
}
