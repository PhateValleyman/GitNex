package org.mian.gitnex.database.repository;

import android.content.Context;
import android.os.AsyncTask;
import androidx.lifecycle.LiveData;
import org.mian.gitnex.database.dao.CommentsDraftDao;
import org.mian.gitnex.database.db.GitnexDatabase;
import org.mian.gitnex.database.models.CommentsDraft;
import java.util.List;

/**
 * Author M M Arif
 */

public class CommentsDraftRepository {

    private static CommentsDraftDao commentsDraftDao;

    public CommentsDraftRepository(Context context) {

        GitnexDatabase db;
        db = GitnexDatabase.getDatabaseInstance(context);
        commentsDraftDao = db.commentsDraftDao();

    }

    public void insertComment(int repositoryId, int draftAccountId, int issueId, String draftText) {

        CommentsDraft commentsDraft = new CommentsDraft();
        commentsDraft.setDraftRepositoryId(repositoryId);
        commentsDraft.setDraftAccountId(draftAccountId);
        commentsDraft.setIssueId(issueId);
        commentsDraft.setDraftText(draftText);

        insertCommentAsync(commentsDraft);
    }

    private static void insertCommentAsync(final CommentsDraft commentsDraft) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                commentsDraftDao.insertComment(commentsDraft);
                return null;
            }
        }.execute();
    }

    public LiveData<List<CommentsDraft>> getDrafts(int accountId) {
        return commentsDraftDao.fetchAllDrafts(accountId);
    }

    public LiveData<CommentsDraft> getCommentByIssueId(int issueId) {
        return commentsDraftDao.fetchDraftByIssueId(issueId);
    }

    public static void deleteSingleDraft(final int draftId) {

        final LiveData<CommentsDraft> draft = commentsDraftDao.fetchDraftById(draftId);

        if(draft != null) {
            new AsyncTask<Void, Void, Void>() {
                @Override
                protected Void doInBackground(Void... voids) {
                    commentsDraftDao.deleteByDraftId(draftId);
                    return null;
                }
            }.execute();
        }
    }

    public static void deleteAllDrafts(final int accountId) {

        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                commentsDraftDao.deleteAllDrafts(accountId);
                return null;
            }
        }.execute();

    }

    public static void updateDraft(final String draftText, final int draftId) {

        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                commentsDraftDao.updateDraft(draftText, draftId);
                return null;
            }
        }.execute();

    }

}
