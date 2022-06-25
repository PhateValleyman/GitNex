package org.mian.gitnex.fragments;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import com.google.android.material.tabs.TabLayoutMediator;
import org.jetbrains.annotations.NotNull;
import org.mian.gitnex.R;
import org.mian.gitnex.databinding.FragmentPrChangesBinding;

/**
 * @author qwerty287
 */
public class PullRequestChangesFragment extends Fragment {

	private FragmentPrChangesBinding binding;
	private final DiffFilesFragment diffFilesFragment = DiffFilesFragment.newInstance();
	private final PullRequestCommitsFragment pullRequestCommitsFragment = PullRequestCommitsFragment.newInstance();

	public PullRequestChangesFragment() {}

	public static PullRequestChangesFragment newInstance() {
		return new PullRequestChangesFragment();
	}

	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		binding = FragmentPrChangesBinding.inflate(inflater, container, false);

		binding.close.setOnClickListener(v -> requireActivity().finish());

		binding.container.setAdapter(new FragmentStateAdapter(requireActivity()) {

			@NonNull
			@Override
			public Fragment createFragment(int position) {

				if(position == 0) {
					return pullRequestCommitsFragment;
				} else {
					return diffFilesFragment;
				}
			}

			@Override
			public int getItemCount() {

				return 2;
			}
		});
		String[] tabs = new String[]{getString(R.string.commits), getString(R.string.tabTextFiles)};
		new TabLayoutMediator(binding.tabs, binding.container, (tab, position) -> tab.setText(tabs[position])).attach();

		return binding.getRoot();

	}

}
