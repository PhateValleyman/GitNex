package org.mian.gitnex.database.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import org.mian.gitnex.database.models.Drafts;
import org.mian.gitnex.database.models.DraftsWithRepositories;
import java.util.List;

/**
 * Author M M Arif
 */

@Dao
public interface DraftsDao {

    @Insert
    long insertDraft(Drafts drafts);

    @Query("SELECT * FROM drafts JOIN repositories ON repositories.repositoryId = drafts.draftRepositoryId WHERE draftAccountId = :accountId" +
            " ORDER BY " +
            "draftId DESC")
    LiveData<List<DraftsWithRepositories>> fetchAllDrafts(int accountId);

    @Query("SELECT * FROM drafts WHERE draftAccountId = :accountId ORDER BY draftId DESC")
    LiveData<List<Drafts>> fetchDrafts(int accountId);

    @Query("SELECT * FROM drafts WHERE draftAccountId = :accountId and draftRepositoryId = :repositoryId")
    LiveData<Drafts> fetchSingleDraftByAccountIdAndRepositoryId(int accountId, int repositoryId);

    @Query("SELECT * FROM drafts WHERE draftId = :draftId")
    LiveData<Drafts> fetchDraftById(int draftId);

    @Query("SELECT * FROM drafts WHERE issueId = :issueId")
    LiveData<Drafts> fetchDraftByIssueId(int issueId);

    @Query("SELECT count(draftId) FROM drafts WHERE issueId = :issueId AND draftRepositoryId = :draftRepositoryId")
    Integer checkDraftDao(int issueId, int draftRepositoryId);

    @Query("UPDATE drafts SET draftText= :draftText WHERE draftId = :draftId")
    void updateDraft(String draftText, int draftId);

    @Query("UPDATE drafts SET draftText= :draftText WHERE issueId = :issueId AND draftRepositoryId = :draftRepositoryId")
    void updateDraftByIssueId(String draftText, int issueId, int draftRepositoryId);

    @Query("DELETE FROM drafts WHERE draftId = :draftId")
    void deleteByDraftId(int draftId);

    @Query("DELETE FROM drafts WHERE draftAccountId = :accountId")
    void deleteAllDrafts(int accountId);

}
