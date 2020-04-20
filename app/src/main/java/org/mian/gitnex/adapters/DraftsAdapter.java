package org.mian.gitnex.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import org.mian.gitnex.R;
import org.mian.gitnex.database.models.DraftsWithRepositories;
import java.util.List;

/**
 * Author M M Arif
 */

public class DraftsAdapter extends RecyclerView.Adapter<DraftsAdapter.DraftsViewHolder> {

    private List<DraftsWithRepositories> draftsList;
    private Context mCtx;
    //private List<CommentsDraft> draftsListFull;

    static class DraftsViewHolder extends RecyclerView.ViewHolder {

        private TextView draftText;
        private TextView repoInfo;

        private DraftsViewHolder(View itemView) {

            super(itemView);
            draftText = itemView.findViewById(R.id.draftText);
            repoInfo = itemView.findViewById(R.id.repoInfo);

            //ImageView draftsDropdownMenu = itemView.findViewById(R.id.draftsDropdownMenu);

            /*draftText.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    Context context = v.getContext();
                    TinyDB tinyDb = new TinyDB(context);

                }
            });*/

        }
    }

    public DraftsAdapter(Context mCtx, List<DraftsWithRepositories> draftsListMain) {
        this.mCtx = mCtx;
        this.draftsList = draftsListMain;
        //draftsListFull = new ArrayList<>(draftsList);
    }

    @NonNull
    @Override
    public DraftsAdapter.DraftsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_drafts, parent, false);
        return new DraftsViewHolder(v);
    }

    @SuppressLint("DefaultLocale")
    @Override
    public void onBindViewHolder(@NonNull DraftsAdapter.DraftsViewHolder holder, int position) {

        DraftsWithRepositories currentItem = draftsList.get(position);

        holder.draftText.setText(currentItem.getDraftText());
        holder.repoInfo.setText(String.format("%s/%s %s%d", currentItem.getRepositoryOwner(), currentItem.getRepositoryName(), mCtx.getResources().getString(R.string.hash), currentItem.getIssueId()));

    }

    @Override
    public int getItemCount() {
        return draftsList.size();
    }

}
