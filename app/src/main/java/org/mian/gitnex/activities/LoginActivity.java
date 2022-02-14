package org.mian.gitnex.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import org.gitnex.tea4j.models.GiteaVersion;
import org.gitnex.tea4j.models.UserInfo;
import org.gitnex.tea4j.models.UserTokens;
import org.mian.gitnex.R;
import org.mian.gitnex.clients.RetrofitClient;
import org.mian.gitnex.database.api.BaseApi;
import org.mian.gitnex.database.api.UserAccountsApi;
import org.mian.gitnex.database.models.UserAccount;
import org.mian.gitnex.databinding.ActivityLoginBinding;
import org.mian.gitnex.helpers.AppUtil;
import org.mian.gitnex.helpers.NetworkStatusObserver;
import org.mian.gitnex.helpers.PathsHelper;
import org.mian.gitnex.helpers.TinyDB;
import org.mian.gitnex.helpers.Toasty;
import org.mian.gitnex.helpers.UrlHelper;
import org.mian.gitnex.helpers.Version;
import org.mian.gitnex.helpers.contexts.AccountContext;
import org.mian.gitnex.structs.Protocol;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;
import io.mikael.urlbuilder.UrlBuilder;
import okhttp3.Credentials;
import retrofit2.Call;
import retrofit2.Callback;

/**
 * Author M M Arif
 */

public class LoginActivity extends BaseActivity {

	private enum LoginType {BASIC, TOKEN}

	private Button loginButton;
	private EditText instanceUrlET, loginUidET, loginPassword, otpCode, loginTokenCode;
	private AutoCompleteTextView protocolSpinner;
	private RadioGroup loginMethod;
	private String device_id = "token";
	private String selectedProtocol;

	private URI instanceUrl;
	private Version giteaVersion;

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		ActivityLoginBinding activityLoginBinding = ActivityLoginBinding.inflate(getLayoutInflater());
		setContentView(activityLoginBinding.getRoot());

		NetworkStatusObserver networkStatusObserver = NetworkStatusObserver.getInstance(ctx);

		loginButton = activityLoginBinding.loginButton;
		instanceUrlET = activityLoginBinding.instanceUrl;
		loginUidET = activityLoginBinding.loginUid;
		loginPassword = activityLoginBinding.loginPasswd;
		otpCode = activityLoginBinding.otpCode;
		protocolSpinner = activityLoginBinding.httpsSpinner;
		loginMethod = activityLoginBinding.loginMethod;
		loginTokenCode = activityLoginBinding.loginTokenCode;

		activityLoginBinding.appVersion.setText(AppUtil.getAppVersion(appCtx));

		ArrayAdapter<Protocol> adapterProtocols = new ArrayAdapter<>(LoginActivity.this, R.layout.list_spinner_items, Protocol.values());

		instanceUrlET.setText(getIntent().getStringExtra("instanceUrl"));

		protocolSpinner.setAdapter(adapterProtocols);
		protocolSpinner.setSelection(0);
		protocolSpinner.setOnItemClickListener((parent, view, position, id) -> {

			selectedProtocol = String.valueOf(parent.getItemAtPosition(position));

			if(selectedProtocol.equals(String.valueOf(Protocol.HTTP))) {

				Toasty.warning(ctx, getResources().getString(R.string.protocolError));
			}
		});

		if(R.id.loginToken == loginMethod.getCheckedRadioButtonId()) {

			AppUtil.setMultiVisibility(View.GONE, findViewById(R.id.login_uidLayout), findViewById(R.id.login_passwdLayout), findViewById(R.id.otpCodeLayout));
			findViewById(R.id.loginTokenCodeLayout).setVisibility(View.VISIBLE);
		}
		else {

			AppUtil.setMultiVisibility(View.VISIBLE, findViewById(R.id.login_uidLayout), findViewById(R.id.login_passwdLayout), findViewById(R.id.otpCodeLayout));
			findViewById(R.id.loginTokenCodeLayout).setVisibility(View.GONE);
		}

