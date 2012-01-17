package org.bitfighter.logbot;

public class AtomData {

	private String id;
	private String author;
	private String log;
	private String commit;
	
	public AtomData(String id, String commit, String author, String log) {
		this.id = id;
		this.author = author;
		this.log = log;
		this.commit = commit;
	}
	
	public String getId() {
		return id;
	}
	
	@Override
	public String toString() {
		return "- Commit " + commit + " | Author: " + author + " | Log: " + log;
	}
}
