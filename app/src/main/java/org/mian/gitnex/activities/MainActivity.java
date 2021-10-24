package org.mian.gitnex.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentManager;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.navigation.NavigationView;
import org.gitnex.tea4j.models.GiteaVersion;
import org.gitnex.tea4j.models.NotificationCount;
import org.gitnex.tea4j.models.UserInfo;
import org.mian.gitnex.R;
import org.mian.gitnex.adapters.UserAccountsNavAdapter;
import org.mian.gitnex.clients.PicassoService;
import org.mian.gitnex.clients.RetrofitClient;
import org.mian.gitnex.database.api.BaseApi;
import org.mian.gitnex.database.api.UserAccountsApi;
import org.mian.gitnex.database.models.UserAccount;
import org.mian.gitnex.databinding.ActivityLauncherBinding;
import org.mian.gitnex.fragments.BottomSheetDraftsFragment;
import org.mian.gitnex.fragments.DraftsFragment;
import org.mian.gitnex.helpers.AlertDialogs;
import org.mian.gitnex.helpers.AppUtil;
import org.mian.gitnex.helpers.Authorization;
import org.mian.gitnex.helpers.ChangeLog;
import org.mian.gitnex.helpers.ColorInverter;
import org.mian.gitnex.helpers.RoundedTransformation;
import org.mian.gitnex.helpers.TinyDB;
import org.mian.gitnex.helpers.Toasty;
import org.mian.gitnex.helpers.Version;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import jp.wasabeef.picasso.transformations.BlurTransformation;
import retrofit2.Call;
import retrofit2.Callback;

public class MainActivity extends BaseActivity implements BottomSheetDraftsFragment.BottomSheetListener {

	private AppBarConfiguration mAppBarConfiguration;
	private Typeface myTypeface;

	private MenuItem navNotifications;
	private TextView notificationCounter;

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		ActivityLauncherBinding binding = ActivityLauncherBinding.inflate(getLayoutInflater());
		setContentView(binding.getRoot());

		boolean connToInternet = AppUtil.hasNetworkConnection(appCtx);
		Intent mainIntent = getIntent();

		// DO NOT MOVE
		if(mainIntent.hasExtra("switchAccountId") &&
			AppUtil.switchToAccount(ctx, Objects.requireNonNull(BaseApi.getInstance(ctx, UserAccountsApi.class))
				.getAccountById(mainIntent.getIntExtra("switchAccountId", 0)))) {

			mainIntent.removeExtra("switchAccountId");
			recreate();
			return;

		}
		// DO NOT MOVE

		tinyDB.putBoolean("noConnection", false);

		if(!tinyDB.getBoolean("loggedInMode")) {
			logout(this, ctx);
			return;
		}

		if(tinyDB.getInt("currentActiveAccountId", -1) <= 0) {
			AlertDialogs.forceLogoutDialog(ctx,
				getResources().getString(R.string.forceLogoutDialogHeader),
				getResources().getString(R.string.forceLogoutDialogDescription), getResources().getString(R.string.alertDialogTokenRevokedCopyPositiveButton));
		}

		switch(tinyDB.getInt("customFontId", -1)) {

			case 0:
				myTypeface = Typeface.createFromAsset(getAssets(), "fonts/roboto.ttf");
				break;

			case 2:
				myTypeface = Typeface.createFromAsset(getAssets(), "fonts/sourcecodeproregular.ttf");
				break;

			default:
				myTypeface = Typeface.createFromAsset(getAssets(), "fonts/manroperegular.ttf");
				break;

		}


