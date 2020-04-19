package org.mian.gitnex.database.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import org.mian.gitnex.database.models.Drafts;
import java.util.List;

/**
 * Author M M Arif
 */

@Dao
public interface DraftsDao {

    @Insert
    void insertDraft(Drafts drafts);

    @Query("SELECT * FROM Drafts WHERE draftAccountId = :accountId ORDER BY draftId DESC")
    LiveData<List<Drafts>> fetchAllDrafts(int accountId);

    @Query("SELECT * FROM Drafts WHERE draftAccountId = :accountId and draftRepositoryId = :repositoryId")
    LiveData<Drafts> fetchSingleDraftByAccountIdAndRepositoryId(int accountId, int repositoryId);

    @Query("SELECT * FROM Drafts WHERE draftId = :draftId")
    LiveData<Drafts> fetchDraftById(int draftId);

    @Query("SELECT * FROM Drafts WHERE issueId = :issueId")
    LiveData<Drafts> fetchDraftByIssueId(int issueId);

    @Query("UPDATE Drafts SET draftText= :draftText WHERE draftId = :draftId")
    void updateDraft(String draftText, int draftId);

    @Query("DELETE FROM Drafts WHERE draftId = :draftId")
    void deleteByDraftId(int draftId);

    @Query("DELETE FROM Drafts WHERE draftAccountId = :accountId")
    void deleteAllDrafts(int accountId);

}
