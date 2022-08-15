package org.mian.gitnex.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import org.gitnex.tea4j.v2.models.User;
import org.mian.gitnex.R;
import org.mian.gitnex.clients.PicassoService;
import org.mian.gitnex.helpers.AppUtil;
import org.mian.gitnex.helpers.RoundedTransformation;
import java.util.List;

/**
 * @author opyale
 */

public class ReactionAuthorsAdapter extends RecyclerView.Adapter<ReactionAuthorsAdapter.ViewHolder> {

	private final Context context;
	private final List<User> userInfos;

	public ReactionAuthorsAdapter(Context context, List<User> userInfos) {
		this.context = context;
		this.userInfos = userInfos;
	}

	@NonNull
	@Override
	public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		return new ViewHolder(LayoutInflater.from(context).inflate(R.layout.list_reaction_authors, parent, false));
	}

	@Override
	public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
		User userInfo = userInfos.get(position);

		PicassoService.getInstance(context).get().load(userInfo.getAvatarUrl()).placeholder(R.drawable.loader_animated).resize(240, 240).transform(new RoundedTransformation(AppUtil.getPixelsFromDensity(context, 6), 0))
			.centerCrop().into(holder.authorAvatar);

		if(userInfo.getFullName() == null || userInfo.getFullName().isEmpty()) {
			holder.authorFullName.setVisibility(View.GONE);
		}
		else {
			holder.authorFullName.setText(userInfo.getFullName());
			holder.authorFullName.setVisibility(View.VISIBLE);
		}

		holder.authorLogin.setText(userInfo.getLogin());
	}

	@Override
	public int getItemCount() {
		return userInfos.size();
	}

	static class ViewHolder extends RecyclerView.ViewHolder {

		private final ImageView authorAvatar;

		private final TextView authorFullName;
		private final TextView authorLogin;

		public ViewHolder(@NonNull View itemView) {
			super(itemView);

			authorAvatar = itemView.findViewById(R.id.authorAvatar);
			authorFullName = itemView.findViewById(R.id.authorFullName);
			authorLogin = itemView.findViewById(R.id.authorLogin);
		}

	}

}
