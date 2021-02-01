package org.mian.gitnex.activities;

import android.os.Bundle;
import android.view.View;
import org.mian.gitnex.databinding.ActivityAdminCronTasksBinding;

/**
 * Author M M Arif
 */

public class AdminCronTasksActivity extends BaseActivity {

	private View.OnClickListener onClickListener;

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		ActivityAdminCronTasksBinding activityAdminCronTasksBinding = ActivityAdminCronTasksBinding.inflate(getLayoutInflater());
		setContentView(activityAdminCronTasksBinding.getRoot());

		initCloseListener();
		activityAdminCronTasksBinding.close.setOnClickListener(onClickListener);

	}

	private void initCloseListener() {
		onClickListener = view -> finish();
	}
}
