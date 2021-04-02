package org.mian.gitnex.adapters;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.amulyakhare.textdrawable.TextDrawable;
import com.amulyakhare.textdrawable.util.ColorGenerator;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import org.gitnex.tea4j.models.UserRepositories;
import org.gitnex.tea4j.models.WatchInfo;
import org.mian.gitnex.R;
import org.mian.gitnex.activities.OpenRepoInBrowserActivity;
import org.mian.gitnex.activities.RepoDetailActivity;
import org.mian.gitnex.activities.RepoForksActivity;
import org.mian.gitnex.activities.RepoStargazersActivity;
import org.mian.gitnex.activities.RepoWatchersActivity;
import org.mian.gitnex.clients.PicassoService;
import org.mian.gitnex.clients.RetrofitClient;
import org.mian.gitnex.database.api.BaseApi;
import org.mian.gitnex.database.api.RepositoriesApi;
import org.mian.gitnex.database.models.Repository;
import org.mian.gitnex.helpers.RoundedTransformation;
import org.mian.gitnex.helpers.TinyDB;
import org.mian.gitnex.helpers.Toasty;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import retrofit2.Call;
import retrofit2.Callback;

/**
 * Author M M Arif
 */

public class MyReposListAdapter extends RecyclerView.Adapter<MyReposListAdapter.MyReposViewHolder> implements Filterable {

	private List<UserRepositories> reposList;
	private Context mCtx;
	private List<UserRepositories> reposListFull;

	static class MyReposViewHolder extends RecyclerView.ViewHolder {

		private ImageView imageAvatar;
		private TextView repoName;
		private TextView repoDescription;
		private TextView repoFullName;
		private ImageView repoPrivatePublic;
		private TextView repoStars;
		private TextView repoForks;
		private TextView repoOpenIssuesCount;
		private TextView repoType;
		private CheckBox isRepoAdmin;
		private LinearLayout archiveRepo;
		private TextView repoBranch;
		private TextView htmlUrl;

		private MyReposViewHolder(View itemView) {

			super(itemView);
			repoName = itemView.findViewById(R.id.repoName);
			repoDescription = itemView.findViewById(R.id.repoDescription);
			imageAvatar = itemView.findViewById(R.id.imageAvatar);
			repoFullName = itemView.findViewById(R.id.repoFullName);
			repoPrivatePublic = itemView.findViewById(R.id.imageRepoType);
			repoStars = itemView.findViewById(R.id.repoStars);
			repoForks = itemView.findViewById(R.id.repoForks);
			repoOpenIssuesCount = itemView.findViewById(R.id.repoOpenIssuesCount);
			ImageView reposDropdownMenu = itemView.findViewById(R.id.reposDropdownMenu);
			repoType = itemView.findViewById(R.id.repoType);
			isRepoAdmin = itemView.findViewById(R.id.repoIsAdmin);
			archiveRepo = itemView.findViewById(R.id.archiveRepoFrame);
			repoBranch = itemView.findViewById(R.id.repoBranch);
			htmlUrl = itemView.findViewById(R.id.htmlUrl);

			itemView.setOnClickListener(v -> {

				Context context = v.getContext();

				Intent intent = new Intent(context, RepoDetailActivity.class);
				intent.putExtra("repoFullName", repoFullName.getText().toString());

				TinyDB tinyDb = TinyDB.getInstance(context);
				tinyDb.putString("repoFullName", repoFullName.getText().toString());
				tinyDb.putString("repoType", repoType.getText().toString());
				//tinyDb.putBoolean("resumeIssues", true);
				tinyDb.putBoolean("isRepoAdmin", isRepoAdmin.isChecked());
				tinyDb.putString("repoBranch", repoBranch.getText().toString());

				String[] parts = repoFullName.getText().toString().split("/");
				final String repoOwner = parts[0];
				final String repoName = parts[1];

				int currentActiveAccountId = tinyDb.getInt("currentActiveAccountId");
				RepositoriesApi repositoryData = BaseApi.getInstance(context, RepositoriesApi.class);

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

							}
							else {

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

			reposDropdownMenu.setOnClickListener(v -> {

				final Context context = v.getContext();

				@SuppressLint("InflateParams") View view = LayoutInflater.from(context).inflate(R.layout.bottom_sheet_repository_in_list, null);

				TextView repoOpenInBrowser = view.findViewById(R.id.repoOpenInBrowser);
				TextView repoStargazers = view.findViewById(R.id.repoStargazers);
				TextView repoWatchers = view.findViewById(R.id.repoWatchers);
				TextView repoForksList = view.findViewById(R.id.repoForksList);
				TextView repoCopyUrl = view.findViewById(R.id.repoCopyUrl);
				TextView bottomSheetHeader = view.findViewById(R.id.bottomSheetHeader);

				bottomSheetHeader.setText(String.format("%s / %s", repoFullName.getText().toString().split("/")[0], repoFullName.getText().toString().split("/")[1]));
				BottomSheetDialog dialog = new BottomSheetDialog(context);
				dialog.setContentView(view);
				dialog.show();

				repoCopyUrl.setOnClickListener(openInBrowser -> {

					ClipboardManager clipboard = (ClipboardManager) Objects.requireNonNull(context).getSystemService(Context.CLIPBOARD_SERVICE);
					ClipData clip = ClipData.newPlainText("repoUrl", htmlUrl.getText().toString());
					assert clipboard != null;
					clipboard.setPrimaryClip(clip);

					Toasty.info(context, context.getString(R.string.copyIssueUrlToastMsg));
					dialog.dismiss();
				});

				repoOpenInBrowser.setOnClickListener(openInBrowser -> {

					Intent intentOpenInBrowser = new Intent(context, OpenRepoInBrowserActivity.class);
					intentOpenInBrowser.putExtra("repoFullNameBrowser", repoFullName.getText());
					context.startActivity(intentOpenInBrowser);
					dialog.dismiss();

				});

				repoStargazers.setOnClickListener(stargazers -> {

					Intent intent = new Intent(context, RepoStargazersActivity.class);
					intent.putExtra("repoFullNameForStars", repoFullName.getText());
					context.startActivity(intent);
					dialog.dismiss();

				});

				repoWatchers.setOnClickListener(watchers -> {

					Intent intentW = new Intent(context, RepoWatchersActivity.class);
					intentW.putExtra("repoFullNameForWatchers", repoFullName.getText());
					context.startActivity(intentW);
					dialog.dismiss();

				});

				repoForksList.setOnClickListener(forks -> {

					Intent intentW = new Intent(context, RepoForksActivity.class);
					intentW.putExtra("repoFullNameForForks", repoFullName.getText());
					context.startActivity(intentW);
					dialog.dismiss();

				});

			});

		}

	}

	public MyReposListAdapter(Context mCtx, List<UserRepositories> reposListMain) {

		this.mCtx = mCtx;
		this.reposList = reposListMain;
		reposListFull = new ArrayList<>(reposList);
	}

	@NonNull
	@Override
	public MyReposListAdapter.MyReposViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

		View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_repositories, parent, false);
		return new MyReposListAdapter.MyReposViewHolder(v);
	}

