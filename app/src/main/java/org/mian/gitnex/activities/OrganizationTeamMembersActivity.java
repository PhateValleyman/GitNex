package org.mian.gitnex.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import androidx.annotation.NonNull;
import org.gitnex.tea4j.models.Teams;
import org.gitnex.tea4j.models.UserInfo;
import org.mian.gitnex.R;
import org.mian.gitnex.adapters.UserGridAdapter;
import org.mian.gitnex.clients.RetrofitClient;
import org.mian.gitnex.databinding.ActivityOrgTeamMembersBinding;
import org.mian.gitnex.fragments.BottomSheetOrganizationTeamsFragment;
import org.mian.gitnex.helpers.Authorization;
import org.mian.gitnex.helpers.TinyDB;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import org.mian.gitnex.structs.BottomSheetListener;

/**
 * Author M M Arif
 */

public class OrganizationTeamMembersActivity extends BaseActivity implements BottomSheetListener {

	private ActivityOrgTeamMembersBinding binding;
	private UserGridAdapter adapter;

    private Teams team;
    private final List<UserInfo> teamUserInfo = new ArrayList<>();

    @SuppressLint("SetTextI18n")
    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

	    binding = ActivityOrgTeamMembersBinding.inflate(getLayoutInflater());

	    setContentView(binding.getRoot());
        setSupportActionBar(binding.toolbar);

	    team = (Teams) getIntent().getSerializableExtra("team");
	    adapter = new UserGridAdapter(ctx, teamUserInfo);

	    if(team.getName() != null && !team.getName().isEmpty()) {
		    binding.toolbarTitle.setText(team.getName());
	    } else {
		    binding.toolbarTitle.setText(R.string.orgTeamMembers);
	    }

	    binding.close.setOnClickListener(view -> finish());
	    binding.members.setAdapter(adapter);

	    StringBuilder permissions = new StringBuilder();

	    // Future proofing in case of gitea becoming able to assign multiple permissions per team
	    for(String permission : Collections.singletonList(team.getPermission())) {

		    switch(permission) {
			    case "none":
				    permissions.append(getString(R.string.teamPermissionNone)).append("\n");
				    break;
			    case "read":
				    permissions.append(getString(R.string.teamPermissionRead)).append("\n");
				    break;
			    case "write":
				    permissions.append(getString(R.string.teamPermissionWrite)).append("\n");
				    break;
			    case "admin":
				    permissions.append(getString(R.string.teamPermissionAdmin)).append("\n");
				    break;
			    case "owner":
				    permissions.append(getString(R.string.teamPermissionOwner)).append("\n");
				    break;
		    }
	    }

	    binding.permissions.setText(permissions.toString());
        fetchMembersAsync();
    }

    @Override
    public void onResume() {

        super.onResume();
        TinyDB tinyDb = TinyDB.getInstance(appCtx);

        if(tinyDb.getBoolean("teamActionFlag")) {
            fetchMembersAsync();
            tinyDb.putBoolean("teamActionFlag", false);
        }
    }

    private void fetchMembersAsync() {

	    Call<List<UserInfo>> call = RetrofitClient
		    .getApiInterface(ctx)
		    .getTeamMembersByOrg(Authorization.get(ctx), team.getId());

	    binding.progressBar.setVisibility(View.VISIBLE);

	    call.enqueue(new Callback<List<UserInfo>>() {

		    @Override
		    public void onResponse(@NonNull Call<List<UserInfo>> call, @NonNull Response<List<UserInfo>> response) {
			    if(response.isSuccessful() && response.body() != null) {
			    	teamUserInfo.clear();
			    	teamUserInfo.addAll(response.body());

				    adapter.notifyDataSetChanged();

				    if(response.body().size() > 0) {
					    binding.noDataMembers.setVisibility(View.GONE);
					    binding.members.setVisibility(View.VISIBLE);
				    } else {
					    binding.members.setVisibility(View.GONE);
					    binding.noDataMembers.setVisibility(View.VISIBLE);
				    }
			    }
			    binding.progressBar.setVisibility(View.GONE);
		    }

		    @Override
		    public void onFailure(@NonNull Call<List<UserInfo>> call, @NonNull Throwable t) {
			    Log.i("onFailure", t.toString());
		    }

	    });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.generic_nav_dotted_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

	    if(id == android.R.id.home) {

		    finish();
		    return true;
	    } else if(id == R.id.genericMenu) {

		    BottomSheetOrganizationTeamsFragment bottomSheet = new BottomSheetOrganizationTeamsFragment();
		    bottomSheet.show(getSupportFragmentManager(), "orgTeamsBottomSheet");
		    return true;
	    } else {
		    return super.onOptionsItemSelected(item);
	    }
    }

    @Override
    public void onButtonClicked(String text) {
        if("newMember".equals(text)) {
            Intent intent = new Intent(OrganizationTeamMembersActivity.this, AddNewTeamMemberActivity.class);
            intent.putExtra("team", team);
            startActivity(intent);
        }
    }

}
