package org.mian.gitnex.helpers;

/**
 * Author M M Arif
 */

public interface StaticGlobalVariables {

	// generic values
	int resultLimitNewGiteaInstances = 25; // Gitea 1.12 and above
	int resultLimitOldGiteaInstances = 10; // Gitea 1.11 and below

	// issues variables
	String tagIssuesListOpen = "IssuesListOpenFragment - ";
	String tagIssuesListClosed = "IssuesListClosedFragment - ";
	int issuesPageInit = 1;
	String issuesRequestType = "issues";
	String issueStateClosed = "closed";

	// pull request
	String tagPullRequestsList = "PullRequestsListFragment";
	int prPageInit = 1;

}
