package org.mian.gitnex.adapters;

import android.content.Context;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.amulyakhare.textdrawable.TextDrawable;
import org.gitnex.tea4j.models.UserInfo;
import org.mian.gitnex.R;
import org.mian.gitnex.clients.PicassoService;
import org.mian.gitnex.helpers.RoundedTransformation;
import java.util.ArrayList;
import java.util.List;

/**
 * Author M M Arif
 */

public class AdminGetUsersAdapter extends RecyclerView.Adapter<AdminGetUsersAdapter.UsersViewHolder> implements Filterable {

    private List<UserInfo> usersList;
    private Context mCtx;
    private List<UserInfo> usersListFull;

    static class UsersViewHolder extends RecyclerView.ViewHolder {

        private ImageView userAvatar;
        private TextView userFullName;
        private TextView userEmail;
        private ImageView userRole;
        private TextView userName;

        private UsersViewHolder(View itemView) {
            super(itemView);

            userAvatar = itemView.findViewById(R.id.userAvatar);
            userFullName = itemView.findViewById(R.id.userFullName);
            userName = itemView.findViewById(R.id.userName);
            userEmail = itemView.findViewById(R.id.userEmail);
            userRole = itemView.findViewById(R.id.userRole);

        }
    }

    public AdminGetUsersAdapter(Context mCtx, List<UserInfo> usersListMain) {
        this.mCtx = mCtx;
        this.usersList = usersListMain;
        usersListFull = new ArrayList<>(usersList);
    }

    @NonNull
    @Override
    public AdminGetUsersAdapter.UsersViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_admin_users, parent, false);
        return new AdminGetUsersAdapter.UsersViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull AdminGetUsersAdapter.UsersViewHolder holder, int position) {

        UserInfo currentItem = usersList.get(position);

        if(!currentItem.getFullname().equals("")) {

            holder.userFullName.setText(Html.fromHtml(currentItem.getFullname()));
            holder.userName.setText(mCtx.getResources().getString(R.string.usernameWithAt, currentItem.getUsername()));
        }
        else {

            holder.userFullName.setText(mCtx.getResources().getString(R.string.usernameWithAt, currentItem.getUsername()));
            holder.userName.setVisibility(View.GONE);
        }

        if(!currentItem.getEmail().equals("")) {

            holder.userEmail.setText(currentItem.getEmail());
        }
        else {

            holder.userEmail.setVisibility(View.GONE);
        }

        if(currentItem.getIs_admin()) {
            holder.userRole.setVisibility(View.VISIBLE);
            TextDrawable drawable = TextDrawable.builder()
                    .beginConfig()
                    .textColor(ResourcesCompat.getColor(mCtx.getResources(), R.color.colorWhite, null))
                    .fontSize(44)
                    .width(180)
                    .height(60)
                    .endConfig()
                    .buildRoundRect(mCtx.getResources().getString(R.string.userRoleAdmin).toLowerCase(), ResourcesCompat.getColor(mCtx.getResources(), R.color.releasePre, null), 8);
            holder.userRole.setImageDrawable(drawable);
        }
        else {
            holder.userRole.setVisibility(View.GONE);
        }

        PicassoService.getInstance(mCtx).get().load(currentItem.getAvatar()).placeholder(R.drawable.loader_animated).transform(new RoundedTransformation(8, 0)).resize(120, 120).centerCrop().into(holder.userAvatar);
    }

    @Override
    public int getItemCount() {
        return usersList.size();
    }

    @Override
    public Filter getFilter() {
        return usersFilter;
    }

    private Filter usersFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            List<UserInfo> filteredList = new ArrayList<>();

            if (constraint == null || constraint.length() == 0) {
                filteredList.addAll(usersListFull);
            } else {
                String filterPattern = constraint.toString().toLowerCase().trim();

                for (UserInfo item : usersListFull) {
                    if (item.getEmail().toLowerCase().contains(filterPattern) || item.getFullname().toLowerCase().contains(filterPattern) || item.getUsername().toLowerCase().contains(filterPattern)) {
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
            usersList.clear();
            usersList.addAll((List) results.values);
            notifyDataSetChanged();
        }
    };

}
