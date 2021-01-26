package org.mian.gitnex.fragments;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import org.mian.gitnex.R;
import org.mian.gitnex.activities.FileViewActivity;
import org.mian.gitnex.activities.RepoDetailActivity;
import org.mian.gitnex.adapters.FilesAdapter;
import org.mian.gitnex.databinding.FragmentFilesBinding;
import org.mian.gitnex.helpers.AppUtil;
import org.mian.gitnex.helpers.Authorization;
import org.mian.gitnex.viewmodels.FilesViewModel;
import java.util.ArrayList;
import java.util.Collections;
import moe.feng.common.view.breadcrumbs.BreadcrumbsView;
import moe.feng.common.view.breadcrumbs.DefaultBreadcrumbsCallback;
import moe.feng.common.view.breadcrumbs.model.BreadcrumbItem;

/**
 * Author M M Arif
 */

public class FilesFragment extends Fragment implements FilesAdapter.FilesAdapterListener {

	private ProgressBar mProgressBar;
	private FilesAdapter adapter;
	private RecyclerView mRecyclerView;
	private TextView noDataFiles;
	private LinearLayout filesFrame;
	private TextView fileStructure;
	private static String repoNameF = "param2";
	private static String repoOwnerF = "param1";
	private static String repoRefF = "param3";
	private BreadcrumbsView mBreadcrumbsView;

	private String repoName;
	private String repoOwner;
	private String ref;

	private OnFragmentInteractionListener mListener;

	public FilesFragment() {

	}

