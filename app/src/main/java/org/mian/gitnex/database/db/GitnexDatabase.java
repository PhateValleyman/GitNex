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
import org.mian.gitnex.database.models.Draft;
import org.mian.gitnex.database.models.Repository;
import org.mian.gitnex.database.models.UserAccount;

/**
 * Author M M Arif
 */

@Database(entities = {Draft.class, Repository.class, UserAccount.class},
        version = 1, exportSchema = false)
public abstract class GitnexDatabase extends RoomDatabase {

    private static GitnexDatabase gitnexDatabase;

    public static GitnexDatabase getDatabaseInstance(Context context) {

        if (gitnexDatabase == null) {
            String DB_NAME = "gitnex";
            gitnexDatabase = Room.databaseBuilder(context, GitnexDatabase.class, DB_NAME)
                    //.fallbackToDestructiveMigration()
                    //.addMigrations(MIGRATION_1_2)
                    .build();
        }

        return gitnexDatabase;
    }

    public abstract DraftsDao draftsDao();

    public abstract RepositoriesDao repositoriesDao();

    public abstract UserAccountsDao userAccountsDao();

    private static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {

            //database.execSQL("DROP TABLE Drafts");
	        //database.execSQL("ALTER TABLE 'Drafts' ADD COLUMN 'draftType' TEXT");

        }
    };
}
