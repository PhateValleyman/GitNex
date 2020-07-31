package org.mian.gitnex.activities;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import org.mian.gitnex.R;
import org.mian.gitnex.helpers.MultiSelectDialog;
import org.mian.gitnex.helpers.TinyDB;
import org.mian.gitnex.models.MultiSelectModel;
import java.util.ArrayList;

/**
 * Author M M Arif
 */

public class AddNewAccountActivity extends BaseActivity {

	final Context ctx = this;
	private Context appCtx;

	@Override
	protected int getLayoutResourceId(){
		return R.layout.activity_add_remove_assignees;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		appCtx = getApplicationContext();
		//supportRequestWindowFeature(Window.FEATURE_NO_TITLE);

		getWindow().getDecorView().setBackground(new ColorDrawable(Color.TRANSPARENT));


	}

}
