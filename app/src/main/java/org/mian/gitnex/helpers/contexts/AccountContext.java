package org.mian.gitnex.helpers.contexts;

import android.content.Context;
import org.mian.gitnex.database.api.UserAccountsApi;
import org.mian.gitnex.database.models.UserAccount;
import org.mian.gitnex.helpers.Version;
import java.util.Objects;
import okhttp3.Credentials;

public class AccountContext {

	private final UserAccount account;

	private boolean isBasicAuth = false;
	private String basicAuthPassword = "";

	public static AccountContext fromId(int id, Context context) {
		return new AccountContext(Objects.requireNonNull(UserAccountsApi.getInstance(context, UserAccountsApi.class)).getAccountById(id));
	}

	public AccountContext(UserAccount account) {
		this.account = account;
	}

	public UserAccount getAccount() {

		return account;
	}

	public String getAuthorization() {
		String loginUid = account.getUserName();

		// TODO
		if(isBasicAuth &&
			!basicAuthPassword.isEmpty()) {

			return Credentials.basic(loginUid, basicAuthPassword);
		}

		return  "token " + account.getToken();
	}

	public String getWebAuthorization() {
		return Credentials.basic("", account.getUserName()); // FIXME this is not correct and will never work!
	}

	public Version getServerVersion() {
		return new Version(account.getServerVersion());
	}

	public boolean requiresVersion(String version) {
		return getServerVersion().higherOrEqual(version);
	}

}
