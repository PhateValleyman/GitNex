package org.mian.gitnex.activities;

import android.os.Bundle;
import org.mian.gitnex.R;
import org.mian.gitnex.databinding.ActivityDiffBinding;
import org.mian.gitnex.fragments.DiffFilesFragment;

/**
 * @author opyale
 */

public class DiffActivity extends BaseActivity {

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		ActivityDiffBinding binding = ActivityDiffBinding.inflate(getLayoutInflater());

		setContentView(binding.getRoot());

		getSupportFragmentManager()
			.beginTransaction()
			.replace(R.id.fragment_container, DiffFilesFragment.newInstance())
			.commit();

	}

}
