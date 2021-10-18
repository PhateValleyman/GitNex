package org.mian.gitnex.adapters;

import android.content.Context;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.text.HtmlCompat;
import androidx.recyclerview.widget.RecyclerView;
import org.gitnex.tea4j.models.GitTag;
import org.mian.gitnex.R;
import org.mian.gitnex.helpers.Markdown;
import java.util.List;

/**
 * Author qwerty287
 */

public class TagsAdapter extends RecyclerView.Adapter<TagsAdapter.TagsViewHolder> {

    private final List<GitTag> tags;
    private final Context context;

	static class TagsViewHolder extends RecyclerView.ViewHolder {

        private final TextView tagName;
        private final TextView tagBody;
        private final LinearLayout downloadFrame;
        private final LinearLayout downloads;
        private final TextView releaseZipDownload;
	    private final TextView releaseTarDownload;
	    private final ImageView downloadDropdownIcon;

        private TagsViewHolder(View itemView) {

            super(itemView);

	        tagName = itemView.findViewById(R.id.tagName);
	        tagBody = itemView.findViewById(R.id.tagBodyContent);
	        downloadFrame = itemView.findViewById(R.id.downloadFrame);
	        downloads = itemView.findViewById(R.id.downloads);
	        releaseZipDownload = itemView.findViewById(R.id.releaseZipDownload);
	        releaseTarDownload = itemView.findViewById(R.id.releaseTarDownload);
	        downloadDropdownIcon = itemView.findViewById(R.id.downloadDropdownIcon);
        }
    }

    public TagsAdapter(Context ctx, List<GitTag> releasesMain) {
        this.context = ctx;
        this.tags = releasesMain;
    }

    @NonNull
    @Override
    public TagsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_tags, parent, false);
        return new TagsViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull TagsViewHolder holder, int position) {

        GitTag currentItem = tags.get(position);

	    holder.tagName.setText(currentItem.getName());

        if(!currentItem.getMessage().equals("")) {
	        Markdown.render(context, currentItem.getMessage(), holder.tagBody);
        }
        else {
	        holder.tagBody.setText(R.string.noReleaseBodyContent);
        }

	    holder.downloadFrame.setOnClickListener(v -> {

		    if(holder.downloads.getVisibility() == View.GONE) {

			    holder.downloadDropdownIcon.setImageResource(R.drawable.ic_chevron_down);
			    holder.downloads.setVisibility(View.VISIBLE);
		    }
		    else {

			    holder.downloadDropdownIcon.setImageResource(R.drawable.ic_chevron_right);
			    holder.downloads.setVisibility(View.GONE);
		    }

	    });

        holder.releaseZipDownload.setText(
                HtmlCompat.fromHtml("<a href='" + currentItem.getZipballUrl() + "'>" + context.getResources().getString(R.string.zipArchiveDownloadReleasesTab) + "</a> ", HtmlCompat.FROM_HTML_MODE_LEGACY));
        holder.releaseZipDownload.setMovementMethod(LinkMovementMethod.getInstance());

        holder.releaseTarDownload.setText(
                HtmlCompat.fromHtml("<a href='" + currentItem.getTarballUrl() + "'>" + context.getResources().getString(R.string.tarArchiveDownloadReleasesTab) + "</a> ", HtmlCompat.FROM_HTML_MODE_LEGACY));
        holder.releaseTarDownload.setMovementMethod(LinkMovementMethod.getInstance());
    }

    @Override
    public int getItemCount() {
        return tags.size();
    }

}
