package org.mian.gitnex.database.db;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;
import org.mian.gitnex.database.dao.DraftsDao;
import org.mian.gitnex.database.dao.RepositoriesDao;
import org.mian.gitnex.database.dao.UserAccountsDao;
import org.mian.gitnex.database.models.Drafts;
import org.mian.gitnex.database.models.Repositories;
import org.mian.gitnex.database.models.UserAccounts;

/**
 * Author M M Arif
 */

@Database(entities = {Drafts.class, Repositories.class, UserAccounts.class},
        version = 4, exportSchema = false)
public abstract class GitnexDatabase extends RoomDatabase {

    private static GitnexDatabase gitnexDatabase;

    public static GitnexDatabase getDatabaseInstance(Context context) {

        if (gitnexDatabase == null) {
            String DB_NAME = "gitnex";
            gitnexDatabase = Room.databaseBuilder(context, GitnexDatabase.class, DB_NAME)
                    //.fallbackToDestructiveMigration()
                    //.addMigrations(MIGRATION_3_4)
                    .build();
        }

        return gitnexDatabase;
    }

    public abstract DraftsDao draftsDao();

    public abstract RepositoriesDao repositoriesDao();

    public abstract UserAccountsDao userAccountsDao();

    private static final Migration MIGRATION_3_4 = new Migration(3, 4) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {

            //database.execSQL("DROP TABLE Drafts");
	        //database.execSQL("ALTER TABLE 'Drafts' ADD COLUMN 'draftType' TEXT");

        }
    };
}