package org.bitfighter.logbot;

import java.net.URL;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang.StringEscapeUtils;
import org.jibble.pircbot.PircBot;
import org.json.JSONObject;
import org.json.XML;

public class FeedNotifierThread extends Thread {
	private boolean loop = true;

	private PircBot bot;
    private String channel;
    private String feedUrl;

	private static final long SLEEP_DELAY = 600000l;  // 10 min
	private static final long INITIAL_SLEEP_DELAY = 15000l;  // 15 sec
	private static final long SEND_DELAY = 1500l;  // 1.5 sec
	
	private static int CACHE_SIZE = 20;
    
    private static LinkedHashSet<String> feedCache = new LinkedHashSet<String>();
    
	
	public FeedNotifierThread(PircBot bot, String channel, String url) {
		try {
			this.channel = channel;
			this.bot = bot;
			this.feedUrl = url;
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
			e.printStackTrace();
		}

		fillCache();

		while(loop) {
			sendNewFeeds();

			try {
				Thread.sleep(SLEEP_DELAY);
			} catch (InterruptedException e) {
				e.printStackTrace();
				continue;
			}
		}
	}

	/**
	 * 
	 */
	private void fillCache() {
		try {
			for(AtomData atomData: getFeedList()) {
				feedCache.add(atomData.getId());
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * @return
	 */
	private List<AtomData> getFeedList() {
		List<AtomData> feedList = null;

		try {
			feedList = new LinkedList<AtomData>();

			String xmlString = Util.convertStreamToString(new URL(feedUrl).openStream());
			JSONObject json = XML.toJSONObject(xmlString);

			for (int i = 0; i < json.getJSONObject("feed").getJSONArray("entry").length(); i ++) {
				JSONObject entry = json.getJSONObject("feed").getJSONArray("entry").getJSONObject(i);

				String id = entry.getString("id");
				String commit = id.substring(id.lastIndexOf('/') + 1).substring(0, 12);
				String author = entry.getJSONObject("author").getString("name");
				String content = StringEscapeUtils.unescapeHtml(entry.getJSONObject("content").getString("content"));
				String log = content.substring(content.lastIndexOf("<br/>") + 5).replaceAll("\\n", " ");

				feedList.add(new AtomData(id, commit, author, log));
			}

			// Reverse list to put newest last
			Collections.reverse(feedList);
		}
		catch (Exception e) {
			e.printStackTrace();
		}

		return feedList;
	}
	
	private void sendNewFeeds() {

	    try {			
			for(AtomData atomData: getFeedList()) {
				if (!feedCache.contains(atomData.getId())) {
					feedCache.add(atomData.getId());
					    	
	            	bot.sendAction(channel, atomData.toString());
	            	
	            	// now log this action, too
	            	// hostname is null because getting my own hostname in not implemented yet
	            	((BitfighterLogBot)bot).onAction(bot.getNick(), bot.getLogin(), null, channel, atomData.toString());
	            	
	        		try {
	        			Thread.sleep(SEND_DELAY);
	        		} catch (InterruptedException e) {
	        			e.printStackTrace();
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
			
			/* Clean up to keep memory usage low since XML -> JSON takes a bit */
			System.runFinalization();
			System.gc();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void finish() {
		loop = false;
	}
}
