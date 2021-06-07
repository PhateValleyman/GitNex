package org.mian.gitnex.database;

import android.content.Context;
import org.mian.gitnex.database.api.BaseApi;
import org.mian.gitnex.database.api.PreferencesApi;
import org.mian.gitnex.database.models.GlobalPreference;
import org.mian.gitnex.database.models.LocalPreference;
import org.mian.gitnex.database.models.PreferencesGroup;
import org.mian.gitnex.helpers.TinyDB;

/**
 * @author opyale
 */

public class TinyDbMigrator {

	private static TinyDB tinyDB;
	private static PreferencesApi preferencesApi;

	public TinyDbMigrator(Context context) {
		tinyDB = TinyDB.getInstance(context);
		preferencesApi = BaseApi.getInstance(context, PreferencesApi.class);
	}

	public void migrate() {

		toGlobalPreference("session", "currentActiveAccountId", "current_account_id");

		toGlobalPreference("preferences", "enableCounterBadges", "enable_counter_badges");
		toGlobalPreference("preferences", "crashReportingEnabled", "crash_reporting_enabled");
		toGlobalPreference("preferences", "cacheSizeStr", "cache_size");
		toGlobalPreference("preferences", "cacheSizeImagesStr", "cache_images_size");
		toGlobalPreference("preferences", "draftsCommentsDeletionEnabled", "drafts_comments_deletion_enabled");
		toGlobalPreference("preferences", "locale", "current_locale");

	}

	private void toLocalPreference(String groupName, int accountId, String tinyDbKey, String preferenceKey) {
		PreferencesGroup preferencesGroup = preferencesApi.getOrCreatePreferencesGroupWithName(groupName);

		LocalPreference localPreference = new LocalPreference(accountId, preferencesGroup.getId(), preferenceKey, tinyDB.getString(tinyDbKey));
		preferencesApi.createLocalPreference(localPreference);
	}

	private void toGlobalPreference(String groupName, String tinyDbKey, String preferenceKey) {
		PreferencesGroup preferencesGroup = preferencesApi.getOrCreatePreferencesGroupWithName(groupName);

		GlobalPreference globalPreference = new GlobalPreference(preferencesGroup.getId(), preferenceKey, tinyDB.getString(tinyDbKey));
		preferencesApi.createGlobalPreference(globalPreference);
	}
}
