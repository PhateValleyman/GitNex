package org.mian.gitnex.activities;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import com.google.android.material.navigation.NavigationView;
import com.squareup.picasso.NetworkPolicy;
import org.mian.gitnex.R;
import org.mian.gitnex.clients.PicassoService;
import org.mian.gitnex.clients.RetrofitClient;
import org.mian.gitnex.database.models.UserAccounts;
import org.mian.gitnex.database.repository.UserAccountsRepository;
import org.mian.gitnex.fragments.AboutFragment;
import org.mian.gitnex.fragments.BottomSheetDraftsFragment;
import org.mian.gitnex.fragments.DraftsFragment;
import org.mian.gitnex.fragments.AdministrationFragment;
import org.mian.gitnex.fragments.ExploreRepositoriesFragment;
import org.mian.gitnex.fragments.MyRepositoriesFragment;
import org.mian.gitnex.fragments.OrganizationsFragment;
import org.mian.gitnex.fragments.ProfileFragment;
import org.mian.gitnex.fragments.RepositoriesFragment;
import org.mian.gitnex.fragments.SettingsFragment;
import org.mian.gitnex.fragments.StarredRepositoriesFragment;
import org.mian.gitnex.helpers.AlertDialogs;
import org.mian.gitnex.helpers.Authorization;
import org.mian.gitnex.helpers.ChangeLog;
import org.mian.gitnex.helpers.RoundedTransformation;
import org.mian.gitnex.helpers.Toasty;
import org.mian.gitnex.models.GiteaVersion;
import org.mian.gitnex.models.UserInfo;
import org.mian.gitnex.util.AppUtil;
import org.mian.gitnex.util.TinyDB;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import retrofit2.Call;
import retrofit2.Callback;

/**
 * Author M M Arif
 */

public class MainActivity extends BaseActivity implements NavigationView.OnNavigationItemSelectedListener, BottomSheetDraftsFragment.BottomSheetListener {

	private DrawerLayout drawer;
	private TextView userFullName;
	private TextView userEmail;
	private ImageView userAvatar;
	private TextView toolbarTitle;
	final Context ctx = this;
	private Typeface myTypeface;

