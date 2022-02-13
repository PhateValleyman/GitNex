package org.mian.gitnex.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import org.gitnex.tea4j.models.OrgPermissions;
import org.gitnex.tea4j.models.Teams;
import org.mian.gitnex.R;
import org.mian.gitnex.activities.OrganizationTeamMembersActivity;
import java.util.ArrayList;
import java.util.List;

/**
 * Author M M Arif
 */

public class TeamsByOrgAdapter extends RecyclerView.Adapter<TeamsByOrgAdapter.OrgTeamsViewHolder> implements Filterable {

    private final List<Teams> teamList;
    private final Context context;
    private final List<Teams> teamListFull;
    private final OrgPermissions permissions;
    private final String orgName;

    static class OrgTeamsViewHolder extends RecyclerView.ViewHolder {

    	private Teams teams;

    	private OrgPermissions permissions;
        private final TextView teamTitle;
        private final TextView teamDescription;
        private final TextView teamPermission;
        private String orgName;

        private OrgTeamsViewHolder(View itemView) {

            super(itemView);
            teamTitle = itemView.findViewById(R.id.teamTitle);
            teamDescription = itemView.findViewById(R.id.teamDescription);
            teamPermission = itemView.findViewById(R.id.teamPermission);

            itemView.setOnClickListener(v -> {

                Context context = v.getContext();

                Intent intent = new Intent(context, OrganizationTeamMembersActivity.class);
                intent.putExtra("teamTitle", teams.getName());
                intent.putExtra("teamId", String.valueOf(teams.getId()));
                intent.putExtra("permissions", permissions);
                intent.putExtra("orgName", orgName);
                context.startActivity(intent);
            });

        }

    }

    public TeamsByOrgAdapter(Context ctx, List<Teams> teamListMain, OrgPermissions permissions, String orgName) {
        this.context = ctx;
        this.teamList = teamListMain;
        this.permissions = permissions;
        teamListFull = new ArrayList<>(teamList);
        this.orgName = orgName;
    }

    @NonNull
    @Override
    public TeamsByOrgAdapter.OrgTeamsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_teams_by_org, parent, false);
        return new TeamsByOrgAdapter.OrgTeamsViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull TeamsByOrgAdapter.OrgTeamsViewHolder holder, int position) {

        Teams currentItem = teamList.get(position);

        holder.teams = currentItem;
        holder.teamTitle.setText(currentItem.getName());
        holder.permissions = permissions;
        holder.orgName = orgName;

        if (!currentItem.getDescription().equals("")) {
            holder.teamDescription.setVisibility(View.VISIBLE);
            holder.teamDescription.setText(currentItem.getDescription());
        }
        else {
            holder.teamDescription.setVisibility(View.GONE);
            holder.teamDescription.setText("");
        }
        holder.teamPermission.setText(context.getResources().getString(R.string.teamPermission, currentItem.getPermission()));
    }

    @Override
    public int getItemCount() {
        return teamList.size();
    }

    @Override
    public Filter getFilter() {
        return orgTeamsFilter;
    }

    private final Filter orgTeamsFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            List<Teams> filteredList = new ArrayList<>();

            if (constraint == null || constraint.length() == 0) {
                filteredList.addAll(teamListFull);
            } else {
                String filterPattern = constraint.toString().toLowerCase().trim();

                for (Teams item : teamListFull) {
                    if (item.getName().toLowerCase().contains(filterPattern) || item.getDescription().toLowerCase().contains(filterPattern)) {
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
            teamList.clear();
            teamList.addAll((List<Teams>) results.values);
            notifyDataSetChanged();
        }
    };

}
