package org.mian.gitnex.database.api;

import android.content.Context;
import org.mian.gitnex.database.dao.PreferencesDao;
import org.mian.gitnex.database.models.GlobalPreference;
import org.mian.gitnex.database.models.LocalPreference;
import org.mian.gitnex.database.models.PreferencesGroup;
import java.util.List;

public class PreferencesApi extends BaseApi {

	private final PreferencesDao preferencesDao;

	protected PreferencesApi(Context context) {
		super(context);
		preferencesDao = gitnexDatabase.preferencesDao();
	}

	public long createPreferencesGroup(PreferencesGroup preferencesGroup) {
		return preferencesDao.createPreferencesGroup(preferencesGroup);
	}

	public void deletePreferencesGroup(PreferencesGroup preferencesGroup) {
		preferencesDao.deletePreferencesGroup(preferencesGroup);
	}

	public void updatePreferencesGroup(PreferencesGroup preferencesGroup) {
		preferencesDao.updatePreferencesGroup(preferencesGroup);
	}

	public PreferencesGroup getPreferencesGroupWithName(String name) {
		return preferencesDao.getPreferencesGroupWithName(name);
	}

	public PreferencesGroup getOrCreatePreferencesGroupWithName(String name) {
		PreferencesGroup preferencesGroup = preferencesDao.getPreferencesGroupWithName(name);

		if(preferencesGroup == null) {
			preferencesGroup = new PreferencesGroup(name);
			preferencesGroup.setId(preferencesDao.createPreferencesGroup(preferencesGroup));
		}

		return preferencesGroup;
	}

	public long createGlobalPreference(GlobalPreference globalPreference) {
		return preferencesDao.createGlobalPreference(globalPreference);
	}

	public void createOrUpdateGlobalPreference(GlobalPreference globalPreference) {
		if(preferencesDao.getGlobalPreferenceWithKey(globalPreference.getKey()) == null) {
			preferencesDao.createGlobalPreference(globalPreference);
		} else {
			preferencesDao.updateGlobalPreference(globalPreference);
		}
	}

	public void deleteGlobalPreference(GlobalPreference globalPreference) {
		preferencesDao.deleteGlobalPreference(globalPreference);
	}

	public void updateGlobalPreference(GlobalPreference globalPreference) {
		preferencesDao.updateGlobalPreference(globalPreference);
	}

	public List<GlobalPreference> getGlobalPreferencesForPreferencesGroup(int preferencesGroupId) {
		return preferencesDao.getGlobalPreferencesForPreferencesGroup(preferencesGroupId);
	}

	public GlobalPreference getGlobalPreferenceWithKey(String key) {
		return preferencesDao.getGlobalPreferenceWithKey(key);
	}

	public List<GlobalPreference> getGlobalPreferencesWithValue(String value) {
		return preferencesDao.getGlobalPreferencesWithValue(value);
	}

	public long createLocalPreference(LocalPreference localPreference) {
		return preferencesDao.createLocalPreference(localPreference);
	}

	public void createOrUpdateLocalPreference(LocalPreference localPreference) {
		if(preferencesDao.getLocalPreferenceWithKey(localPreference.getKey()) == null) {
			preferencesDao.createLocalPreference(localPreference);
		} else {
			preferencesDao.updateLocalPreference(localPreference);
		}
	}

	public void deleteLocalPreference(LocalPreference localPreference) {
		preferencesDao.deleteLocalPreference(localPreference);
	}

	public void updateLocalPreference(LocalPreference localPreference) {
		preferencesDao.updateLocalPreference(localPreference);
	}

	public List<LocalPreference> getLocalPreferencesForAccount(int accountId) {
		return preferencesDao.getLocalPreferencesForAccount(accountId);
	}

	public List<LocalPreference> getLocalPreferencesForPreferencesGroup(int preferencesGroupId) {
		return preferencesDao.getLocalPreferencesForPreferencesGroup(preferencesGroupId);
	}

	public LocalPreference getLocalPreferenceWithKey(String key) {
		return preferencesDao.getLocalPreferenceWithKey(key);
	}

	public List<LocalPreference> getLocalPreferencesWithValue(String value) {
		return preferencesDao.getLocalPreferencesWithValue(value);
	}
}
