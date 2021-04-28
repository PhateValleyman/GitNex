package org.mian.gitnex.adapters;

import android.content.Context;
import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.text.HtmlCompat;
import androidx.core.widget.ImageViewCompat;
import androidx.recyclerview.widget.RecyclerView;
import org.gitnex.tea4j.models.NotificationThread;
import org.mian.gitnex.R;
import org.mian.gitnex.database.api.BaseApi;
import org.mian.gitnex.database.api.RepositoriesApi;
import org.mian.gitnex.database.models.Repository;
import org.mian.gitnex.helpers.AppUtil;
import org.mian.gitnex.helpers.TinyDB;
import java.util.List;

/**
 * Author opyale
 */

public class NotificationsAdapter extends RecyclerView.Adapter<NotificationsAdapter.NotificationsViewHolder> {

	private final Context context;
	private final List<NotificationThread> notificationThreads;
	private final OnMoreClickedListener onMoreClickedListener;
	private final OnNotificationClickedListener onNotificationClickedListener;
	private final TinyDB tinyDb;

	public NotificationsAdapter(Context context, List<NotificationThread> notificationThreads, OnMoreClickedListener onMoreClickedListener, OnNotificationClickedListener onNotificationClickedListener) {

		this.tinyDb = TinyDB.getInstance(context);
		this.context = context;
		this.notificationThreads = notificationThreads;
		this.onMoreClickedListener = onMoreClickedListener;
		this.onNotificationClickedListener = onNotificationClickedListener;
	}

	static class NotificationsViewHolder extends RecyclerView.ViewHolder {

		private final LinearLayout frame;
		private final TextView subject;
		private final TextView repository;
		private final ImageView type;
		private final ImageView pinned;
		private final ImageView more;

		public NotificationsViewHolder(@NonNull View itemView) {

			super(itemView);

			frame = itemView.findViewById(R.id.frame);
			subject = itemView.findViewById(R.id.subject);
			repository = itemView.findViewById(R.id.repository);
			type = itemView.findViewById(R.id.type);
			pinned = itemView.findViewById(R.id.pinned);
			more = itemView.findViewById(R.id.more);
		}
	}

	@NonNull
	@Override
	public NotificationsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

		View v = LayoutInflater.from(context).inflate(R.layout.list_notifications, parent, false);
		return new NotificationsAdapter.NotificationsViewHolder(v);
	}

	@Override
	public void onBindViewHolder(@NonNull NotificationsViewHolder holder, int position) {

		NotificationThread notificationThread = notificationThreads.get(position);

		String url = notificationThread.getSubject().getUrl();
		String subjectId = "<font color='" + ResourcesCompat.getColor(context.getResources(), R.color.lightGray, null) + "'>" + context.getResources()
			.getString(R.string.hash) + url.substring(url.lastIndexOf("/") + 1) + "</font>";

		holder.subject.setText(HtmlCompat.fromHtml(subjectId + " " + notificationThread.getSubject().getTitle(), HtmlCompat.FROM_HTML_MODE_LEGACY));
		holder.repository.setText(notificationThread.getRepository().getFullName());

		if(notificationThread.isPinned()) {
			holder.pinned.setVisibility(View.VISIBLE);
		} else {
			holder.pinned.setVisibility(View.GONE);
		}

		switch(notificationThread.getSubject().getType().toLowerCase()) {

			case "pull":
				holder.type.setImageResource(R.drawable.ic_pull_request);
				break;
			case "issue":
				holder.type.setImageResource(R.drawable.ic_issue);
				break;
			case "commit":
				holder.type.setImageResource(R.drawable.ic_commit);
				break;
			case "repository":
				holder.type.setImageResource(R.drawable.ic_repo);
				break;

			default:
				holder.type.setImageResource(R.drawable.ic_question);
				break;

		}

		switch(notificationThread.getSubject().getState().toLowerCase()) {

			case "closed":
				ImageViewCompat.setImageTintList(holder.type, ColorStateList.valueOf(context.getResources().getColor(R.color.iconIssuePrClosedColor)));
				break;
			case "merged":
				ImageViewCompat.setImageTintList(holder.type, ColorStateList.valueOf(context.getResources().getColor(R.color.iconPrMergedColor)));
				break;

			default:
			case "open":
				ImageViewCompat.setImageTintList(holder.type, ColorStateList.valueOf(AppUtil.getColorFromAttribute(context, R.attr.iconsColor)));
				break;

		}

		holder.frame.setOnClickListener(v -> {

			onNotificationClickedListener.onNotificationClicked(notificationThread);

			String[] parts = notificationThread.getRepository().getFullName().split("/");
			String repoOwner = parts[0];
			String repoName = parts[1];

			int currentActiveAccountId = tinyDb.getInt("currentActiveAccountId");
			RepositoriesApi repositoryData = BaseApi.getInstance(context, RepositoriesApi.class);

			if(repositoryData.checkRepository(currentActiveAccountId, repoOwner, repoName) == 0) {
				long id = repositoryData.insertRepository(currentActiveAccountId, repoOwner, repoName);
				tinyDb.putLong("repositoryId", id);
			} else {
				Repository data = repositoryData.getRepository(currentActiveAccountId, repoOwner, repoName);
				tinyDb.putLong("repositoryId", data.getRepositoryId());
			}
		});

		holder.more.setOnClickListener(v -> onMoreClickedListener.onMoreClicked(notificationThread));

	}

	@Override
	public int getItemCount() {
		return notificationThreads.size();
	}

	public interface OnNotificationClickedListener {
		void onNotificationClicked(NotificationThread notificationThread);
	}

	public interface OnMoreClickedListener {
		void onMoreClicked(NotificationThread notificationThread);
	}

}
