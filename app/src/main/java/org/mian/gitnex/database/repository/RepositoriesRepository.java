package org.mian.gitnex.database.repository;

import android.content.Context;
import android.util.Log;
import androidx.lifecycle.LiveData;
import org.mian.gitnex.database.dao.RepositoriesDao;
import org.mian.gitnex.database.db.GitnexDatabase;
import org.mian.gitnex.database.models.Repositories;
import org.mian.gitnex.helpers.StaticGlobalVariables;
import java.util.List;

/**
 * Author M M Arif
 */

public class RepositoriesRepository {

	private static RepositoriesDao repositoriesDao;
	private static long repositoryId;
	private static Repositories repositories;
	private static Integer checkRepository;

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

	public long insertRepositoryAsyncTask(Repositories repositories) {

		try {

			Thread thread = new Thread(() -> repositoryId = repositoriesDao.newRepository(repositories));
			thread.start();
			thread.join();
		}
		catch(InterruptedException e) {

			Log.e(StaticGlobalVariables.repositoriesRepository, e.toString());
		}

		return repositoryId;
	}

	public Repositories getRepository(int repoAccountId, String repositoryOwner, String repositoryName) {

		try {

			Thread thread = new Thread(() -> repositories = repositoriesDao.getSingleRepositoryDao(repoAccountId, repositoryOwner, repositoryName));
			thread.start();
			thread.join();
		}
		catch(InterruptedException e) {

			Log.e(StaticGlobalVariables.repositoriesRepository, e.toString());
		}

		return repositories;
	}

	public LiveData<List<Repositories>> getAllRepositories() {

		return repositoriesDao.fetchAllRepositories();
	}

	public LiveData<List<Repositories>> getAllRepositoriesByAccount(int repoAccountId) {

		return repositoriesDao.getAllRepositoriesByAccountDao(repoAccountId);
	}

	public Integer checkRepository(int repoAccountId, String repositoryOwner, String repositoryName) {

		try {

			Thread thread = new Thread(() -> checkRepository = repositoriesDao.checkRepositoryDao(repoAccountId, repositoryOwner, repositoryName));
			thread.start();
			thread.join();
		}
		catch(InterruptedException e) {

			Log.e(StaticGlobalVariables.repositoriesRepository, e.toString());
		}

		return checkRepository;
	}

	public Repositories fetchRepositoryById(int repositoryId) {

		try {

			Thread thread = new Thread(() -> repositories = repositoriesDao.fetchRepositoryByIdDao(repositoryId));
			thread.start();
			thread.join();
		}
		catch(InterruptedException e) {

			Log.e(StaticGlobalVariables.repositoriesRepository, e.toString());
		}

		return repositories;
	}

	public Repositories fetchRepositoryByAccountIdByRepositoryId(int repositoryId, int repoAccountId) {

		try {

			Thread thread = new Thread(() -> repositories = repositoriesDao.fetchRepositoryByAccountIdByRepositoryIdDao(repositoryId, repoAccountId));
			thread.start();
			thread.join();
		}
		catch(InterruptedException e) {

			Log.e(StaticGlobalVariables.repositoriesRepository, e.toString());
		}

		return repositories;
	}

	public static void deleteRepositoriesByAccount(final int repoAccountId) {

		new Thread(() -> repositoriesDao.deleteRepositoriesByAccount(repoAccountId)).start();
	}

	public static void deleteRepository(final int repositoryId) {

		new Thread(() -> repositoriesDao.deleteRepository(repositoryId)).start();
	}

}
