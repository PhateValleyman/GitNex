package org.mian.gitnex.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import org.mian.gitnex.R;

public class BottomSheetNotificationsFragment extends BottomSheetDialogFragment {

	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

		View v = inflater.inflate(R.layout.bottom_sheet_notifications, container, false);

		TextView markWatched = v.findViewById(R.id.markWatched);
		TextView markPinned = v.findViewById(R.id.markPinned);

		markWatched.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {

				dismiss();
			}
		});

		markPinned.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {

				dismiss();
			}
		});

		return v;
	}
}
