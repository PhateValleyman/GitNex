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
import org.gitnex.tea4j.models.UserInfo;
import org.mian.gitnex.R;
import org.mian.gitnex.activities.ProfileActivity;
import org.mian.gitnex.clients.PicassoService;
import org.mian.gitnex.helpers.AppUtil;
import org.mian.gitnex.helpers.RoundedTransformation;
import java.util.List;

/**
 * Author M M Arif
 */

public class ProfileFollowingAdapter extends RecyclerView.Adapter<ProfileFollowingAdapter.FollowingViewHolder> {

    private final List<UserInfo> followingList;
    private final Context context;

    static class FollowingViewHolder extends RecyclerView.ViewHolder {

	    private String userLoginId;

        private final ImageView userAvatar;
        private final TextView userFullName;
        private final TextView userName;

        private FollowingViewHolder(View itemView) {

            super(itemView);

            userAvatar = itemView.findViewById(R.id.userAvatar);
            userFullName = itemView.findViewById(R.id.userFullName);
            userName = itemView.findViewById(R.id.userName);

	        userAvatar.setOnClickListener(loginId -> {

		        Context context = loginId.getContext();

		        Intent intent = new Intent(context, ProfileActivity.class);
		        intent.putExtra("username", userLoginId);
		        context.startActivity(intent);
		        //AppUtil.copyToClipboard(context, userLoginId, context.getString(R.string.copyLoginIdToClipBoard, userLoginId));
	        });
        }
    }

    public ProfileFollowingAdapter(Context ctx, List<UserInfo> followingListMain) {

        this.context = ctx;
        this.followingList = followingListMain;
    }

    @NonNull
    @Override
    public ProfileFollowingAdapter.FollowingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_profile_followers_following, parent, false);
        return new FollowingViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ProfileFollowingAdapter.FollowingViewHolder holder, int position) {

        UserInfo currentItem = followingList.get(position);
	    int imgRadius = AppUtil.getPixelsFromDensity(context, 3);

	    holder.userLoginId = currentItem.getLogin();

        if(!currentItem.getFullname().equals("")) {
            holder.userFullName.setText(Html.fromHtml(currentItem.getFullname()));
            holder.userName.setText(context.getResources().getString(R.string.usernameWithAt, currentItem.getUsername()));
        }
        else {
            holder.userFullName.setText(currentItem.getUsername());
            holder.userName.setVisibility(View.GONE);
        }

        PicassoService.getInstance(context).get().load(currentItem.getAvatar()).placeholder(R.drawable.loader_animated).transform(new RoundedTransformation(imgRadius, 0)).resize(120, 120).centerCrop().into(holder.userAvatar);
    }

    @Override
    public int getItemCount() {
        return followingList.size();
    }


}
