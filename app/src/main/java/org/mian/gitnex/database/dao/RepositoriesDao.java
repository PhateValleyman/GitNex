package org.mian.gitnex.database.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import org.mian.gitnex.database.models.Repositories;
import java.util.List;

/**
 * Author M M Arif
 */

@Dao
public interface RepositoriesDao {

    @Insert
    long newRepository(Repositories repositories);

    @Query("SELECT * FROM repositories ORDER BY repositoryId ASC")
    LiveData<List<Repositories>> fetchAllRepositories();

    @Query("SELECT * FROM repositories WHERE repoAccountId = :repoAccountId")
    LiveData<List<Repositories>> getAllRepositoriesByAccountDao(int repoAccountId);

    @Query("SELECT count(repositoryId) FROM repositories WHERE repoAccountId = :repoAccountId AND repositoryOwner = :repositoryOwner AND repositoryName = :repositoryName")
    Integer checkRepositoryDao(int repoAccountId, String repositoryOwner, String repositoryName);

	@Query("SELECT * FROM repositories WHERE repoAccountId = :repoAccountId AND repositoryOwner = :repositoryOwner AND repositoryName = :repositoryName")
	Repositories getSingleRepositoryDao(int repoAccountId, String repositoryOwner, String repositoryName);

    @Query("SELECT * FROM repositories WHERE repositoryId = :repositoryId")
    Repositories fetchRepositoryByIdDao(int repositoryId);

    @Query("SELECT * FROM repositories WHERE repositoryId = :repositoryId AND repoAccountId = :repoAccountId")
    Repositories fetchRepositoryByAccountIdByRepositoryIdDao(int repositoryId, int repoAccountId);

    @Query("UPDATE repositories SET repositoryOwner = :repositoryOwner, repositoryName = :repositoryName  WHERE repositoryId = :repositoryId")
    void updateRepositoryOwnerAndName(String repositoryOwner, String repositoryName, int repositoryId);

    @Query("DELETE FROM repositories WHERE repositoryId = :repositoryId")
    void deleteRepository(int repositoryId);

    @Query("DELETE FROM repositories WHERE repoAccountId = :repoAccountId")
    void deleteRepositoriesByAccount(int repoAccountId);

}