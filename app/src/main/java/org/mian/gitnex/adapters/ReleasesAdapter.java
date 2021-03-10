package org.mian.gitnex.adapters;

import android.content.Context;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.text.HtmlCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import org.gitnex.tea4j.models.Releases;
import org.mian.gitnex.R;
import org.mian.gitnex.clients.PicassoService;
import org.mian.gitnex.helpers.ClickListener;
import org.mian.gitnex.helpers.Markdown;
import org.mian.gitnex.helpers.RoundedTransformation;
import org.mian.gitnex.helpers.TimeHelper;
import org.mian.gitnex.helpers.TinyDB;
import java.util.List;
import java.util.Locale;

/**
 * Author M M Arif
 */

public class ReleasesAdapter extends RecyclerView.Adapter<ReleasesAdapter.ReleasesViewHolder> {

    private List<Releases> releasesList;
    private Context mCtx;

	static class ReleasesViewHolder extends RecyclerView.ViewHolder {

        private TextView releaseType;
        private TextView releaseName;
        private ImageView authorAvatar;
        private TextView authorName;
        private TextView releaseTag;
        private TextView releaseCommitSha;
        private TextView releaseDate;
        private TextView releaseBodyContent;
        private LinearLayout downloadFrame;
        private RelativeLayout downloads;
        private TextView releaseZipDownload;
	    private TextView releaseTarDownload;
	    private ImageView downloadDropdownIcon;
	    private RecyclerView downloadList;

        private ReleasesViewHolder(View itemView) {

            super(itemView);

	        releaseType = itemView.findViewById(R.id.releaseType);
	        releaseName = itemView.findViewById(R.id.releaseName);
	        authorAvatar = itemView.findViewById(R.id.authorAvatar);
	        authorName = itemView.findViewById(R.id.authorName);
	        releaseTag = itemView.findViewById(R.id.releaseTag);
	        releaseCommitSha = itemView.findViewById(R.id.releaseCommitSha);
	        releaseDate = itemView.findViewById(R.id.releaseDate);
	        releaseBodyContent = itemView.findViewById(R.id.releaseBodyContent);
	        downloadFrame = itemView.findViewById(R.id.downloadFrame);
	        downloads = itemView.findViewById(R.id.downloads);
	        releaseZipDownload = itemView.findViewById(R.id.releaseZipDownload);
	        releaseTarDownload = itemView.findViewById(R.id.releaseTarDownload);
	        downloadDropdownIcon = itemView.findViewById(R.id.downloadDropdownIcon);
	        downloadList = itemView.findViewById(R.id.downloadList);

	        downloadList.setHasFixedSize(true);
	        downloadList.setLayoutManager(new LinearLayoutManager(itemView.getContext()));

        }
    }

    public ReleasesAdapter(Context mCtx, List<Releases> releasesMain) {
        this.mCtx = mCtx;
        this.releasesList = releasesMain;
    }

    @NonNull
    @Override
    public ReleasesAdapter.ReleasesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_releases, parent, false);
        return new ReleasesAdapter.ReleasesViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ReleasesAdapter.ReleasesViewHolder holder, int position) {

        final TinyDB tinyDb = TinyDB.getInstance(mCtx);
	    final String locale = tinyDb.getString("locale");
	    final String timeFormat = tinyDb.getString("dateFormat");

        Releases currentItem = releasesList.get(position);

	    holder.releaseName.setText(currentItem.getName());

	    if(currentItem.isPrerelease()) {
		    holder.releaseType.setBackgroundResource(R.drawable.shape_pre_release);
		    holder.releaseType.setText(R.string.releaseTypePre);
	    }
	    else if(currentItem.isDraft()) {
		    holder.releaseType.setVisibility(View.GONE);
	    }
	    else {
		    holder.releaseType.setBackgroundResource(R.drawable.shape_stable_release);
		    holder.releaseType.setText(R.string.releaseTypeStable);
	    }

	    if(currentItem.getAuthor().getAvatar_url() != null) {
		    PicassoService.getInstance(mCtx).get().load(currentItem.getAuthor().getAvatar_url()).placeholder(R.drawable.loader_animated).transform(new RoundedTransformation(8, 0)).resize(120, 120).centerCrop().into(holder.authorAvatar);
	    }

	    holder.authorName.setText(mCtx.getResources().getString(R.string.releasePublishedBy, currentItem.getAuthor().getUsername()));

	    if(currentItem.getTag_name() != null) {
	    	holder.releaseTag.setText(currentItem.getTag_name());
	    }

	    if(currentItem.getPublished_at() != null) {
		    holder.releaseDate.setText(TimeHelper.formatTime(currentItem.getPublished_at(), new Locale(locale), timeFormat, mCtx));
	    }

	    if(timeFormat.equals("pretty")) {
		    holder.releaseDate.setOnClickListener(new ClickListener(TimeHelper.customDateFormatForToastDateFormat(currentItem.getPublished_at()), mCtx));
	    }

        if(!currentItem.getBody().equals("")) {
	        new Markdown(mCtx, currentItem.getBody(), holder.releaseBodyContent);
        }
        else {
	        holder.releaseBodyContent.setText(R.string.noReleaseBodyContent);
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
                HtmlCompat.fromHtml("<a href='" + currentItem.getZipball_url() + "'>" + mCtx.getResources().getString(R.string.zipArchiveDownloadReleasesTab) + "</a> ", HtmlCompat.FROM_HTML_MODE_LEGACY));
        holder.releaseZipDownload.setMovementMethod(LinkMovementMethod.getInstance());

        holder.releaseTarDownload.setText(
                HtmlCompat.fromHtml("<a href='" + currentItem.getTarball_url() + "'>" + mCtx.getResources().getString(R.string.tarArchiveDownloadReleasesTab) + "</a> ", HtmlCompat.FROM_HTML_MODE_LEGACY));
        holder.releaseTarDownload.setMovementMethod(LinkMovementMethod.getInstance());

	    ReleasesDownloadsAdapter adapter = new ReleasesDownloadsAdapter(currentItem.getAssets());
	    holder.downloadList.setAdapter(adapter);

    }

    @Override
    public int getItemCount() {
        return releasesList.size();
    }

}
