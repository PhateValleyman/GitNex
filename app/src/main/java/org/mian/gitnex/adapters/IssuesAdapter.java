package org.mian.gitnex.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.text.HtmlCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.vdurmont.emoji.EmojiParser;
import org.gitnex.tea4j.models.Issues;
import org.mian.gitnex.R;
import org.mian.gitnex.activities.IssueDetailActivity;
import org.mian.gitnex.clients.PicassoService;
import org.mian.gitnex.helpers.AppUtil;
import org.mian.gitnex.helpers.ClickListener;
import org.mian.gitnex.helpers.RoundedTransformation;
import org.mian.gitnex.helpers.TimeHelper;
import org.mian.gitnex.helpers.TinyDB;
import org.ocpsoft.prettytime.PrettyTime;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

/**
 * Author M M Arif
 */

public class IssuesAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

	private final Context context;
	private final int TYPE_LOAD = 0;
	private List<Issues> issuesList;
	private OnLoadMoreListener loadMoreListener;
	private boolean isLoading = false, isMoreDataAvailable = true;

	public IssuesAdapter(Context context, List<Issues> issuesListMain) {

		this.context = context;
		this.issuesList = issuesListMain;

	}

	@NonNull
	@Override
	public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

		LayoutInflater inflater = LayoutInflater.from(context);

		if(viewType == TYPE_LOAD) {
			return new IssuesHolder(inflater.inflate(R.layout.list_issues, parent, false));
		}
		else {
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

			((IssuesHolder) holder).bindData(issuesList.get(position));

		}

	}

	@Override
	public int getItemViewType(int position) {

		if(issuesList.get(position).getTitle() != null) {
			return TYPE_LOAD;
		}
		else {
			return 1;
		}

	}

	@Override
	public int getItemCount() {

		return issuesList.size();

	}

	class IssuesHolder extends RecyclerView.ViewHolder {

		private String userLoginId;

		private final TextView issueNumber;
		private final ImageView issueAssigneeAvatar;
		private final TextView issueTitle;
		private final TextView issueCreatedTime;
		private final TextView issueCommentsCount;

		IssuesHolder(View itemView) {

			super(itemView);

			issueNumber = itemView.findViewById(R.id.issueNumber);
			issueAssigneeAvatar = itemView.findViewById(R.id.assigneeAvatar);
			issueTitle = itemView.findViewById(R.id.issueTitle);
			issueCommentsCount = itemView.findViewById(R.id.issueCommentsCount);
			LinearLayout frameCommentsCount = itemView.findViewById(R.id.frameCommentsCount);
			issueCreatedTime = itemView.findViewById(R.id.issueCreatedTime);

			issueTitle.setOnClickListener(title -> {

				Context context = title.getContext();

				Intent intent = new Intent(context, IssueDetailActivity.class);
				intent.putExtra("issueNumber", issueNumber.getText());

				TinyDB tinyDb = TinyDB.getInstance(context);
				tinyDb.putString("issueNumber", issueNumber.getText().toString());
				tinyDb.putString("issueType", "Issue");
				context.startActivity(intent);

			});

			frameCommentsCount.setOnClickListener(commentsCount -> {

				Context context = commentsCount.getContext();

				Intent intent = new Intent(context, IssueDetailActivity.class);
				intent.putExtra("issueNumber", issueNumber.getText());

				TinyDB tinyDb = TinyDB.getInstance(context);
				tinyDb.putString("issueNumber", issueNumber.getText().toString());
				tinyDb.putString("issueType", "Issue");
				context.startActivity(intent);

			});

			issueAssigneeAvatar.setOnClickListener(loginId -> {

				Context context = loginId.getContext();

				AppUtil.copyToClipboard(context, userLoginId, context.getString(R.string.copyLoginIdToClipBoard, userLoginId));
			});

		}

		@SuppressLint("SetTextI18n")
		void bindData(Issues issuesModel) {

			final TinyDB tinyDb = TinyDB.getInstance(context);
			final String locale = tinyDb.getString("locale");
			final String timeFormat = tinyDb.getString("dateFormat");

			userLoginId = issuesModel.getUser().getLogin();

			PicassoService.getInstance(context).get().load(issuesModel.getUser().getAvatar_url()).placeholder(R.drawable.loader_animated).transform(new RoundedTransformation(8, 0)).resize(120, 120).centerCrop().into(issueAssigneeAvatar);

			String issueNumber_ = "<font color='" + ResourcesCompat.getColor(context.getResources(), R.color.lightGray, null) + "'>" + context.getResources().getString(R.string.hash) + issuesModel.getNumber() + "</font>";
			issueTitle.setText(HtmlCompat.fromHtml(issueNumber_ + " " + EmojiParser.parseToUnicode(issuesModel.getTitle()), HtmlCompat.FROM_HTML_MODE_LEGACY));

			issueNumber.setText(String.valueOf(issuesModel.getNumber()));
			issueCommentsCount.setText(String.valueOf(issuesModel.getComments()));

			switch(timeFormat) {
				case "pretty": {
					PrettyTime prettyTime = new PrettyTime(new Locale(locale));
					String createdTime = prettyTime.format(issuesModel.getCreated_at());
					issueCreatedTime.setText(createdTime);
					issueCreatedTime.setOnClickListener(new ClickListener(TimeHelper.customDateFormatForToastDateFormat(issuesModel.getCreated_at()), context));
					break;
				}
				case "normal": {
					DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd '" + context.getResources().getString(R.string.timeAtText) + "' HH:mm", new Locale(locale));
					String createdTime = formatter.format(issuesModel.getCreated_at());
					issueCreatedTime.setText(createdTime);
					break;
				}
				case "normal1": {
					DateFormat formatter = new SimpleDateFormat("dd-MM-yyyy '" + context.getResources().getString(R.string.timeAtText) + "' HH:mm", new Locale(locale));
					String createdTime = formatter.format(issuesModel.getCreated_at());
					issueCreatedTime.setText(createdTime);
					break;
				}
			}

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

	public void setLoadMoreListener(OnLoadMoreListener loadMoreListener) {

		this.loadMoreListener = loadMoreListener;

	}

	public void updateList(List<Issues> list) {

		issuesList = list;
		notifyDataSetChanged();
	}

}
