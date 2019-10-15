package org.mian.gitnex.database.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import org.mian.gitnex.database.models.CommentsDraft;
import java.util.List;

/**
 * Author M M Arif
 */

@Dao
public interface CommentsDraftDao {

    @Insert
    void insertComment(CommentsDraft commentsDraft);

    @Query("SELECT * FROM commentsDraft WHERE draftAccountId = :accountId ORDER BY draftId DESC")
    LiveData<List<CommentsDraft>> fetchAllDrafts(int accountId);

    @Query("SELECT * FROM commentsDraft WHERE draftAccountId = :accountId and draftRepositoryId = :repositoryId")
    LiveData<CommentsDraft> fetchSingleDraftByAccountIdAndRepositoryId(int accountId, int repositoryId);

    @Query("SELECT * FROM commentsDraft WHERE draftId = :draftId")
    LiveData<CommentsDraft> fetchDraftById(int draftId);

    @Query("SELECT * FROM commentsDraft WHERE issueId = :issueId")
    LiveData<CommentsDraft> fetchDraftByIssueId(int issueId);

    @Query("UPDATE commentsDraft SET draftText= :draftText WHERE draftId = :draftId")
    void updateDraft(String draftText, int draftId);

    @Query("DELETE FROM commentsDraft WHERE draftId = :draftId")
    void deleteByDraftId(int draftId);

    @Query("DELETE FROM commentsDraft WHERE draftAccountId = :accountId")
    void deleteAllDrafts(int accountId);

}
