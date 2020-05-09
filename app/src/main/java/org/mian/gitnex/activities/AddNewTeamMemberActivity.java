package org.mian.gitnex.activities;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import org.mian.gitnex.R;
import org.mian.gitnex.adapters.UserSearchForTeamMemberAdapter;
import org.mian.gitnex.clients.RetrofitClient;
import org.mian.gitnex.helpers.Authorization;
import org.mian.gitnex.models.UserInfo;
import org.mian.gitnex.models.UserSearch;
import org.mian.gitnex.util.TinyDB;
import java.util.List;
import java.util.Objects;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AddNewTeamMemberActivity extends BaseActivity {

	private View.OnClickListener onClickListener;
	final Context ctx = this;
	private Context appCtx;
	private TextView addNewTeamMember;
	private TextView noData;
	private ProgressBar mProgressBar;

	private RecyclerView mRecyclerView;

	private String teamId;

	@Override
	protected int getLayoutResourceId() {

		return R.layout.activity_add_new_team_member;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		appCtx = getApplicationContext();

		InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

		TinyDB tinyDb = new TinyDB(appCtx);
		final String instanceUrl = tinyDb.getString("instanceUrl");
		final String loginUid = tinyDb.getString("loginUid");
		String repoFullName = tinyDb.getString("repoFullName");
		String[] parts = repoFullName.split("/");
		final String instanceToken = "token " + tinyDb.getString(loginUid + "-token");

		ImageView closeActivity = findViewById(R.id.close);
		addNewTeamMember = findViewById(R.id.addNewTeamMeber);
		mRecyclerView = findViewById(R.id.recyclerViewUserSearch);
		mProgressBar = findViewById(R.id.progress_bar);
		noData = findViewById(R.id.noData);

		addNewTeamMember.requestFocus();
		assert imm != null;
		imm.showSoftInput(addNewTeamMember, InputMethodManager.SHOW_IMPLICIT);

		initCloseListener();
		closeActivity.setOnClickListener(onClickListener);

		if(getIntent().getStringExtra("teamId") != null && !Objects.requireNonNull(getIntent().getStringExtra("teamId")).equals("")) {
			teamId = getIntent().getStringExtra("teamId");
		}
		else {
			teamId = "0";
		}

		addNewTeamMember.setOnEditorActionListener((v, actionId, event) -> {

			if(actionId == EditorInfo.IME_ACTION_SEND) {
				if(!addNewTeamMember.getText().toString().equals("")) {
					loadUserSearchList(instanceUrl, instanceToken, addNewTeamMember.getText().toString(), loginUid, teamId);
				}
			}

			return false;

		});

	}

	public void loadUserSearchList(String instanceUrl, String token, String searchKeyword, String loginUid, String teamId) {

		Call<UserSearch> call = RetrofitClient.getInstance(instanceUrl, ctx).getApiInterface().getUserBySearch(Authorization.returnAuthentication(ctx, loginUid, token), searchKeyword, 10);

		call.enqueue(new Callback<UserSearch>() {

			@Override
			public void onResponse(@NonNull Call<UserSearch> call, @NonNull Response<UserSearch> response) {

				if(response.isSuccessful()) {
					assert response.body() != null;
					getUsersList(response.body().getData(), ctx, teamId);
				}
				else {
					Log.i("onResponse", String.valueOf(response.code()));
				}

			}

			@Override
			public void onFailure(@NonNull Call<UserSearch> call, @NonNull Throwable t) {

				Log.i("onFailure", t.toString());
			}

		});
	}

	private void getUsersList(List<UserInfo> dataList, Context context, String teamId) {

		UserSearchForTeamMemberAdapter adapter = new UserSearchForTeamMemberAdapter(dataList, context, Integer.parseInt(teamId));

		mRecyclerView.setHasFixedSize(true);
		mRecyclerView.setLayoutManager(new LinearLayoutManager(ctx));
		DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(mRecyclerView.getContext(), DividerItemDecoration.VERTICAL);
		mRecyclerView.addItemDecoration(dividerItemDecoration);

		mProgressBar.setVisibility(View.VISIBLE);

		if(adapter.getItemCount() > 0) {
			mRecyclerView.setAdapter(adapter);
			noData.setVisibility(View.GONE);
			mProgressBar.setVisibility(View.GONE);
		}
		else {
			noData.setVisibility(View.VISIBLE);
			mProgressBar.setVisibility(View.GONE);
		}

	}

	private void initCloseListener() {

		onClickListener = view -> finish();
	}

}
