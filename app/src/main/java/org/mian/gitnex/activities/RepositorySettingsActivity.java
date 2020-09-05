package org.mian.gitnex.activities;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import org.mian.gitnex.R;
import org.mian.gitnex.helpers.TinyDB;

/**
 * Author M M Arif
 */

public class RepositorySettingsActivity extends BaseActivity {

	private View.OnClickListener onClickListener;
	final Context ctx = this;
	private Context appCtx;

	@Override
	protected int getLayoutResourceId(){
		return R.layout.activity_repository_settings;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		appCtx = getApplicationContext();

		TinyDB tinyDb = new TinyDB(appCtx);
		final String instanceUrl = tinyDb.getString("instanceUrl");
		final String loginUid = tinyDb.getString("loginUid");
		String repoFullName = tinyDb.getString("repoFullName");
		String[] parts = repoFullName.split("/");
		final String instanceToken = "token " + tinyDb.getString(loginUid + "-token");

		ImageView closeActivity = findViewById(R.id.close);

		initCloseListener();
		closeActivity.setOnClickListener(onClickListener);

	}

	private void initCloseListener() {
		onClickListener = view -> finish();
	}

}
