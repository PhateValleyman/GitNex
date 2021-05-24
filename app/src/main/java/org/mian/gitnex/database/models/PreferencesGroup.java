package org.mian.gitnex.database.models;

import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;
import java.io.Serializable;

/**
 * @author opyale
 */

@Entity(
	tableName = "PreferencesGroups",
	indices = @Index(value = "name", unique = true))
public class PreferencesGroup implements Serializable {

	@PrimaryKey(autoGenerate = true)
	private long id;

	private String name;

	public PreferencesGroup(String name) {
		this.name = name;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

}
