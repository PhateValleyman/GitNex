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

    @Query("SELECT * FROM userAccounts ORDER BY accountId ASC")
    LiveData<List<UserAccounts>> fetchAllAccounts();

    @Query("SELECT COUNT(accountId) FROM userAccounts WHERE accountName = :accountName")
    LiveData<Integer> getCount(String accountName);

    @Query("SELECT * FROM userAccounts WHERE accountName = :accountName")
    UserAccounts fetchRowByAccount_(String accountName);

    @Query("SELECT * FROM userAccounts WHERE accountId = :accountId")
    UserAccounts fetchRowByAccountId(int accountId);

    @Query("UPDATE userAccounts SET serverVersion = :serverVersion WHERE accountId = :accountId")
    void updateServerVersion(String serverVersion, int accountId);

    @Query("UPDATE userAccounts SET accountName = :accountName WHERE accountId = :accountId")
    void updateAccountName(String accountName, int accountId);

    @Query("UPDATE userAccounts SET token = :token WHERE accountId = :accountId")
    void updateAccountToken(int accountId, String token);

    @Query("UPDATE userAccounts SET instanceUrl = :instanceUrl, token = :token WHERE accountId = :accountId")
    void updateHostInfo(String instanceUrl, String token, int accountId);

    @Query("UPDATE userAccounts SET userName = :userName WHERE accountId = :accountId")
    void updateUserName(String userName, int accountId);

    @Query("UPDATE userAccounts SET instanceUrl = :instanceUrl, token = :token, userName = :userName, serverVersion = :serverVersion WHERE accountId = :accountId")
    void updateAll(String instanceUrl, String token, String userName, String serverVersion, int accountId);

    @Query("DELETE FROM userAccounts WHERE accountId = :accountId")
    void deleteAccount(int accountId);

}