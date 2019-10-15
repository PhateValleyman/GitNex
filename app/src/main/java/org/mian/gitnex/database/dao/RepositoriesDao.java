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
    void newRepository(Repositories repositories);

    @Query("SELECT * FROM repositories ORDER BY repositoryId ASC")
    LiveData<List<Repositories>> fetchAllRepositories();

    @Query("SELECT * FROM repositories WHERE repoAccountId = :repoAccountId")
    LiveData<List<Repositories>> fetchAllRowsByAccount(int repoAccountId);

    @Query("SELECT * FROM repositories WHERE repositoryId = :repositoryId")
    LiveData<Repositories> fetchSingleRow(int repositoryId);

    @Query("SELECT * FROM repositories WHERE repositoryId = :repositoryId AND repoAccountId = :repoAccountId")
    LiveData<Repositories> fetchSingleRowByAccount(int repositoryId, int repoAccountId);

    @Query("UPDATE repositories SET repositoryOwner = :repositoryOwner, repositoryName = :repositoryName  WHERE repositoryId = :repositoryId")
    void updateRepositoryOwnerAndName(String repositoryOwner, String repositoryName, int repositoryId);

    @Query("DELETE FROM repositories WHERE repositoryId = :repositoryId")
    void deleteRepository(int repositoryId);

    @Query("DELETE FROM repositories WHERE repoAccountId = :repoAccountId")
    void deleteRepositoriesByAccount(int repoAccountId);

}