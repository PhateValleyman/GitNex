package org.mian.gitnex.fragments;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import org.gitnex.tea4j.models.NotificationThread;
import org.mian.gitnex.R;
import org.mian.gitnex.activities.BaseActivity;
import org.mian.gitnex.clients.RetrofitClient;
import org.mian.gitnex.databinding.BottomSheetNotificationsBinding;
import org.mian.gitnex.helpers.AppUtil;
import org.mian.gitnex.helpers.SimpleCallback;
import org.mian.gitnex.helpers.Toasty;

/**
 * Author opyale
 */

public class BottomSheetNotificationsFragment extends BottomSheetDialogFragment {

	private Context context;
	private NotificationThread notificationThread;
	private Runnable onOptionSelectedListener;

	public void onAttach(Context context, NotificationThread notificationThread, Runnable onOptionSelectedListener) {

		super.onAttach(context);

		this.context = context;
		this.notificationThread = notificationThread;
		this.onOptionSelectedListener = onOptionSelectedListener;

	}

	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

		BottomSheetNotificationsBinding bottomSheetNotificationsBinding = BottomSheetNotificationsBinding.inflate(inflater, container, false);

		TextView markRead = bottomSheetNotificationsBinding.markRead;
		TextView markUnread = bottomSheetNotificationsBinding.markUnread;
		TextView markPinned = bottomSheetNotificationsBinding.markPinned;

		if(notificationThread.isPinned()) {
			AppUtil.setMultiVisibility(View.GONE, markUnread, markPinned);
		} else if(notificationThread.isUnread()) {
			markUnread.setVisibility(View.GONE);
		} else {
			markRead.setVisibility(View.GONE);
		}

		markPinned.setOnClickListener(v12 ->
			RetrofitClient.getApiInterface(context)
				.markNotificationThreadAsRead(((BaseActivity) context).getAccount().getAuthorization(), notificationThread.getId(), "pinned")
				.enqueue((SimpleCallback<Void>) (call, voidResponse) -> {

					if(voidResponse.isPresent() && voidResponse.get().isSuccessful()) {
						onOptionSelectedListener.run();
					} else {
						Toasty.error(context, getString(R.string.genericError));
					}

					dismiss();
				}));

		markRead.setOnClickListener(v1 ->
			RetrofitClient.getApiInterface(context)
				.markNotificationThreadAsRead(((BaseActivity) context).getAccount().getAuthorization(), notificationThread.getId(), "read")
				.enqueue((SimpleCallback<Void>) (call, voidResponse) -> {

					if(voidResponse.isPresent() && voidResponse.get().isSuccessful()) {
						onOptionSelectedListener.run();
					} else {
						Toasty.error(context, getString(R.string.genericError));
					}

					dismiss();
				}));

		markUnread.setOnClickListener(v13 ->
			RetrofitClient.getApiInterface(context)
				.markNotificationThreadAsRead(((BaseActivity) context).getAccount().getAuthorization(), notificationThread.getId(), "unread")
				.enqueue((SimpleCallback<Void>) (call, voidResponse) -> {

					if(voidResponse.isPresent() && voidResponse.get().isSuccessful()) {
						onOptionSelectedListener.run();
					} else {
						Toasty.error(context, getString(R.string.genericError));
					}

					dismiss();
				}));

		return bottomSheetNotificationsBinding.getRoot();

	}

}
