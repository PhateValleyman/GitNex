package org.mian.gitnex.fragments;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.content.res.AppCompatResources;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import org.mian.gitnex.R;
import org.mian.gitnex.databinding.BottomSheetUserProfileBinding;

/**
 * Template M M Arif
 * @author qwerty287
 */

public class BottomSheetUserProfileFragment extends BottomSheetDialogFragment {

	private final boolean following;

	public BottomSheetUserProfileFragment(boolean following) {
		this.following = following;
	}

    private BottomSheetUserProfileFragment.BottomSheetListener bmListener;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

	    BottomSheetUserProfileBinding bottomSheetUserProfileBinding = BottomSheetUserProfileBinding.inflate(inflater, container, false);

	    if(following) {
	    	bottomSheetUserProfileBinding.followUnfollowUser.setText(R.string.unfollowUser);
		    Drawable drawable = AppCompatResources.getDrawable(requireContext(), R.drawable.ic_person_remove); assert drawable != null;
		    drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
	    	bottomSheetUserProfileBinding.followUnfollowUser.setCompoundDrawablesRelative(drawable, null, null, null);
	    }

        bottomSheetUserProfileBinding.followUnfollowUser.setOnClickListener(v1 -> {

            bmListener.onButtonClicked("follow");
            dismiss();

        });

        return bottomSheetUserProfileBinding.getRoot();
    }

    public interface BottomSheetListener {
        void onButtonClicked(String text);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        try {
            bmListener = (BottomSheetUserProfileFragment.BottomSheetListener) context;
        }
        catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement BottomSheetListener");
        }
    }

}
