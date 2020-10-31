package org.mian.gitnex.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.text.Html;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;
import org.mian.gitnex.R;
import org.mian.gitnex.database.api.DraftsApi;
import org.mian.gitnex.database.models.DraftWithRepository;
import org.mian.gitnex.fragments.BottomSheetReplyFragment;
import org.mian.gitnex.helpers.Markdown;
import org.mian.gitnex.helpers.TinyDB;
import org.mian.gitnex.helpers.Toasty;
import java.util.List;

/**
 * Author M M Arif
 */

public class DraftsAdapter extends RecyclerView.Adapter<DraftsAdapter.DraftsViewHolder> {

    private List<DraftWithRepository> draftsList;
    private final FragmentManager fragmentManager;
    private final Context mCtx;

    class DraftsViewHolder extends RecyclerView.ViewHolder {

    	private DraftWithRepository draftWithRepository;

        private final TextView draftText;
        private final TextView repoInfo;
	    private final ImageView editCommentStatus;

        private DraftsViewHolder(View itemView) {

            super(itemView);

            draftText = itemView.findViewById(R.id.draftText);
            repoInfo = itemView.findViewById(R.id.repoInfo);
            ImageView deleteDraft = itemView.findViewById(R.id.deleteDraft);
	        editCommentStatus = itemView.findViewById(R.id.editCommentStatus);

            deleteDraft.setOnClickListener(itemDelete -> {

                int getDraftId = draftWithRepository.getDraftId();
                deleteDraft(getAdapterPosition());
	            DraftsApi draftsApi = new DraftsApi(mCtx);
	            draftsApi.deleteSingleDraft(getDraftId);

            });

	        itemView.setOnClickListener(itemEdit -> {

		        Bundle bundle = new Bundle();

                bundle.putString("commentBody", draftWithRepository.getDraftText());
                bundle.putString("issueNumber", String.valueOf(draftWithRepository.getIssueId()));
                bundle.putString("repositoryId", String.valueOf(draftWithRepository.getRepositoryId()));
                bundle.putString("draftTitle", repoInfo.getText().toString());
		        bundle.putString("commentId", draftWithRepository.getCommentId());
		        bundle.putString("draftId", String.valueOf(draftWithRepository.getDraftId()));

                if(!draftWithRepository.getCommentId().isEmpty()) {
	                bundle.putString("commentAction", "edit");
                }

                TinyDB tinyDb = TinyDB.getInstance(mCtx);
                tinyDb.putString("issueNumber", String.valueOf(draftWithRepository.getIssueId()));
                tinyDb.putLong("repositoryId", draftWithRepository.getRepositoryId());
                //tinyDb.putString("issueType", issueType.getText().toString());

		        BottomSheetReplyFragment.newInstance(bundle).show(fragmentManager, "replyBottomSheet");

            });

        }

    }

    public DraftsAdapter(Context mCtx, FragmentManager fragmentManager, List<DraftWithRepository> draftsListMain) {
        this.mCtx = mCtx;
        this.fragmentManager = fragmentManager;
        this.draftsList = draftsListMain;
    }

    private void deleteDraft(int position) {

        draftsList.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, draftsList.size());
        Toasty.success(mCtx, mCtx.getResources().getString(R.string.draftsSingleDeleteSuccess));

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

        DraftWithRepository currentItem = draftsList.get(position);

	    String issueNumber = "<font color='" + mCtx.getResources().getColor(R.color.lightGray) + "'>" + mCtx.getResources().getString(R.string.hash) + currentItem.getIssueId() + "</font>";
	    Spanned headTitle = Html.fromHtml(issueNumber + " " + currentItem.getRepositoryOwner() + " / " + currentItem.getRepositoryName());

	    holder.repoInfo.setText(headTitle);
	    holder.draftWithRepository = currentItem;

	    new Markdown(mCtx, currentItem.getDraftText(), holder.draftText);

	    if(!currentItem.getCommentId().equalsIgnoreCase("new")) {
		    holder.editCommentStatus.setVisibility(View.VISIBLE);
	    }
	    else {
	    	holder.editCommentStatus.setVisibility(View.GONE);
	    }
    }

    @Override
    public int getItemCount() {
        return draftsList.size();
    }

	public void updateList(List<DraftWithRepository> list) {

		draftsList = list;
		notifyDataSetChanged();
	}
}
