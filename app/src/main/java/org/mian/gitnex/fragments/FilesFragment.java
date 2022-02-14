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
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import org.gitnex.tea4j.models.Files;
import org.mian.gitnex.R;
import org.mian.gitnex.activities.BaseActivity;
import org.mian.gitnex.activities.FileViewActivity;
import org.mian.gitnex.activities.RepoDetailActivity;
import org.mian.gitnex.adapters.FilesAdapter;
import org.mian.gitnex.database.api.BaseApi;
import org.mian.gitnex.database.api.UserAccountsApi;
import org.mian.gitnex.database.models.UserAccount;
import org.mian.gitnex.databinding.FragmentFilesBinding;
import org.mian.gitnex.helpers.AppUtil;
import org.mian.gitnex.helpers.Path;
import org.mian.gitnex.helpers.contexts.RepositoryContext;
import org.mian.gitnex.viewmodels.FilesViewModel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import moe.feng.common.view.breadcrumbs.DefaultBreadcrumbsCallback;
import moe.feng.common.view.breadcrumbs.model.BreadcrumbItem;

/**
 * Author M M Arif
 */

public class FilesFragment extends Fragment implements FilesAdapter.FilesAdapterListener {

	private FragmentFilesBinding binding;

	private RepositoryContext repository;

	private final Path path = new Path();

	private FilesAdapter filesAdapter;

	private OnFragmentInteractionListener mListener;

	public FilesFragment() {}

