package org.mian.gitnex.fragments;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;
import com.squareup.picasso.Picasso;

import org.mian.gitnex.R;
import org.mian.gitnex.activities.MainActivity;
import org.mian.gitnex.helpers.RoundedTransformation;
import org.mian.gitnex.util.TinyDB;

import java.util.Objects;

/**
 * Author M M Arif
 */

public class ProfileFragment extends Fragment {

    private Context ctx = getContext();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_profile, container, false);
        ((MainActivity) Objects.requireNonNull(getActivity())).setActionBarTitle(getResources().getString(R.string.pageTitleProfile));
        setHasOptionsMenu(true);

        TinyDB tinyDb = new TinyDB(getContext());

        TextView userFullName = v.findViewById(R.id.userFullName);
        ImageView userAvatar = v.findViewById(R.id.userAvatar);
        TextView userLogin = v.findViewById(R.id.userLogin);
        TextView userEmail = v.findViewById(R.id.userEmail);

        userFullName.setText(tinyDb.getString("userFullname"));
        Picasso.get().load(tinyDb.getString("userAvatar")).transform(new RoundedTransformation(8, 0)).resize(120, 120).centerCrop().into(userAvatar);
        userLogin.setText(getString(R.string.usernameWithAt, tinyDb.getString("userLogin")));
        userEmail.setText(tinyDb.getString("userEmail"));

        ProfileFragment.SectionsPagerAdapter mSectionsPagerAdapter = new ProfileFragment.SectionsPagerAdapter(getFragmentManager());

        ViewPager mViewPager = v.findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        TabLayout tabLayout = v.findViewById(R.id.tabs);

        mViewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.addOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(mViewPager));

        return v;

    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        menu.clear();
        Objects.requireNonNull(getActivity()).getMenuInflater().inflate(R.menu.profile_dotted_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        int id = item.getItemId();

        switch (id) {
            case android.R.id.home:
                ((MainActivity) ctx).finish();
                return true;
            case R.id.profileMenu:
                ProfileBottomSheetFragment bottomSheet = new ProfileBottomSheetFragment();
                assert getFragmentManager() != null;
                bottomSheet.show(getFragmentManager(), "profileBottomSheet");
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }

    public class SectionsPagerAdapter extends FragmentStatePagerAdapter {

        SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @NonNull
        @Override
        public Fragment getItem(int position) {

            Fragment fragment = null;
            switch (position) {
                case 0: // followers
                    return ProfileFollowersFragment.newInstance("repoOwner", "repoName");
                case 1: // following
                    return ProfileFollowingFragment.newInstance("repoOwner", "repoName");
                case 2: // emails
                    return ProfileEmailsFragment.newInstance("repoOwner", "repoName");
            }
            return fragment;
        }

        @Override
        public int getCount() {
            return 3;
        }

    }

}
