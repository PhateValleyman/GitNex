package org.mian.gitnex.database.models;

import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;
import java.io.Serializable;

/**
 * @author opyale
 */

@Entity(
	tableName = "preferencesGroups",
	indices = @Index(value = "name", unique = true))
public class PreferencesGroup implements Serializable {

	@PrimaryKey(autoGenerate = true)
	private int id;

	private String name;

	public PreferencesGroup(String name) {
		this.name = name;
	}

	public int getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

}
