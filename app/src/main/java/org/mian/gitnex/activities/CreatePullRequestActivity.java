package org.mian.gitnex.activities;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import org.mian.gitnex.R;
import org.mian.gitnex.databinding.ActivityCreatePrBinding;
import org.mian.gitnex.helpers.TinyDB;

/**
 * Author M M Arif
 */

public class CreatePullRequestActivity extends BaseActivity {

	private View.OnClickListener onClickListener;
	private Context ctx = this;
	private Context appCtx;
	private TinyDB tinyDb;
	private ActivityCreatePrBinding viewBinding;

	@Override
	protected int getLayoutResourceId(){
		return R.layout.activity_create_pr;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		appCtx = getApplicationContext();
		tinyDb = new TinyDB(appCtx);

		final String instanceUrl = tinyDb.getString("instanceUrl");
		final String loginUid = tinyDb.getString("loginUid");
		final String loginFullName = tinyDb.getString("userFullname");
		String repoFullName = tinyDb.getString("repoFullName");
		String[] parts = repoFullName.split("/");
		final String repoOwner = parts[0];
		final String repoName = parts[1];
		final String instanceToken = "token " + tinyDb.getString(loginUid + "-token");

		ImageView closeActivity = findViewById(R.id.close);

		initCloseListener();
		closeActivity.setOnClickListener(onClickListener);

	}

	private void initCloseListener() {

		onClickListener = view -> finish();
	}

	private void disableProcessButton() {

		viewBinding.createPr.setEnabled(false);
	}

	private void enableProcessButton() {

		viewBinding.createPr.setEnabled(true);
	}
}
