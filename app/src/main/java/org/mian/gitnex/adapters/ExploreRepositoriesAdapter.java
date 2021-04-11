package org.mian.gitnex.adapters;

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
import org.mian.gitnex.database.api.RepositoriesApi;
import org.mian.gitnex.database.models.Repository;
import org.mian.gitnex.helpers.ClickListener;
import org.mian.gitnex.helpers.RoundedTransformation;
import org.mian.gitnex.helpers.TimeHelper;
import org.mian.gitnex.helpers.TinyDB;
import org.mian.gitnex.helpers.Toasty;
import org.ocpsoft.prettytime.PrettyTime;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;
import retrofit2.Call;
import retrofit2.Callback;

/**
 * Author M M Arif
 */

public class ExploreRepositoriesAdapter extends RecyclerView.Adapter<ExploreRepositoriesAdapter.ReposSearchViewHolder> {

	private final List<UserRepositories> reposList;
	private final Context mCtx;

	public ExploreRepositoriesAdapter(List<UserRepositories> dataList, Context mCtx) {

		this.mCtx = mCtx;
		this.reposList = dataList;
	}

	static class ReposSearchViewHolder extends RecyclerView.ViewHolder {

		private UserRepositories userRepositories;

		private final ImageView image;
		private final TextView repoName;
		private final TextView orgName;
		private final TextView repoDescription;
		private CheckBox isRepoAdmin;
		private final TextView repoStars;
		private final TextView repoLastUpdated;

		private ReposSearchViewHolder(View itemView) {

			super(itemView);
			repoName = itemView.findViewById(R.id.repoName);
			orgName = itemView.findViewById(R.id.orgName);
			repoDescription = itemView.findViewById(R.id.repoDescription);
			isRepoAdmin = itemView.findViewById(R.id.repoIsAdmin);
			image = itemView.findViewById(R.id.imageAvatar);
			repoStars = itemView.findViewById(R.id.repoStars);
			repoLastUpdated = itemView.findViewById(R.id.repoLastUpdated);

			itemView.setOnClickListener(v -> {

				Context context = v.getContext();
				TinyDB tinyDb = TinyDB.getInstance(context);

				Intent intent = new Intent(context, RepoDetailActivity.class);
				intent.putExtra("repoFullName", userRepositories.getFullName());

				tinyDb.putString("repoFullName", userRepositories.getFullName());
				tinyDb.putBoolean("resumeIssues", true);
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
				RepositoriesApi repositoryData = new RepositoriesApi(context);

				//RepositoriesRepository.deleteRepositoriesByAccount(currentActiveAccountId);
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

									Toasty.error(context, context.getString(R.string.genericApiStatusError));

								}

							}

						}

						@Override
						public void onFailure(@NonNull Call<WatchInfo> call, @NonNull Throwable t) {

							tinyDb.putBoolean("repoWatch", false);
							Toasty.error(context, context.getString(R.string.genericApiStatusError));
						}
					});

				}

				context.startActivity(intent);

			});
		}

	}

	@NonNull
	@Override
	public ExploreRepositoriesAdapter.ReposSearchViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

		View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_repositories, parent, false);
		return new ExploreRepositoriesAdapter.ReposSearchViewHolder(v);
	}

	@Override
	public void onBindViewHolder(@NonNull final ExploreRepositoriesAdapter.ReposSearchViewHolder holder, int position) {

		TinyDB tinyDb = TinyDB.getInstance(mCtx);
		UserRepositories currentItem = reposList.get(position);

		String locale = tinyDb.getString("locale");
		String timeFormat = tinyDb.getString("dateFormat");
		holder.userRepositories = currentItem;
		holder.orgName.setText(currentItem.getFullName().split("/")[0]);
		holder.repoName.setText(currentItem.getFullName().split("/")[1]);
		holder.repoStars.setText(currentItem.getStars_count());

		ColorGenerator generator = ColorGenerator.MATERIAL;
		int color = generator.getColor(currentItem.getName());
		String firstCharacter = String.valueOf(currentItem.getName().charAt(0));

		TextDrawable drawable = TextDrawable.builder().beginConfig().useFont(Typeface.DEFAULT).fontSize(18).toUpperCase().width(28).height(28).endConfig().buildRoundRect(firstCharacter, color, 3);

		if(currentItem.getAvatar_url() != null) {
			if(!currentItem.getAvatar_url().equals("")) {
				PicassoService.getInstance(mCtx).get().load(currentItem.getAvatar_url()).placeholder(R.drawable.loader_animated).transform(new RoundedTransformation(8, 0)).resize(120, 120).centerCrop().into(holder.image);
			}
			else {
				holder.image.setImageDrawable(drawable);
			}
		}
		else {
			holder.image.setImageDrawable(drawable);
		}

		if(currentItem.getUpdated_at() != null) {

			switch(timeFormat) {
				case "pretty": {
					PrettyTime prettyTime = new PrettyTime(new Locale(locale));
					String createdTime = prettyTime.format(currentItem.getUpdated_at());
					holder.repoLastUpdated.setText(mCtx.getString(R.string.lastUpdatedAt, createdTime));
					holder.repoLastUpdated.setOnClickListener(new ClickListener(TimeHelper.customDateFormatForToastDateFormat(currentItem.getUpdated_at()), mCtx));
					break;
				}
				case "normal": {
					DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd '" + mCtx.getResources().getString(R.string.timeAtText) + "' HH:mm", new Locale(locale));
					String createdTime = formatter.format(currentItem.getUpdated_at());
					holder.repoLastUpdated.setText(mCtx.getString(R.string.lastUpdatedAt, createdTime));
					break;
				}
				case "normal1": {
					DateFormat formatter = new SimpleDateFormat("dd-MM-yyyy '" + mCtx.getResources().getString(R.string.timeAtText) + "' HH:mm", new Locale(locale));
					String createdTime = formatter.format(currentItem.getUpdated_at());
					holder.repoLastUpdated.setText(mCtx.getString(R.string.lastUpdatedAt, createdTime));
					break;
				}
			}
		}
		else {
			holder.repoLastUpdated.setVisibility(View.GONE);
		}

		if(!currentItem.getDescription().equals("")) {
			holder.repoDescription.setText(currentItem.getDescription());
		}

		if(holder.isRepoAdmin == null) {
			holder.isRepoAdmin = new CheckBox(mCtx);
		}
		holder.isRepoAdmin.setChecked(currentItem.getPermissions().isAdmin());
	}

	@Override
	public int getItemCount() {

		return reposList.size();
	}

	public void notifyDataChanged() {

		notifyDataSetChanged();
	}

}
