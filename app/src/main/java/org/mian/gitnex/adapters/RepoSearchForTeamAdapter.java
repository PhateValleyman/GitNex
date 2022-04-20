package org.mian.gitnex.adapters;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.amulyakhare.textdrawable.TextDrawable;
import com.amulyakhare.textdrawable.util.ColorGenerator;
import org.gitnex.tea4j.v2.models.Repository;
import org.mian.gitnex.R;
import org.mian.gitnex.clients.PicassoService;
import org.mian.gitnex.helpers.AlertDialogs;
import org.mian.gitnex.helpers.AppUtil;
import org.mian.gitnex.helpers.RoundedTransformation;
import java.util.List;

/**
 * Author M M Arif
 */

public class RepoSearchForTeamAdapter extends RecyclerView.Adapter<RepoSearchForTeamAdapter.UserSearchViewHolder> {

	private final List<Repository> repoSearchList;
	private final Context context;
	private final int teamId;
	private final String orgName;

	public RepoSearchForTeamAdapter(List<Repository> dataList, Context ctx, int teamId, String orgName) {
		this.context = ctx;
		this.repoSearchList = dataList;
		this.teamId = teamId;
		this.orgName = orgName;
	}

	class UserSearchViewHolder extends RecyclerView.ViewHolder {

		private Repository repoInfo;

		private final ImageView repoAvatar;
		private final TextView name;

		private UserSearchViewHolder(View itemView) {

			super(itemView);
			repoAvatar = itemView.findViewById(R.id.userAvatar);
			name = itemView.findViewById(R.id.userFullName);
			itemView.findViewById(R.id.userName).setVisibility(View.GONE);
			ImageView addRepoButtonAdd = itemView.findViewById(R.id.addCollaboratorButtonAdd);
			ImageView addRepoButtonRemove = itemView.findViewById(R.id.addCollaboratorButtonRemove);
			addRepoButtonAdd.setVisibility(View.VISIBLE);
			addRepoButtonRemove.setVisibility(View.GONE);

			addRepoButtonAdd.setOnClickListener(v -> AlertDialogs.addRepoDialog(context, orgName, repoInfo.getName(), Integer.parseInt(String.valueOf(teamId))));

			addRepoButtonRemove.setOnClickListener(v ->
				AlertDialogs.removeRepoDialog(context, orgName, repoInfo.getName(), Integer.parseInt(String.valueOf(teamId))));
		}

	}

	@NonNull
	@Override
	public RepoSearchForTeamAdapter.UserSearchViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_collaborators_search, parent, false);
		return new RepoSearchForTeamAdapter.UserSearchViewHolder(v);
	}

	@Override
	public void onBindViewHolder(@NonNull final RepoSearchForTeamAdapter.UserSearchViewHolder holder, int position) {

		Repository currentItem = repoSearchList.get(position);
		holder.repoInfo = currentItem;
		int imgRadius = AppUtil.getPixelsFromDensity(context, 3);

		holder.name.setText(currentItem.getName());

		TextDrawable drawable = TextDrawable.builder().beginConfig().useFont(Typeface.DEFAULT).fontSize(18).toUpperCase().width(28).height(28)
			.endConfig().buildRoundRect(String.valueOf(currentItem.getFullName().charAt(0)), ColorGenerator.Companion.getMATERIAL().getColor(currentItem.getName()), 3);

		if(currentItem.getAvatarUrl() != null && !currentItem.getAvatarUrl().equals("")) {
			PicassoService.getInstance(context).get().load(currentItem.getAvatarUrl()).placeholder(R.drawable.loader_animated)
				.transform(new RoundedTransformation(imgRadius, 0)).resize(120, 120).centerCrop().into(holder.repoAvatar);
		}
		else {
			holder.repoAvatar.setImageDrawable(drawable);
		}

	}

	@Override
	public int getItemCount() {
		return repoSearchList.size();
	}

}
