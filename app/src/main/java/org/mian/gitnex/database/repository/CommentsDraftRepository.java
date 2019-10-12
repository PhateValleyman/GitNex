package org.mian.gitnex.database.repository;

import android.content.Context;
import android.os.AsyncTask;
import androidx.lifecycle.LiveData;
import androidx.room.Room;
import org.mian.gitnex.database.db.GitnexDatabase;
import org.mian.gitnex.database.models.CommentsDraft;
import java.util.List;

/**
 * Author M M Arif
 */

public class CommentsDraftRepository {

    private static GitnexDatabase gitnexDatabase;

    public CommentsDraftRepository(Context context) {

        String DB_NAME = "gitnex";
        gitnexDatabase = Room.databaseBuilder(context, GitnexDatabase.class, DB_NAME).build();

    }

    /*public void insertComment(String title,
                           String description) {

        insertTask(title, description, userId);
    }*/

    public void insertComment(String userId, String title, String description) {

        CommentsDraft commentsDraft = new CommentsDraft();
        commentsDraft.setUserId(userId);
        commentsDraft.setTitle(title);
        commentsDraft.setDescription(description);
        //commentsDraft.setCreatedAt(AppUtils.getCurrentDateTime());

        insertCommentAsync(commentsDraft);
    }

    private static void insertCommentAsync(final CommentsDraft commentsDraft) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                gitnexDatabase.commentsDraftDaoAccess().insertComment(commentsDraft);
                return null;
            }
        }.execute();
    }

    public LiveData<List<CommentsDraft>> getComments() {
        return gitnexDatabase.commentsDraftDaoAccess().fetchAllTasks();
    }

}
