package org.mian.gitnex.adapters;

import android.content.Context;
import android.content.Intent;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import org.gitnex.tea4j.v2.models.User;
import org.mian.gitnex.R;
import org.mian.gitnex.activities.BaseActivity;
import org.mian.gitnex.activities.ProfileActivity;
import org.mian.gitnex.clients.PicassoService;
import org.mian.gitnex.clients.RetrofitClient;
import org.mian.gitnex.helpers.AlertDialogs;
import org.mian.gitnex.helpers.AppUtil;
import org.mian.gitnex.helpers.RoundedTransformation;
import org.mian.gitnex.helpers.Toasty;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Author M M Arif
 */

public class UserSearchForTeamMemberAdapter extends RecyclerView.Adapter<UserSearchForTeamMemberAdapter.UserSearchViewHolder> {

	private final List<User> usersSearchList;
	private final Context context;
	private final int teamId;
	private final String orgName;

	public UserSearchForTeamMemberAdapter(List<User> dataList, Context ctx, int teamId, String orgName) {
		this.context = ctx;
		this.usersSearchList = dataList;
		this.teamId = teamId;
		this.orgName = orgName;
	}

	class UserSearchViewHolder extends RecyclerView.ViewHolder {

		private User userInfo;

		private final ImageView userAvatar;
		private final TextView userFullName;
		private final TextView userName;
		private final ImageView addMemberButtonAdd;
		private final ImageView addMemberButtonRemove;

		private UserSearchViewHolder(View itemView) {

			super(itemView);
			userAvatar = itemView.findViewById(R.id.userAvatar);
			userFullName = itemView.findViewById(R.id.userFullName);
			userName = itemView.findViewById(R.id.userName);
			addMemberButtonAdd = itemView.findViewById(R.id.addCollaboratorButtonAdd);
			addMemberButtonRemove = itemView.findViewById(R.id.addCollaboratorButtonRemove);

			addMemberButtonAdd.setOnClickListener(v -> {
				AlertDialogs.addMemberDialog(context, userInfo.getLogin(),
						context.getResources().getString(R.string.addTeamMemberTitle),
						context.getResources().getString(R.string.addTeamMemberMessage),
						context.getResources().getString(R.string.addButton),
						context.getResources().getString(R.string.cancelButton), Integer.parseInt(String.valueOf(teamId)));
			});

			addMemberButtonRemove.setOnClickListener(v -> {
				AlertDialogs.removeMemberDialog(context, userInfo.getLogin(),
						context.getResources().getString(R.string.removeTeamMemberTitle),
						context.getResources().getString(R.string.removeTeamMemberMessage),
						context.getResources().getString(R.string.removeButton),
						context.getResources().getString(R.string.cancelButton), Integer.parseInt(String.valueOf(teamId)));
			});

			userAvatar.setOnClickListener(loginId -> {
				Intent intent = new Intent(context, ProfileActivity.class);
				intent.putExtra("username", userInfo.getLogin());
				context.startActivity(intent);
			});

			userAvatar.setOnLongClickListener(loginId -> {
				AppUtil.copyToClipboard(context, userInfo.getLogin(), context.getString(R.string.copyLoginIdToClipBoard, userInfo.getLogin()));
				return true;
			});
		}

	}

	@NonNull
	@Override
	public UserSearchForTeamMemberAdapter.UserSearchViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_collaborators_search, parent, false);
		return new UserSearchForTeamMemberAdapter.UserSearchViewHolder(v);
	}

	@Override
	public void onBindViewHolder(@NonNull final UserSearchForTeamMemberAdapter.UserSearchViewHolder holder, int position) {

		User currentItem = usersSearchList.get(position);
		holder.userInfo = currentItem;
		int imgRadius = AppUtil.getPixelsFromDensity(context, 3);

		if (!currentItem.getFullName().equals("")) {

			holder.userFullName.setText(Html.fromHtml(currentItem.getFullName()));
		}
		else {

			holder.userFullName.setText(context.getResources().getString(R.string.usernameWithAt, currentItem.getLogin()));
		}

		holder.userName.setText(context.getResources().getString(R.string.usernameWithAt, currentItem.getLogin()));

		if (!currentItem.getAvatarUrl().equals("")) {
			PicassoService.getInstance(context).get().load(currentItem.getAvatarUrl()).placeholder(R.drawable.loader_animated).transform(new RoundedTransformation(imgRadius, 0)).resize(120, 120).centerCrop().into(holder.userAvatar);
		}

		if(getItemCount() > 0) {

			final String loginUid = ((BaseActivity) context).getAccount().getAccount().getUserName();

			Call<User> call = RetrofitClient
					.getApiInterface(context)
					.orgListTeamMember((long) teamId, currentItem.getLogin());

			call.enqueue(new Callback<User>() {

				@Override
				public void onResponse(@NonNull Call<User> call, @NonNull Response<User> response) {

					if(response.code() == 200) {

						if(!currentItem.getLogin().equals(loginUid)) {
							holder.addMemberButtonRemove.setVisibility(View.VISIBLE);
						}
						else {
							holder.addMemberButtonRemove.setVisibility(View.GONE);
						}

					}
					else if(response.code() == 404) {

						if(!currentItem.getLogin().equals(loginUid)) {
							holder.addMemberButtonAdd.setVisibility(View.VISIBLE);
						}
						else {
							holder.addMemberButtonAdd.setVisibility(View.GONE);
						}

					}
					else {
						holder.addMemberButtonRemove.setVisibility(View.GONE);
						holder.addMemberButtonAdd.setVisibility(View.GONE);
					}

				}

				@Override
				public void onFailure(@NonNull Call<User> call, @NonNull Throwable t) {

					Toasty.error(context, context.getResources().getString(R.string.genericServerResponseError));
				}

			});

		}

	}

	@Override
	public int getItemCount() {
		return usersSearchList.size();
	}

}
