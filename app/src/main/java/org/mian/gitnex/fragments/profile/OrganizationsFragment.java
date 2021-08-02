package org.mian.gitnex.fragments.profile;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import org.gitnex.tea4j.models.UserOrganizations;
import org.mian.gitnex.R;
import org.mian.gitnex.adapters.profile.OrganizationsAdapter;
import org.mian.gitnex.clients.RetrofitClient;
import org.mian.gitnex.databinding.FragmentOrganizationsBinding;
import org.mian.gitnex.helpers.AlertDialogs;
import org.mian.gitnex.helpers.Authorization;
import org.mian.gitnex.helpers.Constants;
import org.mian.gitnex.helpers.SnackBar;
import org.mian.gitnex.helpers.TinyDB;
import org.mian.gitnex.helpers.Toasty;
import org.mian.gitnex.helpers.Version;
import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Author M M Arif
 */

public class OrganizationsFragment extends Fragment {

	private Context context;
	private FragmentOrganizationsBinding fragmentOrganizationsBinding;

	private List<UserOrganizations> organizationsList;
	private OrganizationsAdapter adapter;

	private int pageSize;
	private int resultLimit = Constants.resultLimitOldGiteaInstances;

	private static final String usernameBundle = "";
	private String username;

	public OrganizationsFragment() {}

	public static OrganizationsFragment newInstance(String username) {
		OrganizationsFragment fragment = new OrganizationsFragment();
		Bundle args = new Bundle();
		args.putString(usernameBundle, username);
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (getArguments() != null) {
			username = getArguments().getString(usernameBundle);
		}
	}

	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

		fragmentOrganizationsBinding = FragmentOrganizationsBinding.inflate(inflater, container, false);
		setHasOptionsMenu(true);
		context = getContext();

		TinyDB tinyDb = TinyDB.getInstance(context);

		// if gitea is 1.12 or higher use the new limit
		if(new Version(tinyDb.getString("giteaVersion")).higherOrEqual("1.12.0")) {
			resultLimit = Constants.resultLimitNewGiteaInstances;
		}

		organizationsList = new ArrayList<>();

		fragmentOrganizationsBinding.addNewOrganization.setVisibility(View.GONE);

		fragmentOrganizationsBinding.pullToRefresh.setOnRefreshListener(() -> new Handler(Looper.getMainLooper()).postDelayed(() -> {
			fragmentOrganizationsBinding.pullToRefresh.setRefreshing(false);
			loadInitial(Authorization.get(context), username, resultLimit);
			adapter.notifyDataChanged();
		}, 200));

		adapter = new OrganizationsAdapter(context, organizationsList);
		adapter.setLoadMoreListener(() -> fragmentOrganizationsBinding.recyclerView.post(() -> {
			if(organizationsList.size() == resultLimit || pageSize == resultLimit) {
				int page = (organizationsList.size() + resultLimit) / resultLimit;
				loadMore(Authorization.get(context), username, page, resultLimit);
			}
		}));

		DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(context, DividerItemDecoration.VERTICAL);
		fragmentOrganizationsBinding.recyclerView.setHasFixedSize(true);
		fragmentOrganizationsBinding.recyclerView.addItemDecoration(dividerItemDecoration);
		fragmentOrganizationsBinding.recyclerView.setLayoutManager(new LinearLayoutManager(context));
		fragmentOrganizationsBinding.recyclerView.setAdapter(adapter);

		loadInitial(Authorization.get(context), username, resultLimit);

