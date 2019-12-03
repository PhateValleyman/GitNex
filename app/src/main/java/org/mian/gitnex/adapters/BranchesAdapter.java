package org.mian.gitnex.adapters;

import android.content.Context;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import org.mian.gitnex.R;
import org.mian.gitnex.models.Branches;
import org.mian.gitnex.util.TinyDB;

import java.util.List;

/**
 * Author M M Arif
 */

public class BranchesAdapter extends RecyclerView.Adapter<BranchesAdapter.BranchesViewHolder> {

    private List<Branches> branchesList;
    private Context mCtx;

    public BranchesAdapter(Context mCtx, List<Branches> branchesMain) {
        this.mCtx = mCtx;
        this.branchesList = branchesMain;
    }

    @NonNull
    @Override
    public BranchesAdapter.BranchesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.branches_list, parent, false);
        return new BranchesAdapter.BranchesViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull BranchesAdapter.BranchesViewHolder holder, int position) {

        final TinyDB tinyDb = new TinyDB(mCtx);
        final String instanceUrl = tinyDb.getString("instanceUrl");

        Branches currentItem = branchesList.get(position);
        holder.branchNameTv.setText(currentItem.getName());

        if (currentItem.getCommit().getAuthor().getName() != null || !currentItem.getCommit().getAuthor().getName().equals("")) {
            holder.branchCommitAuthor.setText(mCtx.getResources().getString(R.string.commitAuthor, currentItem.getCommit().getAuthor().getName()));
        } else {
            holder.branchCommitAuthor.setText(mCtx.getResources().getString(R.string.commitAuthor, currentItem.getCommit().getAuthor().getUsername()));
        }

        holder.branchCommitHash.setText(
                Html.fromHtml("<a href='" + currentItem.getCommit().getUrl() + "'>" + mCtx.getResources().getString(R.string.commitLinkBranchesTab) + "</a> "));
        holder.branchCommitHash.setMovementMethod(LinkMovementMethod.getInstance());

    }

    @Override
    public int getItemCount() {
        return branchesList.size();
    }

    static class BranchesViewHolder extends RecyclerView.ViewHolder {

        private TextView branchNameTv;
        private TextView branchCommitAuthor;
        private TextView branchCommitHash;

        private BranchesViewHolder(View itemView) {
            super(itemView);

            branchNameTv = itemView.findViewById(R.id.branchName);
            branchCommitAuthor = itemView.findViewById(R.id.branchCommitAuthor);
            branchCommitHash = itemView.findViewById(R.id.branchCommitHash);

        }
    }

}


