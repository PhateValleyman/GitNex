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

    @Query("SELECT * FROM repositories ORDER BY repositoryId asc")
    LiveData<List<Repositories>> fetchAllRows();

}