		setSupportActionBar(binding.toolbar);
		DrawerLayout drawer = binding.drawerLayout;
		NavigationView navigationView = binding.navView;
		View hView = navigationView.getHeaderView(0);
		// Passing each menu ID as a set of Ids because each
		// menu should be considered as top level destinations.
		mAppBarConfiguration = new AppBarConfiguration.Builder(R.id.nav_home, R.id.nav_starred_repos, R.id.nav_organizations, R.id.nav_repositories,
			R.id.nav_notifications, R.id.nav_explore, R.id.nav_comments_draft, R.id.nav_profile, R.id.nav_administration, R.id.nav_settings)
			.setOpenableLayout(drawer).build();
		NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_launcher);
		NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
		NavigationUI.setupWithNavController(navigationView, navController);

		Menu menu = navigationView.getMenu();
		navNotifications = menu.findItem(R.id.nav_notifications);

		//TextView toolbarTitle = binding.toolbarTitle;
		//toolbarTitle.setTypeface(myTypeface);

		String loginUid = tinyDB.getString("loginUid");
		String instanceToken = "token " + tinyDB.getString(loginUid + "-token");

		getNotificationsCount(instanceToken);

		menu.findItem(R.id.nav_logout).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {

			@Override
			public boolean onMenuItemClick(MenuItem item) {
				logout(MainActivity.this, ctx);
				overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
				return false;
			}
		});

		drawer.addDrawerListener(new DrawerLayout.DrawerListener() {

			@Override
			public void onDrawerSlide(@NonNull View drawerView, float slideOffset) {}

			@Override
			public void onDrawerOpened(@NonNull View drawerView) {

				if(tinyDB.getBoolean("noConnection")) {
					Toasty.error(ctx, getResources().getString(R.string.checkNetConnection));
					tinyDB.putBoolean("noConnection", false);
				}

				String userEmailNav = tinyDB.getString("userEmail");
				String userFullNameNav = tinyDB.getString("userFullname");
				String userAvatarNav = tinyDB.getString("userAvatar");

				TextView userEmail = hView.findViewById(R.id.userEmail);
				TextView userFullName = hView.findViewById(R.id.userFullname);
				ImageView userAvatar = hView.findViewById(R.id.userAvatar);
				ImageView userAvatarBackground = hView.findViewById(R.id.userAvatarBackground);
				CardView navRecyclerViewFrame = hView.findViewById(R.id.userAccountsFrame);

				List<UserAccount> userAccountsList = new ArrayList<>();
				UserAccountsApi userAccountsApi;
				userAccountsApi = BaseApi.getInstance(ctx, UserAccountsApi.class);

				RecyclerView navRecyclerViewUserAccounts = hView.findViewById(R.id.userAccounts);
				UserAccountsNavAdapter adapterUserAccounts;

				adapterUserAccounts = new UserAccountsNavAdapter(ctx, userAccountsList, drawer);

				assert userAccountsApi != null;
				userAccountsApi.getAllAccounts().observe((AppCompatActivity) ctx, userAccounts -> {
					if(userAccounts.size() > 0) {
						userAccountsList.addAll(userAccounts);
						navRecyclerViewUserAccounts.setAdapter(adapterUserAccounts);
						navRecyclerViewFrame.setVisibility(View.VISIBLE);
					}
				});

				userEmail.setTypeface(myTypeface);
				userFullName.setTypeface(myTypeface);

				if(!userEmailNav.equals("")) {
					userEmail.setText(userEmailNav);
				}

				if(!userFullNameNav.equals("")) {
					userFullName.setText(Html.fromHtml(userFullNameNav));
				}

				if(!userAvatarNav.equals("")) {

					int avatarRadius = AppUtil.getPixelsFromDensity(ctx, 3);

					PicassoService.getInstance(ctx).get()
						.load(userAvatarNav)
						.placeholder(R.drawable.loader_animated)
						.transform(new RoundedTransformation(avatarRadius, 0))
						.resize(160, 160)
						.centerCrop().into(userAvatar);

					PicassoService.getInstance(ctx).get()
						.load(userAvatarNav)
						.transform(new BlurTransformation(ctx))
						.into(userAvatarBackground, new com.squareup.picasso.Callback() {

							@Override
							public void onSuccess() {
								int textColor = new ColorInverter().getImageViewContrastColor(userAvatarBackground);

								userFullName.setTextColor(textColor);
								userEmail.setTextColor(textColor);
							}

							@Override public void onError(Exception e) {}
						});
				}

				userAvatar.setOnClickListener(v -> {
					navController.navigate(R.id.nav_profile);
					drawer.closeDrawers();
				});

				getNotificationsCount(instanceToken);
			}

			@Override
			public void onDrawerClosed(@NonNull View drawerView) {}

			@Override
			public void onDrawerStateChanged(int newState) {}

		});

		String launchFragment = mainIntent.getStringExtra("launchFragment");

		if(launchFragment != null) {

			mainIntent.removeExtra("launchFragment");

			switch(launchFragment) {

				case "drafts":
					navController.navigate(R.id.nav_comments_draft);
					return;

				case "notifications":
					navController.navigate(R.id.nav_notifications);
					return;
			}
		}

		String launchFragmentByHandler = mainIntent.getStringExtra("launchFragmentByLinkHandler");

		if(launchFragmentByHandler != null) {

			mainIntent.removeExtra("launchFragmentByLinkHandler");

			switch(launchFragmentByHandler) {

				case "repos":
					navController.navigate(R.id.nav_repositories);
					return;

				case "org":
					navController.navigate(R.id.nav_organizations);
					return;

				case "notification":
					navController.navigate(R.id.nav_notifications);
					return;

				case "explore":
					navController.navigate(R.id.nav_explore);
					return;

				case "profile":
					navController.navigate(R.id.nav_profile);
					return;

				case "admin":
					navController.navigate(R.id.nav_administration);
					return;

			}
		}

		if(savedInstanceState == null) {

			if(!new Version(tinyDB.getString("giteaVersion")).higherOrEqual("1.12.3")) {
				if(tinyDB.getInt("homeScreenId") == 7) {
					tinyDB.putInt("homeScreenId", 0);
				}
			}

			switch(tinyDB.getInt("homeScreenId")) {

				case 1:
					navController.navigate(R.id.nav_starred_repos);
					break;

				case 2:
					navController.navigate(R.id.nav_organizations);
					break;

				case 3:
					navController.navigate(R.id.nav_repositories);
					break;

				case 4:
					navController.navigate(R.id.nav_profile);
					break;

				case 5:
					navController.navigate(R.id.nav_explore);
					break;

				case 6:
					navController.navigate(R.id.nav_comments_draft);
					break;

				case 7:
					navController.navigate(R.id.nav_notifications);
					break;

				default:
					navController.navigate(R.id.nav_home);
					break;
			}
		}

		if(!connToInternet) {
			if(!tinyDB.getBoolean("noConnection")) {
				Toasty.error(ctx, getResources().getString(R.string.checkNetConnection));
			}

			tinyDB.putBoolean("noConnection", true);
		}
		else {
			loadUserInfo();
			giteaVersion();
			tinyDB.putBoolean("noConnection", false);
		}

		// Changelog popup
		int versionCode = AppUtil.getAppBuildNo(appCtx);

		if(versionCode > tinyDB.getInt("versionCode")) {

			tinyDB.putInt("versionCode", versionCode);
			tinyDB.putBoolean("versionFlag", true);

			ChangeLog changelogDialog = new ChangeLog(this);
			changelogDialog.showDialog();
		}
	}

	@Override
	public boolean onSupportNavigateUp() {

		NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_launcher);
		return NavigationUI.navigateUp(navController, mAppBarConfiguration) || super.onSupportNavigateUp();
	}

	private void giteaVersion() {

		Call<GiteaVersion> callVersion = RetrofitClient.getApiInterface(ctx).getGiteaVersionWithToken(Authorization.get(ctx));
		callVersion.enqueue(new Callback<GiteaVersion>() {

			@Override
			public void onResponse(@NonNull final Call<GiteaVersion> callVersion, @NonNull retrofit2.Response<GiteaVersion> responseVersion) {

				if(responseVersion.code() == 200 && responseVersion.body() != null) {
					String version = responseVersion.body().getVersion();

					tinyDB.putString("giteaVersion", version);
					Objects.requireNonNull(BaseApi.getInstance(ctx, UserAccountsApi.class)).updateServerVersion(version, tinyDB.getInt("currentActiveAccountId"));
				}
			}

			@Override
			public void onFailure(@NonNull Call<GiteaVersion> callVersion, @NonNull Throwable t) {
				Log.e("onFailure-version", t.toString());
			}
		});
	}

	private void loadUserInfo() {

		final TinyDB tinyDb = TinyDB.getInstance(appCtx);

		Call<UserInfo> call = RetrofitClient.getApiInterface(ctx).getUserInfo(Authorization.get(ctx));

		call.enqueue(new Callback<UserInfo>() {

			@Override
			public void onResponse(@NonNull Call<UserInfo> call, @NonNull retrofit2.Response<UserInfo> response) {

				UserInfo userDetails = response.body();

				if(response.isSuccessful()) {

					if(response.code() == 200) {

						assert userDetails != null;

						if(userDetails.getIs_admin() != null) {

							tinyDb.putBoolean("userIsAdmin", userDetails.getIs_admin());
						}

						tinyDb.putString("userLogin", userDetails.getLogin());
						tinyDb.putInt("userId", userDetails.getId());

						if(!userDetails.getFullname().equals("")) {

							tinyDb.putString("userFullname", userDetails.getFullname());
						}
						else {

							tinyDb.putString("userFullname", userDetails.getLogin());
						}

						tinyDb.putString("userEmail", userDetails.getEmail());
						tinyDb.putString("userAvatar", userDetails.getAvatar());

						if(userDetails.getLang() != null) {

							tinyDb.putString("userLang", userDetails.getLang());
						}
						else {

							tinyDb.putString("userLang", "");
						}
					}
				}
				else if(response.code() == 401) {

					AlertDialogs.authorizationTokenRevokedDialog(ctx, getResources().getString(R.string.alertDialogTokenRevokedTitle), getResources().getString(R.string.alertDialogTokenRevokedMessage), getResources().getString(R.string.alertDialogTokenRevokedCopyNegativeButton), getResources().getString(R.string.alertDialogTokenRevokedCopyPositiveButton));
				}
				else {

					String toastError = getResources().getString(R.string.genericApiStatusError) + response.code();
					Toasty.error(ctx, toastError);
				}
			}

			@Override
			public void onFailure(@NonNull Call<UserInfo> call, @NonNull Throwable t) {

				Log.e("onFailure", t.toString());
			}
		});

	}

	private void getNotificationsCount(String token) {

		Call<NotificationCount> call = RetrofitClient.getApiInterface(ctx).checkUnreadNotifications(token);

		call.enqueue(new Callback<NotificationCount>() {

			@Override
			public void onResponse(@NonNull Call<NotificationCount> call, @NonNull retrofit2.Response<NotificationCount> response) {

				NotificationCount notificationCount = response.body();

				if(response.code() == 200) {
					assert notificationCount != null;
					notificationCounter = navNotifications.getActionView().findViewById(R.id.counterBadgeNotification);
					notificationCounter.setText(String.valueOf(notificationCount.getCounter()));
				}
			}

			@Override
			public void onFailure(@NonNull Call<NotificationCount> call, @NonNull Throwable t) {

				Log.e("onFailure-notification", t.toString());
			}
		});
	}

	public static void logout(Activity activity, Context ctx) {

		TinyDB tinyDB = TinyDB.getInstance(ctx);

		tinyDB.putBoolean("loggedInMode", false);
		tinyDB.remove("basicAuthPassword");
		tinyDB.putBoolean("basicAuthFlag", false);
		//tinyDb.clear();
		activity.finish();
		ctx.startActivity(new Intent(ctx, LoginActivity.class));
	}

	@Override
	public void onButtonClicked(String text) {
		int currentActiveAccountId = tinyDB.getInt("currentActiveAccountId");

		if("deleteDrafts".equals(text)) {

			if(currentActiveAccountId > 0) {
				new AlertDialog.Builder(ctx)
					.setTitle(R.string.deleteAllDrafts)
					.setIcon(R.drawable.ic_delete)
					.setCancelable(false)
					.setMessage(R.string.deleteAllDraftsDialogMessage)
					.setPositiveButton(R.string.menuDeleteText, (dialog, which) -> {

						dialog.dismiss();
					})
					.setNeutralButton(R.string.cancelButton, null).show();

			}
			else {

				Toasty.error(ctx, getResources().getString(R.string.genericError));
			}

		}

	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		int id = item.getItemId();

		if(id == R.id.genericMenu) {

			BottomSheetDraftsFragment bottomSheet = new BottomSheetDraftsFragment();
			bottomSheet.show(getSupportFragmentManager(), "draftsBottomSheet");
			return true;
		}

		return super.onOptionsItemSelected(item);
	}

}
