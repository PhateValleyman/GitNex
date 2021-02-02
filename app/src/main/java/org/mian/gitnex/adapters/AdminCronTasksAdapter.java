package org.mian.gitnex.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import org.mian.gitnex.R;
import org.mian.gitnex.models.CronTasks;
import java.util.List;

/**
 * Author M M Arif
 */

public class AdminCronTasksAdapter extends RecyclerView.Adapter<AdminCronTasksAdapter.CronTasksViewHolder> {

	private final List<CronTasks> tasksList;
	private final Context mCtx;

	static class CronTasksViewHolder extends RecyclerView.ViewHolder {

		private final ImageView runTask;
		private final TextView taskName;
		/*private final TextView schedule;
		private final TextView next;
		private final TextView prev;
		private final int execTimes;*/

		private CronTasksViewHolder(View itemView) {
			super(itemView);

			runTask = itemView.findViewById(R.id.runTask);
			taskName = itemView.findViewById(R.id.taskName);
			/*schedule = itemView.findViewById(R.id.schedule);
			next = itemView.findViewById(R.id.next);
			prev = itemView.findViewById(R.id.prev);
			execTimes = itemView.findViewById(R.id.execTimes);*/
		}
	}

	public AdminCronTasksAdapter(Context mCtx, List<CronTasks> tasksListMain) {

		this.mCtx = mCtx;
		this.tasksList = tasksListMain;
	}

	@NonNull
	@Override
	public AdminCronTasksAdapter.CronTasksViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

		View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_admin_cron_tasks, parent, false);
		return new AdminCronTasksAdapter.CronTasksViewHolder(v);
	}

	@Override
	public void onBindViewHolder(@NonNull AdminCronTasksAdapter.CronTasksViewHolder holder, int position) {

		CronTasks currentItem = tasksList.get(position);

		holder.taskName.setText(currentItem.getName());
	}

	@Override
	public int getItemCount() {
		return tasksList.size();
	}
}
