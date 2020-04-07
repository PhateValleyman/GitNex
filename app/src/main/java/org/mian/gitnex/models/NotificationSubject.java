package org.mian.gitnex.models;

/**
 * Author opyale
 */

public class NotificationSubject {
	private String latest_comment_url;
	private String title;
	private String type;
	private String url;

	public NotificationSubject(String latest_comment_url, String title, String type, String url) {

		this.latest_comment_url = latest_comment_url;
		this.title = title;
		this.type = type;
		this.url = url;
	}

	public String getLatest_comment_url() {
		return latest_comment_url;
	}

	public void setLatest_comment_url(String latest_comment_url) {
		this.latest_comment_url = latest_comment_url;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

}
