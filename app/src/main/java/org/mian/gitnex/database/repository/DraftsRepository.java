package org.mian.gitnex.database.repository;

import android.content.Context;
import android.os.AsyncTask;
import androidx.lifecycle.LiveData;
import org.mian.gitnex.database.dao.DraftsDao;
import org.mian.gitnex.database.db.GitnexDatabase;
import org.mian.gitnex.database.models.Drafts;
import org.mian.gitnex.database.models.DraftsWithRepositories;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * Author M M Arif
 */

public class DraftsRepository {

	private static DraftsDao draftsDao;
	private static long draftId;

	public DraftsRepository(Context context) {

		GitnexDatabase db;
		db = GitnexDatabase.getDatabaseInstance(context);
		draftsDao = db.draftsDao();

	}

	public long insertDraft(int repositoryId, int draftAccountId, int issueId, String draftText, String draftType) {

		Drafts drafts = new Drafts();
		drafts.setDraftRepositoryId(repositoryId);
		drafts.setDraftAccountId(draftAccountId);
		drafts.setIssueId(issueId);
		drafts.setDraftText(draftText);
		drafts.setDraftType(draftType);

		return insertDraftAsyncTask(drafts);

	}

	private static long insertDraftAsyncTask(final Drafts drafts) {

		new Thread(() -> draftId = draftsDao.insertDraft(drafts)).start();
		return draftId;
	}

	public Integer checkDraft(int issueId, int draftRepositoryId) throws ExecutionException, InterruptedException {

		return new DraftsRepository.checkDraftAsyncTask(issueId, draftRepositoryId).execute().get();
	}

	private static class checkDraftAsyncTask extends AsyncTask<Void, Void, Integer> {

		int issueId;
		int draftRepositoryId;

		checkDraftAsyncTask(int issueId, int draftRepositoryId) {

			this.issueId = issueId;
			this.draftRepositoryId = draftRepositoryId;
		}

		@Override
		protected Integer doInBackground(Void... params) {

			return draftsDao.checkDraftDao(issueId, draftRepositoryId);
		}

	}

	public LiveData<List<DraftsWithRepositories>> getDrafts(int accountId) {

		return draftsDao.fetchAllDrafts(accountId);
	}

	public LiveData<Drafts> getDraftByIssueId(int issueId) {

		return draftsDao.fetchDraftByIssueId(issueId);
	}

	public static void deleteSingleDraft(final int draftId) {

		final LiveData<Drafts> draft = draftsDao.fetchDraftById(draftId);

		if(draft != null) {

			new Thread(() -> draftsDao.deleteByDraftId(draftId)).start();
		}
	}

	public static void deleteAllDrafts(final int accountId) {

		new Thread(() -> draftsDao.deleteAllDrafts(accountId)).start();
	}

	public static void updateDraft(final String draftText, final int draftId) {

		new Thread(() -> draftsDao.updateDraft(draftText, draftId)).start();
	}

	public static void updateDraftByIssueIdAsyncTask(final String draftText, final int issueId, final int draftRepositoryId) {

		new Thread(() -> draftsDao.updateDraftByIssueId(draftText, issueId, draftRepositoryId)).start();
	}

}
