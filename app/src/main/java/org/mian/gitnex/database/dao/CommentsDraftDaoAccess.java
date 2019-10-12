package org.mian.gitnex.database.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import org.mian.gitnex.database.models.CommentsDraft;
import java.util.List;

/**
 * Author M M Arif
 */

@Dao
public interface CommentsDraftDaoAccess {

    @Insert
    void insertComment(CommentsDraft commentsDraft);

    @Query("SELECT * FROM CommentsDraft ORDER BY id desc")
    LiveData<List<CommentsDraft>> fetchAllTasks();

}
