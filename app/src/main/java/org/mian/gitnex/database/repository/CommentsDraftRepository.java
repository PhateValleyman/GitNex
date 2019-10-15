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

    /*public void insertComment(String title,
                           String description) {

        insertTask(title, description, userId);
    }*/

    public void insertComment(int repositoryId, int issueId, String draftText) {

        CommentsDraft commentsDraft = new CommentsDraft();
        commentsDraft.setDraftRepositoryId(repositoryId);
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

    public LiveData<List<CommentsDraft>> getComments(int accountId) {
        return commentsDraftDao.fetchAllDrafts(accountId);
    }

}
