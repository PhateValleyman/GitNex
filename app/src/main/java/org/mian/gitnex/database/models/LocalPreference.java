package org.mian.gitnex.database.models;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;
import org.jetbrains.annotations.NotNull;
import java.io.Serializable;

/**
 * @author opyale
 */

@Entity(
	tableName = "LocalPreferences",
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

	@NonNull
	private String key;
	@Nullable
	private String value;

	public LocalPreference(long userAccountId, long preferencesGroupId, @NotNull String key, @Nullable String value) {
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

	@NotNull
	public String getKey() {
		return key;
	}

	public void setKey(@NotNull String key) {
		this.key = key;
	}

	@Nullable
	public String getValue() {
		return value;
	}

	public void setValue(@Nullable String value) {
		this.value = value;
	}

}
