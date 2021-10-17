package org.mian.gitnex.fragments;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import org.gitnex.tea4j.models.OrgPermissions;
import org.mian.gitnex.clients.RetrofitClient;
import org.mian.gitnex.databinding.BottomSheetOrganizationBinding;
import org.mian.gitnex.helpers.Authorization;
import org.mian.gitnex.helpers.TinyDB;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Author M M Arif
 */

public class BottomSheetOrganizationFragment extends BottomSheetDialogFragment {

    private BottomSheetOrganizationFragment.BottomSheetListener bmListener;
    private String org;

    public BottomSheetOrganizationFragment(String org) {
    	this.org = org;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

	    BottomSheetOrganizationBinding bottomSheetOrganizationBinding = BottomSheetOrganizationBinding.inflate(inflater, container, false);

	    RetrofitClient.getApiInterface(requireContext()).getOrgPermissions(Authorization.get(requireContext()), TinyDB.getInstance(requireContext()).getString("loginUid"), org)
	    .enqueue(new Callback<List<OrgPermissions>>() {

		    @Override
		    public void onResponse(@NonNull Call<List<OrgPermissions>> call, @NonNull Response<List<OrgPermissions>> response) {
		    	OrgPermissions permissions = response.body().get(0);
				if(response.isSuccessful() && permissions != null) {
					if(!permissions.canCreateRepositories()) {
						bottomSheetOrganizationBinding.createRepository.setVisibility(View.GONE);
					}
					if(!permissions.isOwner()) {
						bottomSheetOrganizationBinding.createLabel.setVisibility(View.GONE);
						bottomSheetOrganizationBinding.createTeam.setVisibility(View.GONE);
					}
				}
		    }

		    @Override
		    public void onFailure(@NonNull Call<List<OrgPermissions>> call, @NonNull Throwable t) {

		    }
	    });

	    bottomSheetOrganizationBinding.createTeam.setOnClickListener(v1 -> {

            bmListener.onButtonClicked("team");
            dismiss();
        });

	    bottomSheetOrganizationBinding.createLabel.setOnClickListener(v1 -> {

		    bmListener.onButtonClicked("label");
		    dismiss();
	    });

	    bottomSheetOrganizationBinding.createRepository.setOnClickListener(v12 -> {

            bmListener.onButtonClicked("repository");
            dismiss();
        });

	    bottomSheetOrganizationBinding.copyOrgUrl.setOnClickListener(v1 -> {

		    bmListener.onButtonClicked("copyOrgUrl");
		    dismiss();
	    });

        return bottomSheetOrganizationBinding.getRoot();
    }

    public interface BottomSheetListener {

        void onButtonClicked(String text);
    }

    @Override
    public void onAttach(@NonNull Context context) {

        super.onAttach(context);

        try {
            bmListener = (BottomSheetOrganizationFragment.BottomSheetListener) context;
        }
        catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement BottomSheetListener");
        }
    }

}
