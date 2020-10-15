package org.mian.gitnex.activities;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import org.apache.commons.lang3.StringUtils;
import org.mian.gitnex.R;
import org.mian.gitnex.clients.RetrofitClient;
import org.mian.gitnex.database.api.RepositoriesApi;
import org.mian.gitnex.database.api.UserAccountsApi;
import org.mian.gitnex.database.models.Repository;
import org.mian.gitnex.databinding.ActivityDeeplinksBinding;
import org.mian.gitnex.helpers.TinyDB;
import org.mian.gitnex.models.UserRepositories;
import java.util.Objects;
import retrofit2.Call;
import retrofit2.Callback;

/**
 * Author M M Arif
 */

public class DeepLinksActivity extends BaseActivity {

	private ActivityDeeplinksBinding viewBinding;
	private Context ctx = this;
	private Context appCtx;
	private TinyDB tinyDb;
	private String currentInstance;
	private String instanceToken;

	@Override
	protected int getLayoutResourceId() {

		return R.layout.activity_deeplinks;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		appCtx = getApplicationContext();
		tinyDb = new TinyDB(appCtx);

		viewBinding = ActivityDeeplinksBinding.inflate(getLayoutInflater());
		View view = viewBinding.getRoot();
		setContentView(view);

		Intent intent = getIntent();
		Uri data = intent.getData();
		assert data != null;

		// check for login
		if(!tinyDb.getBoolean("loggedInMode")) {

			finish();
			ctx.startActivity(new Intent(ctx, LoginActivity.class));
		}

		// check for the links(URI) to be in the db
		UserAccountsApi userAccountsApi = new UserAccountsApi(ctx);

		userAccountsApi.getAllAccounts().observe((AppCompatActivity) ctx, userAccounts -> {

			if(userAccounts.size() > 0) {

				String hostUri;
				for(int i = 0; i < userAccounts.size(); i++) {

					hostUri = userAccounts.get(i).getInstanceUrl();

					currentInstance = userAccounts.get(i).getInstanceUrl();
					instanceToken = userAccounts.get(i).getToken();

					if(!hostUri.contains(Objects.requireNonNull(data.getHost()))) {

						viewBinding.addNewAccountFrame.setVisibility(View.VISIBLE);
						viewBinding.addAccountText.setText(String.format(getResources().getString(R.string.accountDoesNotExist), data.getHost()));

						viewBinding.addNewAccount.setOnClickListener(addNewAccount -> {

							Intent accountIntent = new Intent(view.getContext(), AddNewAccountActivity.class);
							startActivity(accountIntent);
							finish();
						});

						viewBinding.launchApp.setOnClickListener(launchApp -> {

							Intent accountIntent = new Intent(view.getContext(), MainActivity.class);
							startActivity(accountIntent);
							finish();
						});
					}

					Log.e("DeepLinks-5", String.valueOf(currentInstance));
				}

			}
		});

		// redirect to proper fragment/activity, If no action is there, show options where user to want to go like repos, profile, notifications etc
		if(data.getPathSegments().size() > 0) {

			if(data.getPathSegments().contains("issues")) { // issue

				String[] restOfUrl = Objects.requireNonNull(data.getPath()).split("/");

				if(!Objects.requireNonNull(data.getLastPathSegment()).contains("issues") & StringUtils.isNumeric(data.getLastPathSegment())) {

					Intent issueIntent = new Intent(ctx, IssueDetailActivity.class);
					issueIntent.putExtra("issueNumber", data.getLastPathSegment());

					tinyDb.putString("issueNumber", data.getLastPathSegment());
					tinyDb.putString("issueType", "Issue");

					tinyDb.putString("repoFullName", restOfUrl[restOfUrl.length - 4] + "/" + restOfUrl[restOfUrl.length - 3]);

					final String repoOwner = restOfUrl[restOfUrl.length - 4];
					final String repoName = restOfUrl[restOfUrl.length - 3];

					int currentActiveAccountId = tinyDb.getInt("currentActiveAccountId");
					RepositoriesApi repositoryData = new RepositoriesApi(ctx);

					Integer count = repositoryData.checkRepository(currentActiveAccountId, repoOwner, repoName);

					if(count == 0) {

						long id = repositoryData.insertRepository(currentActiveAccountId, repoOwner, repoName);
						tinyDb.putLong("repositoryId", id);
					}
					else {

						Repository dataRepo = repositoryData.getRepository(currentActiveAccountId, repoOwner, repoName);
						tinyDb.putLong("repositoryId", dataRepo.getRepositoryId());
					}

					ctx.startActivity(issueIntent);
					finish();
				}
				else if(Objects.requireNonNull(data.getLastPathSegment()).contains("issues")) {

					new Handler(Looper.getMainLooper()).postDelayed(() -> {

						goToRepoIssues(currentInstance, instanceToken, restOfUrl[restOfUrl.length - 3], restOfUrl[restOfUrl.length - 2]);
						finish();
					}, 500);
				}
				else {

					Intent mainIntent = new Intent(ctx, MainActivity.class);
					ctx.startActivity(mainIntent);
					finish();
				}

				Log.e("DeepLinks-1", Objects.requireNonNull(data.getLastPathSegment()));
			}
			else if(data.getPathSegments().contains("pulls")) { // pr

				if(!Objects.requireNonNull(data.getLastPathSegment()).contains("pulls")) {

					// open pr

				}
				else {

					// redirect to pr fragment

				}
				Log.e("DeepLinks-2", Objects.requireNonNull(data.getLastPathSegment()));
			}

		}

		Log.e("DeepLinks-3", String.valueOf(data.getPathSegments()));

	}