		return fragmentOrganizationsBinding.getRoot();
	}

	private void loadInitial(String token, String username, int resultLimit) {

		Call<List<UserOrganizations>> call = RetrofitClient
			.getApiInterface(context)
			.getUserProfileOrganizations(token, username, 1, resultLimit);

		call.enqueue(new Callback<List<UserOrganizations>>() {
			@Override
			public void onResponse(@NonNull Call<List<UserOrganizations>> call, @NonNull Response<List<UserOrganizations>> response) {

				if(response.isSuccessful()) {

					switch(response.code()) {
						case 200:
							assert response.body() != null;
							if(response.body().size() > 0) {
								organizationsList.clear();
								organizationsList.addAll(response.body());
								adapter.notifyDataChanged();
								fragmentOrganizationsBinding.noDataOrg.setVisibility(View.GONE);
							}
							else {
								organizationsList.clear();
								adapter.notifyDataChanged();
								fragmentOrganizationsBinding.noDataOrg.setVisibility(View.VISIBLE);
							}
							fragmentOrganizationsBinding.progressBar.setVisibility(View.GONE);
							break;

						case 401:
							AlertDialogs.authorizationTokenRevokedDialog(context, getResources().getString(R.string.alertDialogTokenRevokedTitle),
								getResources().getString(R.string.alertDialogTokenRevokedMessage), getResources().getString(R.string.alertDialogTokenRevokedCopyNegativeButton),
								getResources().getString(R.string.alertDialogTokenRevokedCopyPositiveButton));
							break;

						case 403:
							Toasty.error(context, context.getString(R.string.authorizeError));
							break;

						case 404:
							fragmentOrganizationsBinding.noDataOrg.setVisibility(View.VISIBLE);
							fragmentOrganizationsBinding.progressBar.setVisibility(View.GONE);
							break;

						default:
							Toasty.error(context, getString(R.string.genericError));
							break;
					}
				}
			}

			@Override
			public void onFailure(@NonNull Call<List<UserOrganizations>> call, @NonNull Throwable t) {
				Toasty.error(context, getString(R.string.genericError));
			}
		});
	}

	private void loadMore(String token, String username, int page, int resultLimit) {

		fragmentOrganizationsBinding.progressLoadMore.setVisibility(View.VISIBLE);

		Call<List<UserOrganizations>> call = RetrofitClient
			.getApiInterface(context)
			.getUserProfileOrganizations(token, username, page, resultLimit);

		call.enqueue(new Callback<List<UserOrganizations>>() {

			@Override
			public void onResponse(@NonNull Call<List<UserOrganizations>> call, @NonNull Response<List<UserOrganizations>> response) {

				if(response.isSuccessful()) {

					switch(response.code()) {
						case 200:
							List<UserOrganizations> result = response.body();
							assert result != null;
							if(result.size() > 0) {
								pageSize = result.size();
								organizationsList.addAll(result);
							}
							else {
								SnackBar.info(context, fragmentOrganizationsBinding.getRoot(), getString(R.string.noMoreData));
								adapter.setMoreDataAvailable(false);
							}
							adapter.notifyDataChanged();
							fragmentOrganizationsBinding.progressLoadMore.setVisibility(View.GONE);
							break;

						case 401:
							AlertDialogs.authorizationTokenRevokedDialog(context, getResources().getString(R.string.alertDialogTokenRevokedTitle),
								getResources().getString(R.string.alertDialogTokenRevokedMessage), getResources().getString(R.string.alertDialogTokenRevokedCopyNegativeButton),
								getResources().getString(R.string.alertDialogTokenRevokedCopyPositiveButton));
							break;

						case 403:
							Toasty.error(context, context.getString(R.string.authorizeError));
							break;

						case 404:
							fragmentOrganizationsBinding.noDataOrg.setVisibility(View.VISIBLE);
							fragmentOrganizationsBinding.progressBar.setVisibility(View.GONE);
							break;

						default:
							Toasty.error(context, getString(R.string.genericError));
							break;
					}
				}
			}

			@Override
			public void onFailure(@NonNull Call<List<UserOrganizations>> call, @NonNull Throwable t) {
				Toasty.error(context, getString(R.string.genericError));
			}
		});
	}

	@Override
	public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {

		inflater.inflate(R.menu.search_menu, menu);
		super.onCreateOptionsMenu(menu, inflater);

		MenuItem searchItem = menu.findItem(R.id.action_search);
		androidx.appcompat.widget.SearchView searchView = (androidx.appcompat.widget.SearchView) searchItem.getActionView();
		searchView.setImeOptions(EditorInfo.IME_ACTION_DONE);

		searchView.setOnQueryTextListener(new androidx.appcompat.widget.SearchView.OnQueryTextListener() {

			@Override
			public boolean onQueryTextSubmit(String query) {
				return false;
			}

			@Override
			public boolean onQueryTextChange(String newText) {
				filter(newText);
				return false;
			}
		});
	}

	private void filter(String text) {

		List<UserOrganizations> arr = new ArrayList<>();

		for(UserOrganizations d : organizationsList) {
			if(d == null || d.getUsername() == null || d.getDescription() == null) {
				continue;
			}
			if(d.getUsername().toLowerCase().contains(text) || d.getDescription().toLowerCase().contains(text)) {
				arr.add(d);
			}
		}
		adapter.updateList(arr);
	}
}
