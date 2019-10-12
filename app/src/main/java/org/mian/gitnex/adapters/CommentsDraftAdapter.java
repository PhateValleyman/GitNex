package org.mian.gitnex.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import org.mian.gitnex.R;
import org.mian.gitnex.database.models.CommentsDraft;
import java.util.List;

/**
 * Author M M Arif
 */

public class CommentsDraftAdapter extends RecyclerView.Adapter<CommentsDraftAdapter.CommentsViewHolder> {

    private List<CommentsDraft> draftsList;
    private Context mCtx;
    //private List<CommentsDraft> draftsListFull;

    class CommentsViewHolder extends RecyclerView.ViewHolder {

        private TextView draftText;
        private CommentsViewHolder(View itemView) {

            super(itemView);
            draftText = itemView.findViewById(R.id.draftText);

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

    public CommentsDraftAdapter(Context mCtx, List<CommentsDraft> draftsListMain) {
        this.mCtx = mCtx;
        this.draftsList = draftsListMain;
        //draftsListFull = new ArrayList<>(draftsList);
    }

    @NonNull
    @Override
    public CommentsDraftAdapter.CommentsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.comments_draft_list, parent, false);
        return new CommentsDraftAdapter.CommentsViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull CommentsDraftAdapter.CommentsViewHolder holder, int position) {

        CommentsDraft currentItem = draftsList.get(position);

        holder.draftText.setText(currentItem.getTitle());

    }

    @Override
    public int getItemCount() {
        return draftsList.size();
    }

}
