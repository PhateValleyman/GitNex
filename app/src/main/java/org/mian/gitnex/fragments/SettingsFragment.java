package org.mian.gitnex.fragments;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import org.mian.gitnex.R;
import org.mian.gitnex.activities.MainActivity;
import org.mian.gitnex.activities.SettingsAppearanceActivity;
import org.mian.gitnex.activities.SettingsDraftsActivity;
import org.mian.gitnex.activities.SettingsFileViewerActivity;
import org.mian.gitnex.activities.SettingsGeneralActivity;
import org.mian.gitnex.activities.SettingsNotificationsActivity;
import org.mian.gitnex.activities.SettingsReportsActivity;
import org.mian.gitnex.activities.SettingsSecurityActivity;
import org.mian.gitnex.activities.SettingsTranslationActivity;
import org.mian.gitnex.databinding.FragmentSettingsBinding;
import org.mian.gitnex.helpers.TinyDB;
import org.mian.gitnex.helpers.Version;

/**
 * Author M M Arif
 */

public class SettingsFragment extends Fragment {

	private Context ctx;
	private TinyDB tinyDB;

	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

		FragmentSettingsBinding fragmentSettingsBinding = FragmentSettingsBinding.inflate(inflater, container, false);

		ctx = getContext();
		tinyDB = TinyDB.getInstance(ctx);

		((MainActivity) requireActivity()).setActionBarTitle(getResources().getString(R.string.navSettings));

		LinearLayout generalFrame = fragmentSettingsBinding.generalFrame;
		LinearLayout appearanceFrame = fragmentSettingsBinding.appearanceFrame;
		LinearLayout fileViewerFrame = fragmentSettingsBinding.fileViewerFrame;
		LinearLayout draftsFrame = fragmentSettingsBinding.draftsFrame;
		LinearLayout securityFrame = fragmentSettingsBinding.securityFrame;
		LinearLayout notificationsFrame = fragmentSettingsBinding.notificationsFrame;
		LinearLayout languagesFrame = fragmentSettingsBinding.languagesFrame;
		LinearLayout reportsFrame = fragmentSettingsBinding.reportsFrame;
		LinearLayout rateAppFrame = fragmentSettingsBinding.rateAppFrame;
		LinearLayout aboutAppFrame = fragmentSettingsBinding.aboutAppFrame;

		if(new Version(tinyDB.getString("giteaVersion")).higherOrEqual("1.12.3")) {

			notificationsFrame.setVisibility(View.VISIBLE);
		}

		generalFrame.setOnClickListener(generalFrameCall -> startActivity(new Intent(ctx, SettingsGeneralActivity.class)));

		appearanceFrame.setOnClickListener(v1 -> startActivity(new Intent(ctx, SettingsAppearanceActivity.class)));

		fileViewerFrame.setOnClickListener(v1 -> startActivity(new Intent(ctx, SettingsFileViewerActivity.class)));

		draftsFrame.setOnClickListener(v1 -> startActivity(new Intent(ctx, SettingsDraftsActivity.class)));

		securityFrame.setOnClickListener(v1 -> startActivity(new Intent(ctx, SettingsSecurityActivity.class)));

		notificationsFrame.setOnClickListener(v1 -> startActivity(new Intent(ctx, SettingsNotificationsActivity.class)));

		languagesFrame.setOnClickListener(v1 -> startActivity(new Intent(ctx, SettingsTranslationActivity.class)));

		reportsFrame.setOnClickListener(v1 -> startActivity(new Intent(ctx, SettingsReportsActivity.class)));

		rateAppFrame.setOnClickListener(aboutApp -> rateThisApp());

		aboutAppFrame.setOnClickListener(aboutApp -> requireActivity().getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new AboutFragment()).commit());

		return fragmentSettingsBinding.getRoot();
	}

	public void rateThisApp() {

		try {

			startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + requireActivity().getPackageName())));
		}
		catch(ActivityNotFoundException e) {

			startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + requireActivity().getPackageName())));
		}
	}

	@Override
	public void onResume() {

		super.onResume();

		if(tinyDB.getBoolean("refreshParent")) {

			requireActivity().recreate();
			requireActivity().overridePendingTransition(0, 0);
			tinyDB.putBoolean("refreshParent", false);
		}
	}

}
