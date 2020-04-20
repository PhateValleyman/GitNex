package org.mian.gitnex.database.repository;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import androidx.lifecycle.LiveData;
import org.mian.gitnex.database.dao.RepositoriesDao;
import org.mian.gitnex.database.db.GitnexDatabase;
import org.mian.gitnex.database.models.Repositories;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * Author M M Arif
 */

public class RepositoriesRepository {

	private static RepositoriesDao repositoriesDao;
	private static long repositoryId;

	public RepositoriesRepository(Context context) {

		GitnexDatabase db;
		db = GitnexDatabase.getDatabaseInstance(context);
		repositoriesDao = db.repositoriesDao();

	}

	public long insertRepository(int repoAccountId, String repositoryOwner, String repositoryName) {

		Repositories repositories = new Repositories();
		repositories.setRepoAccountId(repoAccountId);
		repositories.setRepositoryOwner(repositoryOwner);
		repositories.setRepositoryName(repositoryName);

		return insertRepositoryAsyncTask(repositories);

	}

	private static long insertRepositoryAsyncTask(final Repositories repositories) {

		try {
			new AsyncTask<Void, Void, Long>() {

				@Override
				protected Long doInBackground(Void... voids) {

					repositoryId = repositoriesDao.newRepository(repositories);
					return repositoryId;
				}

				@Override
				protected void onPostExecute(Long repositoryId) {

					super.onPostExecute(repositoryId);
				}

			}.execute().get();
		}
		catch(ExecutionException | InterruptedException e) {
			Log.e("checkRepository", e.toString());
		}

		return repositoryId;
	}

	public Repositories getRepository(int repoAccountId, String repositoryOwner, String repositoryName) throws ExecutionException, InterruptedException {

		return new RepositoriesRepository.getRepositoryAsyncTask(repoAccountId, repositoryOwner, repositoryName).execute().get();
	}

	private static class getRepositoryAsyncTask extends AsyncTask<Void, Void, Repositories> {

		int repoAccountId;
		String repositoryOwner;
		String repositoryName;

		getRepositoryAsyncTask(int repoAccountId, String repositoryOwner, String repositoryName) {

			this.repoAccountId = repoAccountId;
			this.repositoryOwner = repositoryOwner;
			this.repositoryName = repositoryName;
		}

		@Override
		protected Repositories doInBackground(Void... params) {

			return repositoriesDao.getSingleRepositoryDao(repoAccountId, repositoryOwner, repositoryName);
		}

	}

	public LiveData<List<Repositories>> getAllRepositories() {

		return repositoriesDao.fetchAllRepositories();
	}

	public LiveData<List<Repositories>> getAllRepositoriesByAccount(int repoAccountId) {

		return repositoriesDao.getAllRepositoriesByAccountDao(repoAccountId);
	}

	public Integer checkRepository(int repoAccountId, String repositoryOwner, String repositoryName) throws ExecutionException, InterruptedException {

		return new RepositoriesRepository.checkRepositoryAsyncTask(repoAccountId, repositoryOwner, repositoryName).execute().get();
	}

	private static class checkRepositoryAsyncTask extends AsyncTask<Void, Void, Integer> {

		int repoAccountId;
		String repositoryOwner;
		String repositoryName;

		checkRepositoryAsyncTask(int repoAccountId, String repositoryOwner, String repositoryName) {

			this.repoAccountId = repoAccountId;
			this.repositoryOwner = repositoryOwner;
			this.repositoryName = repositoryName;
		}

		@Override
		protected Integer doInBackground(Void... params) {

			return repositoriesDao.checkRepositoryDao(repoAccountId, repositoryOwner, repositoryName);
		}

	}

	public Repositories fetchRepositoryById(int repositoryId) throws ExecutionException, InterruptedException {

		return new RepositoriesRepository.getRepositoryByIdAsyncTask().execute(repositoryId).get();
	}

	private static class getRepositoryByIdAsyncTask extends AsyncTask<Integer, Void, Repositories> {

		@Override
		protected Repositories doInBackground(Integer... params) {

			return repositoriesDao.fetchRepositoryByIdDao(params[0]);
		}

	}

	public Repositories fetchRepositoryByAccountIdByRepositoryId(int repositoryId, int repoAccountId) throws ExecutionException, InterruptedException {

		return new RepositoriesRepository.getRepositoryByAccountIdByRepositoryIdAsyncTask().execute(repositoryId, repoAccountId).get();
	}

	private static class getRepositoryByAccountIdByRepositoryIdAsyncTask extends AsyncTask<Integer, Void, Repositories> {

		@Override
		protected Repositories doInBackground(Integer... params) {

			return repositoriesDao.fetchRepositoryByAccountIdByRepositoryIdDao(params[0], params[1]);
		}

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
