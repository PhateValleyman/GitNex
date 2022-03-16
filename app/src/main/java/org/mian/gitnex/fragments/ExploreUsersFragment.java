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
import org.gitnex.tea4j.v2.models.InlineResponse2001;
import org.gitnex.tea4j.v2.models.User;
import org.mian.gitnex.R;
import org.mian.gitnex.activities.BaseActivity;
import org.mian.gitnex.adapters.UsersAdapter;
import org.mian.gitnex.clients.RetrofitClient;
import org.mian.gitnex.databinding.FragmentExploreUsersBinding;
import org.mian.gitnex.helpers.Constants;
import org.mian.gitnex.helpers.SnackBar;
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

	private List<User> usersList;
	private UsersAdapter adapter;
	private int pageSize;
	private final String TAG = Constants.exploreUsers;
	private int resultLimit;

	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		viewBinding = FragmentExploreUsersBinding.inflate(inflater, container, false);
		context = getContext();

		resultLimit = Constants.getCurrentResultLimit(context);

		usersList = new ArrayList<>();
		adapter = new UsersAdapter(usersList, context);

		viewBinding.searchKeyword.setOnEditorActionListener((v1, actionId, event) -> {
			if(actionId == EditorInfo.IME_ACTION_SEND) {
				if(!Objects.requireNonNull(viewBinding.searchKeyword.getText()).toString().equals("")) {
					InputMethodManager imm = (InputMethodManager) requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
					imm.hideSoftInputFromWindow(viewBinding.searchKeyword.getWindowToken(), 0);

					viewBinding.progressBar.setVisibility(View.VISIBLE);
					loadInitial(((BaseActivity) requireActivity()).getAccount().getAuthorization(), String.valueOf(viewBinding.searchKeyword.getText()), resultLimit);

					adapter.setLoadMoreListener(() -> viewBinding.recyclerViewExploreUsers.post(() -> {
						if(usersList.size() == resultLimit || pageSize == resultLimit) {
							int page = (usersList.size() + resultLimit) / resultLimit;
							loadMore(((BaseActivity) requireActivity()).getAccount().getAuthorization(), String.valueOf(viewBinding.searchKeyword.getText()), resultLimit, page);
						}
					}));
				}
			}
			return false;
		});

		viewBinding.pullToRefresh.setOnRefreshListener(() -> new Handler(Looper.getMainLooper()).postDelayed(() -> {
			viewBinding.pullToRefresh.setRefreshing(false);
			loadInitial(((BaseActivity) requireActivity()).getAccount().getAuthorization(), "", resultLimit);
			adapter.notifyDataChanged();
		}, 200));

		adapter.setLoadMoreListener(() -> viewBinding.recyclerViewExploreUsers.post(() -> {
			if(usersList.size() == resultLimit || pageSize == resultLimit) {
				int page = (usersList.size() + resultLimit) / resultLimit;
				loadMore(((BaseActivity) requireActivity()).getAccount().getAuthorization(), "", resultLimit, page);
			}
		}));

		DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(context, DividerItemDecoration.VERTICAL);
		viewBinding.recyclerViewExploreUsers.setHasFixedSize(true);
		viewBinding.recyclerViewExploreUsers.addItemDecoration(dividerItemDecoration);
		viewBinding.recyclerViewExploreUsers.setLayoutManager(new LinearLayoutManager(context));
		viewBinding.recyclerViewExploreUsers.setAdapter(adapter);

		loadInitial(((BaseActivity) requireActivity()).getAccount().getAuthorization(), "", resultLimit);

		return viewBinding.getRoot();
	}

	private void loadInitial(String token, String searchKeyword, int resultLimit) {

		Call<InlineResponse2001> call = RetrofitClient
			.getApiInterface(context).userSearch(searchKeyword, null, resultLimit, 1);
		call.enqueue(new Callback<InlineResponse2001>() {
			@Override
			public void onResponse(@NonNull Call<InlineResponse2001> call, @NonNull Response<InlineResponse2001> response) {
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
			public void onFailure(@NonNull Call<InlineResponse2001> call, @NonNull Throwable t) {
				Log.e(TAG, t.toString());
			}
		});
	}

	private void loadMore(String token, String searchKeyword, int resultLimit, int page) {

		viewBinding.progressBar.setVisibility(View.VISIBLE);
		Call<InlineResponse2001> call = RetrofitClient.getApiInterface(context).userSearch(searchKeyword, null, resultLimit, page);
		call.enqueue(new Callback<InlineResponse2001>() {
			@Override
			public void onResponse(@NonNull Call<InlineResponse2001> call, @NonNull Response<InlineResponse2001> response) {
				if(response.isSuccessful()) {
					assert response.body() != null;
					List<User> result = response.body().getData();
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
					viewBinding.progressBar.setVisibility(View.GONE);
				}
				else {
					Log.e(TAG, String.valueOf(response.code()));
				}
			}

			@Override
			public void onFailure(@NonNull Call<InlineResponse2001> call, @NonNull Throwable t) {
				Log.e(TAG, t.toString());
			}
		});
	}
}
