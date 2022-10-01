package org.mian.gitnex.adapters;

import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.widget.ImageViewCompat;
import androidx.recyclerview.widget.RecyclerView;
import org.gitnex.tea4j.v2.models.CommitStatus;
import org.mian.gitnex.R;
import org.mian.gitnex.helpers.AppUtil;
import java.util.List;

/**
 * @author qwerty287
 */
public class CommitStatusesAdapter
		extends RecyclerView.Adapter<CommitStatusesAdapter.CronTasksViewHolder> {

	private final List<CommitStatus> statuses;

	static class CronTasksViewHolder extends RecyclerView.ViewHolder {

		private CommitStatus status;

		private final TextView name;
		private final TextView description;
		private final ImageView icon;

		private CronTasksViewHolder(View itemView) {

			super(itemView);

			icon = itemView.findViewById(R.id.statusIcon);
			name = itemView.findViewById(R.id.name);
			description = itemView.findViewById(R.id.description);

			itemView.setOnClickListener(
					taskInfo -> openUrl());
		}

		private void openUrl() {
			if (status.getTargetUrl() != null) {
				AppUtil.openUrlInBrowser(itemView.getContext(), status.getTargetUrl());
			}
		}
	}

	public CommitStatusesAdapter(List<CommitStatus> statuses) {
		this.statuses = statuses;
	}

	@NonNull @Override
	public CommitStatusesAdapter.CronTasksViewHolder onCreateViewHolder(
			@NonNull ViewGroup parent, int viewType) {

		View v =
				LayoutInflater.from(parent.getContext())
						.inflate(R.layout.list_commit_status, parent, false);
		return new CommitStatusesAdapter.CronTasksViewHolder(v);
	}

	@Override
	public void onBindViewHolder(
			@NonNull CommitStatusesAdapter.CronTasksViewHolder holder, int position) {

		CommitStatus currentItem = statuses.get(position);

		holder.status = currentItem;
		holder.name.setText(currentItem.getContext());
		holder.description.setText(currentItem.getDescription());
		switch (currentItem.getStatus()) {
			case "pending":
				holder.icon.setImageResource(R.drawable.ic_dot_fill);
				ImageViewCompat.setImageTintList(
					holder.icon,
					ColorStateList.valueOf(
						holder.name.getContext().getResources()
							.getColor(R.color.lightYellow, null)));
			case "success":
				holder.icon.setImageResource(R.drawable.ic_check);
				ImageViewCompat.setImageTintList(
					holder.icon,
					ColorStateList.valueOf(
						holder.name.getContext().getResources()
							.getColor(R.color.colorLightGreen, null)));

			case "error":
			case "failure":
				holder.icon.setImageResource(R.drawable.ic_close);
				ImageViewCompat.setImageTintList(
					holder.icon,
					ColorStateList.valueOf(
						holder.name.getContext().getResources()
							.getColor(R.color.iconIssuePrClosedColor, null)));
			case "warning":
				holder.icon.setImageResource(R.drawable.ic_warning);
				ImageViewCompat.setImageTintList(
					holder.icon,
					ColorStateList.valueOf(
						holder.name.getContext().getResources()
							.getColor(R.color.lightYellow, null)));
			default:
				holder.icon.setVisibility(View.GONE);
		}
	}

	@Override
	public int getItemCount() {
		return statuses.size();
	}
}