	private void goToRepoIssues(String url, String token, String repoOwner, String repoName) {

		Call<UserRepositories> call = RetrofitClient
			.getInstance(url, ctx)
			.getApiInterface()
			.getUserRepository(token, repoOwner, repoName);

		call.enqueue(new Callback<UserRepositories>() {

			@Override
			public void onResponse(@NonNull Call<UserRepositories> call, @NonNull retrofit2.Response<UserRepositories> response) {

				UserRepositories repoInfo = response.body();

				if (response.code() == 200) {

					assert repoInfo != null;

					Intent repoIntent = new Intent(ctx, RepoDetailActivity.class);
					repoIntent.putExtra("repoFullName", repoInfo.getFullName());
					repoIntent.putExtra("goToIssues", "yes");

					tinyDb.putString("repoFullName", repoInfo.getFullName());
					if(repoInfo.getPrivateFlag()) {

						tinyDb.putString("repoType", getResources().getString(R.string.strPrivate));
					}
					else {

						tinyDb.putString("repoType", getResources().getString(R.string.strPublic));
					}
					tinyDb.putBoolean("isRepoAdmin", repoInfo.getPermissions().isAdmin());
					tinyDb.putString("repoBranch", repoInfo.getDefault_branch());

					int currentActiveAccountId = tinyDb.getInt("currentActiveAccountId");
					RepositoriesApi repositoryData = new RepositoriesApi(ctx);

					Integer count = repositoryData.checkRepository(currentActiveAccountId, repoOwner, repoName);

					if(count == 0) {

						long id = repositoryData.insertRepository(currentActiveAccountId, repoOwner, repoName);
						tinyDb.putLong("repositoryId", id);
					}
					else {

						Repository data = repositoryData.getRepository(currentActiveAccountId, repoOwner, repoName);
						tinyDb.putLong("repositoryId", data.getRepositoryId());
					}

					ctx.startActivity(repoIntent);
				}

				else {

					Log.e("onFailure-links", String.valueOf(response.code()));
				}

			}

			@Override
			public void onFailure(@NonNull Call<UserRepositories> call, @NonNull Throwable t) {
				
				Log.e("onFailure-links", t.toString());
			}
		});

	}
}