	public static FilesFragment newInstance(RepositoryContext repository) {

		FilesFragment fragment = new FilesFragment();
		fragment.setArguments(repository.getBundle());

		return fragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		repository = RepositoryContext.fromBundle(requireArguments());
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		binding = FragmentFilesBinding.inflate(inflater, container, false);
		setHasOptionsMenu(true);

		filesAdapter = new FilesAdapter(getContext(), this);

		binding.recyclerView.setHasFixedSize(true);
		binding.recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
		binding.recyclerView.setAdapter(filesAdapter);

		DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(binding.recyclerView.getContext(), DividerItemDecoration.VERTICAL);
		binding.recyclerView.addItemDecoration(dividerItemDecoration);

		binding.breadcrumbsView.setItems(new ArrayList<>(Collections.singletonList(BreadcrumbItem.createSimpleItem(getResources().getString(R.string.filesBreadcrumbRoot) + getResources().getString(R.string.colonDivider) + repository.getBranchRef()))));
		// noinspection unchecked
		binding.breadcrumbsView.setCallback(new DefaultBreadcrumbsCallback<BreadcrumbItem>() {

			@SuppressLint("SetTextI18n")
			@Override
			public void onNavigateBack(BreadcrumbItem item, int position) {

				if(position == 0) {

					path.clear();
					fetchDataAsync(((BaseActivity) requireActivity()).getAccount().getAuthorization(), repository.getOwner(), repository.getName(), repository.getBranchRef());
					return;

				}

				path.pop(path.size() - position);
				fetchDataAsyncSub(((BaseActivity) requireActivity()).getAccount().getAuthorization(), repository.getOwner(), repository.getName(), path.toString(), repository.getBranchRef());

			}

			@Override public void onNavigateNewLocation(BreadcrumbItem newItem, int changedPosition) {}

		});

		requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), new OnBackPressedCallback(true) {

			@Override
			public void handleOnBackPressed() {
				if(path.size() == 0 || ((RepoDetailActivity) requireActivity()).mViewPager.getCurrentItem() != 1) {
					requireActivity().finish();
					return;
				}
				path.remove(path.size() - 1);
				binding.breadcrumbsView.removeLastItem();
				if(path.size() == 0) {
					fetchDataAsync(((BaseActivity) requireActivity()).getAccount().getAuthorization(), repository.getOwner(), repository.getName(), repository.getBranchRef());
				} else {
					fetchDataAsyncSub(((BaseActivity) requireActivity()).getAccount().getAuthorization(), repository.getOwner(), repository.getName(), path.toString(), repository.getBranchRef());
				}
			}
		});

		((RepoDetailActivity) requireActivity()).setFragmentRefreshListenerFiles(repoBranch -> {

			repository.setBranchRef(repoBranch);
			path.clear();
			binding.breadcrumbsView.setItems(new ArrayList<>(Collections.singletonList(BreadcrumbItem.createSimpleItem(getResources().getString(R.string.filesBreadcrumbRoot) + getResources().getString(R.string.colonDivider) + repository.getBranchRef()))));
			fetchDataAsync(((BaseActivity) requireActivity()).getAccount().getAuthorization(), repository.getOwner(), repository.getName(), repoBranch);

		});

		String dir = requireActivity().getIntent().getStringExtra("dir");
		if(dir != null) {
			fetchDataAsyncSub(((BaseActivity) requireActivity()).getAccount().getAuthorization(), repository.getOwner(), repository.getName(), dir, repository.getBranchRef());
			for(String segment: dir.split("/")) {
				binding.breadcrumbsView.addItem(new BreadcrumbItem(Collections.singletonList(segment)));
				path.add(segment);
			}
		}
		else {
			fetchDataAsync(((BaseActivity) requireActivity()).getAccount().getAuthorization(), repository.getOwner(), repository.getName(), repository.getBranchRef());
		}

		return binding.getRoot();
	}

	@Override
	public void onResume() {

		super.onResume();
	}

	@Override
	public void onClickFile(Files file) {

		switch(file.getType()) {

			case "dir":
				path.add(file.getName());
				binding.breadcrumbsView.addItem(new BreadcrumbItem(Collections.singletonList(file.getName())));

				fetchDataAsyncSub(((BaseActivity) requireActivity()).getAccount().getAuthorization(), repository.getOwner(), repository.getName(), path.toString(), repository.getBranchRef());
				break;

			case "file":
			case "symlink":
				Intent intent = repository.getIntent(getContext(), FileViewActivity.class);
				intent.putExtra("file", file);

				requireContext().startActivity(intent);
				break;

			case "submodule":
				String rawUrl = file.getSubmodule_git_url();
				if(rawUrl == null) {
					return;
				}
				Uri url = AppUtil.getUriFromGitUrl(rawUrl);
				String host = url.getHost();

				UserAccountsApi userAccountsApi = BaseApi.getInstance(requireContext(), UserAccountsApi.class);
				List<UserAccount> userAccounts = userAccountsApi.loggedInUserAccounts();
				UserAccount account = null;

				for(UserAccount userAccount : userAccounts) {
					Uri instanceUri = Uri.parse(userAccount.getInstanceUrl());
					if(instanceUri.getHost().toLowerCase().equals(host)) {
						account = userAccount;
						// if scheme is wrong fix it
						if (!url.getScheme().equals(instanceUri.getScheme())) {
							url = AppUtil.changeScheme(url, instanceUri.getScheme());
						}
						break;
					}
				}

				if(account != null) {
					AppUtil.switchToAccount(requireContext(), account, true); // TODO switch back
					List<String> splittedUrl = url.getPathSegments();
					if(splittedUrl.size() < 2) {
						AppUtil.openUrlInBrowser(requireContext(), url.toString());
					}
					String owner = splittedUrl.get(splittedUrl.size() - 2);
					String repo = splittedUrl.get(splittedUrl.size() - 1);
					if (repo.endsWith(".git")) { // Git clone URL
						repo = repo.substring(0, repo.length() - 4);
					}

					startActivity(new RepositoryContext(owner, repo, requireContext()).getIntent(requireContext(), RepoDetailActivity.class));
				} else {
					AppUtil.openUrlInBrowser(requireContext(), url.toString());
				}
				break;
		}
	}

	private void fetchDataAsync(String instanceToken, String owner, String repo, String ref) {

		binding.recyclerView.setVisibility(View.GONE);
		binding.progressBar.setVisibility(View.VISIBLE);

		FilesViewModel filesModel = new ViewModelProvider(this).get(FilesViewModel.class);

		filesModel.getFilesList(instanceToken, owner, repo, ref, getContext(), binding.progressBar, binding.noDataFiles).observe(getViewLifecycleOwner(), filesListMain -> {

			filesAdapter.getOriginalFiles().clear();
			filesAdapter.getOriginalFiles().addAll(filesListMain);
			filesAdapter.notifyOriginalDataSetChanged();

			if(filesListMain.size() > 0) {

				AppUtil.setMultiVisibility(View.VISIBLE, binding.recyclerView, binding.filesFrame);
				binding.noDataFiles.setVisibility(View.GONE);

			}
			else {
				AppUtil.setMultiVisibility(View.VISIBLE, binding.recyclerView, binding.filesFrame, binding.noDataFiles);
			}

			binding.filesFrame.setVisibility(View.VISIBLE);
			binding.progressBar.setVisibility(View.GONE);

		});

	}

	private void fetchDataAsyncSub(String instanceToken, String owner, String repo, String filesDir, String ref) {

		binding.recyclerView.setVisibility(View.GONE);
		binding.progressBar.setVisibility(View.VISIBLE);

		FilesViewModel filesModel = new ViewModelProvider(this).get(FilesViewModel.class);

		filesModel.getFilesList2(instanceToken, owner, repo, filesDir, ref, getContext(), binding.progressBar, binding.noDataFiles).observe(getViewLifecycleOwner(), filesListMain2 -> {

			filesAdapter.getOriginalFiles().clear();
			filesAdapter.getOriginalFiles().addAll(filesListMain2);
			filesAdapter.notifyOriginalDataSetChanged();

			if(filesListMain2.size() > 0) {

				AppUtil.setMultiVisibility(View.VISIBLE, binding.recyclerView, binding.filesFrame);
				binding.noDataFiles.setVisibility(View.GONE);
			}
			else {
				AppUtil.setMultiVisibility(View.VISIBLE, binding.recyclerView, binding.filesFrame, binding.noDataFiles);
			}

			binding.filesFrame.setVisibility(View.VISIBLE);
			binding.progressBar.setVisibility(View.GONE);

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

				if(binding.recyclerView.getAdapter() != null) {
					filesAdapter.getFilter().filter(newText);
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
