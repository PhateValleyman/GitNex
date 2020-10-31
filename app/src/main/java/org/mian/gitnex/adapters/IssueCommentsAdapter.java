package org.mian.gitnex.adapters;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.gson.JsonElement;
import com.vdurmont.emoji.EmojiParser;
import org.mian.gitnex.R;
import org.mian.gitnex.clients.PicassoService;
import org.mian.gitnex.clients.RetrofitClient;
import org.mian.gitnex.fragments.BottomSheetReplyFragment;
import org.mian.gitnex.helpers.AlertDialogs;
import org.mian.gitnex.helpers.Markdown;
import org.mian.gitnex.helpers.RoundedTransformation;
import org.mian.gitnex.helpers.TimeHelper;
import org.mian.gitnex.helpers.TinyDB;
import org.mian.gitnex.helpers.Toasty;
import org.mian.gitnex.models.IssueComments;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import retrofit2.Call;
import retrofit2.Callback;

/**
 * Author M M Arif
 */

public class IssueCommentsAdapter extends RecyclerView.Adapter<IssueCommentsAdapter.IssueCommentViewHolder> {

	private final Context ctx;
	private final List<IssueComments> issuesComments;
	private final FragmentManager fragmentManager;
	private final BottomSheetReplyFragment.OnInteractedListener onInteractedListener;

	public IssueCommentsAdapter(Context ctx, List<IssueComments> issuesCommentsMain, FragmentManager fragmentManager, BottomSheetReplyFragment.OnInteractedListener onInteractedListener) {

		this.ctx = ctx;
		this.issuesComments = issuesCommentsMain;
		this.fragmentManager = fragmentManager;
		this.onInteractedListener = onInteractedListener;

	}

	class IssueCommentViewHolder extends RecyclerView.ViewHolder {

		private IssueComments issueComment;

		private final ImageView avatar;
		private final TextView author;
		private final TextView information;
		private final TextView comment;

