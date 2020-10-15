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
import org.mian.gitnex.models.PullRequests;
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
				}
			}
		});

		// redirect to proper fragment/activity, If no action is there, show options where user to want to go like repos, profile, notifications etc
		if(data.getPathSegments().size() > 0) {

			String[] restOfUrl = Objects.requireNonNull(data.getPath()).split("/");

			if(data.getPathSegments().contains("issues")) { // issue

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

						goToRepoSection(currentInstance, instanceToken, restOfUrl[restOfUrl.length - 3], restOfUrl[restOfUrl.length - 2], "issue");
						finish();
					}, 500);
				}
				else {

					Intent mainIntent = new Intent(ctx, MainActivity.class);
					ctx.startActivity(mainIntent);
					finish();
				}
			}
			else if(data.getPathSegments().contains("pulls")) { // pr

				if(!Objects.requireNonNull(data.getLastPathSegment()).contains("pulls") & StringUtils.isNumeric(data.getLastPathSegment())) {

					new Handler(Looper.getMainLooper()).postDelayed(() -> {

						getPullRequest(currentInstance, instanceToken, restOfUrl[restOfUrl.length - 4], restOfUrl[restOfUrl.length - 3],
							Integer.parseInt(data.getLastPathSegment()));
						finish();
					}, 500);

				}
				else if(Objects.requireNonNull(data.getLastPathSegment()).contains("pulls")) {

					new Handler(Looper.getMainLooper()).postDelayed(() -> {

						goToRepoSection(currentInstance, instanceToken, restOfUrl[restOfUrl.length - 3], restOfUrl[restOfUrl.length - 2], "pull");
						finish();
					}, 500);
				}
				else {

					Intent mainIntent = new Intent(ctx, MainActivity.class);
					ctx.startActivity(mainIntent);
					finish();
				}

				Log.e("DeepLinks-2", Objects.requireNonNull(data.getLastPathSegment()));
			}
			else { // no action, show options

				Intent mainIntent = new Intent(ctx, MainActivity.class);
				Log.e("DeepLinks", String.valueOf(tinyDb.getInt("defaultScreenId")));

				if(tinyDb.getInt("defaultScreenId") == 1) { // repos

					mainIntent.putExtra("launchFragmentByLinkHandler", "repos");
					ctx.startActivity(mainIntent);
					finish();
				}
				else if(tinyDb.getInt("defaultScreenId") == 2) { // org

					mainIntent.putExtra("launchFragmentByLinkHandler", "org");
					ctx.startActivity(mainIntent);
					finish();
				}
				else if(tinyDb.getInt("defaultScreenId") == 3) { // notifications

					mainIntent.putExtra("launchFragmentByLinkHandler", "notification");
					ctx.startActivity(mainIntent);
					finish();
				}
				else if(tinyDb.getInt("defaultScreenId") == 4) { // explore

					mainIntent.putExtra("launchFragmentByLinkHandler", "explore");
					ctx.startActivity(mainIntent);
					finish();
				}
				else if(tinyDb.getInt("defaultScreenId") == 0) { // show options

					viewBinding.noActionFrame.setVisibility(View.VISIBLE);

					viewBinding.repository.setOnClickListener(repository -> {

						tinyDb.putInt("defaultScreenId", 1);
						tinyDb.putString("defaultScreenStr", getResources().getString(R.string.navRepos));
						mainIntent.putExtra("launchFragmentByLinkHandler", "repos");
						ctx.startActivity(mainIntent);
						finish();
					});

					viewBinding.organization.setOnClickListener(organization -> {

						tinyDb.putInt("defaultScreenId", 2);
						tinyDb.putString("defaultScreenStr", getResources().getString(R.string.navOrgs));
						mainIntent.putExtra("launchFragmentByLinkHandler", "org");
						ctx.startActivity(mainIntent);
						finish();
					});

					viewBinding.notification.setOnClickListener(notification -> {

						tinyDb.putInt("defaultScreenId", 3);
						tinyDb.putString("defaultScreenStr", getResources().getString(R.string.pageTitleNotifications));
						mainIntent.putExtra("launchFragmentByLinkHandler", "notification");
						ctx.startActivity(mainIntent);
						finish();
					});

					viewBinding.explore.setOnClickListener(explore -> {

						tinyDb.putInt("defaultScreenId", 4);
						tinyDb.putString("defaultScreenStr", getResources().getString(R.string.navExplore));
						mainIntent.putExtra("launchFragmentByLinkHandler", "explore");
						ctx.startActivity(mainIntent);
						finish();
					});

					viewBinding.launchApp2.setOnClickListener(launchApp2 -> {

						tinyDb.putInt("defaultScreenId", 0);
						tinyDb.putString("defaultScreenStr", getResources().getString(R.string.generalDeepLinkSelectedText));
						ctx.startActivity(mainIntent);
						finish();
					});
				}
			}
		}
	}

	private void getPullRequest(String url, String token, String repoOwner, String repoName, int index) {

		Call<PullRequests> call = RetrofitClient
			.getInstance(url, ctx)
			.getApiInterface()
			.getPullRequestByIndex(token, repoOwner, repoName, index);

		call.enqueue(new Callback<PullRequests>() {

			@Override
			public void onResponse(@NonNull Call<PullRequests> call, @NonNull retrofit2.Response<PullRequests> response) {

				PullRequests prInfo = response.body();

				if (response.code() == 200) {

					assert prInfo != null;

					Intent intent = new Intent(ctx, IssueDetailActivity.class);
					intent.putExtra("issueNumber", index);
					intent.putExtra("prMergeable", prInfo.isMergeable());

					if(prInfo.getHead() != null) {

						intent.putExtra("prHeadBranch", prInfo.getHead().getRef());
						tinyDb.putString("prHeadBranch", prInfo.getHead().getRef());

						if(prInfo.getHead().getRepo() != null) {

							tinyDb.putString("prIsFork", String.valueOf(prInfo.getHead().getRepo().isFork()));
							tinyDb.putString("prForkFullName", prInfo.getHead().getRepo().getFull_name());
						}
						else {

							// pull was done from a deleted fork
							tinyDb.putString("prIsFork", "true");
							tinyDb.putString("prForkFullName", ctx.getString(R.string.prDeletedFrok));
						}
					}

					tinyDb.putString("issueNumber", String.valueOf(index));
					tinyDb.putString("prMergeable", String.valueOf(prInfo.isMergeable()));
					tinyDb.putString("issueType", "Pull");

					tinyDb.putString("repoFullName", repoOwner + "/" + repoName);

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

					ctx.startActivity(intent);
				}

				else {

					Log.e("onFailure-links-pr", String.valueOf(response.code()));
				}
			}

			@Override
			public void onFailure(@NonNull Call<PullRequests> call, @NonNull Throwable t) {

				Log.e("onFailure-links-pr", t.toString());
			}
		});
	}

	private void goToRepoSection(String url, String token, String repoOwner, String repoName, String type) {

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
					repoIntent.putExtra("goToSection", "yes");
					repoIntent.putExtra("goToSectionType", type);

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
