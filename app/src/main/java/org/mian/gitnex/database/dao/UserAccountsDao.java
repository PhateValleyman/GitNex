package org.mian.gitnex.database.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import org.mian.gitnex.database.models.UserAccounts;
import java.util.List;

/**
 * Author M M Arif
 */

@Dao
public interface UserAccountsDao {

    @Insert
    void newAccount(UserAccounts userAccounts);

    @Query("SELECT * FROM userAccounts ORDER BY accountId asc")
    LiveData<List<UserAccounts>> fetchAllRows();

}