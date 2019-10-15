package org.mian.gitnex.database.repository;

import android.content.Context;
import android.os.AsyncTask;
import androidx.lifecycle.LiveData;
import org.mian.gitnex.database.dao.RepositoriesDao;
import org.mian.gitnex.database.db.GitnexDatabase;
import org.mian.gitnex.database.models.Repositories;
import java.util.List;

/**
 * Author M M Arif
 */

public class RepositoriesRepository {

    private static RepositoriesDao repositoriesDao;

    public RepositoriesRepository(Context context) {

        GitnexDatabase db;
        db = GitnexDatabase.getDatabaseInstance(context);
        repositoriesDao = db.repositoriesDao();

    }

    public void insertNewRepository(int repoAccountId, String repositoryOwner, String repositoryName) {

        Repositories repositories = new Repositories();
        repositories.setRepoAccountId(repoAccountId);
        repositories.setRepositoryOwner(repositoryOwner);
        repositories.setRepositoryName(repositoryName);

        insertNewRepositoryAsync(repositories);
    }

    private static void insertNewRepositoryAsync(final Repositories repositories) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                repositoriesDao.newRepository(repositories);
                return null;
            }
        }.execute();
    }

    public LiveData<List<Repositories>> getAllRepositories() {
        return repositoriesDao.fetchAllRepositories();
    }

    public LiveData<List<Repositories>> getAllRepositoriesByAccount(int repoAccountId) {
        return repositoriesDao.fetchAllRowsByAccount(repoAccountId);
    }

    public LiveData<Repositories> fetchSingleRepository(int repositoryId) {
        return repositoriesDao.fetchSingleRow(repositoryId);
    }

    public LiveData<Repositories> fetchSingleRowByAccount(int repositoryId, int repoAccountId) {
        return repositoriesDao.fetchSingleRowByAccount(repositoryId, repoAccountId);
    }

    public static void deleteRepositoriesByAccount(final int repoAccountId) {

        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                repositoriesDao.deleteRepositoriesByAccount(repoAccountId);
                return null;
            }
        }.execute();

    }

    public static void deleteRepository(final int repositoryId) {

        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                repositoriesDao.deleteRepository(repositoryId);
                return null;
            }
        }.execute();

    }

}
