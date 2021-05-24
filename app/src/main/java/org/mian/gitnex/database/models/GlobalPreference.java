package org.mian.gitnex.database.models;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;
import java.io.Serializable;

/**
 * @author opyale
 */

@Entity(
	tableName = "GlobalPreferences",
	foreignKeys = @ForeignKey(entity = PreferencesGroup.class, parentColumns = "id", childColumns = "preferencesGroupId", onDelete = ForeignKey.CASCADE),
	indices = @Index(value = {"preferencesGroupId", "key"}, unique = true)
)
public class GlobalPreference implements Serializable {

	@PrimaryKey(autoGenerate = true)
	private long id;

	private long preferencesGroupId;

	@NonNull
	private String key;
	@Nullable
	private String value;

	public GlobalPreference(long preferencesGroupId, @NonNull String key, @Nullable String value) {
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

	public long getPreferencesGroupId() {
		return preferencesGroupId;
	}

	public void setPreferencesGroupId(long preferencesGroupId) {
		this.preferencesGroupId = preferencesGroupId;
	}

	@NonNull
	public String getKey() {
		return key;
	}

	public void setKey(@NonNull String key) {
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