		loginMethod.setOnCheckedChangeListener((group, checkedId) -> {

			if(checkedId == R.id.loginToken) {

				AppUtil.setMultiVisibility(View.GONE, findViewById(R.id.login_uidLayout), findViewById(R.id.login_passwdLayout), findViewById(R.id.otpCodeLayout));
				findViewById(R.id.loginTokenCodeLayout).setVisibility(View.VISIBLE);
			}
			else {

				AppUtil.setMultiVisibility(View.VISIBLE, findViewById(R.id.login_uidLayout), findViewById(R.id.login_passwdLayout), findViewById(R.id.otpCodeLayout));
				findViewById(R.id.loginTokenCodeLayout).setVisibility(View.GONE);
			}
		});

		Handler handler = new Handler(getMainLooper());

		networkStatusObserver.registerNetworkStatusListener(hasNetworkConnection -> {

			handler.post(() -> {

				if(hasNetworkConnection) {

					enableProcessButton();
				}
				else {

					disableProcessButton();
					loginButton.setText(getResources().getString(R.string.btnLogin));
					Toasty.error(ctx, getResources().getString(R.string.checkNetConnection));
				}

			});
		});

		loadDefaults();

		loginButton.setOnClickListener(view -> {

			disableProcessButton();
			login();
		});
	}

	private void login() {

		try {

			if(selectedProtocol == null) {

				Toasty.error(ctx, getResources().getString(R.string.protocolEmptyError));
				enableProcessButton();
				return;
			}

			String loginUid = loginUidET.getText().toString();
			String loginPass = loginPassword.getText().toString();
			String loginToken = loginTokenCode.getText().toString().trim();

			LoginType loginType = (loginMethod.getCheckedRadioButtonId() == R.id.loginUsernamePassword) ? LoginType.BASIC : LoginType.TOKEN;

			URI rawInstanceUrl = UrlBuilder.fromString(UrlHelper.fixScheme(instanceUrlET.getText().toString(), "http")).toUri();

			instanceUrl = UrlBuilder.fromUri(rawInstanceUrl).withScheme(selectedProtocol.toLowerCase()).withPath(PathsHelper.join(rawInstanceUrl.getPath(), "/api/v1/"))
				.toUri();

			// cache values to make them available the next time the user wants to log in
			tinyDB.putString("loginType", loginType.name().toLowerCase());
			tinyDB.putString("instanceUrlRaw", instanceUrlET.getText().toString());

			if(instanceUrlET.getText().toString().equals("")) {

				Toasty.error(ctx, getResources().getString(R.string.emptyFieldURL));
				enableProcessButton();
				return;
			}

			if(loginType == LoginType.BASIC) {

				if(otpCode.length() != 0 && otpCode.length() != 6) {

					Toasty.warning(ctx, getResources().getString(R.string.loginOTPTypeError));
					enableProcessButton();
					return;
				}

				if(loginUid.equals("")) {
					Toasty.error(ctx, getResources().getString(R.string.emptyFieldUsername));
					enableProcessButton();
					return;
				}

				if(loginPass.equals("")) {
					Toasty.error(ctx, getResources().getString(R.string.emptyFieldPassword));
					enableProcessButton();
					return;
				}

				int loginOTP = (otpCode.length() > 0) ? Integer.parseInt(otpCode.getText().toString().trim()) : 0;

				versionCheck(loginUid, loginPass, loginOTP, loginToken, loginType);

			}
			else {

				if(loginToken.equals("")) {

					Toasty.error(ctx, getResources().getString(R.string.loginTokenError));
					enableProcessButton();
					return;
				}

				versionCheck(loginUid, loginPass, 123, loginToken, loginType);
			}

		}
		catch(Exception e) {

			Log.e("onFailure-login", e.toString());
			Toasty.error(ctx, getResources().getString(R.string.malformedUrl));
			enableProcessButton();
		}
	}

	private void versionCheck(final String loginUid, final String loginPass, final int loginOTP, final String loginToken,
		final LoginType loginType) {

		Call<GiteaVersion> callVersion;

		if(!loginToken.equals("")) {

			callVersion = RetrofitClient.getApiInterface(appCtx).getGiteaVersionWithToken("token " + loginToken);
		}
		else {

			String credential = Credentials.basic(loginUid, loginPass, StandardCharsets.UTF_8);

			callVersion =
				(loginOTP != 0) ? RetrofitClient.getApiInterface(appCtx).getGiteaVersionWithOTP(credential, loginOTP) :
					RetrofitClient.getApiInterface(appCtx).getGiteaVersionWithBasic(credential);
		}

		callVersion.enqueue(new Callback<GiteaVersion>() {

			@Override
			public void onResponse(@NonNull final Call<GiteaVersion> callVersion, @NonNull retrofit2.Response<GiteaVersion> responseVersion) {

				if(responseVersion.code() == 200) {

					GiteaVersion version = responseVersion.body();
					assert version != null;

					if(!Version.valid(version.getVersion())) {

						Toasty.error(ctx, getResources().getString(R.string.versionUnknown));
						enableProcessButton();
						return;
					}

					giteaVersion = new Version(version.getVersion());

					if(giteaVersion.less(getString(R.string.versionLow))) {

						AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(ctx)
							.setTitle(getString(R.string.versionAlertDialogHeader))
							.setMessage(getResources().getString(R.string.versionUnsupportedOld, version.getVersion()))
							.setIcon(R.drawable.ic_warning)
							.setCancelable(true);

						alertDialogBuilder.setNeutralButton(getString(R.string.cancelButton), (dialog, which) -> {

							dialog.dismiss();
							enableProcessButton();
						});

						alertDialogBuilder.setPositiveButton(getString(R.string.textContinue), (dialog, which) -> {

							dialog.dismiss();
							login(loginType, loginUid, loginPass, loginOTP, loginToken);
						});

						alertDialogBuilder.create().show();

					}
					else if(giteaVersion.lessOrEqual(getString(R.string.versionHigh))) {

						login(loginType, loginUid, loginPass, loginOTP, loginToken);
					}
					else {

						Toasty.warning(ctx, getResources().getString(R.string.versionUnsupportedNew));
						login(loginType, loginUid, loginPass, loginOTP, loginToken);

					}

				}
				else if(responseVersion.code() == 403) {

					login(loginType, loginUid, loginPass, loginOTP, loginToken);
				}
			}

			private void login(LoginType loginType, String loginUid, String loginPass, int loginOTP, String loginToken) {

				// ToDo: before store/create token: get UserInfo to check DB/AccountManager if there already exist a token
				// the setup methods then can better handle all different cases

				if(loginType == LoginType.BASIC) {

					setup(loginUid, loginPass, loginOTP);
				}
				else if(loginType == LoginType.TOKEN) { // Token

					setupUsingExistingToken(loginToken);
				}
			}

			@Override
			public void onFailure(@NonNull Call<GiteaVersion> callVersion, @NonNull Throwable t) {

				Log.e("onFailure-versionCheck", t.toString());
				Toasty.error(ctx, getResources().getString(R.string.errorOnLogin));
				enableProcessButton();
			}
		});
	}

	private void setupUsingExistingToken(final String loginToken) {

		Call<UserInfo> call = RetrofitClient.getApiInterface(appCtx).getUserInfo("token " + loginToken);

		call.enqueue(new Callback<UserInfo>() {

			@Override
			public void onResponse(@NonNull Call<UserInfo> call, @NonNull retrofit2.Response<UserInfo> response) {

				UserInfo userDetails = response.body();

				switch(response.code()) {

					case 200:

						assert userDetails != null;

						// insert new account to db if does not exist
						String accountName = userDetails.getUsername() + "@" + instanceUrl;
						UserAccountsApi userAccountsApi = BaseApi.getInstance(ctx, UserAccountsApi.class);
						assert userAccountsApi != null;
						boolean userAccountExists = userAccountsApi.userAccountExists(accountName);
						UserAccount account;
						if(!userAccountExists) {
							long accountId = userAccountsApi.createNewAccount(accountName, instanceUrl.toString(), userDetails.getUsername(), loginToken, giteaVersion.toString());
							account = userAccountsApi.getAccountById((int) accountId);
						}
						else {
							userAccountsApi.updateTokenByAccountName(accountName, loginToken);
							userAccountsApi.login(userAccountsApi.getAccountByName(accountName).getAccountId());
							account = userAccountsApi.getAccountByName(accountName);
						}

						AppUtil.switchToAccount(LoginActivity.this, account);

						enableProcessButton();
						startActivity(new Intent(LoginActivity.this, MainActivity.class));
						finish();
						break;
					case 401:

						Toasty.error(ctx, getResources().getString(R.string.unauthorizedApiError));
						enableProcessButton();
						break;
					default:

						Toasty.error(ctx, getResources().getString(R.string.genericApiError, response.code()));
						enableProcessButton();
				}
			}

			@Override
			public void onFailure(@NonNull Call<UserInfo> call, @NonNull Throwable t) {

				Log.e("onFailure", t.toString());
				Toasty.error(ctx, getResources().getString(R.string.genericError));
				enableProcessButton();
			}
		});

	}

	private void setup(final String loginUid, final String loginPass, final int loginOTP) {

		final String credential = Credentials.basic(loginUid, loginPass, StandardCharsets.UTF_8);
		final String tokenName = "gitnex-app-" + device_id;

		Call<List<UserTokens>> call;
		if(loginOTP != 0) {

			call = RetrofitClient.getApiInterface(appCtx).getUserTokensWithOTP(credential, loginOTP, loginUid);
		}
		else {

			call = RetrofitClient.getApiInterface(appCtx).getUserTokens(credential, loginUid);
		}

		call.enqueue(new Callback<List<UserTokens>>() {

			@Override
			public void onResponse(@NonNull Call<List<UserTokens>> call, @NonNull retrofit2.Response<List<UserTokens>> response) {

				List<UserTokens> userTokens = response.body();

				if(response.code() == 200) {

					assert userTokens != null;

					for(UserTokens t : userTokens) {

						if(t.getName().equals(tokenName)) {

							// this app had created an token on this instance before
							// -> since it looks like GitNex forgot the secret we have to delete it first

							Call<Void> delcall;
							if(loginOTP != 0) {

								delcall = RetrofitClient.getApiInterface(ctx)
									.deleteTokenWithOTP(credential, loginOTP, loginUid, t.getId());
							}
							else {

								delcall = RetrofitClient.getApiInterface(ctx).deleteToken(credential, loginUid, t.getId());
							}

							delcall.enqueue(new Callback<Void>() {

								@Override
								public void onResponse(@NonNull Call<Void> delcall, @NonNull retrofit2.Response<Void> response) {

									if(response.code() == 204) {

										setupToken(loginUid, loginPass, loginOTP, tokenName);
									}
									else {

										Toasty.error(ctx, getResources().getString(R.string.genericApiError, response.code()));
										enableProcessButton();
									}
								}

								@Override
								public void onFailure(@NonNull Call<Void> delcall, @NonNull Throwable t) {

									Log.e("onFailure-login", t.toString());
									Toasty.error(ctx, getResources().getString(R.string.malformedJson));
									enableProcessButton();
								}
							});
							return;
						}
					}

					setupToken(loginUid, loginPass, loginOTP, tokenName);
				}
				else {

					Toasty.error(ctx, getResources().getString(R.string.genericApiError, response.code()));
					enableProcessButton();
				}
			}

			@Override
			public void onFailure(@NonNull Call<List<UserTokens>> call, @NonNull Throwable t) {

				Log.e("onFailure-login", t.toString());
				Toasty.error(ctx, getResources().getString(R.string.malformedJson));
				enableProcessButton();
			}
		});

	}

	private void setupToken(final String loginUid, final String loginPass, final int loginOTP, final String tokenName) {

		final String credential = Credentials.basic(loginUid, loginPass, StandardCharsets.UTF_8);

		UserTokens createUserToken = new UserTokens(tokenName);
		Call<UserTokens> callCreateToken;

		if(loginOTP != 0) {

			callCreateToken = RetrofitClient.getApiInterface(ctx)
				.createNewTokenWithOTP(credential, loginOTP, loginUid, createUserToken);
		}
		else {

			callCreateToken = RetrofitClient.getApiInterface(ctx)
				.createNewToken(credential, loginUid, createUserToken);
		}

		callCreateToken.enqueue(new Callback<UserTokens>() {

			@Override
			public void onResponse(@NonNull Call<UserTokens> callCreateToken, @NonNull retrofit2.Response<UserTokens> responseCreate) {

				if(responseCreate.code() == 201) {

					UserTokens newToken = responseCreate.body();
					assert newToken != null;

					if(!newToken.getSha1().equals("")) {

						Call<UserInfo> call = RetrofitClient.getApiInterface(ctx)
							.getUserInfo("token " + newToken.getSha1());

						call.enqueue(new Callback<UserInfo>() {

							@Override
							public void onResponse(@NonNull Call<UserInfo> call, @NonNull retrofit2.Response<UserInfo> response) {

								UserInfo userDetails = response.body();

								switch(response.code()) {

									case 200:

										assert userDetails != null;

										// insert new account to db if does not exist
										String accountName = userDetails.getUsername() + "@" + instanceUrl;
										UserAccountsApi userAccountsApi = BaseApi.getInstance(ctx, UserAccountsApi.class);
										assert userAccountsApi != null;
										boolean userAccountExists = userAccountsApi.userAccountExists(accountName);

										UserAccount account;
										if(!userAccountExists) {
											long accountId = userAccountsApi
												.createNewAccount(accountName, instanceUrl.toString(), userDetails.getUsername(), newToken.getSha1(), giteaVersion.toString());
											account = userAccountsApi.getAccountById((int) accountId);
										}
										else {
											userAccountsApi.updateTokenByAccountName(accountName, newToken.getSha1());
											account = userAccountsApi.getAccountByName(accountName);
										}

										AppUtil.switchToAccount(LoginActivity.this, account);

										startActivity(new Intent(LoginActivity.this, MainActivity.class));
										finish();
										break;
									case 401:

										Toasty.error(ctx, getResources().getString(R.string.unauthorizedApiError));
										enableProcessButton();
										break;
									default:

										Toasty.error(ctx, getResources().getString(R.string.genericApiError, response.code()));
										enableProcessButton();
								}
							}

							@Override
							public void onFailure(@NonNull Call<UserInfo> call, @NonNull Throwable t) {

								Log.e("onFailure", t.toString());
								Toasty.error(ctx, getResources().getString(R.string.genericError));
								enableProcessButton();
							}
						});
					}
				}
				else if(responseCreate.code() == 500) {

					Toasty.error(ctx, getResources().getString(R.string.genericApiError, responseCreate.code()));
					enableProcessButton();
				}
			}

			@Override
			public void onFailure(@NonNull Call<UserTokens> createUserToken, @NonNull Throwable t) {

				Log.e("onFailure-token", t.toString());
			}
		});
	}


	private void loadDefaults() {

		if(tinyDB.getString("loginType").equals(LoginType.BASIC.name().toLowerCase())) {

			loginMethod.check(R.id.loginUsernamePassword);
		}
		else {

			loginMethod.check(R.id.loginToken);
		}

		if(!tinyDB.getString("instanceUrlRaw").equals("")) {

			instanceUrlET.setText(tinyDB.getString("instanceUrlRaw"));
		}

		if(getAccount() != null) {

			loginUidET.setText(getAccount().getAccount().getUserName());
		}

		if(!tinyDB.getString("uniqueAppId").isEmpty()) {
			device_id = tinyDB.getString("uniqueAppId");
		}
		else {

			device_id = UUID.randomUUID().toString();
			tinyDB.putString("uniqueAppId", device_id);
		}
	}

	private void disableProcessButton() {

		loginButton.setText(R.string.processingText);
		loginButton.setEnabled(false);
	}

	private void enableProcessButton() {

		loginButton.setText(R.string.btnLogin);
		loginButton.setEnabled(true);
	}

}