	@Override
	protected int getLayoutResourceId() {

		return R.layout.activity_main;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		final TinyDB tinyDb = new TinyDB(getApplicationContext());
		tinyDb.putBoolean("noConnection", false);

		final String instanceUrl = tinyDb.getString("instanceUrl");
		final String loginUid = tinyDb.getString("loginUid");
		final String instanceToken = "token " + tinyDb.getString(loginUid + "-token");

		if(tinyDb.getString("dateFormat").isEmpty()) {
			tinyDb.putString("dateFormat", "pretty");
		}

		if(tinyDb.getString("codeBlockStr").isEmpty()) {
			tinyDb.putInt("codeBlockColor", getResources().getColor(R.color.colorLightGreen));
			tinyDb.putInt("codeBlockBackground", getResources().getColor(R.color.black));
		}

		if(tinyDb.getString("enableCounterIssueBadgeInit").isEmpty()) {
			tinyDb.putBoolean("enableCounterIssueBadge", true);
		}

		if(tinyDb.getString("homeScreenStr").isEmpty()) {
			tinyDb.putInt("homeScreenId", 0);
		}

		boolean connToInternet = AppUtil.haveNetworkConnection(getApplicationContext());

		if(!tinyDb.getBoolean("loggedInMode")) {
			logout(this, ctx);
			return;
		}

		String accountName = loginUid + "@" + instanceUrl;
		try {
			getAcountData(accountName);
		}
		catch(ExecutionException | InterruptedException e) {
			Log.e("getAcountData", e.toString());
		}

		Toolbar toolbar = findViewById(R.id.toolbar);
		toolbarTitle = toolbar.findViewById(R.id.toolbar_title);

		switch(tinyDb.getInt("customFontId", -1)) {

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

		toolbarTitle.setTypeface(myTypeface);
		setSupportActionBar(toolbar);

		FragmentManager fm = getSupportFragmentManager();
		Fragment fragmentById = fm.findFragmentById(R.id.fragment_container);
		if(fragmentById instanceof SettingsFragment) {
			toolbarTitle.setText(getResources().getString(R.string.pageTitleSettings));
		}
		else if(fragmentById instanceof MyRepositoriesFragment) {
			toolbarTitle.setText(getResources().getString(R.string.pageTitleMyRepos));
		}
		else if(fragmentById instanceof StarredRepositoriesFragment) {
			toolbarTitle.setText(getResources().getString(R.string.pageTitleStarredRepos));
		}
		else if(fragmentById instanceof OrganizationsFragment) {
			toolbarTitle.setText(getResources().getString(R.string.pageTitleOrganizations));
		}
		else if(fragmentById instanceof ExploreRepositoriesFragment) {
			toolbarTitle.setText(getResources().getString(R.string.pageTitleExplore));
		}
		else if(fragmentById instanceof ProfileFragment) {
			toolbarTitle.setText(getResources().getString(R.string.pageTitleProfile));
		}
		else if(fragmentById instanceof AboutFragment) {
			toolbarTitle.setText(getResources().getString(R.string.pageTitleAbout));
		}
		else if(fragmentById instanceof DraftsFragment) {
			toolbarTitle.setText(getResources().getString(R.string.pageTitleDrafts));
		}
		else if(fragmentById instanceof AdministrationFragment) {
			toolbarTitle.setText(getResources().getString(R.string.pageTitleAdministration));
		}

		drawer = findViewById(R.id.drawer_layout);
		NavigationView navigationView = findViewById(R.id.nav_view);
		navigationView.setNavigationItemSelectedListener(this);
		final View hView = navigationView.getHeaderView(0);

		ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
		toggle.getDrawerArrowDrawable().setColor(getResources().getColor(R.color.darkGreen));
		drawer.addDrawerListener(toggle);

		drawer.addDrawerListener(new DrawerLayout.DrawerListener() {

			@Override
			public void onDrawerSlide(@NonNull View drawerView, float slideOffset) {

			}

			@Override
			public void onDrawerOpened(@NonNull View drawerView) {

				if(tinyDb.getBoolean("noConnection")) {
					Toasty.info(getApplicationContext(), getResources().getString(R.string.checkNetConnection));
					tinyDb.putBoolean("noConnection", false);
				}

				String userEmailNav = tinyDb.getString("userEmail");
				String userFullNameNav = tinyDb.getString("userFullname");
				String userAvatarNav = tinyDb.getString("userAvatar");

				userEmail = hView.findViewById(R.id.userEmail);
				if(!userEmailNav.equals("")) {
					userEmail.setText(userEmailNav);
					userEmail.setTypeface(myTypeface);
				}

				userFullName = hView.findViewById(R.id.userFullname);
				if(!userFullNameNav.equals("")) {
					userFullName.setText(userFullNameNav);
					userFullName.setTypeface(myTypeface);
				}

				userAvatar = hView.findViewById(R.id.userAvatar);
				if(!userAvatarNav.equals("")) {
					PicassoService.getInstance(ctx).get().load(userAvatarNav).networkPolicy(NetworkPolicy.OFFLINE).placeholder(R.drawable.loader_animated).transform(new RoundedTransformation(8, 0)).resize(160, 160).centerCrop().into(userAvatar);
				}

				userAvatar.setOnClickListener(v -> {

					getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new ProfileFragment()).commit();
					drawer.closeDrawers();

				});

			}

			@Override
			public void onDrawerClosed(@NonNull View drawerView) {
				// Called when a drawer has settled in a completely closed state.
			}

			@Override
			public void onDrawerStateChanged(int newState) {
				// Called when the drawer motion state changes. The new state will be one of STATE_IDLE, STATE_DRAGGING or STATE_SETTLING.
			}
		});

		toggle.syncState();

		if(savedInstanceState == null) {
			if(tinyDb.getInt("homeScreenId") == 0) {
				toolbarTitle.setText(getResources().getString(R.string.pageTitleMyRepos));
				getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new MyRepositoriesFragment()).commit();
				navigationView.setCheckedItem(R.id.nav_home);
			}
			else if(tinyDb.getInt("homeScreenId") == 1) {
				toolbarTitle.setText(getResources().getString(R.string.pageTitleStarredRepos));
				getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new StarredRepositoriesFragment()).commit();
				navigationView.setCheckedItem(R.id.nav_starred_repos);
			}
			else if(tinyDb.getInt("homeScreenId") == 2) {
				toolbarTitle.setText(getResources().getString(R.string.pageTitleOrganizations));
				getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new OrganizationsFragment()).commit();
				navigationView.setCheckedItem(R.id.nav_organizations);
			}
			else if(tinyDb.getInt("homeScreenId") == 3) {
				toolbarTitle.setText(getResources().getString(R.string.pageTitleRepositories));
				getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new RepositoriesFragment()).commit();
				navigationView.setCheckedItem(R.id.nav_repositories);
			}
			else if(tinyDb.getInt("homeScreenId") == 4) {
				toolbarTitle.setText(getResources().getString(R.string.pageTitleProfile));
				getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new ProfileFragment()).commit();
				navigationView.setCheckedItem(R.id.nav_profile);
			}
			else {
				toolbarTitle.setText(getResources().getString(R.string.pageTitleMyRepos));
				getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new MyRepositoriesFragment()).commit();
				navigationView.setCheckedItem(R.id.nav_home);
			}
		}

		if(!connToInternet) {

			if(!tinyDb.getBoolean("noConnection")) {
				Toasty.info(getApplicationContext(), getResources().getString(R.string.checkNetConnection));
			}

			tinyDb.putBoolean("noConnection", true);

		}
		else {

			displayUserInfo(instanceUrl, instanceToken, loginUid);
			giteaVersion(instanceUrl);
			tinyDb.putBoolean("noConnection", false);

		}

		// Changelog popup
		int versionCode = 0;
		try {
			PackageInfo packageInfo = getApplicationContext().getPackageManager().getPackageInfo(getApplicationContext().getPackageName(), 0);
			versionCode = packageInfo.versionCode;
		}
		catch(PackageManager.NameNotFoundException e) {
			Log.e("changelogDialog", Objects.requireNonNull(e.getMessage()));
		}

		if(versionCode > tinyDb.getInt("versionCode")) {
			tinyDb.putInt("versionCode", versionCode);
			tinyDb.putBoolean("versionFlag", true);
			ChangeLog changelogDialog = new ChangeLog(this);
			changelogDialog.showDialog();
		}
	}

	@Override
	public void onButtonClicked(String text) {

		TinyDB tinyDb = new TinyDB(ctx);
		int currentActiveAccountId = tinyDb.getInt("currentActiveAccountId");

		if("deleteDrafts".equals(text)) {

			if(currentActiveAccountId > 0) {

				FragmentManager fm = getSupportFragmentManager();
				DraftsFragment frag = (DraftsFragment) fm.findFragmentById(R.id.fragment_container);

				if(frag != null) {

					new AlertDialog.Builder(ctx).setTitle(R.string.deleteAllDrafts).setIcon(R.drawable.ic_delete).setCancelable(false).setMessage(R.string.deleteAllDraftsDialogMessage).setPositiveButton(R.string.menuDeleteText, (dialog, which) -> {

						frag.deleteAllDrafts(currentActiveAccountId);
						dialog.dismiss();

					}).setNegativeButton(R.string.cancelButton, (dialog, which) -> dialog.dismiss()).show();

				}
				else {
					Toasty.error(ctx, getResources().getString(R.string.genericError));
				}

			}
			else {
				Toasty.error(ctx, getResources().getString(R.string.genericError));
			}

		}

	}

	public void getAcountData(String accountName) throws ExecutionException, InterruptedException {

		UserAccountsRepository accountData = new UserAccountsRepository(ctx);
		UserAccounts data = accountData.getAccountData(accountName);

		if(data != null) {
			TinyDB tinyDb = new TinyDB(ctx.getApplicationContext());
			tinyDb.putInt("currentActiveAccountId", data.getAccountId());
		}
		else {
			AlertDialogs.forceLogoutDialog(ctx, getResources().getString(R.string.forceLogoutDialogHeader), getResources().getString(R.string.forceLogoutDialogDescription), getResources().getString(R.string.alertDialogTokenRevokedCopyPositiveButton));
		}

	}

	@Override
	public void onBackPressed() {

		if(drawer.isDrawerOpen(GravityCompat.START)) {
			drawer.closeDrawer(GravityCompat.START);
		}
		else {
			super.onBackPressed();
		}

	}

	@Override
	public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {

		switch(menuItem.getItemId()) {
			case R.id.nav_home:
				toolbarTitle.setText(getResources().getString(R.string.pageTitleMyRepos));
				getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new MyRepositoriesFragment()).commit();
				break;
			case R.id.nav_organizations:
				toolbarTitle.setText(getResources().getString(R.string.pageTitleOrganizations));
				getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new OrganizationsFragment()).commit();
				break;
			case R.id.nav_profile:
				toolbarTitle.setText(getResources().getString(R.string.pageTitleProfile));
				getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new ProfileFragment()).commit();
				break;
			case R.id.nav_repositories:
				toolbarTitle.setText(getResources().getString(R.string.pageTitleRepositories));
				getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new RepositoriesFragment()).commit();

				break;
			case R.id.nav_settings:
				toolbarTitle.setText(getResources().getString(R.string.pageTitleSettings));
				getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new SettingsFragment()).commit();
				break;
			case R.id.nav_logout:
				logout(this, ctx);
				overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
				break;
			case R.id.nav_about:
				toolbarTitle.setText(getResources().getString(R.string.pageTitleAbout));
				getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new AboutFragment()).commit();
				break;
			case R.id.nav_rate_app:
				rateThisApp();
				break;
			case R.id.nav_starred_repos:
				toolbarTitle.setText(getResources().getString(R.string.pageTitleStarredRepos));
				getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new StarredRepositoriesFragment()).commit();
				break;
			case R.id.nav_explore:
				toolbarTitle.setText(getResources().getString(R.string.pageTitleExplore));
				getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new ExploreRepositoriesFragment()).commit();
				break;
			case R.id.nav_comments_draft:
				toolbarTitle.setText(getResources().getString(R.string.pageTitleDrafts));
				getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new DraftsFragment()).commit();
				break;
			case R.id.nav_administration:
				toolbarTitle.setText(getResources().getString(R.string.pageTitleAdministration));
				getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new AdministrationFragment()).commit();
				break;

		}

		drawer.closeDrawer(GravityCompat.START);
		return true;
	}

	public void rateThisApp() {

		try {
			startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + getPackageName())));
		}
		catch(ActivityNotFoundException e) {
			startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + getPackageName())));
		}
	}

	public static void logout(Activity activity, Context ctx) {

		TinyDB tinyDb = new TinyDB(ctx.getApplicationContext());
		tinyDb.putBoolean("loggedInMode", false);
		tinyDb.remove("basicAuthPassword");
		tinyDb.putBoolean("basicAuthFlag", false);
		//tinyDb.clear();
		activity.finish();
		ctx.startActivity(new Intent(ctx, LoginActivity.class));

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

	private void giteaVersion(final String instanceUrl) {

		final TinyDB tinyDb = new TinyDB(getApplicationContext());

		final String token = "token " + tinyDb.getString(tinyDb.getString("loginUid") + "-token");

		Call<GiteaVersion> callVersion = RetrofitClient.getInstance(instanceUrl, getApplicationContext()).getApiInterface().getGiteaVersionWithToken(token);

		callVersion.enqueue(new Callback<GiteaVersion>() {

			@Override
			public void onResponse(@NonNull final Call<GiteaVersion> callVersion, @NonNull retrofit2.Response<GiteaVersion> responseVersion) {

				if(responseVersion.code() == 200) {

					GiteaVersion version = responseVersion.body();
					assert version != null;

					tinyDb.putString("giteaVersion", version.getVersion());

				}

			}

			@Override
			public void onFailure(@NonNull Call<GiteaVersion> callVersion, @NonNull Throwable t) {

				Log.e("onFailure-version", t.toString());

			}

		});

	}

	private void displayUserInfo(String instanceUrl, String token, String loginUid) {

		final TinyDB tinyDb = new TinyDB(getApplicationContext());

		Call<UserInfo> call = RetrofitClient.getInstance(instanceUrl, getApplicationContext()).getApiInterface().getUserInfo(Authorization.returnAuthentication(getApplicationContext(), loginUid, token));

		NavigationView navigationView = findViewById(R.id.nav_view);
		final View hView = navigationView.getHeaderView(0);

		call.enqueue(new Callback<UserInfo>() {

			@Override
			public void onResponse(@NonNull Call<UserInfo> call, @NonNull retrofit2.Response<UserInfo> response) {

				UserInfo userDetails = response.body();

				if(response.isSuccessful()) {

					if(response.code() == 200) {

						assert userDetails != null;
						if(userDetails.getIs_admin() != null) {
							tinyDb.putBoolean("userIsAdmin", userDetails.getIs_admin());
							navigationView.getMenu().findItem(R.id.nav_administration).setVisible(userDetails.getIs_admin());
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
							tinyDb.putString("userLang", "...");
						}

						userAvatar = hView.findViewById(R.id.userAvatar);
						if(!Objects.requireNonNull(userDetails).getAvatar().equals("")) {
							PicassoService.getInstance(ctx).get().load(userDetails.getAvatar()).placeholder(R.drawable.loader_animated).transform(new RoundedTransformation(8, 0)).resize(160, 160).centerCrop().into(userAvatar);
						}
						else {
							userAvatar.setImageResource(R.mipmap.app_logo_round);
						}

						userFullName = hView.findViewById(R.id.userFullname);
						if(!userDetails.getFullname().equals("")) {
							userFullName.setText(userDetails.getFullname());
						}
						else if(!userDetails.getLogin().equals("")) {
							userFullName.setText(userDetails.getLogin());
						}
						else {
							userFullName.setText("...");
						}

						userEmail = hView.findViewById(R.id.userEmail);
						if(!userDetails.getEmail().equals("")) {
							userEmail.setText(userDetails.getEmail());
						}
						else {
							userEmail.setText("...");
						}

						userAvatar.setOnClickListener(v -> {

							getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new ProfileFragment()).commit();
							drawer.closeDrawers();

						});

					}

				}
				else if(response.code() == 401) {

					AlertDialogs.authorizationTokenRevokedDialog(ctx, getResources().getString(R.string.alertDialogTokenRevokedTitle), getResources().getString(R.string.alertDialogTokenRevokedMessage), getResources().getString(R.string.alertDialogTokenRevokedCopyNegativeButton), getResources().getString(R.string.alertDialogTokenRevokedCopyPositiveButton));

				}
				else {

					String toastError = getResources().getString(R.string.genericApiStatusError) + response.code();
					Toasty.info(getApplicationContext(), toastError);

				}

			}

			@Override
			public void onFailure(@NonNull Call<UserInfo> call, @NonNull Throwable t) {

				Log.e("onFailure", t.toString());
			}
		});

	}

}
