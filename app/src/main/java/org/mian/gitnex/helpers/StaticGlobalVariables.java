package org.mian.gitnex.helpers;

/**
 * Author M M Arif
 */

public interface StaticGlobalVariables {

	// issues variables
	int issuesPageInit = 1;
	int resultLimitNewGiteaInstances = 25; // Gitea 1.12 and above
	int resultLimitOldGiteaInstances = 10; // Gitea 1.11 and below
	String issuesRequestType = "issues";
	String issueStateClosed = "closed";

	// drafts
	String draftTypeComment = "comment";
	String draftTypeIssue = "issue";

	// TAGS
	String tagIssuesListOpen = "IssuesListOpenFragment";
	String tagIssuesListClosed = "IssuesListClosedFragment";
	String draftsRepository = "DraftsRepository";
	String repositoriesRepository = "RepositoriesRepository";
	String replyToIssueActivity = "ReplyToIssueActivity";
}
