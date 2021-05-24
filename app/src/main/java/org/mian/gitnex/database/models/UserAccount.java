package org.mian.gitnex.database.models;

import androidx.annotation.Nullable;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import java.io.Serializable;

/**
 * Author M M Arif
 */

@Entity(tableName = "UserAccounts")
public class UserAccount implements Serializable {

    @PrimaryKey(autoGenerate = true)
    private int accountId;

    @Nullable
    private String accountName;
    private String instanceUrl;
    private String userName;
    private String token;
    @Nullable
    private String serverVersion;

    public int getAccountId() {
        return accountId;
    }

    public void setAccountId(int accountId) {
        this.accountId = accountId;
    }

    @Nullable
    public String getAccountName() {
        return accountName;
    }

    public void setAccountName(@Nullable String accountName) {
        this.accountName = accountName;
    }

    public String getInstanceUrl() {
        return instanceUrl;
    }

    public void setInstanceUrl(String instanceUrl) {
        this.instanceUrl = instanceUrl;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    @Nullable
    public String getServerVersion() {
        return serverVersion;
    }

    public void setServerVersion(@Nullable String serverVersion) {
        this.serverVersion = serverVersion;
    }
}
