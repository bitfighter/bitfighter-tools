package org.bitfighter.logbot;

import java.net.URL;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;

import org.jibble.pircbot.PircBot;

import com.sun.syndication.feed.synd.SyndContentImpl;
import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.io.SyndFeedInput;
import com.sun.syndication.io.XmlReader;

public class FeedReaderThread extends Thread {
	private boolean loop = true;

	private PircBot bot;
    private String channel;
	
	private static final long SLEEP_DELAY = 60000l;
	private static final long INITIAL_SLEEP_DELAY = 15000l;
	private static final long SEND_DELAY = 1500l;
	
	private static int CACHE_SIZE = 20;
	
	
	private static SyndFeedInput feedInput = new SyndFeedInput();

    private static XmlReader xmlReader;
    private static SyndFeed feed;
    private static URL feedUrl;
    
    private static LinkedHashSet<String> feedCache = new LinkedHashSet<String>();
    
	
	public FeedReaderThread(PircBot bot, String channel, String url) {
		try {
			this.channel = channel;
			this.bot = bot;
			feedUrl = new URL(url);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void run() {
		/* Sleep for just a bit on start-up */
		try {
			Thread.sleep(INITIAL_SLEEP_DELAY);
		} catch (InterruptedException e) {
			// Do nothing
		}

		fillCache();

		while(loop) {
			sendNewFeeds();

			try {
				Thread.sleep(SLEEP_DELAY);
			} catch (InterruptedException e) {
				continue;
			}
		}
	}
	
	/**
	 * @return
	 */
	private List<?> getFeedList() {
		List<?> feedList = null;
		try {
			xmlReader = new XmlReader(feedUrl);
			feed = feedInput.build(xmlReader);
			feedList = feed.getEntries();
			
			// make newest last
			Collections.reverse(feedList);
			
			CACHE_SIZE = feedCache.size();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		
		return feedList;
	}

	/**
	 * 
	 */
	private void fillCache() {
		try {
			for(Object o: getFeedList()) {
				SyndEntry entry = (SyndEntry) o;
				feedCache.add(entry.getUri());
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 
	 */
	private void sendNewFeeds() {

	    try {			
			for(Object o: getFeedList()) {
				SyndEntry entry = (SyndEntry) o;
				
				String id = entry.getUri();
				
				if (!feedCache.contains(id)) {
					feedCache.add(id);
					
					String commit = entry.getUri().substring(entry.getUri().lastIndexOf('/') + 1).substring(0, 12);
	            	String content = ((SyndContentImpl) entry.getContents().get(0)).getValue();
	            	String log = content.substring(content.lastIndexOf("<br/>") + 5).replaceAll("\\n", " ");
	            	
	            	bot.sendAction(channel, "- Commit " + commit + " | Author: " + entry.getAuthor() + " | Log: " + log);

//	            	System.out.println("* BitfighterLogBot - Commit " + commit + " | Author: " + entry.getAuthor() + " | Log: " + log);
	            	
	        		try {
	        			Thread.sleep(SEND_DELAY);
	        		} catch (InterruptedException e) {
	        			continue;
	        		}
				}
			}
			
			// cycle the cache
			if (feedCache.size() > CACHE_SIZE) {
				while (feedCache.size() > CACHE_SIZE) {
					feedCache.remove(feedCache.iterator().next());
				}
			}
			
			/* Clean up to keep memory usage low */
			System.gc(); 
			System.runFinalization();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void finish() {
		loop = false;
	}
}
