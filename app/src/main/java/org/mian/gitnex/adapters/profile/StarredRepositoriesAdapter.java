package org.mian.gitnex.adapters.profile;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.amulyakhare.textdrawable.TextDrawable;
import com.amulyakhare.textdrawable.util.ColorGenerator;
import org.mian.gitnex.R;
import org.mian.gitnex.activities.RepoDetailActivity;
import org.mian.gitnex.clients.PicassoService;
import org.mian.gitnex.database.api.BaseApi;
import org.mian.gitnex.database.api.RepositoriesApi;
import org.mian.gitnex.database.models.Repository;
import org.mian.gitnex.helpers.*;
import org.mian.gitnex.helpers.contexts.RepositoryContext;
import java.util.List;
import java.util.Locale;

/**
 * @author M M Arif
 */

public class StarredRepositoriesAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

	private final Context context;
	private List<org.gitnex.tea4j.v2.models.Repository> reposList;
	private Runnable loadMoreListener;
	private boolean isLoading = false, isMoreDataAvailable = true;

	public StarredRepositoriesAdapter(Context ctx, List<org.gitnex.tea4j.v2.models.Repository> reposListMain) {
		this.context = ctx;
		this.reposList = reposListMain;
	}

	@NonNull
	@Override
	public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

		LayoutInflater inflater = LayoutInflater.from(context);
		return new StarredRepositoriesAdapter.StarredRepositoriesHolder(inflater.inflate(R.layout.list_repositories, parent, false));
	}

	@Override
	public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

		if(position >= getItemCount() - 1 && isMoreDataAvailable && !isLoading && loadMoreListener != null) {
			isLoading = true;
			loadMoreListener.run();
		}
		((StarredRepositoriesAdapter.StarredRepositoriesHolder) holder).bindData(reposList.get(position));
	}

	@Override
	public int getItemViewType(int position) {
		return position;
	}

	@Override
	public int getItemCount() {
		return reposList.size();
	}

	class StarredRepositoriesHolder extends RecyclerView.ViewHolder {

		private org.gitnex.tea4j.v2.models.Repository userRepositories;

		private final ImageView avatar;
		private final TextView repoName;
		private final TextView orgName;
		private final TextView repoDescription;
		private CheckBox isRepoAdmin;
		private final TextView repoStars;
		private final TextView repoLastUpdated;

		StarredRepositoriesHolder(View itemView) {

			super(itemView);
			repoName = itemView.findViewById(R.id.repoName);
			orgName = itemView.findViewById(R.id.orgName);
			repoDescription = itemView.findViewById(R.id.repoDescription);
			isRepoAdmin = itemView.findViewById(R.id.repoIsAdmin);
			avatar = itemView.findViewById(R.id.imageAvatar);
			repoStars = itemView.findViewById(R.id.repoStars);
			repoLastUpdated = itemView.findViewById(R.id.repoLastUpdated);

			itemView.setOnClickListener(v -> {
				Context context = v.getContext();
				RepositoryContext repo = new RepositoryContext(userRepositories, context);
				Intent intent = repo.getIntent(context, RepoDetailActivity.class);

				int currentActiveAccountId = TinyDB.getInstance(context).getInt("currentActiveAccountId");
				RepositoriesApi repositoryData = BaseApi.getInstance(context, RepositoriesApi.class);

				assert repositoryData != null;
				Integer count = repositoryData.checkRepository(currentActiveAccountId, repo.getOwner(), repo.getName());

				if(count == 0) {
					long id = repositoryData.insertRepository(currentActiveAccountId, repo.getOwner(), repo.getName());
					repo.setRepositoryId((int) id);
				}
				else {
					Repository data = repositoryData.getRepository(currentActiveAccountId, repo.getOwner(), repo.getName());
					repo.setRepositoryId(data.getRepositoryId());
				}

				context.startActivity(intent);
			});
		}

		@SuppressLint("SetTextI18n")
		void bindData(org.gitnex.tea4j.v2.models.Repository userRepositories) {

			this.userRepositories = userRepositories;
			TinyDB tinyDb = TinyDB.getInstance(context);
			int imgRadius = AppUtil.getPixelsFromDensity(context, 3);

			Locale locale = context.getResources().getConfiguration().locale;
			String timeFormat = tinyDb.getString("dateFormat", "pretty");

			orgName.setText(userRepositories.getFullName().split("/")[0]);
			repoName.setText(userRepositories.getFullName().split("/")[1]);
			repoStars.setText(String.valueOf(userRepositories.getStarsCount()));

			ColorGenerator generator = ColorGenerator.MATERIAL;
			int color = generator.getColor(userRepositories.getName());
			String firstCharacter = String.valueOf(userRepositories.getFullName().charAt(0));

			TextDrawable drawable = TextDrawable.builder().beginConfig().useFont(Typeface.DEFAULT).fontSize(18).toUpperCase().width(28).height(28).endConfig().buildRoundRect(firstCharacter, color, 3);

			if(userRepositories.getAvatarUrl() != null) {
				if(!userRepositories.getAvatarUrl().equals("")) {
					PicassoService
						.getInstance(context).get().load(userRepositories.getAvatarUrl()).placeholder(R.drawable.loader_animated).transform(new RoundedTransformation(imgRadius, 0)).resize(120, 120).centerCrop().into(avatar);
				}
				else {
					avatar.setImageDrawable(drawable);
				}
			}
			else {
				avatar.setImageDrawable(drawable);
			}

			if(userRepositories.getUpdatedAt() != null) {

				repoLastUpdated.setText(context.getString(R.string.lastUpdatedAt, TimeHelper
					.formatTime(userRepositories.getUpdatedAt(), locale, timeFormat, context)));
				if(timeFormat.equals("pretty")) {
					repoLastUpdated.setOnClickListener(new ClickListener(TimeHelper.customDateFormatForToastDateFormat(userRepositories.getUpdatedAt()), context));
				}
			}
			else {
				repoLastUpdated.setVisibility(View.GONE);
			}

			if(!userRepositories.getDescription().equals("")) {
				repoDescription.setText(userRepositories.getDescription());
			}
			else {
				repoDescription.setText(context.getString(R.string.noDataDescription));
			}

			if(isRepoAdmin == null) {
				isRepoAdmin = new CheckBox(context);
			}
			isRepoAdmin.setChecked(userRepositories.getPermissions().isAdmin());

		}
	}

	public void setMoreDataAvailable(boolean moreDataAvailable) {
		isMoreDataAvailable = moreDataAvailable;
	}

	@SuppressLint("NotifyDataSetChanged")
	public void notifyDataChanged() {
		notifyDataSetChanged();
		isLoading = false;
	}

	public void setLoadMoreListener(Runnable loadMoreListener) {
		this.loadMoreListener = loadMoreListener;
	}

	public void updateList(List<org.gitnex.tea4j.v2.models.Repository> list) {
		reposList = list;
		notifyDataChanged();
	}
}
