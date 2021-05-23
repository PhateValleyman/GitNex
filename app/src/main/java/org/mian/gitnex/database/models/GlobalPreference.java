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
	tableName = "globalPreferences",
	foreignKeys = @ForeignKey(entity = PreferencesGroup.class, parentColumns = "id", childColumns = "preferencesGroupId", onDelete = ForeignKey.CASCADE),
	indices = @Index(value = {"preferencesGroupId", "key"}, unique = true)
)
public class GlobalPreference implements Serializable {

	@PrimaryKey(autoGenerate = true)
	private long id;

	private long preferencesGroupId;

	private String key;
	private String value;

	public GlobalPreference(long preferencesGroupId, String key, String value) {
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
