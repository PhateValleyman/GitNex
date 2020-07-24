package org.mian.gitnex.fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import org.mian.gitnex.R;
import org.mian.gitnex.activities.SettingsAppearanceActivity;
import org.mian.gitnex.activities.SettingsDraftsActivity;
import org.mian.gitnex.activities.SettingsFileViewerActivity;
import org.mian.gitnex.activities.SettingsNotificationsActivity;
import org.mian.gitnex.activities.SettingsReportsActivity;
import org.mian.gitnex.activities.SettingsSecurityActivity;
import org.mian.gitnex.activities.SettingsTranslationActivity;
import org.mian.gitnex.helpers.TinyDB;
import org.mian.gitnex.helpers.Version;
import java.util.Objects;

/**
 * Author M M Arif
 */

public class SettingsFragment extends Fragment {

	private Context ctx;
	private TinyDB tinyDB;

	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

		View v = inflater.inflate(R.layout.fragment_settings, container, false);

		LinearLayout appearanceFrame = v.findViewById(R.id.appearanceFrame);
		LinearLayout fileViewerFrame = v.findViewById(R.id.fileViewerFrame);
		LinearLayout draftsFrame = v.findViewById(R.id.draftsFrame);
		LinearLayout securityFrame = v.findViewById(R.id.securityFrame);
		LinearLayout notificationsFrame = v.findViewById(R.id.notificationsFrame);
		LinearLayout languagesFrame = v.findViewById(R.id.languagesFrame);
		LinearLayout reportsFrame = v.findViewById(R.id.reportsFrame);

		ctx = getContext();
		tinyDB = new TinyDB(ctx);

		if(new Version(tinyDB.getString("giteaVersion")).higherOrEqual("1.12.3")) {

			notificationsFrame.setVisibility(View.VISIBLE);
		}

		appearanceFrame.setOnClickListener(v1 -> startActivity(new Intent(ctx, SettingsAppearanceActivity.class)));

		fileViewerFrame.setOnClickListener(v1 -> startActivity(new Intent(ctx, SettingsFileViewerActivity.class)));

		draftsFrame.setOnClickListener(v1 -> startActivity(new Intent(ctx, SettingsDraftsActivity.class)));

		securityFrame.setOnClickListener(v1 -> startActivity(new Intent(ctx, SettingsSecurityActivity.class)));

		notificationsFrame.setOnClickListener(v1 -> startActivity(new Intent(ctx, SettingsNotificationsActivity.class)));

		languagesFrame.setOnClickListener(v1 -> startActivity(new Intent(ctx, SettingsTranslationActivity.class)));

		reportsFrame.setOnClickListener(v1 -> startActivity(new Intent(ctx, SettingsReportsActivity.class)));

		return v;

	}

	@Override
	public void onResume() {

		super.onResume();

		if(tinyDB.getBoolean("refreshParent")) {

			Objects.requireNonNull(getActivity()).recreate();
			getActivity().overridePendingTransition(0, 0);
			tinyDB.putBoolean("refreshParent", false);

		}
	}

}