		private IssueCommentViewHolder(View view) {

			super(view);

			avatar = view.findViewById(R.id.avatar);
			author = view.findViewById(R.id.author);
			information = view.findViewById(R.id.information);
			ImageView menu = view.findViewById(R.id.menu);
			comment = view.findViewById(R.id.comment);

			menu.setOnClickListener(v -> {

				final Context ctx = v.getContext();
				final TinyDB tinyDb = TinyDB.getInstance(ctx);
				final String loginUid = tinyDb.getString("loginUid");

				@SuppressLint("InflateParams") View vw = LayoutInflater.from(ctx).inflate(R.layout.bottom_sheet_issue_comments, null);

				TextView commentMenuEdit = vw.findViewById(R.id.commentMenuEdit);
				TextView commentShare = vw.findViewById(R.id.issueCommentShare);
				TextView commentMenuQuote = vw.findViewById(R.id.commentMenuQuote);
				TextView commentMenuCopy = vw.findViewById(R.id.commentMenuCopy);
				TextView commentMenuDelete = vw.findViewById(R.id.commentMenuDelete);
				TextView issueCommentCopyUrl = vw.findViewById(R.id.issueCommentCopyUrl);

				if(!loginUid.contentEquals(issueComment.getUser().getUsername())) {
					commentMenuEdit.setVisibility(View.GONE);
					commentMenuDelete.setVisibility(View.GONE);
				}

				if(issueComment.getBody().isEmpty()) {
					commentMenuCopy.setVisibility(View.GONE);
				}

				BottomSheetDialog dialog = new BottomSheetDialog(ctx);
				dialog.setContentView(vw);
				dialog.show();

				commentMenuEdit.setOnClickListener(v1 -> {

					Bundle bundle = new Bundle();
					bundle.putInt("commentId", issueComment.getId());
					bundle.putString("commentAction", "edit");
					bundle.putString("commentBody", issueComment.getBody());

					BottomSheetReplyFragment bottomSheetReplyFragment = BottomSheetReplyFragment.newInstance(bundle);
					bottomSheetReplyFragment.setOnInteractedListener(onInteractedListener);
					bottomSheetReplyFragment.show(fragmentManager, "replyBottomSheet");

					dialog.dismiss();

				});

				commentShare.setOnClickListener(v1 -> {

					// get comment Url
					CharSequence commentUrl = issueComment.getHtml_url();

					// share issue comment
					Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
					sharingIntent.setType("text/plain");
					String intentHeader = tinyDb.getString("issueNumber") + ctx.getResources().getString(R.string.hash) + "issuecomment-" + issueComment.getId() + " " + tinyDb.getString("issueTitle");
					sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, intentHeader);
					sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, commentUrl);
					ctx.startActivity(Intent.createChooser(sharingIntent, intentHeader));

					dialog.dismiss();

				});

				issueCommentCopyUrl.setOnClickListener(v1 -> {

					// comment Url
					CharSequence commentUrl = issueComment.getHtml_url();

					ClipboardManager clipboard = (ClipboardManager) Objects.requireNonNull(ctx).getSystemService(Context.CLIPBOARD_SERVICE);
					assert clipboard != null;

					ClipData clip = ClipData.newPlainText(commentUrl, commentUrl);
					clipboard.setPrimaryClip(clip);

					dialog.dismiss();
					Toasty.success(ctx, ctx.getString(R.string.copyIssueUrlToastMsg));

				});

				commentMenuQuote.setOnClickListener(v1 -> {

					StringBuilder stringBuilder = new StringBuilder();
					String commenterName = issueComment.getUser().getUsername();

					if(!commenterName.equals(tinyDb.getString("userLogin"))) {

						stringBuilder.append("@").append(commenterName).append("\n\n");
					}

					String[] lines = issueComment.getBody().split("\\R");

					for(String line : lines) {

						stringBuilder.append(">").append(line).append("\n");
					}

					stringBuilder.append("\n");

					Bundle bundle = new Bundle();
					bundle.putString("commentBody", stringBuilder.toString());
					bundle.putBoolean("cursorToEnd", true);

					dialog.dismiss();
					BottomSheetReplyFragment.newInstance(bundle).show(fragmentManager, "replyBottomSheet");

				});

				commentMenuCopy.setOnClickListener(v1 -> {

					ClipboardManager clipboard = (ClipboardManager) Objects.requireNonNull(ctx).getSystemService(Context.CLIPBOARD_SERVICE);
					assert clipboard != null;

					ClipData clip = ClipData.newPlainText("Comment on issue #" + tinyDb.getString("issueNumber"), issueComment.getBody());
					clipboard.setPrimaryClip(clip);

					dialog.dismiss();
					Toasty.success(ctx, ctx.getString(R.string.copyIssueCommentToastMsg));

				});

				commentMenuDelete.setOnClickListener(v1 -> {

					deleteIssueComment(ctx, issueComment.getId(), getAdapterPosition());
					dialog.dismiss();

				});

			});

		}

	}

	private void updateAdapter(int position) {

		issuesComments.remove(position);
		notifyItemRemoved(position);
		notifyItemRangeChanged(position, issuesComments.size());

	}

	private void deleteIssueComment(final Context ctx, final int commentId, int position) {

		final TinyDB tinyDb = TinyDB.getInstance(ctx);
		final String loginUid = tinyDb.getString("loginUid");
		final String instanceToken = "token " + tinyDb.getString(loginUid + "-token");
		String[] repoFullName = tinyDb.getString("repoFullName").split("/");

		if (repoFullName.length != 2) {
			return;
		}

		final String repoOwner = repoFullName[0];
		final String repoName = repoFullName[1];

		Call<JsonElement> call = RetrofitClient
				.getApiInterface(ctx)
				.deleteComment(instanceToken, repoOwner, repoName, commentId);

		call.enqueue(new Callback<JsonElement>() {

			@Override
			public void onResponse(@NonNull Call<JsonElement> call, @NonNull retrofit2.Response<JsonElement> response) {

				switch(response.code()) {

					case 204:
						updateAdapter(position);
						Toasty.success(ctx, ctx.getResources().getString(R.string.deleteCommentSuccess));
						break;

					case 401:
						AlertDialogs.authorizationTokenRevokedDialog(ctx, ctx.getResources().getString(R.string.alertDialogTokenRevokedTitle),
							ctx.getResources().getString(R.string.alertDialogTokenRevokedMessage),
							ctx.getResources().getString(R.string.alertDialogTokenRevokedCopyNegativeButton),
							ctx.getResources().getString(R.string.alertDialogTokenRevokedCopyPositiveButton));
						break;

					case 403:
						Toasty.error(ctx, ctx.getString(R.string.authorizeError));
						break;

					case 404:
						Toasty.warning(ctx, ctx.getString(R.string.apiNotFound));
						break;

					default:
						Toasty.error(ctx, ctx.getString(R.string.genericError));

				}
			}

			@Override
			public void onFailure(@NonNull Call<JsonElement> call, @NonNull Throwable t) {

				Toasty.error(ctx, ctx.getResources().getString(R.string.genericServerResponseError));
			}
		});
	}

	@NonNull
	@Override
	public IssueCommentsAdapter.IssueCommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

		View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_issue_comments, parent, false);
		return new IssueCommentsAdapter.IssueCommentViewHolder(v);

	}
	@Override
	public void onBindViewHolder(@NonNull IssueCommentsAdapter.IssueCommentViewHolder holder, int position) {

		IssueComments issueComment = issuesComments.get(position);

		holder.issueComment = issueComment;
		holder.author.setText(issueComment.getUser().getUsername());

		PicassoService.getInstance(ctx).get()
			.load(issueComment.getUser().getAvatar_url())
			.placeholder(R.drawable.loader_animated)
			.transform(new RoundedTransformation(4, 0))
			.resize(40, 40)
			.centerCrop()
			.into(holder.avatar);

		new Markdown(ctx, EmojiParser.parseToUnicode(issueComment.getBody()), holder.comment);

		StringBuilder informationBuilder = new StringBuilder(TimeHelper.formatTime(issueComment.getCreated_at(), Locale.getDefault(), "pretty", ctx));

		if(!issueComment.getCreated_at().equals(issueComment.getUpdated_at())) {

			informationBuilder.append(ctx.getString(R.string.colorfulBulletSpan))
				.append(ctx.getString(R.string.modifiedText));
		}

		holder.information.setText(informationBuilder.toString());

	}

	@Override
	public int getItemCount() {

		return issuesComments.size();
	}

}
