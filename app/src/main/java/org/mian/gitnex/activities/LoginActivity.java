package org.mian.gitnex.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import org.gitnex.tea4j.v2.models.AccessToken;
import org.gitnex.tea4j.v2.models.CreateAccessTokenOption;
import org.gitnex.tea4j.v2.models.ServerVersion;
import org.gitnex.tea4j.v2.models.User;
import org.mian.gitnex.R;
import org.mian.gitnex.clients.RetrofitClient;
import org.mian.gitnex.database.api.BaseApi;
import org.mian.gitnex.database.api.UserAccountsApi;
import org.mian.gitnex.database.models.UserAccount;
import org.mian.gitnex.databinding.ActivityLoginBinding;
import org.mian.gitnex.helpers.AppUtil;
import org.mian.gitnex.helpers.NetworkStatusObserver;
import org.mian.gitnex.helpers.PathsHelper;
import org.mian.gitnex.helpers.Toasty;
import org.mian.gitnex.helpers.UrlHelper;
import org.mian.gitnex.helpers.Version;
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
		} else {
			AppUtil.setMultiVisibility(View.VISIBLE, findViewById(R.id.login_uidLayout), findViewById(R.id.login_passwdLayout), findViewById(R.id.otpCodeLayout));
			findViewById(R.id.loginTokenCodeLayout).setVisibility(View.GONE);
		}

		loginMethod.setOnCheckedChangeListener((group, checkedId) -> {
			if(checkedId == R.id.loginToken) {
				AppUtil.setMultiVisibility(View.GONE, findViewById(R.id.login_uidLayout), findViewById(R.id.login_passwdLayout), findViewById(R.id.otpCodeLayout));
				findViewById(R.id.loginTokenCodeLayout).setVisibility(View.VISIBLE);
			} else {
				AppUtil.setMultiVisibility(View.VISIBLE, findViewById(R.id.login_uidLayout), findViewById(R.id.login_passwdLayout), findViewById(R.id.otpCodeLayout));
				findViewById(R.id.loginTokenCodeLayout).setVisibility(View.GONE);
			}
		});

		networkStatusObserver.registerNetworkStatusListener(hasNetworkConnection -> runOnUiThread(() -> {
			if(hasNetworkConnection) {
				enableProcessButton();
			} else {
				disableProcessButton();
				loginButton.setText(getResources().getString(R.string.btnLogin));
				Toasty.error(ctx, getResources().getString(R.string.checkNetConnection));
			}
		}));

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

		Call<ServerVersion> callVersion;

		if(!loginToken.equals("")) {

			callVersion = RetrofitClient.getApiInterface(ctx, instanceUrl.toString(), loginToken).getVersion();
		}
		else {

			String credential = Credentials.basic(loginUid, loginPass, StandardCharsets.UTF_8);

			callVersion =
				(loginOTP != 0) ? RetrofitClient.getApiInterface(ctx, instanceUrl.toString(), credential).getVersion(loginOTP) :
					RetrofitClient.getApiInterface(ctx, instanceUrl.toString(), credential).getVersion();
		}

		callVersion.enqueue(new Callback<ServerVersion>() {

			@Override
			public void onResponse(@NonNull final Call<ServerVersion> callVersion, @NonNull retrofit2.Response<ServerVersion> responseVersion) {

				if(responseVersion.code() == 200) {

					ServerVersion version = responseVersion.body();
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
			public void onFailure(@NonNull Call<ServerVersion> callVersion, @NonNull Throwable t) {

				Log.e("onFailure-versionCheck", t.toString());
				Toasty.error(ctx, getResources().getString(R.string.errorOnLogin));
				enableProcessButton();
			}
		});
	}

	private void setupUsingExistingToken(final String loginToken) {

		Call<User> call = RetrofitClient.getApiInterface(ctx, instanceUrl.toString(), loginToken).userGetCurrent();

		call.enqueue(new Callback<User>() {

			@Override
			public void onResponse(@NonNull Call<User> call, @NonNull retrofit2.Response<User> response) {

				User userDetails = response.body();

				switch(response.code()) {

					case 200:

						assert userDetails != null;

						// insert new account to db if does not exist
						String accountName = userDetails.getLogin() + "@" + instanceUrl;
						UserAccountsApi userAccountsApi = BaseApi.getInstance(ctx, UserAccountsApi.class);
						assert userAccountsApi != null;
						boolean userAccountExists = userAccountsApi.userAccountExists(accountName);
						UserAccount account;
						if(!userAccountExists) {
							long accountId = userAccountsApi.createNewAccount(accountName, instanceUrl.toString(), userDetails.getLogin(), loginToken, giteaVersion.toString());
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
			public void onFailure(@NonNull Call<User> call, @NonNull Throwable t) {

				Log.e("onFailure", t.toString());
				Toasty.error(ctx, getResources().getString(R.string.genericError));
				enableProcessButton();
			}
		});

	}

	private void setup(final String loginUid, final String loginPass, final int loginOTP) {

		final String credential = Credentials.basic(loginUid, loginPass, StandardCharsets.UTF_8);
		final String tokenName = "gitnex-app-" + device_id;

		Call<List<AccessToken>> call;
		if(loginOTP != 0) {

			call = RetrofitClient.getApiInterface(ctx, instanceUrl.toString(), credential).userGetTokens(loginOTP, loginUid, null, null);
		}
		else {

			call = RetrofitClient.getApiInterface(ctx, instanceUrl.toString(), credential).userGetTokens(loginUid, null, null);
		}

		call.enqueue(new Callback<List<AccessToken>>() {

			@Override
			public void onResponse(@NonNull Call<List<AccessToken>> call, @NonNull retrofit2.Response<List<AccessToken>> response) {

				List<AccessToken> userTokens = response.body();

				if(response.code() == 200) {

					assert userTokens != null;

					for(AccessToken t : userTokens) {

						if(t.getName().equals(tokenName)) {

							// this app had created an token on this instance before
							// -> since it looks like GitNex forgot the secret we have to delete it first

							Call<Void> delcall;
							if(loginOTP != 0) {

								delcall = RetrofitClient.getApiInterface(ctx, instanceUrl.toString(), credential)
									.userDeleteAccessToken(loginOTP, loginUid, String.valueOf(t.getId()));
							}
							else {

								delcall = RetrofitClient.getApiInterface(ctx, instanceUrl.toString(), credential).userDeleteAccessToken(loginUid, String.valueOf(t.getId()));
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
			public void onFailure(@NonNull Call<List<AccessToken>> call, @NonNull Throwable t) {

				Log.e("onFailure-login", t.toString());
				Toasty.error(ctx, getResources().getString(R.string.malformedJson));
				enableProcessButton();
			}
		});

	}

	private void setupToken(final String loginUid, final String loginPass, final int loginOTP, final String tokenName) {

		final String credential = Credentials.basic(loginUid, loginPass, StandardCharsets.UTF_8);

		CreateAccessTokenOption createUserToken = new CreateAccessTokenOption().name(tokenName);
		Call<AccessToken> callCreateToken;

		if(loginOTP != 0) {

			callCreateToken = RetrofitClient.getApiInterface(ctx, instanceUrl.toString(), credential)
				.userCreateToken(loginOTP, loginUid, createUserToken);
		}
		else {

			callCreateToken = RetrofitClient.getApiInterface(ctx, instanceUrl.toString(), credential)
				.userCreateToken(loginUid, createUserToken);
		}

		callCreateToken.enqueue(new Callback<AccessToken>() {

			@Override
			public void onResponse(@NonNull Call<AccessToken> callCreateToken, @NonNull retrofit2.Response<AccessToken> responseCreate) {

				if(responseCreate.code() == 201) {

					AccessToken newToken = responseCreate.body();
					assert newToken != null;

					if(!newToken.getSha1().equals("")) {

						Call<User> call = RetrofitClient.getApiInterface(ctx, instanceUrl.toString(), newToken.getSha1())
							.userGetCurrent();

						call.enqueue(new Callback<User>() {

							@Override
							public void onResponse(@NonNull Call<User> call, @NonNull retrofit2.Response<User> response) {

								User userDetails = response.body();

								switch(response.code()) {

									case 200:

										assert userDetails != null;

										// insert new account to db if does not exist
										String accountName = userDetails.getLogin() + "@" + instanceUrl;
										UserAccountsApi userAccountsApi = BaseApi.getInstance(ctx, UserAccountsApi.class);
										assert userAccountsApi != null;
										boolean userAccountExists = userAccountsApi.userAccountExists(accountName);

										UserAccount account;
										if(!userAccountExists) {
											long accountId = userAccountsApi
												.createNewAccount(accountName, instanceUrl.toString(), userDetails.getLogin(), newToken.getSha1(), giteaVersion.toString());
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
							public void onFailure(@NonNull Call<User> call, @NonNull Throwable t) {

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
			public void onFailure(@NonNull Call<AccessToken> createUserToken, @NonNull Throwable t) {

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
