package org.mian.gitnex.database.models;

/**
 * Author M M Arif
 */

public class DraftWithRepository {

	private int repositoryId;
	private int draftId;

	private int repoAccountId;
	private String repositoryOwner;
	private String repositoryName;

	private int draftRepositoryId;
	private int draftAccountId;
	private int issueId;
	private String draftText;
	private String draftType;

	public int getRepositoryId() {

		return repositoryId;
	}

	public void setRepositoryId(int repositoryId) {

		this.repositoryId = repositoryId;
	}

	public int getDraftId() {

		return draftId;
	}

	public void setDraftId(int draftId) {

		this.draftId = draftId;
	}

	public int getRepoAccountId() {

		return repoAccountId;
	}

	public void setRepoAccountId(int repoAccountId) {

		this.repoAccountId = repoAccountId;
	}

	public String getRepositoryOwner() {

		return repositoryOwner;
	}

	public void setRepositoryOwner(String repositoryOwner) {

		this.repositoryOwner = repositoryOwner;
	}

	public String getRepositoryName() {

		return repositoryName;
	}

	public void setRepositoryName(String repositoryName) {

		this.repositoryName = repositoryName;
	}

	public int getDraftRepositoryId() {

		return draftRepositoryId;
	}

	public void setDraftRepositoryId(int draftRepositoryId) {

		this.draftRepositoryId = draftRepositoryId;
	}

	public int getDraftAccountId() {

		return draftAccountId;
	}

	public void setDraftAccountId(int draftAccountId) {

		this.draftAccountId = draftAccountId;
	}

	public int getIssueId() {

		return issueId;
	}

	public void setIssueId(int issueId) {

		this.issueId = issueId;
	}

	public String getDraftText() {

		return draftText;
	}

	public void setDraftText(String draftText) {

		this.draftText = draftText;
	}

	public String getDraftType() {

		return draftType;
	}

	public void setDraftType(String draftType) {

		this.draftType = draftType;
	}

}
