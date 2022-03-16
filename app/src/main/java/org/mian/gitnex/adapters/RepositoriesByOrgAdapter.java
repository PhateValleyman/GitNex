package org.mian.gitnex.adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
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
import org.ocpsoft.prettytime.PrettyTime;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Author M M Arif
 */

public class RepositoriesByOrgAdapter extends RecyclerView.Adapter<RepositoriesByOrgAdapter.OrgReposViewHolder> implements Filterable {

    private final List<org.gitnex.tea4j.v2.models.Repository> reposList;
    private final Context context;
    private final List<org.gitnex.tea4j.v2.models.Repository> reposListFull;

    static class OrgReposViewHolder extends RecyclerView.ViewHolder {

	    private org.gitnex.tea4j.v2.models.Repository userRepositories;

	    private final ImageView image;
	    private final TextView repoName;
	    private final TextView orgName;
	    private final TextView repoDescription;
	    private CheckBox isRepoAdmin;
	    private final TextView repoStars;
	    private final TextView repoLastUpdated;
	    private final View spacerView;

        private OrgReposViewHolder(View itemView) {

	        super(itemView);
	        repoName = itemView.findViewById(R.id.repoName);
	        orgName = itemView.findViewById(R.id.orgName);
	        repoDescription = itemView.findViewById(R.id.repoDescription);
	        isRepoAdmin = itemView.findViewById(R.id.repoIsAdmin);
	        image = itemView.findViewById(R.id.imageAvatar);
	        repoStars = itemView.findViewById(R.id.repoStars);
	        repoLastUpdated = itemView.findViewById(R.id.repoLastUpdated);
	        spacerView = itemView.findViewById(R.id.spacerView);

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

    }

    public RepositoriesByOrgAdapter(Context ctx, List<org.gitnex.tea4j.v2.models.Repository> reposListMain) {

        this.context = ctx;
        this.reposList = reposListMain;
        reposListFull = new ArrayList<>(reposList);
    }

    @NonNull
    @Override
    public RepositoriesByOrgAdapter.OrgReposViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_repositories, parent, false);
        return new RepositoriesByOrgAdapter.OrgReposViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull RepositoriesByOrgAdapter.OrgReposViewHolder holder, int position) {

	    TinyDB tinyDb = TinyDB.getInstance(context);
	    org.gitnex.tea4j.v2.models.Repository currentItem = reposList.get(position);
	    int imgRadius = AppUtil.getPixelsFromDensity(context, 3);

	    Locale locale = context.getResources().getConfiguration().locale;
	    String timeFormat = tinyDb.getString("dateFormat", "pretty");
	    holder.userRepositories = currentItem;
	    holder.orgName.setText(currentItem.getFullName().split("/")[0]);
	    holder.repoName.setText(currentItem.getFullName().split("/")[1]);
	    holder.repoStars.setText(String.valueOf(currentItem.getStarsCount()));

        ColorGenerator generator = ColorGenerator.MATERIAL;
        int color = generator.getColor(currentItem.getName());
        String firstCharacter = String.valueOf(currentItem.getFullName().charAt(0));

        TextDrawable drawable = TextDrawable.builder()
                .beginConfig()
                .useFont(Typeface.DEFAULT)
                .fontSize(18)
                .toUpperCase()
                .width(28)
                .height(28)
                .endConfig()
                .buildRoundRect(firstCharacter, color, 3);

        if (currentItem.getAvatarUrl() != null) {
            if (!currentItem.getAvatarUrl().equals("")) {
                PicassoService.getInstance(context).get().load(currentItem.getAvatarUrl()).placeholder(R.drawable.loader_animated).transform(new RoundedTransformation(imgRadius, 0)).resize(120, 120).centerCrop().into(holder.image);
            } else {
                holder.image.setImageDrawable(drawable);
            }
        }
        else {
            holder.image.setImageDrawable(drawable);
        }

	    if(currentItem.getUpdatedAt() != null) {

		    switch(timeFormat) {
			    case "pretty": {
				    PrettyTime prettyTime = new PrettyTime(locale);
				    String createdTime = prettyTime.format(currentItem.getUpdatedAt());
				    holder.repoLastUpdated.setText(context.getString(R.string.lastUpdatedAt, createdTime));
				    holder.repoLastUpdated.setOnClickListener(new ClickListener(TimeHelper.customDateFormatForToastDateFormat(currentItem.getUpdatedAt()), context));
				    break;
			    }
			    case "normal": {
				    DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd '" + context.getResources().getString(R.string.timeAtText) + "' HH:mm", locale);
				    String createdTime = formatter.format(currentItem.getUpdatedAt());
				    holder.repoLastUpdated.setText(context.getString(R.string.lastUpdatedAt, createdTime));
				    break;
			    }
			    case "normal1": {
				    DateFormat formatter = new SimpleDateFormat("dd-MM-yyyy '" + context.getResources().getString(R.string.timeAtText) + "' HH:mm", locale);
				    String createdTime = formatter.format(currentItem.getUpdatedAt());
				    holder.repoLastUpdated.setText(context.getString(R.string.lastUpdatedAt, createdTime));
				    break;
			    }
		    }
	    }
	    else {
		    holder.repoLastUpdated.setVisibility(View.GONE);
	    }

	    if(!currentItem.getDescription().equals("")) {
		    holder.repoDescription.setVisibility(View.VISIBLE);
		    holder.repoDescription.setText(currentItem.getDescription());
		    holder.spacerView.setVisibility(View.GONE);
	    }
	    else {
		    holder.repoDescription.setVisibility(View.GONE);
		    holder.spacerView.setVisibility(View.VISIBLE);
	    }

	    if(holder.isRepoAdmin == null) {
		    holder.isRepoAdmin = new CheckBox(context);
	    }
	    holder.isRepoAdmin.setChecked(currentItem.getPermissions().isAdmin());
    }

    @Override
    public int getItemCount() {
        return reposList.size();
    }

    @Override
    public Filter getFilter() {
        return orgReposFilter;
    }

    private final Filter orgReposFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            List<org.gitnex.tea4j.v2.models.Repository> filteredList = new ArrayList<>();

            if (constraint == null || constraint.length() == 0) {
                filteredList.addAll(reposListFull);
            } else {
                String filterPattern = constraint.toString().toLowerCase().trim();

                for (org.gitnex.tea4j.v2.models.Repository item : reposListFull) {
                    if (item.getFullName().toLowerCase().contains(filterPattern) || item.getDescription().toLowerCase().contains(filterPattern)) {
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
