package org.mian.gitnex.database.db;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;
import org.mian.gitnex.database.dao.DraftsDao;
import org.mian.gitnex.database.dao.PreferencesDao;
import org.mian.gitnex.database.dao.RepositoriesDao;
import org.mian.gitnex.database.dao.UserAccountsDao;
import org.mian.gitnex.database.models.Draft;
import org.mian.gitnex.database.models.GlobalPreference;
import org.mian.gitnex.database.models.LocalPreference;
import org.mian.gitnex.database.models.PreferencesGroup;
import org.mian.gitnex.database.models.Repository;
import org.mian.gitnex.database.models.UserAccount;

/**
 * Author M M Arif
 */

@Database(entities = {
	Draft.class,
	GlobalPreference.class,
	LocalPreference.class,
	PreferencesGroup.class,
	Repository.class,
	UserAccount.class
}, version = 4, exportSchema = false)
public abstract class GitnexDatabase extends RoomDatabase {

	private static final String DB_NAME = "gitnex";
    private static GitnexDatabase gitnexDatabase;

    public abstract DraftsDao draftsDao();
    public abstract PreferencesDao preferencesDao();
    public abstract RepositoriesDao repositoriesDao();
    public abstract UserAccountsDao userAccountsDao();

    private static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            //database.execSQL("DROP TABLE Drafts");
	        database.execSQL("ALTER TABLE 'Drafts' ADD COLUMN 'commentId' TEXT");
        }
    };

	private static final Migration MIGRATION_2_3 = new Migration(2, 3) {
		@Override
		public void migrate(@NonNull SupportSQLiteDatabase database) {
			database.execSQL("ALTER TABLE 'Drafts' ADD COLUMN 'issueType' TEXT");
		}
	};

	private static final Migration MIGRATION_3_4 = new Migration(3, 4) {
		@Override
		public void migrate(@NonNull SupportSQLiteDatabase database) {
			database.execSQL("CREATE TABLE IF NOT EXISTS `PreferencesGroups` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT)");
			database.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_preferencesGroups_name` ON `PreferencesGroups` (`name`)");

			database.execSQL("CREATE TABLE IF NOT EXISTS `GlobalPreferences` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `preferencesGroupId` INTEGER NOT NULL, `key` TEXT NOT NULL, `value` TEXT, FOREIGN KEY(`preferencesGroupId`) REFERENCES `PreferencesGroups`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )");
			database.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_globalPreferences_preferencesGroupId_key` ON `GlobalPreferences` (`preferencesGroupId`, `key`)");

			database.execSQL("CREATE TABLE IF NOT EXISTS `LocalPreferences` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `userAccountId` INTEGER NOT NULL, `preferencesGroupId` INTEGER NOT NULL, `key` TEXT NOT NULL, `value` TEXT, FOREIGN KEY(`userAccountId`) REFERENCES `UserAccounts`(`accountId`) ON UPDATE NO ACTION ON DELETE CASCADE , FOREIGN KEY(`preferencesGroupId`) REFERENCES `PreferencesGroups`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )");
			database.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_localPreferences_userAccountId_preferencesGroupId_key` ON `LocalPreferences` (`userAccountId`, `preferencesGroupId`, `key`)");

			database.execSQL("ALTER TABLE `userAccounts` RENAME TO `userAccounts_temp`");
			database.execSQL("ALTER TABLE `userAccounts_temp` RENAME TO `UserAccounts`");
		}
	};

	public static GitnexDatabase getDatabaseInstance(Context context) {

		if (gitnexDatabase == null) {
			synchronized(GitnexDatabase.class) {
				if(gitnexDatabase == null) {

					gitnexDatabase = Room.databaseBuilder(context, GitnexDatabase.class, DB_NAME)
						// .fallbackToDestructiveMigration()
						.allowMainThreadQueries()
						.addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4)
						.build();

				}
			}
		}

		return gitnexDatabase;

	}
}
