package org.mian.gitnex.fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import org.mian.gitnex.R;
import org.mian.gitnex.activities.CreditsActivity;
import org.mian.gitnex.activities.SponsorsActivity;
import org.mian.gitnex.util.AppUtil;
import org.mian.gitnex.util.TinyDB;
import java.util.Objects;
import de.psdev.licensesdialog.LicensesDialog;
import de.psdev.licensesdialog.licenses.ApacheSoftwareLicense20;
import de.psdev.licensesdialog.model.Notice;
import de.psdev.licensesdialog.model.Notices;

/**
 * Author M M Arif
 */

public class AboutFragment extends Fragment {

    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_about, container, false);

        TinyDB tinyDb = new TinyDB(getContext());

        final TextView appVerBuild;
        final TextView donationLink;
        final TextView donationLinkPatreon;
        final TextView translateLink;
        final TextView creditsButton;
        final TextView sponsorsButton;
        final TextView appWebsite;
        final TextView appRepo;
        final TextView openSourceLicenses;

        appVerBuild = view.findViewById(R.id.appVerBuild);
        TextView viewTextGiteaVersion = view.findViewById(R.id.giteaVersion);
        creditsButton = view.findViewById(R.id.creditsButton);
        donationLink = view.findViewById(R.id.donationLink);
        donationLinkPatreon = view.findViewById(R.id.donationLinkPatreon);
        translateLink = view.findViewById(R.id.translateLink);
        sponsorsButton = view.findViewById(R.id.sponsorsButton);
        appWebsite = view.findViewById(R.id.appWebsite);
        appRepo = view.findViewById(R.id.appRepo);
        openSourceLicenses = view.findViewById(R.id.openSourceLicenses);

        appVerBuild.setText(getString(R.string.appVerBuild, AppUtil.getAppVersion(Objects.requireNonNull(getContext())), AppUtil.getAppBuildNo(getContext())));

        donationLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_VIEW);
                intent.addCategory(Intent.CATEGORY_BROWSABLE);
                intent.setData(Uri.parse(getResources().getString(R.string.supportLink)));
                startActivity(intent);
            }
        });

        donationLinkPatreon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_VIEW);
                intent.addCategory(Intent.CATEGORY_BROWSABLE);
                intent.setData(Uri.parse(getResources().getString(R.string.supportLinkPatreon)));
                startActivity(intent);
            }
        });

        translateLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_VIEW);
                intent.addCategory(Intent.CATEGORY_BROWSABLE);
                intent.setData(Uri.parse(getResources().getString(R.string.crowdInLink)));
                startActivity(intent);
            }
        });

        appWebsite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_VIEW);
                intent.addCategory(Intent.CATEGORY_BROWSABLE);
                intent.setData(Uri.parse(getResources().getString(R.string.appWebsiteLink)));
                startActivity(intent);
            }
        });

        appRepo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_VIEW);
                intent.addCategory(Intent.CATEGORY_BROWSABLE);
                intent.setData(Uri.parse(getResources().getString(R.string.appRepoLink)));
                startActivity(intent);
            }
        });

        openSourceLicenses.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showLicenseDialog();
            }
        });

        creditsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getContext(), CreditsActivity.class));
            }
        });

        sponsorsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getContext(), SponsorsActivity.class));
            }
        });

        String commit = getResources().getString(R.string.commitPage) + tinyDb.getString("giteaVersion");
        viewTextGiteaVersion.setText(commit);

        return view;
    }


    private void showLicenseDialog() {
        final Notices notices = new Notices();
        notices.addNotice(new Notice("Retrofit", "https://square.github.io/retrofit/", "Square, Inc.", new ApacheSoftwareLicense20()));

        new LicensesDialog.Builder(Objects.requireNonNull(getActivity()))
                .setNotices(notices)
                .setIncludeOwnLicense(true)
                .build()
                .show();
    }

}
