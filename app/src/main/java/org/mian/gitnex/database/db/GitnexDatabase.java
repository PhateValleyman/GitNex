package org.mian.gitnex.database.db;

import androidx.room.Database;
import androidx.room.RoomDatabase;
import org.mian.gitnex.database.dao.CommentsDraftDaoAccess;
import org.mian.gitnex.database.models.CommentsDraft;

/**
 * Author M M Arif
 */

@Database(entities = {CommentsDraft.class}, version = 1, exportSchema = false)
public abstract class GitnexDatabase extends RoomDatabase {

    public abstract CommentsDraftDaoAccess commentsDraftDaoAccess();
}