	public static FilesFragment newInstance(String param1, String param2, String param3) {

		FilesFragment fragment = new FilesFragment();
		Bundle args = new Bundle();
		args.putString(repoOwnerF, param1);
		args.putString(repoNameF, param2);
		args.putString(repoRefF, param3);
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		if(getArguments() != null) {
			repoName = getArguments().getString(repoNameF);
			repoOwner = getArguments().getString(repoOwnerF);
			ref = getArguments().getString(repoRefF);
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		FragmentFilesBinding fragmentFilesBinding = FragmentFilesBinding.inflate(inflater, container, false);
		setHasOptionsMenu(true);

		noDataFiles = fragmentFilesBinding.noDataFiles;
		filesFrame = fragmentFilesBinding.filesFrame;

		fileStructure = fragmentFilesBinding.fileStructure;
		mRecyclerView = fragmentFilesBinding.recyclerView;
		mRecyclerView.setHasFixedSize(true);
		mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

		DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(mRecyclerView.getContext(), DividerItemDecoration.VERTICAL);
		mRecyclerView.addItemDecoration(dividerItemDecoration);

		mProgressBar = fragmentFilesBinding.progressBar;

		mBreadcrumbsView = fragmentFilesBinding.breadcrumbsView;
		mBreadcrumbsView.setItems(new ArrayList<>(Collections.singletonList(BreadcrumbItem.createSimpleItem(getResources().getString(R.string.filesBreadcrumbRoot) + getResources().getString(R.string.colonDivider) + ref))));

		((RepoDetailActivity) requireActivity()).setFragmentRefreshListenerFiles(repoBranch -> {

			fileStructure.setText("");
			ref = repoBranch;
			mBreadcrumbsView.setItems(new ArrayList<>(Collections.singletonList(BreadcrumbItem.createSimpleItem(getResources().getString(R.string.filesBreadcrumbRoot) + getResources().getString(R.string.colonDivider) + ref))));
			fetchDataAsync(Authorization.get(getContext()), repoOwner, repoName, repoBranch);

		});

		fetchDataAsync(Authorization.get(getContext()), repoOwner, repoName, ref);

		return fragmentFilesBinding.getRoot();
	}

	@Override
	public void onResume() {

		super.onResume();
	}

	@Override
	public void onClickDir(String dirName) {

		StringBuilder breadcrumbBuilder = new StringBuilder();

		breadcrumbBuilder.append(fileStructure.getText().toString()).append("/").append(dirName);

		fileStructure.setText(breadcrumbBuilder);

		String dirName_ = fileStructure.getText().toString();
		dirName_ = dirName_.startsWith("/") ? dirName_.substring(1) : dirName_;
		final String finalDirName_ = dirName_;

		mBreadcrumbsView.addItem(new BreadcrumbItem(Collections.singletonList(dirName)));
		//noinspection unchecked
		mBreadcrumbsView.setCallback(new DefaultBreadcrumbsCallback<BreadcrumbItem>() {

			@SuppressLint("SetTextI18n")
			@Override
			public void onNavigateBack(BreadcrumbItem item, int position) {

				if(position == 0) {

					fetchDataAsync(Authorization.get(getContext()), repoOwner, repoName, ref);
					fileStructure.setText("");
					return;
				}

				String filterDir = fileStructure.getText().toString();
				String result = filterDir.substring(0, filterDir.indexOf(item.getSelectedItem()));
				fileStructure.setText(result + item.getSelectedItem());

				String currentIndex = (result + item.getSelectedItem()).substring(1);

				fetchDataAsyncSub(Authorization.get(getContext()), repoOwner, repoName, currentIndex, ref);

			}

			@Override
			public void onNavigateNewLocation(BreadcrumbItem newItem, int changedPosition) {

			}
		});

		fetchDataAsyncSub(Authorization.get(getContext()), repoOwner, repoName, finalDirName_, ref);

	}

	@Override
	public void onClickFile(String fileName) {

		Intent intent = new Intent(getContext(), FileViewActivity.class);

		if(!fileStructure.getText().toString().equals("Root")) {

			intent.putExtra("singleFileName", fileStructure.getText().toString() + "/" + fileName);
		}
		else {

			intent.putExtra("singleFileName", fileName);
		}

		requireContext().startActivity(intent);
	}

	private void fetchDataAsync(String instanceToken, String owner, String repo, String ref) {

		mRecyclerView.setVisibility(View.GONE);
		mProgressBar.setVisibility(View.VISIBLE);

		FilesViewModel filesModel = new ViewModelProvider(this).get(FilesViewModel.class);

		filesModel.getFilesList(instanceToken, owner, repo, ref, getContext(), mProgressBar, noDataFiles).observe(getViewLifecycleOwner(), filesListMain -> {

			adapter = new FilesAdapter(getContext(), filesListMain, FilesFragment.this);
			mBreadcrumbsView.removeItemAfter(1);

			if(adapter.getItemCount() > 0) {

				mRecyclerView.setAdapter(adapter);
				AppUtil.setMultiVisibility(View.VISIBLE, mRecyclerView, filesFrame);
				noDataFiles.setVisibility(View.GONE);
			}
			else {

				adapter.notifyDataSetChanged();
				mRecyclerView.setAdapter(adapter);
				AppUtil.setMultiVisibility(View.VISIBLE, mRecyclerView, filesFrame, noDataFiles);
			}

			filesFrame.setVisibility(View.VISIBLE);
			mProgressBar.setVisibility(View.GONE);
		});

	}

	private void fetchDataAsyncSub(String instanceToken, String owner, String repo, String filesDir, String ref) {

		mRecyclerView.setVisibility(View.GONE);
		mProgressBar.setVisibility(View.VISIBLE);

		FilesViewModel filesModel2 = new ViewModelProvider(this).get(FilesViewModel.class);

		filesModel2.getFilesList2(instanceToken, owner, repo, filesDir, ref, getContext(), mProgressBar, noDataFiles).observe(this, filesListMain2 -> {

			adapter = new FilesAdapter(getContext(), filesListMain2, FilesFragment.this);

			if(adapter.getItemCount() > 0) {
				mRecyclerView.setAdapter(adapter);
				AppUtil.setMultiVisibility(View.VISIBLE, mRecyclerView, filesFrame);
				noDataFiles.setVisibility(View.GONE);
			}
			else {
				adapter.notifyDataSetChanged();
				mRecyclerView.setAdapter(adapter);
				AppUtil.setMultiVisibility(View.VISIBLE, mRecyclerView, filesFrame, noDataFiles);
			}

			filesFrame.setVisibility(View.VISIBLE);
			mProgressBar.setVisibility(View.GONE);

		});

	}

	@Override
	public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {

		menu.clear();
		inflater.inflate(R.menu.search_menu, menu);
		inflater.inflate(R.menu.files_switch_branches_menu, menu);
		super.onCreateOptionsMenu(menu, inflater);

		MenuItem searchItem = menu.findItem(R.id.action_search);
		androidx.appcompat.widget.SearchView searchView = (androidx.appcompat.widget.SearchView) searchItem.getActionView();
		searchView.setImeOptions(EditorInfo.IME_ACTION_DONE);

		searchView.setOnQueryTextListener(new androidx.appcompat.widget.SearchView.OnQueryTextListener() {

			@Override
			public boolean onQueryTextChange(String newText) {

				if(mRecyclerView.getAdapter() != null) {
					adapter.getFilter().filter(newText);
				}

				return false;
			}

			@Override
			public boolean onQueryTextSubmit(String query) { return false; }

		});

	}

	public void onButtonPressed(Uri uri) {

		if(mListener != null) {
			mListener.onFragmentInteraction(uri);
		}
	}

	@Override
	public void onDetach() {

		super.onDetach();
		mListener = null;
	}

	public interface OnFragmentInteractionListener { void onFragmentInteraction(Uri uri); }

}
