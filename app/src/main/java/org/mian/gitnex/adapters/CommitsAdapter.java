package org.mian.gitnex.adapters;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.vdurmont.emoji.EmojiParser;
import org.gitnex.tea4j.models.Commits;
import org.mian.gitnex.R;
import org.mian.gitnex.clients.PicassoService;
import org.mian.gitnex.helpers.AppUtil;
import org.mian.gitnex.helpers.RoundedTransformation;
import org.mian.gitnex.helpers.AppUtil;
import org.mian.gitnex.helpers.ClickListener;
import org.mian.gitnex.helpers.TimeHelper;
import org.mian.gitnex.helpers.TinyDB;
import java.util.List;
import java.util.Locale;

/**
 * Author M M Arif
 */

public class CommitsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final Context context;
    private final int TYPE_LOAD = 0;
    private List<Commits> commitsList;
    private CommitsAdapter.OnLoadMoreListener loadMoreListener;
    private boolean isLoading = false;
    private boolean isMoreDataAvailable = true;

    public CommitsAdapter(Context ctx, List<Commits> commitsListMain) {

        this.context = ctx;
        this.commitsList = commitsListMain;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        LayoutInflater inflater = LayoutInflater.from(context);

        if(viewType == TYPE_LOAD) {
            return new CommitsHolder(inflater.inflate(R.layout.list_commits, parent, false));
        } else {
            return new LoadHolder(inflater.inflate(R.layout.row_load, parent, false));
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

        if(position >= getItemCount() - 1 && isMoreDataAvailable && !isLoading && loadMoreListener != null) {
            isLoading = true;
            loadMoreListener.onLoadMore();
        }

        if(getItemViewType(position) == TYPE_LOAD) {
            ((CommitsHolder) holder).bindData(commitsList.get(position));
        }
    }

    @Override
    public int getItemViewType(int position) {

        if(commitsList.get(position).getSha() != null) {
            return TYPE_LOAD;
        } else {
            return 1;
        }
    }

    @Override
    public int getItemCount() {
        return commitsList.size();
    }

    class CommitsHolder extends RecyclerView.ViewHolder {

    	View rootView;

        TextView commitSubject;
        TextView commitBody;
        TextView commitCommitter;
        ImageView commitCommitterAvatar;
        TextView commitSha;

        CommitsHolder(View itemView) {

            super(itemView);

            rootView = itemView;

            commitSubject = itemView.findViewById(R.id.commitSubject);
            commitBody = itemView.findViewById(R.id.commitBody);
            commitCommitter = itemView.findViewById(R.id.commitCommitter);
            commitCommitterAvatar = itemView.findViewById(R.id.commitCommitterAvatar);
            commitSha = itemView.findViewById(R.id.commitSha);

        }


        void bindData(Commits commitsModel) {

            TinyDB tinyDb = TinyDB.getInstance(context);

            String[] commitMessageParts = commitsModel.getCommit().getMessage().split("(\r\n|\n)", 2);

            if(commitMessageParts.length > 1 && !commitMessageParts[1].trim().isEmpty()) {
	            commitBody.setVisibility(View.VISIBLE);
	            commitSubject.setText(EmojiParser.parseToUnicode(commitMessageParts[0].trim()));
	            commitBody.setText(EmojiParser.parseToUnicode(commitMessageParts[1].trim()));
            } else {
	            commitSubject.setText(EmojiParser.parseToUnicode(commitMessageParts[0].trim()));
	            commitBody.setVisibility(View.GONE);
            }

            commitCommitter.setText(
            	context.getString(R.string.commitCommittedByWhen,
		            commitsModel.getCommit().getCommitter().getName(),
		            TimeHelper.formatTime(commitsModel.getCommit().getCommitter().getDate(), new Locale(tinyDb.getString("locale")), "pretty", context)));

	        if(commitsModel.getCommitter().getAvatar_url() != null &&
		        !commitsModel.getCommitter().getAvatar_url().isEmpty()) {

		        int imgRadius = AppUtil.getPixelsFromDensity(context, 3);

		        PicassoService.getInstance(context).get()
			        .load(commitsModel.getCommitter().getAvatar_url())
			        .placeholder(R.drawable.loader_animated)
			        .transform(new RoundedTransformation(imgRadius, 0))
			        .resize(120, 120)
			        .centerCrop().into(commitCommitterAvatar);

	        } else {
		        commitCommitterAvatar.setImageDrawable(null);
	        }

	        commitSha.setText(commitsModel.getSha().substring(0, Math.min(commitsModel.getSha().length(), 10)));
            rootView.setOnClickListener(v -> AppUtil.openUrlInBrowser(context, commitsModel.getHtml_url()));

        }
    }

    static class LoadHolder extends RecyclerView.ViewHolder {
        LoadHolder(View itemView) {
            super(itemView);
        }
    }

    public void setMoreDataAvailable(boolean moreDataAvailable) {
        isMoreDataAvailable = moreDataAvailable;
    }

    public void notifyDataChanged() {
        notifyDataSetChanged();
        isLoading = false;
    }

    public interface OnLoadMoreListener {
        void onLoadMore();
    }

    public void setLoadMoreListener(CommitsAdapter.OnLoadMoreListener loadMoreListener) {
        this.loadMoreListener = loadMoreListener;
    }

    public void updateList(List<Commits> list) {
        commitsList = list;
        notifyDataSetChanged();
    }
}
