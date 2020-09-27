package org.mian.gitnex.fragments;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import org.mian.gitnex.R;
import org.mian.gitnex.activities.MainActivity;
import org.mian.gitnex.adapters.SearchIssuesAdapter;
import org.mian.gitnex.clients.RetrofitClient;
import org.mian.gitnex.databinding.FragmentSearchIssuesBinding;
import org.mian.gitnex.helpers.Authorization;
import org.mian.gitnex.helpers.TinyDB;
import org.mian.gitnex.models.Issues;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Author M M Arif
 */

public class SearchIssuesFragment extends Fragment {

	private Context ctx;
	private TinyDB tinyDb;
	private FragmentSearchIssuesBinding viewBinding;
	private SearchIssuesAdapter adapter;
	private List<Issues> dataList;

	private String instanceUrl;
	private String loginUid;
	private String instanceToken;

	private String type = "issues";
	private String state = "open";

	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		viewBinding = FragmentSearchIssuesBinding.inflate(inflater, container, false);
		setHasOptionsMenu(true);

		ctx = getContext();
		tinyDb = new TinyDB(getContext());

		((MainActivity) requireActivity()).setActionBarTitle(getResources().getString(R.string.navSearchIssuesPulls));

		instanceUrl = tinyDb.getString("instanceUrl");
		loginUid = tinyDb.getString("loginUid");
		instanceToken = "token " + tinyDb.getString(loginUid + "-token");

		dataList = new ArrayList<>();
		adapter = new SearchIssuesAdapter(dataList, ctx);

		viewBinding.recyclerViewSearchIssues.setHasFixedSize(true);
		viewBinding.recyclerViewSearchIssues.setLayoutManager(new LinearLayoutManager(ctx));
		viewBinding.recyclerViewSearchIssues.setAdapter(adapter);

		loadDefaultList();

		viewBinding.searchKeyword.setOnEditorActionListener((v1, actionId, event) -> {

			if(actionId == EditorInfo.IME_ACTION_SEND) {

				if(!Objects.requireNonNull(viewBinding.searchKeyword.getText()).toString().equals("")) {

					viewBinding.progressBar.setVisibility(View.VISIBLE);
					viewBinding.recyclerViewSearchIssues.setVisibility(View.GONE);
					loadSearchIssuesList(viewBinding.searchKeyword.getText().toString());
				}
			}

			return false;
		});

		return viewBinding.getRoot();
	}

	private void loadDefaultList() {

		Call<List<Issues>> call = RetrofitClient.getInstance(instanceUrl, getContext()).getApiInterface().queryIssues(
			Authorization.returnAuthentication(getContext(), loginUid, instanceToken), null, type, state, 1);

		call.enqueue(new Callback<List<Issues>>() {

			@Override
			public void onResponse(@NonNull Call<List<Issues>> call, @NonNull Response<List<Issues>> response) {

				if(response.code() == 200) {

					assert response.body() != null;
					if(response.body().size() > 0) {

						dataList.clear();
						dataList.addAll(response.body());
						adapter.notifyDataChanged();
						viewBinding.noData.setVisibility(View.GONE);

					}
					else {

						dataList.clear();
						adapter.notifyDataChanged();
						viewBinding.noData.setVisibility(View.VISIBLE);

					}

					viewBinding.progressBar.setVisibility(View.GONE);

				}
				else {

					Log.e("onResponse", String.valueOf(response.code()));
				}

			}

			@Override
			public void onFailure(@NonNull Call<List<Issues>> call, @NonNull Throwable t) {

				Log.e("onFailure", Objects.requireNonNull(t.getMessage()));
			}

		});

	}

	private void loadSearchIssuesList(String searchKeyword) {

		Call<List<Issues>> call = RetrofitClient.getInstance(instanceUrl, getContext()).getApiInterface().queryIssues(
			Authorization.returnAuthentication(getContext(), loginUid, instanceToken), searchKeyword, type, state, 1);

		call.enqueue(new Callback<List<Issues>>() {

			@Override
			public void onResponse(@NonNull Call<List<Issues>> call, @NonNull Response<List<Issues>> response) {

				if(response.code() == 200) {

					viewBinding.recyclerViewSearchIssues.setVisibility(View.VISIBLE);

					assert response.body() != null;
					if(response.body().size() > 0) {

						dataList.clear();
						dataList.addAll(response.body());
						adapter.notifyDataChanged();
						viewBinding.noData.setVisibility(View.GONE);

					}
					else {

						dataList.clear();
						adapter.notifyDataChanged();
						viewBinding.noData.setVisibility(View.VISIBLE);

					}

					viewBinding.progressBar.setVisibility(View.GONE);

				}
				else {

					Log.e("onResponse", String.valueOf(response.code()));
				}

			}

			@Override
			public void onFailure(@NonNull Call<List<Issues>> call, @NonNull Throwable t) {

				Log.i("onFailure", Objects.requireNonNull(t.getMessage()));
			}

		});

	}

}
