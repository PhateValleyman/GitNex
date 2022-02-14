package org.mian.gitnex.adapters;

import android.content.Context;
import android.content.Intent;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;
import org.gitnex.tea4j.models.Collaborators;
import org.gitnex.tea4j.models.UserInfo;
import org.mian.gitnex.R;
import org.mian.gitnex.actions.CollaboratorActions;
import org.mian.gitnex.activities.BaseActivity;
import org.mian.gitnex.activities.ProfileActivity;
import org.mian.gitnex.clients.PicassoService;
import org.mian.gitnex.clients.RetrofitClient;
import org.mian.gitnex.helpers.AlertDialogs;
import org.mian.gitnex.helpers.AppUtil;
import org.mian.gitnex.helpers.RoundedTransformation;
import org.mian.gitnex.helpers.contexts.RepositoryContext;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Author M M Arif
 */

public class CollaboratorSearchAdapter extends RecyclerView.Adapter<CollaboratorSearchAdapter.CollaboratorSearchViewHolder> {

    private final List<UserInfo> usersSearchList;
    private final Context context;
    private final RepositoryContext repository;

    public CollaboratorSearchAdapter(List<UserInfo> dataList, Context ctx, RepositoryContext repository) {
        this.context = ctx;
        this.usersSearchList = dataList;
        this.repository = repository;
    }

    class CollaboratorSearchViewHolder extends RecyclerView.ViewHolder {

	    private UserInfo userInfo;

        private final ImageView userAvatar;
        private final TextView userFullName;
        private final TextView userName;
        private final ImageView addCollaboratorButtonAdd;
        private final ImageView addCollaboratorButtonRemove;

        private final String[] permissionList = {"Read", "Write", "Admin"};
        final private int permissionSelectedChoice = 0;

        private CollaboratorSearchViewHolder(View itemView) {

            super(itemView);
            userAvatar = itemView.findViewById(R.id.userAvatar);
            userFullName = itemView.findViewById(R.id.userFullName);
            userName = itemView.findViewById(R.id.userName);
            addCollaboratorButtonAdd = itemView.findViewById(R.id.addCollaboratorButtonAdd);
            addCollaboratorButtonRemove = itemView.findViewById(R.id.addCollaboratorButtonRemove);

            addCollaboratorButtonAdd.setOnClickListener(v -> {
                AlertDialog.Builder pBuilder = new AlertDialog.Builder(context);

                pBuilder.setTitle(R.string.newTeamPermission);
                pBuilder.setSingleChoiceItems(permissionList, permissionSelectedChoice, null)
                        .setCancelable(false)
                        .setNegativeButton(R.string.cancelButton, null)
                        .setPositiveButton(R.string.addButton, (dialog, which) -> {

                            ListView lw = ((AlertDialog)dialog).getListView();
                            Object checkedItem = lw.getAdapter().getItem(lw.getCheckedItemPosition());

                            CollaboratorActions.addCollaborator(context,  String.valueOf(checkedItem).toLowerCase(), userInfo.getUsername(), repository);
                        });

                AlertDialog pDialog = pBuilder.create();
                pDialog.show();
            });

            addCollaboratorButtonRemove.setOnClickListener(v -> AlertDialogs.collaboratorRemoveDialog(context, userInfo.getUsername(), repository));

	        userAvatar.setOnClickListener(loginId -> {
		        Intent intent = new Intent(context, ProfileActivity.class);
		        intent.putExtra("username", userInfo.getLogin());
		        context.startActivity(intent);
	        });

	        userAvatar.setOnLongClickListener(loginId -> {
		        AppUtil.copyToClipboard(context, userInfo.getLogin(), context.getString(R.string.copyLoginIdToClipBoard, userInfo.getLogin()));
		        return true;
	        });
        }

    }

    @NonNull
    @Override
    public CollaboratorSearchViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_collaborators_search, parent, false);
        return new CollaboratorSearchAdapter.CollaboratorSearchViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull final CollaboratorSearchViewHolder holder, int position) {

        UserInfo currentItem = usersSearchList.get(position);
	    int imgRadius = AppUtil.getPixelsFromDensity(context, 3);
	    holder.userInfo = currentItem;

        if (!currentItem.getFullname().equals("")) {

            holder.userFullName.setText(Html.fromHtml(currentItem.getFullname()));
        }
        else {

            holder.userFullName.setText(context.getResources().getString(R.string.usernameWithAt, currentItem.getUsername()));
        }

	    holder.userName.setText(context.getResources().getString(R.string.usernameWithAt, currentItem.getUsername()));

	    if (!currentItem.getAvatar().equals("")) {
            PicassoService.getInstance(context).get().load(currentItem.getAvatar()).placeholder(R.drawable.loader_animated).transform(new RoundedTransformation(imgRadius, 0)).resize(120, 120).centerCrop().into(holder.userAvatar);
        }

        if(getItemCount() > 0) {

            final String loginUid = ((BaseActivity) context).getAccount().getAccount().getUserName();

            Call<Collaborators> call = RetrofitClient
                    .getApiInterface(context)
                    .checkRepoCollaborator(((BaseActivity) context).getAccount().getAuthorization(), repository.getOwner(), repository.getName(), currentItem.getUsername());

            call.enqueue(new Callback<Collaborators>() {

                @Override
                public void onResponse(@NonNull Call<Collaborators> call, @NonNull Response<Collaborators> response) {

                    if(response.code() == 204) {
                        if(!currentItem.getUsername().equals(loginUid) && !currentItem.getUsername().equals(repository.getOwner())) {
                            holder.addCollaboratorButtonRemove.setVisibility(View.VISIBLE);
                        }
                        else {
                            holder.addCollaboratorButtonRemove.setVisibility(View.GONE);
                        }
                    }
                    else if(response.code() == 404) {
                        if(!currentItem.getUsername().equals(loginUid) && !currentItem.getUsername().equals(repository.getOwner())) {
                            holder.addCollaboratorButtonAdd.setVisibility(View.VISIBLE);
                        }
                        else {
                            holder.addCollaboratorButtonAdd.setVisibility(View.GONE);
                        }
                    }
                    else {
                        holder.addCollaboratorButtonRemove.setVisibility(View.GONE);
                        holder.addCollaboratorButtonAdd.setVisibility(View.GONE);
                        Log.i("onResponse", String.valueOf(response.code()));
                    }

                }

                @Override
                public void onFailure(@NonNull Call<Collaborators> call, @NonNull Throwable t) {
                    Log.i("onFailure", t.toString());
                }

            });

        }

    }

    @Override
    public int getItemCount() {
        return usersSearchList.size();
    }
}