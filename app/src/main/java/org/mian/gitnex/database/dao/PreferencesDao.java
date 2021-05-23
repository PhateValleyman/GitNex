package org.mian.gitnex.database.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import org.mian.gitnex.database.models.GlobalPreference;
import org.mian.gitnex.database.models.LocalPreference;
import org.mian.gitnex.database.models.PreferencesGroup;
import java.util.List;

/**
 * @author opyale
 */

@Dao
public interface PreferencesDao {

	// Preferences groups

	@Insert
	long createPreferencesGroup(PreferencesGroup preferencesGroup);

	@Delete
	void deletePreferencesGroup(PreferencesGroup preferencesGroup);

	@Update
	void updatePreferencesGroup(PreferencesGroup preferencesGroup);

	@Query("SELECT * FROM preferencesGroups WHERE `name` = :name")
	PreferencesGroup getPreferencesGroupWithName(String name);

	// Global preferences

	@Insert
	long createGlobalPreference(GlobalPreference globalPreference);

	@Delete
	void deleteGlobalPreference(GlobalPreference globalPreference);

	@Update
	void updateGlobalPreference(GlobalPreference globalPreference);

	@Query("SELECT * FROM globalPreferences WHERE `preferencesGroupId` = :preferencesGroupId")
	List<GlobalPreference> getGlobalPreferencesForPreferencesGroup(int preferencesGroupId);

	@Query("SELECT * FROM globalPreferences WHERE `key` = :key")
	GlobalPreference getGlobalPreferenceWithKey(String key);

	@Query("SELECT * FROM globalPreferences WHERE `value` = :value")
	List<GlobalPreference> getGlobalPreferencesWithValue(String value);

	// Local preferences

	@Insert
	long createLocalPreference(LocalPreference localPreference);

	@Delete
	void deleteLocalPreference(LocalPreference localPreference);

	@Update
	void updateLocalPreference(LocalPreference localPreference);

	@Query("SELECT * FROM localPreferences WHERE `userAccountId` = :accountId")
	List<LocalPreference> getLocalPreferencesForAccount(int accountId);

	@Query("SELECT * FROM localPreferences WHERE `preferencesGroupId` = :preferencesGroupId")
	List<LocalPreference> getLocalPreferencesForPreferencesGroup(int preferencesGroupId);

	@Query("SELECT * FROM localPreferences WHERE `key` = :key")
	LocalPreference getLocalPreferenceWithKey(String key);

	@Query("SELECT * FROM localPreferences WHERE `value` = :value")
	List<LocalPreference> getLocalPreferencesWithValue(String value);

}
