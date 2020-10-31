package org.mian.gitnex.fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import org.mian.gitnex.R;
import org.mian.gitnex.activities.MainActivity;
import org.mian.gitnex.databinding.FragmentAboutBinding;
import org.mian.gitnex.helpers.AppUtil;
import org.mian.gitnex.helpers.TinyDB;

/**
 * Author M M Arif
 */

public class AboutFragment extends Fragment {

	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		FragmentAboutBinding viewBinding = FragmentAboutBinding.inflate(inflater, container, false);
        TinyDB tinyDb = TinyDB.getInstance(getContext());

	    viewBinding.appVersion.setText(AppUtil.getAppVersion(requireContext()));
	    viewBinding.userServerVersion.setText(tinyDb.getString("giteaVersion"));
	    viewBinding.appBuild.setText(String.valueOf(AppUtil.getAppBuildNo(requireContext())));

		((MainActivity) requireActivity()).setActionBarTitle(getResources().getString(R.string.pageTitleAbout));

		viewBinding.donationLinkLiberapay.setOnClickListener(v1 -> {

            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_VIEW);
            intent.addCategory(Intent.CATEGORY_BROWSABLE);
            intent.setData(Uri.parse(getResources().getString(R.string.supportLink)));
            startActivity(intent);
        });

		viewBinding.donationLinkPatreon.setOnClickListener(v12 -> {

            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_VIEW);
            intent.addCategory(Intent.CATEGORY_BROWSABLE);
            intent.setData(Uri.parse(getResources().getString(R.string.supportLinkPatreon)));
            startActivity(intent);
        });

		viewBinding.translateLink.setOnClickListener(v13 -> {

            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_VIEW);
            intent.addCategory(Intent.CATEGORY_BROWSABLE);
            intent.setData(Uri.parse(getResources().getString(R.string.crowdInLink)));
            startActivity(intent);
        });

		viewBinding.appWebsite.setOnClickListener(v14 -> {

            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_VIEW);
            intent.addCategory(Intent.CATEGORY_BROWSABLE);
            intent.setData(Uri.parse(getResources().getString(R.string.appWebsiteLink)));
            startActivity(intent);
        });

		if(AppUtil.isPro(requireContext())) {

			viewBinding.supportHeader.setVisibility(View.GONE);
			viewBinding.dividerSupport.setVisibility(View.GONE);
			viewBinding.donationLinkLiberapay.setVisibility(View.GONE);
			viewBinding.donationLinkPatreon.setVisibility(View.GONE);
		}

	    return viewBinding.getRoot();
    }

}
