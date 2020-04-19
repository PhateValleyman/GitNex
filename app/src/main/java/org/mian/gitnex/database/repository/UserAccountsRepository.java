package org.mian.gitnex.database.repository;

import android.content.Context;
import android.os.AsyncTask;
import androidx.lifecycle.LiveData;
import org.mian.gitnex.database.dao.UserAccountsDao;
import org.mian.gitnex.database.db.GitnexDatabase;
import org.mian.gitnex.database.models.UserAccounts;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * Author M M Arif
 */

public class UserAccountsRepository {

    private static UserAccountsDao userAccountsDao;

    public UserAccountsRepository(Context context) {

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

        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                userAccountsDao.newAccount(userAccounts);
                return null;
            }
        }.execute();
        
    }

    public static void updateServerVersion(final String serverVersion, final int accountId) {

        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                userAccountsDao.updateServerVersion(serverVersion, accountId);
                return null;
            }
        }.execute();

    }

    public static void updateToken(final int accountId, final String token) {

        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                userAccountsDao.updateAccountToken(accountId, token);
                return null;
            }
        }.execute();

    }

	public UserAccounts getAccountData(String accountName) throws ExecutionException, InterruptedException {
		return new GetAccountByNameAsyncTask().execute(accountName).get();
	}

	private static class GetAccountByNameAsyncTask extends AsyncTask<String, Void, UserAccounts>
	{

		@Override
		protected UserAccounts doInBackground(String... params) {
			return userAccountsDao.fetchRowByAccount_(params[0]);
		}

	}

    public static LiveData<Integer> getCount(String accountName) {
        return userAccountsDao.getCount(accountName);
    }

    public LiveData<List<UserAccounts>> getAllAccounts() {
        return userAccountsDao.fetchAllAccounts();
    }

    public static void deleteAccount(final int accountId) {

        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                userAccountsDao.deleteAccount(accountId);
                return null;
            }
        }.execute();

    }

}
