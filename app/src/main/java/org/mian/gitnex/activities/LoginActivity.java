package org.mian.gitnex.activities;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import com.tooltip.Tooltip;
import org.mian.gitnex.R;
import org.mian.gitnex.clients.RetrofitClient;
import org.mian.gitnex.database.api.UserAccountsApi;
import org.mian.gitnex.helpers.AppUtil;
import org.mian.gitnex.helpers.NetworkObserver;
import org.mian.gitnex.helpers.PathsHelper;
import org.mian.gitnex.helpers.TinyDB;
import org.mian.gitnex.helpers.Toasty;
import org.mian.gitnex.helpers.UrlHelper;
import org.mian.gitnex.helpers.Version;
import org.mian.gitnex.models.GiteaVersion;
import org.mian.gitnex.models.UserInfo;
import org.mian.gitnex.models.UserTokens;
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

	private enum Protocol {HTTPS, HTTP}

	private enum LoginType {BASIC, TOKEN}

	private Context appCtx;
	private Context ctx = this;
	private TinyDB tinyDB;

	private Button loginButton;
	private EditText instanceUrlET, loginUidET, loginPassword, otpCode, loginTokenCode;
	private Spinner protocolSpinner;
	private TextView otpInfo;
	private RadioGroup loginMethod;
	private String device_id = "token";

	@Override
	protected int getLayoutResourceId() {

		return R.layout.activity_login;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		appCtx = getApplicationContext();

		tinyDB = new TinyDB(appCtx);
		NetworkObserver networkMonitor = new NetworkObserver(ctx);

		loginButton = findViewById(R.id.login_button);
		instanceUrlET = findViewById(R.id.instance_url);
		loginUidET = findViewById(R.id.login_uid);
		loginPassword = findViewById(R.id.login_passwd);
		otpCode = findViewById(R.id.otpCode);
		otpInfo = findViewById(R.id.otpInfo);
		ImageView info_button = findViewById(R.id.info);
		protocolSpinner = findViewById(R.id.httpsSpinner);
		loginMethod = findViewById(R.id.loginMethod);
		loginTokenCode = findViewById(R.id.loginTokenCode);

		((TextView) findViewById(R.id.appVersion)).setText(AppUtil.getAppVersion(appCtx));

		ArrayAdapter<Protocol> adapterProtocols = new ArrayAdapter<>(LoginActivity.this, R.layout.spinner_item, Protocol.values());
		adapterProtocols.setDropDownViewResource(R.layout.spinner_dropdown_item);

		protocolSpinner.setAdapter(adapterProtocols);
		protocolSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

			public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {

				if(protocolSpinner.getSelectedItem() == Protocol.HTTP) {
					Toasty.warning(ctx, getResources().getString(R.string.protocolError));
				}
			}

			public void onNothingSelected(AdapterView<?> parent) {

			}

		});

		info_button.setOnClickListener(
			view -> new Tooltip.Builder(view).setText(R.string.urlInfoTooltip).setTextColor(getResources().getColor(R.color.colorWhite))
				.setBackgroundColor(getResources().getColor(R.color.tooltipBackground)).setCancelable(true).setDismissOnClick(true).setPadding(30)
				.setCornerRadius(R.dimen.tooltipCornor).setGravity(Gravity.BOTTOM).show());

		loginMethod.setOnCheckedChangeListener((group, checkedId) -> {

			if(checkedId == R.id.loginToken) {

				AppUtil.setMultiVisibility(View.GONE, loginUidET, loginPassword, otpCode, otpInfo);
				loginTokenCode.setVisibility(View.VISIBLE);

			}
			else {

				AppUtil.setMultiVisibility(View.VISIBLE, loginUidET, loginPassword, otpCode, otpInfo);
				loginTokenCode.setVisibility(View.GONE);

			}
		});

		networkMonitor.onInternetStateListener(isAvailable -> {

			if(isAvailable) {

				enableProcessButton();
			}
			else {

				disableProcessButton();
				loginButton.setText(getResources().getString(R.string.btnLogin));
				Toasty.error(ctx, getResources().getString(R.string.checkNetConnection));
			}
		});

		loadDefaults();

		loginButton.setOnClickListener(view -> {

			disableProcessButton();
			login();

		});

	}

	private void login() {

		try {

			String loginUid = loginUidET.getText().toString();
			String loginPass = loginPassword.getText().toString();
			String loginToken = loginTokenCode.getText().toString().trim();

			Protocol protocol = (Protocol) protocolSpinner.getSelectedItem();
			LoginType loginType = (loginMethod.getCheckedRadioButtonId() == R.id.loginUsernamePassword) ? LoginType.BASIC : LoginType.TOKEN;

			URI rawInstanceUrl = UrlBuilder.fromString(UrlHelper.fixScheme(instanceUrlET.getText().toString(), "http")).toUri();

			URI instanceUrlWithProtocol = UrlBuilder.fromUri(rawInstanceUrl).withPath(PathsHelper.join(rawInstanceUrl.getPath()))
				.withScheme(protocol.name().toLowerCase()).toUri();

			URI instanceUrl = UrlBuilder.fromUri(instanceUrlWithProtocol).withPath(PathsHelper.join(instanceUrlWithProtocol.getPath(), "/api/v1/"))
				.toUri();

			tinyDB.putString("loginType", loginType.name().toLowerCase());
			tinyDB.putString("instanceUrlRaw", instanceUrlET.getText().toString());
			tinyDB.putString("instanceUrl", instanceUrl.toString());
			tinyDB.putString("instanceUrlWithProtocol", instanceUrlWithProtocol.toString());

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

				if(rawInstanceUrl.getUserInfo() != null) {

					tinyDB.putString("basicAuthPassword", loginPass);
					tinyDB.putBoolean("basicAuthFlag", true);

				}

				if(loginUid.equals("")) {

					Toasty.error(ctx, getResources().getString(R.string.emptyFieldUsername));
					enableProcessButton();
					return;

				}

				if(loginUid.contains("@")) {

					Toasty.warning(ctx, getResources().getString(R.string.userInvalidUserName));
					enableProcessButton();
					return;

				}

				if(loginPass.equals("")) {

					Toasty.error(ctx, getResources().getString(R.string.emptyFieldPassword));
					enableProcessButton();
					return;

				}

				int loginOTP = (otpCode.length() > 0) ? Integer.parseInt(otpCode.getText().toString().trim()) : 0;
				tinyDB.putString("loginUid", loginUid);

				versionCheck(instanceUrl.toString(), loginUid, loginPass, loginOTP, loginToken, loginType);

			}
			else {

				if(loginToken.equals("")) {

					Toasty.error(ctx, getResources().getString(R.string.loginTokenError));
					enableProcessButton();
					return;

				}

				versionCheck(instanceUrl.toString(), loginUid, loginPass, 123, loginToken, loginType);

			}

		}
		catch(Exception e) {

			Log.e("onFailure-login", e.toString());
			Toasty.error(ctx, getResources().getString(R.string.malformedUrl));
			enableProcessButton();

		}
	}

	private void versionCheck(final String instanceUrl, final String loginUid, final String loginPass, final int loginOTP, final String loginToken,
		final LoginType loginType) {

		Call<GiteaVersion> callVersion;

		if(!loginToken.equals("")) {

			callVersion = RetrofitClient.getInstance(instanceUrl, ctx).getApiInterface().getGiteaVersionWithToken(loginToken);
		}
		else {

			String credential = Credentials.basic(loginUid, loginPass, StandardCharsets.UTF_8);

			callVersion =
				(loginOTP != 0) ? RetrofitClient.getInstance(instanceUrl, ctx).getApiInterface().getGiteaVersionWithOTP(credential, loginOTP) :
					RetrofitClient.getInstance(instanceUrl, ctx).getApiInterface().getGiteaVersionWithBasic(credential);

		}

		callVersion.enqueue(new Callback<GiteaVersion>() {

			@Override
			public void onResponse(@NonNull final Call<GiteaVersion> callVersion, @NonNull retrofit2.Response<GiteaVersion> responseVersion) {

				if(responseVersion.code() == 200) {

					GiteaVersion version = responseVersion.body();
					Version gitea_version;

					assert version != null;

					try {
						gitea_version = new Version(version.getVersion());
					}
					catch(Exception e) {

						Toasty.error(ctx, getResources().getString(R.string.versionUnknown));
						enableProcessButton();
						return;
					}

					if(gitea_version.less(getString(R.string.versionLow))) {

						AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(ctx).setTitle(getString(R.string.versionAlertDialogHeader))
							.setMessage(getResources().getString(R.string.versionUnsupportedOld, version.getVersion())).setIcon(R.drawable.ic_warning)
							.setCancelable(true);

						alertDialogBuilder.setNegativeButton(getString(R.string.cancelButton), (dialog, which) -> {

							dialog.dismiss();
							enableProcessButton();
						});

						alertDialogBuilder.setPositiveButton(getString(R.string.textContinue), (dialog, which) -> {

							dialog.dismiss();
							login(loginType, instanceUrl, loginUid, loginPass, loginOTP, loginToken);
						});

						alertDialogBuilder.create().show();

					}
					else if(gitea_version.lessOrEqual(getString(R.string.versionHigh))) {

						login(loginType, instanceUrl, loginUid, loginPass, loginOTP, loginToken);
					}
					else {

						Toasty.warning(ctx, getResources().getString(R.string.versionUnsupportedNew));
						login(loginType, instanceUrl, loginUid, loginPass, loginOTP, loginToken);

					}

				}
				else if(responseVersion.code() == 403) {

					login(loginType, instanceUrl, loginUid, loginPass, loginOTP, loginToken);
				}
			}

			private void login(LoginType loginType, String instanceUrl, String loginUid, String loginPass, int loginOTP, String loginToken) {

				// ToDo: before store/create token: get UserInfo to check DB/AccountManager if there already exist a token
				// the setup methods then can better handle all different cases

				if(loginType == LoginType.BASIC) {
					setup(instanceUrl, loginUid, loginPass, loginOTP);
				}
				else if(loginType == LoginType.TOKEN) { // Token
					setupUsingExistingToken(instanceUrl, loginToken);
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

	private void setupUsingExistingToken(String instanceUrl, final String loginToken) {

		Call<UserInfo> call = RetrofitClient.getInstance(instanceUrl, ctx).getApiInterface().getUserInfo("token " + loginToken);

		call.enqueue(new Callback<UserInfo>() {

			@Override
			public void onResponse(@NonNull Call<UserInfo> call, @NonNull retrofit2.Response<UserInfo> response) {

				UserInfo userDetails = response.body();

				switch(response.code()) {

					case 200:
						assert userDetails != null;
						tinyDB.putBoolean("loggedInMode", true);
						tinyDB.putString(userDetails.getLogin() + "-token", loginToken);
						tinyDB.putString("loginUid", userDetails.getLogin());
						tinyDB.putString("userLogin", userDetails.getUsername());

						// insert new account to db if does not exist
						String accountName = userDetails.getUsername() + "@" + instanceUrl;
						UserAccountsApi userAccountsApi = new UserAccountsApi(ctx);
						int checkAccount = userAccountsApi.getCount(accountName);

						if(checkAccount == 0) {
							userAccountsApi.insertNewAccount(accountName, instanceUrl, userDetails.getUsername(), loginToken, "");
						}
						else {
							userAccountsApi.updateTokenByAccountName(accountName, loginToken);
						}

						enableProcessButton();
						startActivity(new Intent(LoginActivity.this, MainActivity.class));
						finish();
						break;

					case 401:
						Toasty.error(ctx, getResources().getString(R.string.unauthorizedApiError));
						enableProcessButton();
						break;

					default:
						Toasty.error(ctx, getResources().getString(R.string.genericApiStatusError) + response.code());
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

	private void setup(final String instanceUrl, final String loginUid, final String loginPass, final int loginOTP) {

		final String credential = Credentials.basic(loginUid, loginPass, StandardCharsets.UTF_8);
		final String tokenName = "gitnex-app-" + device_id;

		Call<List<UserTokens>> call;
		if(loginOTP != 0) {

			call = RetrofitClient.getInstance(instanceUrl, ctx).getApiInterface().getUserTokensWithOTP(credential, loginOTP, loginUid);
		}
		else {

			call = RetrofitClient.getInstance(instanceUrl, ctx).getApiInterface().getUserTokens(credential, loginUid);
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

								delcall = RetrofitClient.getInstance(instanceUrl, ctx).getApiInterface()
									.deleteTokenWithOTP(credential, loginOTP, loginUid, t.getId());
							}
							else {

								delcall = RetrofitClient.getInstance(instanceUrl, ctx).getApiInterface().deleteToken(credential, loginUid, t.getId());
							}
							delcall.enqueue(new Callback<Void>() {

								@Override
								public void onResponse(@NonNull Call<Void> delcall, @NonNull retrofit2.Response<Void> response) {

									if(response.code() == 204) {

										setupToken(instanceUrl, loginUid, loginPass, loginOTP, tokenName);
									}
									else {

										Toasty.error(ctx, getResources().getString(R.string.genericApiStatusError) + response.code());
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

					setupToken(instanceUrl, loginUid, loginPass, loginOTP, tokenName);
				}
				else {

					Toasty.error(ctx, getResources().getString(R.string.genericApiStatusError) + response.code());
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

	private void setupToken(final String instanceUrl, final String loginUid, final String loginPass, final int loginOTP, final String tokenName) {

		final String credential = Credentials.basic(loginUid, loginPass, StandardCharsets.UTF_8);

		UserTokens createUserToken = new UserTokens(tokenName);
		Call<UserTokens> callCreateToken;

		if(loginOTP != 0) {

			callCreateToken = RetrofitClient.getInstance(instanceUrl, ctx).getApiInterface()
				.createNewTokenWithOTP(credential, loginOTP, loginUid, createUserToken);
		}
		else {

			callCreateToken = RetrofitClient.getInstance(instanceUrl, ctx).getApiInterface().createNewToken(credential, loginUid, createUserToken);
		}

		callCreateToken.enqueue(new Callback<UserTokens>() {

			@Override
			public void onResponse(@NonNull Call<UserTokens> callCreateToken, @NonNull retrofit2.Response<UserTokens> responseCreate) {

				if(responseCreate.code() == 201) {

					UserTokens newToken = responseCreate.body();
					assert newToken != null;

					if(!newToken.getSha1().equals("")) {

						Call<UserInfo> call = RetrofitClient.getInstance(instanceUrl, ctx).getApiInterface()
							.getUserInfo("token " + newToken.getSha1());

						call.enqueue(new Callback<UserInfo>() {

							@Override
							public void onResponse(@NonNull Call<UserInfo> call, @NonNull retrofit2.Response<UserInfo> response) {

								UserInfo userDetails = response.body();

								switch(response.code()) {

									case 200:
										assert userDetails != null;
										tinyDB.remove("loginPass");
										tinyDB.putBoolean("loggedInMode", true);
										tinyDB.putString("userLogin", userDetails.getUsername());
										tinyDB.putString(loginUid + "-token", newToken.getSha1());
										tinyDB.putString(loginUid + "-token-last-eight", newToken.getToken_last_eight());

										// insert new account to db if does not exist
										String accountName = userDetails.getUsername() + "@" + instanceUrl;
										UserAccountsApi userAccountsApi = new UserAccountsApi(ctx);
										int checkAccount = userAccountsApi.getCount(accountName);

										if(checkAccount == 0) {
											userAccountsApi
												.insertNewAccount(accountName, instanceUrl, userDetails.getUsername(), newToken.getSha1(), "");
										}
										else {
											userAccountsApi.updateTokenByAccountName(accountName, newToken.getSha1());
										}

										startActivity(new Intent(LoginActivity.this, MainActivity.class));
										finish();
										break;

									case 401:
										Toasty.error(ctx, getResources().getString(R.string.unauthorizedApiError));
										enableProcessButton();
										break;

									default:
										Toasty.error(ctx, getResources().getString(R.string.genericApiStatusError) + response.code());
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

					Toasty.error(ctx, getResources().getString(R.string.genericApiStatusError) + responseCreate.code());
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

		if(!tinyDB.getString("loginUid").equals("")) {
			loginUidET.setText(tinyDB.getString("loginUid"));
		}

		if(tinyDB.getBoolean("loggedInMode")) {

			startActivity(new Intent(LoginActivity.this, MainActivity.class));
			finish();
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

		GradientDrawable shape = new GradientDrawable();
		shape.setCornerRadius(8);
		shape.setColor(getResources().getColor(R.color.hintColor));

		loginButton.setText(R.string.processingText);
		loginButton.setBackground(shape);
		loginButton.setEnabled(false);

	}

	private void enableProcessButton() {

		GradientDrawable shape = new GradientDrawable();
		shape.setCornerRadius(8);
		shape.setColor(getResources().getColor(R.color.btnBackground));

		loginButton.setText(R.string.btnLogin);
		loginButton.setBackground(shape);
		loginButton.setEnabled(true);

	}

}
