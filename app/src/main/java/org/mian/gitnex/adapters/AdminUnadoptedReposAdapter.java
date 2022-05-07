package org.mian.gitnex.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;
import org.mian.gitnex.R;
import org.mian.gitnex.clients.RetrofitClient;
import org.mian.gitnex.helpers.AlertDialogs;
import org.mian.gitnex.helpers.Toasty;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;

/**
 * @author M M Arif
 * @author qwerty287
 */

public class AdminUnadoptedReposAdapter extends RecyclerView.Adapter<AdminUnadoptedReposAdapter.UnadoptedViewHolder> {

	private List<String> repos;
	private final Runnable updateList;
	private final Runnable loadMoreListener;
	private boolean isLoading = false, hasMore = true;

	class UnadoptedViewHolder extends RecyclerView.ViewHolder {

		private String repoName;

		private final TextView name;

		private UnadoptedViewHolder(View itemView) {

			super(itemView);
			Context ctx = itemView.getContext();

			name = itemView.findViewById(R.id.repo_name);

			itemView.setOnClickListener(taskInfo -> new AlertDialog.Builder(ctx)
				.setTitle(repoName)
				.setNeutralButton(R.string.close, null)
				.setPositiveButton(R.string.menuDeleteText, ((dialog, which) -> delete(ctx, repoName)))
				.setNegativeButton(R.string.adoptRepo, ((dialog, which) -> adopt(ctx, repoName)))
				.show());
		}
	}

	public AdminUnadoptedReposAdapter(List<String> list, Runnable updateList, Runnable loadMore) {
		this.repos = list;
		this.updateList = updateList;
		this.loadMoreListener = loadMore;
	}

	@NonNull
	@Override
	public UnadoptedViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

		View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_admin_unadopted_repos, parent, false);
		return new UnadoptedViewHolder(v);
	}

	@Override
	public void onBindViewHolder(@NonNull UnadoptedViewHolder holder, int position) {
		if(position >= getItemCount() - 1 && hasMore && !isLoading && loadMoreListener != null) {
			isLoading = true;
			loadMoreListener.run();
		}

		String currentItem = repos.get(position);

		holder.repoName = currentItem;
		holder.name.setText(currentItem);
	}

	private void delete(final Context ctx, final String name) {

		String[] repoSplit = name.split("/");

		Call<Void> call = RetrofitClient
			.getApiInterface(ctx)
			.adminDeleteUnadoptedRepository(repoSplit[0], repoSplit[1]);

		call.enqueue(new Callback<>() {

			@Override
			public void onResponse(@NonNull Call<Void> call, @NonNull retrofit2.Response<Void> response) {

				switch(response.code()) {

					case 204:
						updateList.run();
						Toasty.success(ctx, ctx.getString(R.string.repoDeletionSuccess));
						break;

					case 401:
						AlertDialogs.authorizationTokenRevokedDialog(ctx);
						break;

					case 403:
						Toasty.error(ctx, ctx.getString(R.string.authorizeError));
						break;

					case 404:
						Toasty.warning(ctx, ctx.getString(R.string.apiNotFound));
						break;

					default:
						Toasty.error(ctx, ctx.getString(R.string.genericError));

				}
			}

			@Override
			public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {

				Toasty.error(ctx, ctx.getString(R.string.genericServerResponseError));
			}
		});
	}

	private void adopt(final Context ctx, final String name) {

		String[] repoSplit = name.split("/");

		Call<Void> call = RetrofitClient
			.getApiInterface(ctx)
			.adminAdoptRepository(repoSplit[0], repoSplit[1]);

		call.enqueue(new Callback<>() {

			@Override
			public void onResponse(@NonNull Call<Void> call, @NonNull retrofit2.Response<Void> response) {

				switch(response.code()) {

					case 204:
						updateList.run();
						Toasty.success(ctx, ctx.getString(R.string.repoAdopted, name));
						break;

					case 401:
						AlertDialogs.authorizationTokenRevokedDialog(ctx);
						break;

					case 403:
						Toasty.error(ctx, ctx.getString(R.string.authorizeError));
						break;

					case 404:
						Toasty.warning(ctx, ctx.getString(R.string.apiNotFound));
						break;

					default:
						Toasty.error(ctx, ctx.getString(R.string.genericError));

				}
			}

			@Override
			public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {

				Toasty.error(ctx, ctx.getString(R.string.genericServerResponseError));
			}
		});
	}

	@Override
	public int getItemCount() {
		return repos.size();
	}

	@SuppressLint("NotifyDataSetChanged")
	public void updateList(List<String> list) {
		this.repos = list;
		notifyDataSetChanged();
	}

	public void setHasMore(boolean hasMore) {
		this.hasMore = hasMore;
		isLoading = false;
	}
}
