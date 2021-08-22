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
import org.gitnex.tea4j.models.UserRepositories;
import org.gitnex.tea4j.models.WatchInfo;
import org.mian.gitnex.R;
import org.mian.gitnex.activities.RepoDetailActivity;
import org.mian.gitnex.clients.PicassoService;
import org.mian.gitnex.clients.RetrofitClient;
import org.mian.gitnex.database.api.BaseApi;
import org.mian.gitnex.database.api.RepositoriesApi;
import org.mian.gitnex.database.models.Repository;
import org.mian.gitnex.helpers.AppUtil;
import org.mian.gitnex.helpers.ClickListener;
import org.mian.gitnex.helpers.RoundedTransformation;
import org.mian.gitnex.helpers.TimeHelper;
import org.mian.gitnex.helpers.TinyDB;
import org.mian.gitnex.helpers.Toasty;
import java.util.List;
import java.util.Locale;
import retrofit2.Call;
import retrofit2.Callback;

/**
 * Author M M Arif
 */

public class RepositoriesAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

	private final Context context;
	private final int TYPE_LOAD = 0;
	private List<UserRepositories> reposList;
	private OnLoadMoreListener loadMoreListener;
	private boolean isLoading = false, isMoreDataAvailable = true;

	public RepositoriesAdapter(Context ctx, List<UserRepositories> reposListMain) {
		this.context = ctx;
		this.reposList = reposListMain;
	}

	@NonNull
	@Override
	public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

		LayoutInflater inflater = LayoutInflater.from(context);

		if(viewType == TYPE_LOAD) {
			return new RepositoriesAdapter.RepositoriesHolder(inflater.inflate(R.layout.list_repositories, parent, false));
		}
		else {
			return new RepositoriesAdapter.LoadHolder(inflater.inflate(R.layout.row_load, parent, false));
		}
	}

	@Override
	public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

		if(position >= getItemCount() - 1 && isMoreDataAvailable && !isLoading && loadMoreListener != null) {
			isLoading = true;
			loadMoreListener.onLoadMore();
		}

		if(getItemViewType(position) == TYPE_LOAD) {
			((RepositoriesAdapter.RepositoriesHolder) holder).bindData(reposList.get(position));
		}
	}

	@Override
	public int getItemViewType(int position) {
		if(reposList.get(position).getFullName() != null) {
			return TYPE_LOAD;
		}
		else {
			return 1;
		}
	}

	@Override
	public int getItemCount() {
		return reposList.size();
	}

	class RepositoriesHolder extends RecyclerView.ViewHolder {

		private UserRepositories userRepositories;

		private final ImageView avatar;
		private final TextView repoName;
		private final TextView orgName;
		private final TextView repoDescription;
		private CheckBox isRepoAdmin;
		private final TextView repoStars;
		private final TextView repoLastUpdated;

		RepositoriesHolder(View itemView) {

			super(itemView);
			repoName = itemView.findViewById(R.id.repoName);
			orgName = itemView.findViewById(R.id.orgName);
			repoDescription = itemView.findViewById(R.id.repoDescription);
			isRepoAdmin = itemView.findViewById(R.id.repoIsAdmin);
			avatar = itemView.findViewById(R.id.imageAvatar);
			repoStars = itemView.findViewById(R.id.repoStars);
			repoLastUpdated = itemView.findViewById(R.id.repoLastUpdated);
		}

		@SuppressLint("SetTextI18n")
		void bindData(UserRepositories userRepositories) {

			this.userRepositories = userRepositories;
			TinyDB tinyDb = TinyDB.getInstance(context);
			int imgRadius = AppUtil.getPixelsFromDensity(context, 3);

			Locale locale = context.getResources().getConfiguration().locale;
			String timeFormat = tinyDb.getString("dateFormat");

			orgName.setText(userRepositories.getFullName().split("/")[0]);
			repoName.setText(userRepositories.getFullName().split("/")[1]);
			repoStars.setText(userRepositories.getStars_count());

			ColorGenerator generator = ColorGenerator.MATERIAL;
			int color = generator.getColor(userRepositories.getName());
			String firstCharacter = String.valueOf(userRepositories.getFullName().charAt(0));

			TextDrawable drawable = TextDrawable.builder().beginConfig().useFont(Typeface.DEFAULT).fontSize(18).toUpperCase().width(28).height(28).endConfig().buildRoundRect(firstCharacter, color, 3);

			if(userRepositories.getAvatar_url() != null) {
				if(!userRepositories.getAvatar_url().equals("")) {
					PicassoService
						.getInstance(context).get().load(userRepositories.getAvatar_url()).placeholder(R.drawable.loader_animated).transform(new RoundedTransformation(imgRadius, 0)).resize(120, 120).centerCrop().into(avatar);
				}
				else {
					avatar.setImageDrawable(drawable);
				}
			}
			else {
				avatar.setImageDrawable(drawable);
			}

			if(userRepositories.getUpdated_at() != null) {

				repoLastUpdated.setText(context.getString(R.string.lastUpdatedAt, TimeHelper.formatTime(userRepositories.getUpdated_at(), locale, timeFormat, context)));
				if(timeFormat.equals("pretty")) {
					repoLastUpdated.setOnClickListener(new ClickListener(TimeHelper.customDateFormatForToastDateFormat(userRepositories.getUpdated_at()), context));
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

			itemView.setOnClickListener(v -> {

				Context context = v.getContext();

				Intent intent = new Intent(context, RepoDetailActivity.class);
				intent.putExtra("repoFullName", userRepositories.getFullName());

				tinyDb.putString("repoFullName", userRepositories.getFullName());
				//tinyDb.putBoolean("resumeIssues", true);
				tinyDb.putBoolean("isRepoAdmin", isRepoAdmin.isChecked());
				tinyDb.putString("repoBranch", userRepositories.getDefault_branch());

				if(userRepositories.getPrivateFlag()) {
					tinyDb.putString("repoType", context.getResources().getString(R.string.strPrivate));
				}
				else {
					tinyDb.putString("repoType", context.getResources().getString(R.string.strPublic));
				}

				String[] parts = userRepositories.getFullName().split("/");
				final String repoOwner = parts[0];
				final String repoName = parts[1];

				int currentActiveAccountId = tinyDb.getInt("currentActiveAccountId");
				RepositoriesApi repositoryData = BaseApi.getInstance(context, RepositoriesApi.class);

				//RepositoriesRepository.deleteRepositoriesByAccount(currentActiveAccountId);
				assert repositoryData != null;
				Integer count = repositoryData.checkRepository(currentActiveAccountId, repoOwner, repoName);

				if(count == 0) {

					long id = repositoryData.insertRepository(currentActiveAccountId, repoOwner, repoName);
					tinyDb.putLong("repositoryId", id);
				}
				else {

					Repository data = repositoryData.getRepository(currentActiveAccountId, repoOwner, repoName);
					tinyDb.putLong("repositoryId", data.getRepositoryId());
				}

				//store if user is watching this repo
				{

					final String token = "token " + tinyDb.getString(tinyDb.getString("loginUid") + "-token");

					WatchInfo watch = new WatchInfo();

					Call<WatchInfo> call;

					call = RetrofitClient.getApiInterface(context).checkRepoWatchStatus(token, repoOwner, repoName);

					call.enqueue(new Callback<WatchInfo>() {

						@Override
						public void onResponse(@NonNull Call<WatchInfo> call, @NonNull retrofit2.Response<WatchInfo> response) {

							if(response.isSuccessful()) {

								assert response.body() != null;
								tinyDb.putBoolean("repoWatch", response.body().getSubscribed());

							} else {

								tinyDb.putBoolean("repoWatch", false);

								if(response.code() != 404) {

									Toasty.info(context, context.getString(R.string.genericApiStatusError));

								}

							}

						}

						@Override
						public void onFailure(@NonNull Call<WatchInfo> call, @NonNull Throwable t) {

							tinyDb.putBoolean("repoWatch", false);
							Toasty.info(context, context.getString(R.string.genericApiStatusError));

						}
					});

				}

				context.startActivity(intent);

			});
		}
	}

	static class LoadHolder extends RecyclerView.ViewHolder {
		LoadHolder(View itemView) {
			super(itemView);
		}
	}

	public void setMoreDataAvailable(boolean moreDataAvailable) {
		isMoreDataAvailable = moreDataAvailable;
	}

	public void notifyDataChanged() {
		notifyDataSetChanged();
		isLoading = false;
	}

	public interface OnLoadMoreListener {
		void onLoadMore();
	}

	public void setLoadMoreListener(OnLoadMoreListener loadMoreListener) {
		this.loadMoreListener = loadMoreListener;
	}

	public void updateList(List<UserRepositories> list) {
		reposList = list;
		notifyDataSetChanged();
	}
}