	@Override
	public void onBindViewHolder(@NonNull MyReposListAdapter.MyReposViewHolder holder, int position) {

		UserRepositories currentItem = reposList.get(position);
		holder.repoDescription.setVisibility(View.GONE);
		holder.repoBranch.setText(currentItem.getDefault_branch());
		holder.htmlUrl.setText(currentItem.getHtml_url());

		ColorGenerator generator = ColorGenerator.MATERIAL;
		int color = generator.getColor(currentItem.getName());
		String firstCharacter = String.valueOf(currentItem.getName().charAt(0));

		TextDrawable drawable = TextDrawable.builder().beginConfig().useFont(Typeface.DEFAULT).fontSize(18).toUpperCase().width(28).height(28).endConfig().buildRoundRect(firstCharacter, color, 3);

		if(currentItem.getAvatar_url() != null) {
			if(!currentItem.getAvatar_url().equals("")) {
				PicassoService.getInstance(mCtx).get().load(currentItem.getAvatar_url()).placeholder(R.drawable.loader_animated).transform(new RoundedTransformation(8, 0)).resize(120, 120).centerCrop().into(holder.imageAvatar);
			}
			else {
				holder.imageAvatar.setImageDrawable(drawable);
			}
		}
		else {
			holder.imageAvatar.setImageDrawable(drawable);
		}

		holder.repoName.setText(currentItem.getName());
		if(!currentItem.getDescription().equals("")) {
			holder.repoDescription.setVisibility(View.VISIBLE);
			holder.repoDescription.setText(currentItem.getDescription());
		}
		holder.repoFullName.setText(currentItem.getFullName());
		if(currentItem.getPrivateFlag()) {
			holder.repoPrivatePublic.setVisibility(View.VISIBLE);
			holder.repoType.setText(R.string.strPrivate);
		}
		else {
			holder.repoPrivatePublic.setVisibility(View.GONE);
			holder.repoType.setText(R.string.strPublic);
		}
		holder.repoStars.setText(currentItem.getStars_count());
		holder.repoForks.setText(currentItem.getForks_count());
		holder.repoOpenIssuesCount.setText(currentItem.getOpen_issues_count());

		if(holder.isRepoAdmin == null) {
			holder.isRepoAdmin = new CheckBox(mCtx);
		}
		holder.isRepoAdmin.setChecked(currentItem.getPermissions().isAdmin());

		if(currentItem.isArchived()) {
			holder.archiveRepo.setVisibility(View.VISIBLE);
		}
		else {
			holder.archiveRepo.setVisibility(View.GONE);
		}

	}

	@Override
	public int getItemCount() {

		return reposList.size();
	}

	@Override
	public Filter getFilter() {

		return myReposFilter;
	}

	private Filter myReposFilter = new Filter() {

		@Override
		protected FilterResults performFiltering(CharSequence constraint) {

			List<UserRepositories> filteredList = new ArrayList<>();

			if(constraint == null || constraint.length() == 0) {
				filteredList.addAll(reposListFull);
			}
			else {
				String filterPattern = constraint.toString().toLowerCase().trim();

				for(UserRepositories item : reposListFull) {
					if(item.getFullName().toLowerCase().contains(filterPattern) || item.getDescription().toLowerCase().contains(filterPattern)) {
						filteredList.add(item);
					}
				}
			}

			FilterResults results = new FilterResults();
			results.values = filteredList;

			return results;
		}

		@Override
		protected void publishResults(CharSequence constraint, FilterResults results) {

			reposList.clear();
			reposList.addAll((List) results.values);
			notifyDataSetChanged();
		}
	};

}
