package org.mian.gitnex.database.api;

import android.content.Context;
import android.util.Log;
import androidx.lifecycle.LiveData;
import org.mian.gitnex.database.dao.UserAccountsDao;
import org.mian.gitnex.database.db.GitnexDatabase;
import org.mian.gitnex.database.models.UserAccounts;
import org.mian.gitnex.helpers.StaticGlobalVariables;
import java.util.List;

/**
 * Author M M Arif
 */

public class UserAccountsApi {

	private static UserAccountsDao userAccountsDao;
	private static UserAccounts userAccounts;
	private static Integer checkAccount;

	public UserAccountsApi(Context context) {

		GitnexDatabase db;
		db = GitnexDatabase.getDatabaseInstance(context);
		userAccountsDao = db.userAccountsDao();
	}

	public void insertNewAccount(String accountName, String instanceUrl, String userName, String token, String serverVersion) {

		UserAccounts userAccounts = new UserAccounts();
		userAccounts.setAccountName(accountName);
		userAccounts.setInstanceUrl(instanceUrl);
		userAccounts.setUserName(userName);
		userAccounts.setToken(token);
		userAccounts.setServerVersion(serverVersion);

		insertNewAccountAsync(userAccounts);
	}

	private static void insertNewAccountAsync(final UserAccounts userAccounts) {

		new Thread(() -> userAccountsDao.newAccount(userAccounts)).start();
	}

	public static void updateServerVersion(final String serverVersion, final int accountId) {

		new Thread(() -> userAccountsDao.updateServerVersion(serverVersion, accountId)).start();
	}

	public static void updateToken(final int accountId, final String token) {

		new Thread(() -> userAccountsDao.updateAccountToken(accountId, token)).start();
	}

	public UserAccounts getAccountData(String accountName) {

		try {

			Thread thread = new Thread(() -> userAccounts = userAccountsDao.fetchRowByAccount_(accountName));
			thread.start();
			thread.join();
		}
		catch(InterruptedException e) {

			Log.e(StaticGlobalVariables.userAccountsRepository, e.toString());
		}

		return userAccounts;
	}

	public Integer getCount(String accountName) {

		try {

			Thread thread = new Thread(() -> checkAccount = userAccountsDao.getCount(accountName));
			thread.start();
			thread.join();
		}
		catch(InterruptedException e) {

			Log.e(StaticGlobalVariables.userAccountsRepository, e.toString());
		}

		return checkAccount;
	}

	public LiveData<List<UserAccounts>> getAllAccounts() {

		return userAccountsDao.fetchAllAccounts();
	}

	public static void deleteAccount(final int accountId) {

		new Thread(() -> userAccountsDao.deleteAccount(accountId)).start();
	}

}
