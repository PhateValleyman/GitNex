package org.mian.gitnex.fragments;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import org.mian.gitnex.R;
import org.mian.gitnex.helpers.StaticGlobalVariables;

/**
 * Author M M Arif
 */

public class BottomSheetDraftsFragment extends BottomSheetDialogFragment {

	private String TAG = StaticGlobalVariables.tagDraftsBottomSheet;
	private BottomSheetDraftsFragment.BottomSheetListener bmListener;

	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.bottom_sheet_drafts, container, false);

		TextView deleteAllDrafts = v.findViewById(R.id.deleteAllDrafts);

		deleteAllDrafts.setOnClickListener(v1 -> {

			Context ctx = v1.getContext();
			dismiss();

			new AlertDialog.Builder(ctx)
					.setTitle(R.string.deleteAllDrafts)
					.setIcon(R.drawable.ic_delete)
					.setCancelable(false)
					.setMessage(R.string.deleteAllDraftsDialogMessage)
					.setPositiveButton(R.string.menuDeleteText, (dialog, which) -> {
						bmListener.onButtonClicked("deleteDrafts");
						dialog.dismiss();
					})
					.setNegativeButton(R.string.cancelButton, (dialog, which) -> dialog.dismiss())
					.show();

		});

		return v;
	}

	public interface BottomSheetListener {
		void onButtonClicked(String text);
	}

	@Override
	public void onAttach(@NonNull Context context) {
		super.onAttach(context);

		try {
			bmListener = (BottomSheetDraftsFragment.BottomSheetListener) context;
		}
		catch (ClassCastException e) {
			Log.e(TAG, e.toString());
		}
	}

}
