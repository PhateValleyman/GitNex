package org.mian.gitnex.database.repository;

import android.content.Context;
import android.os.AsyncTask;
import androidx.lifecycle.LiveData;
import org.mian.gitnex.database.dao.DraftsDao;
import org.mian.gitnex.database.db.GitnexDatabase;
import org.mian.gitnex.database.models.Drafts;
import java.util.List;

/**
 * Author M M Arif
 */

public class DraftsRepository {

    private static DraftsDao draftsDao;

    public DraftsRepository(Context context) {

        GitnexDatabase db;
        db = GitnexDatabase.getDatabaseInstance(context);
        draftsDao = db.draftsDao();

    }

    public void insertDraftQuery(int repositoryId, int draftAccountId, int issueId, String draftText, String draftType) {

        Drafts drafts = new Drafts();
        drafts.setDraftRepositoryId(repositoryId);
        drafts.setDraftAccountId(draftAccountId);
        drafts.setIssueId(issueId);
        drafts.setDraftText(draftText);
	    drafts.setDraftType(draftType);

	    insertDraftAsync(drafts);
    }

    private static void insertDraftAsync(final Drafts drafts) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                draftsDao.insertDraft(drafts);
                return null;
            }
        }.execute();
    }

    public LiveData<List<Drafts>> getDrafts(int accountId) {
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

}
