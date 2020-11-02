package org.mian.gitnex.models;

import java.util.List;

/**
 * Author M M Arif
 */

public class CreatePullRequest {

	private String title;
	private String body;
	private String assignee;
	private String base;
	private String head;
	private int milestone;
	private String due_date;
	private String message;

	private List<String> assignees;
	private List<Integer> labels;

	public CreatePullRequest(String title, String body, String assignee, String base, String head, int milestone, String due_date, List<String> assignees, List<Integer> labels) {

		this.title = title;
		this.body = body;
		this.assignee = assignee;
		this.base = base;           // merge into branch
		this.head = head;           // pull from branch
		this.milestone = milestone;
		this.due_date = due_date;
		this.assignees = assignees;
		this.labels = labels;
	}

}
