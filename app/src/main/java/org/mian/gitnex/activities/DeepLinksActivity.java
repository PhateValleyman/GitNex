package org.mian.gitnex.activities;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import org.mian.gitnex.R;
import org.mian.gitnex.database.api.UserAccountsApi;
import org.mian.gitnex.databinding.ActivityDeeplinksBinding;
import org.mian.gitnex.helpers.TinyDB;
import java.util.Objects;

/**
 * Author M M Arif
 */

public class DeepLinksActivity extends BaseActivity {

	private ActivityDeeplinksBinding viewBinding;
	private Context ctx = this;
	private Context appCtx;
	private TinyDB tinyDb;

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

	}
}
