package org.mian.gitnex.database.repository;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import androidx.lifecycle.LiveData;
import org.mian.gitnex.database.dao.DraftsDao;
import org.mian.gitnex.database.db.GitnexDatabase;
import org.mian.gitnex.database.models.Drafts;
import org.mian.gitnex.database.models.DraftsWithRepositories;
import org.mian.gitnex.helpers.StaticGlobalVariables;
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

        try {
            new AsyncTask<Void, Void, Long>() {

                @Override
                protected Long doInBackground(Void... voids) {

                    draftId = draftsDao.insertDraft(drafts);
                    return draftId;
                }

                @Override
                protected void onPostExecute(Long draftId) {

                    super.onPostExecute(draftId);
                }

            }.execute().get();
        }
        catch(ExecutionException | InterruptedException e) {
            Log.e(StaticGlobalVariables.draftsRepository, e.toString());
        }

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
            new AsyncTask<Void, Void, Void>() {
                @Override
                protected Void doInBackground(Void... voids) {
                    draftsDao.deleteByDraftId(draftId);
                    return null;
                }
            }.execute();
        }
    }

    public static void deleteAllDrafts(final int accountId) {

        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                draftsDao.deleteAllDrafts(accountId);
                return null;
            }
        }.execute();

    }

    public static void updateDraft(final String draftText, final int draftId) {

        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                draftsDao.updateDraft(draftText, draftId);
                return null;
            }
        }.execute();

    }

    public static void updateDraftByIssueIdAsycTask(final String draftText, final int issueId, final int draftRepositoryId) {

        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                draftsDao.updateDraftByIssueId(draftText, issueId, draftRepositoryId);
                return null;
            }
        }.execute();

    }


}
