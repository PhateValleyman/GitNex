package org.mian.gitnex.database.models;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;
import java.io.Serializable;
import static androidx.room.ForeignKey.CASCADE;

/**
 * Author M M Arif
 */

@Entity(tableName = "commentsDraft", foreignKeys = @ForeignKey(entity = Repositories.class,
        parentColumns = "repositoryId",
        childColumns = "draftRepositoryId",
        onDelete = CASCADE),
        indices = {@Index("draftRepositoryId")})
public class CommentsDraft implements Serializable {

    @PrimaryKey(autoGenerate = true)
    private int draftId;

    private int draftRepositoryId;
    private int draftAccountId;
    private int issueId;
    private String draftText;

    public int getDraftId() {
        return draftId;
    }

    public void setDraftId(int draftId) {
        this.draftId = draftId;
    }

    public int getDraftRepositoryId() {
        return draftRepositoryId;
    }

    public void setDraftRepositoryId(int draftRepositoryId) {
        this.draftRepositoryId = draftRepositoryId;
    }

    public int getDraftAccountId() {
        return draftAccountId;
    }

    public void setDraftAccountId(int draftAccountId) {
        this.draftAccountId = draftAccountId;
    }

    public int getIssueId() {
        return issueId;
    }

    public void setIssueId(int issueId) {
        this.issueId = issueId;
    }

    public String getDraftText() {
        return draftText;
    }

    public void setDraftText(String draftText) {
        this.draftText = draftText;
    }
}