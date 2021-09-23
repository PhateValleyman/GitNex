package org.mian.gitnex.fragments;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import org.gitnex.tea4j.models.UserInfo;
import org.gitnex.tea4j.models.UserSearch;
import org.mian.gitnex.R;
import org.mian.gitnex.adapters.ExploreUsersAdapter;
import org.mian.gitnex.clients.RetrofitClient;
import org.mian.gitnex.databinding.FragmentExploreUsersBinding;
import org.mian.gitnex.helpers.Authorization;
import org.mian.gitnex.helpers.Constants;
import org.mian.gitnex.helpers.SnackBar;
import org.mian.gitnex.helpers.TinyDB;
import org.mian.gitnex.helpers.Version;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Author M M Arif
 */

public class ExploreUsersFragment extends Fragment {

	private FragmentExploreUsersBinding viewBinding;
	private Context context;

	private List<UserInfo> usersList;
	private ExploreUsersAdapter adapter;
	private int pageSize;
	private final String TAG = Constants.exploreUsers;
	private int resultLimit;

	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		viewBinding = FragmentExploreUsersBinding.inflate(inflater, container, false);
		context = getContext();

		resultLimit = Constants.getCurrentResultLimit(context);

		usersList = new ArrayList<>();
		adapter = new ExploreUsersAdapter(context, usersList);

		viewBinding.searchKeyword.setOnEditorActionListener((v1, actionId, event) -> {
			if(actionId == EditorInfo.IME_ACTION_SEND) {
				if(!Objects.requireNonNull(viewBinding.searchKeyword.getText()).toString().equals("")) {
					InputMethodManager imm = (InputMethodManager) requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
					imm.hideSoftInputFromWindow(viewBinding.searchKeyword.getWindowToken(), 0);

					viewBinding.progressBar.setVisibility(View.VISIBLE);
					loadInitial(Authorization.get(context), String.valueOf(viewBinding.searchKeyword.getText()), resultLimit);

					adapter.setLoadMoreListener(() -> viewBinding.recyclerViewExploreUsers.post(() -> {
						if(usersList.size() == resultLimit || pageSize == resultLimit) {
							int page = (usersList.size() + resultLimit) / resultLimit;
							loadMore(Authorization.get(context), String.valueOf(viewBinding.searchKeyword.getText()), resultLimit, page);
						}
					}));
				}
			}
			return false;
		});

		viewBinding.pullToRefresh.setOnRefreshListener(() -> new Handler(Looper.getMainLooper()).postDelayed(() -> {
			viewBinding.pullToRefresh.setRefreshing(false);
			loadInitial(Authorization.get(context), "", resultLimit);
			adapter.notifyDataChanged();
		}, 200));

		adapter.setLoadMoreListener(() -> viewBinding.recyclerViewExploreUsers.post(() -> {
			if(usersList.size() == resultLimit || pageSize == resultLimit) {
				int page = (usersList.size() + resultLimit) / resultLimit;
				loadMore(Authorization.get(context), "", resultLimit, page);
			}
		}));

		DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(context, DividerItemDecoration.VERTICAL);
		viewBinding.recyclerViewExploreUsers.setHasFixedSize(true);
		viewBinding.recyclerViewExploreUsers.addItemDecoration(dividerItemDecoration);
		viewBinding.recyclerViewExploreUsers.setLayoutManager(new LinearLayoutManager(context));
		viewBinding.recyclerViewExploreUsers.setAdapter(adapter);

		loadInitial(Authorization.get(context), "", resultLimit);

		return viewBinding.getRoot();
	}

	private void loadInitial(String token, String searchKeyword, int resultLimit) {

		Call<UserSearch> call = RetrofitClient
			.getApiInterface(context).getUserBySearch(token, searchKeyword, resultLimit, 1);
		call.enqueue(new Callback<UserSearch>() {
			@Override
			public void onResponse(@NonNull Call<UserSearch> call, @NonNull Response<UserSearch> response) {
				if(response.isSuccessful()) {
					if(response.body() != null && response.body().getData().size() > 0) {
						usersList.clear();
						usersList.addAll(response.body().getData());
						adapter.notifyDataChanged();
						viewBinding.noData.setVisibility(View.GONE);
					}
					else {
						usersList.clear();
						adapter.notifyDataChanged();
						viewBinding.noData.setVisibility(View.VISIBLE);
					}
					viewBinding.progressBar.setVisibility(View.GONE);
				}
				else if(response.code() == 404) {
					viewBinding.noData.setVisibility(View.VISIBLE);
					viewBinding.progressBar.setVisibility(View.GONE);
				}
				else {
					Log.e(TAG, String.valueOf(response.code()));
				}
			}

			@Override
			public void onFailure(@NonNull Call<UserSearch> call, @NonNull Throwable t) {
				Log.e(TAG, t.toString());
			}
		});
	}

	private void loadMore(String token, String searchKeyword, int resultLimit, int page) {

		viewBinding.loadingMoreView.setVisibility(View.VISIBLE);
		Call<UserSearch> call = RetrofitClient.getApiInterface(context).getUserBySearch(token, searchKeyword, resultLimit, page);
		call.enqueue(new Callback<UserSearch>() {
			@Override
			public void onResponse(@NonNull Call<UserSearch> call, @NonNull Response<UserSearch> response) {
				if(response.isSuccessful()) {
					assert response.body() != null;
					List<UserInfo> result = response.body().getData();
					if(result != null) {
						if(result.size() > 0) {
							pageSize = result.size();
							usersList.addAll(result);
						}
						else {
							SnackBar.info(context, viewBinding.getRoot(), getString(R.string.noMoreData));
							adapter.setMoreDataAvailable(false);
						}
					}
					adapter.notifyDataChanged();
					viewBinding.loadingMoreView.setVisibility(View.GONE);
				}
				else {
					Log.e(TAG, String.valueOf(response.code()));
				}
			}

			@Override
			public void onFailure(@NonNull Call<UserSearch> call, @NonNull Throwable t) {
				Log.e(TAG, t.toString());
			}
		});
	}
}
