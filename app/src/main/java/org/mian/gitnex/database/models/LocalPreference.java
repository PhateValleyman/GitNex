package org.mian.gitnex.database.models;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;
import java.io.Serializable;

/**
 * @author opyale
 */

@Entity(
	tableName = "localPreferences",
	foreignKeys = {
		@ForeignKey(entity = UserAccount.class, parentColumns = "accountId", childColumns = "userAccountId", onDelete = ForeignKey.CASCADE),
		@ForeignKey(entity = PreferencesGroup.class, parentColumns = "id", childColumns = "preferencesGroupId", onDelete = ForeignKey.CASCADE)
	},
	indices = @Index(value = {"userAccountId", "preferencesGroupId", "key"}, unique = true)
)
public class LocalPreference implements Serializable {

	@PrimaryKey(autoGenerate = true)
	private long id;

	private long userAccountId;
	private long preferencesGroupId;

	private String key;
	private String value;

	public LocalPreference(long userAccountId, long preferencesGroupId, String key, String value) {
		this.userAccountId = userAccountId;
		this.preferencesGroupId = preferencesGroupId;
		this.key = key;
		this.value = value;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public long getUserAccountId() {
		return userAccountId;
	}

	public void setUserAccountId(long userAccountId) {
		this.userAccountId = userAccountId;
	}

	public long getPreferencesGroupId() {
		return preferencesGroupId;
	}

	public void setPreferencesGroupId(long preferencesGroupId) {
		this.preferencesGroupId = preferencesGroupId;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

}
