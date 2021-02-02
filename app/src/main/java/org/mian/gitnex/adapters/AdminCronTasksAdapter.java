package org.mian.gitnex.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;
import org.apache.commons.lang3.StringUtils;
import org.mian.gitnex.R;
import org.mian.gitnex.helpers.TimeHelper;
import org.mian.gitnex.helpers.TinyDB;
import org.mian.gitnex.models.CronTasks;
import java.util.List;
import java.util.Locale;

/**
 * Author M M Arif
 */

public class AdminCronTasksAdapter extends RecyclerView.Adapter<AdminCronTasksAdapter.CronTasksViewHolder> {

	private final List<CronTasks> tasksList;
	private final Context mCtx;

	static class CronTasksViewHolder extends RecyclerView.ViewHolder {

		private CronTasks cronTasks;

		private final ImageView runTask;
		private final TextView taskName;
		private final LinearLayout cronTasksInfo;
		private final LinearLayout cronTasksRun;

		private CronTasksViewHolder(View itemView) {

			super(itemView);
			Context ctx = itemView.getContext();
			TinyDB tinyDb = TinyDB.getInstance(ctx);

			final String locale = tinyDb.getString("locale");
			final String timeFormat = tinyDb.getString("dateFormat");

			runTask = itemView.findViewById(R.id.runTask);
			taskName = itemView.findViewById(R.id.taskName);
			cronTasksInfo = itemView.findViewById(R.id.cronTasksInfo);
			cronTasksRun = itemView.findViewById(R.id.cronTasksRun);

			cronTasksInfo.setOnClickListener(taskInfo -> {

				String nextRun = "";
				String lastRun = "";

				if(cronTasks.getNext() != null) {
					nextRun = TimeHelper.formatTime(cronTasks.getNext(), new Locale(locale), timeFormat, ctx);
				}
				if(cronTasks.getPrev() != null) {
					lastRun = TimeHelper.formatTime(cronTasks.getPrev(), new Locale(locale), timeFormat, ctx);
				}

				View view = LayoutInflater.from(ctx).inflate(R.layout.layout_cron_task_info, null);

				TextView taskScheduleContent = view.findViewById(R.id.taskScheduleContent);
				TextView nextRunContent = view.findViewById(R.id.nextRunContent);
				TextView lastRunContent = view.findViewById(R.id.lastRunContent);
				TextView execTimeContent = view.findViewById(R.id.execTimeContent);

				taskScheduleContent.setText(cronTasks.getSchedule());
				nextRunContent.setText(nextRun);
				lastRunContent.setText(lastRun);
				execTimeContent.setText(String.valueOf(cronTasks.getExec_times()));

				AlertDialog.Builder alertDialog = new AlertDialog.Builder(ctx);

				alertDialog.setTitle(StringUtils.capitalize(cronTasks.getName().replace("_", " ")));
				alertDialog.setView(view);
				alertDialog.setPositiveButton(ctx.getString(R.string.close), null);
				alertDialog.create().show();

			});
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

		holder.cronTasks = currentItem;
		holder.taskName.setText(StringUtils.capitalize(currentItem.getName().replace("_", " ")));
	}

	@Override
	public int getItemCount() {
		return tasksList.size();
	}
}
