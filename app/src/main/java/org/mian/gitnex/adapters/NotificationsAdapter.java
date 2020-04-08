package org.mian.gitnex.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import org.mian.gitnex.R;
import org.mian.gitnex.models.NotificationThread;
import java.util.List;

public class NotificationsAdapter extends BaseAdapter {
	private Context context;
	private List<NotificationThread> notificationThreads;

	public NotificationsAdapter(Context context, List<NotificationThread> notificationThreads) {
		this.context = context;
		this.notificationThreads = notificationThreads;
	}

	@Override
	public int getCount() {
		return notificationThreads.size();
	}

	@Override
	public Object getItem(int position) {
		return notificationThreads.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (convertView == null) {
			convertView = LayoutInflater.from(context).inflate(R.layout.list_notifications, parent, false);
		}

		NotificationThread notificationThread = (NotificationThread) getItem(position);

		TextView subject = convertView.findViewById(R.id.subject);
		TextView repository = convertView.findViewById(R.id.repository);
		ImageView type = convertView.findViewById(R.id.type);
		ImageView pinned = convertView.findViewById(R.id.pinned);
		ImageButton more = convertView.findViewById(R.id.more);

		subject.setText(notificationThread.getSubject().getTitle());
		repository.setText(notificationThread.getRepository().getFullname());

		if(notificationThread.isPinned()) {
			pinned.setVisibility(View.VISIBLE);
		} else {
			pinned.setVisibility(View.GONE);
		}

		switch(notificationThread.getSubject().getType()) {
			case "Pull":
				type.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_merge, null));
				break;

			case "Issue":
				type.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_info_outline_24dp, null));
				break;
		}

		more.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// Open BottomSheetFragment
			}
		});

		return convertView;
	}

}
