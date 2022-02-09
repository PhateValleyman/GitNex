package org.mian.gitnex.fragments;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import org.mian.gitnex.R;
import org.mian.gitnex.actions.IssueActions;
import org.mian.gitnex.actions.PullRequestActions;
import org.mian.gitnex.activities.BaseActivity;
import org.mian.gitnex.activities.EditIssueActivity;
import org.mian.gitnex.activities.FileDiffActivity;
import org.mian.gitnex.activities.MergePullRequestActivity;
import org.mian.gitnex.databinding.BottomSheetSingleIssueBinding;
import org.mian.gitnex.helpers.AlertDialogs;
import org.mian.gitnex.helpers.TinyDB;
import org.mian.gitnex.helpers.Toasty;
import org.mian.gitnex.helpers.Version;
import org.mian.gitnex.helpers.contexts.IssueContext;
import org.mian.gitnex.structs.BottomSheetListener;
import org.mian.gitnex.views.ReactionSpinner;
import java.util.Objects;

/**
 * Author M M Arif
 */

public class BottomSheetSingleIssueFragment extends BottomSheetDialogFragment {

	private BottomSheetListener bmListener;
	private final IssueContext issue;

	public BottomSheetSingleIssueFragment(IssueContext issue) {
		this.issue = issue;
	}

	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

		BottomSheetSingleIssueBinding bottomSheetSingleIssueBinding = BottomSheetSingleIssueBinding.inflate(inflater, container, false);

		final Context ctx = getContext();
		final TinyDB tinyDB = TinyDB.getInstance(ctx);

		TextView editIssue = bottomSheetSingleIssueBinding.editIssue;
		TextView editLabels = bottomSheetSingleIssueBinding.editLabels;
		TextView closeIssue = bottomSheetSingleIssueBinding.closeIssue;
		TextView reOpenIssue = bottomSheetSingleIssueBinding.reOpenIssue;
		TextView addRemoveAssignees = bottomSheetSingleIssueBinding.addRemoveAssignees;
		TextView copyIssueUrl = bottomSheetSingleIssueBinding.copyIssueUrl;
		TextView openFilesDiff = bottomSheetSingleIssueBinding.openFilesDiff;
		TextView updatePullRequest = bottomSheetSingleIssueBinding.updatePullRequest;
		TextView mergePullRequest = bottomSheetSingleIssueBinding.mergePullRequest;
		TextView deletePullRequestBranch = bottomSheetSingleIssueBinding.deletePrHeadBranch;
		TextView shareIssue = bottomSheetSingleIssueBinding.shareIssue;
		TextView subscribeIssue = bottomSheetSingleIssueBinding.subscribeIssue;
		TextView unsubscribeIssue = bottomSheetSingleIssueBinding.unsubscribeIssue;

		LinearLayout linearLayout = bottomSheetSingleIssueBinding.commentReactionButtons;

		Bundle bundle1 = new Bundle();
		bundle1.putString("repoOwner", issue.getRepository().getOwner());
		bundle1.putString("repoName", issue.getRepository().getName());
		bundle1.putInt("issueId", issue.getIssueIndex());

		TextView loadReactions = new TextView(ctx);
		loadReactions.setText(Objects.requireNonNull(ctx).getString(R.string.genericWaitFor));
		loadReactions.setGravity(Gravity.CENTER);
		loadReactions.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 160));
		linearLayout.addView(loadReactions);

		ReactionSpinner reactionSpinner = new ReactionSpinner(ctx, bundle1);
		reactionSpinner.setOnInteractedListener(() -> {

			// TODO move to another way
			tinyDB.putBoolean("singleIssueUpdate", true);

			bmListener.onButtonClicked("onResume");
			dismiss();
		});

		Handler handler = new Handler();
		handler.postDelayed(() -> {
			linearLayout.removeView(loadReactions);
			reactionSpinner.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 160));
			linearLayout.addView(reactionSpinner);
		}, 2500);

		if(issue.getIssueType().equalsIgnoreCase("Pull")) {

			editIssue.setText(R.string.editPrText);
			copyIssueUrl.setText(R.string.copyPrUrlText);
			shareIssue.setText(R.string.sharePr);

			if(issue.getPullRequest().isMerged() || issue.getIssue().getState().equals("closed")) {
				updatePullRequest.setVisibility(View.GONE);
				mergePullRequest.setVisibility(View.GONE);
				deletePullRequestBranch.setVisibility(View.VISIBLE);
			}
			else {
				updatePullRequest.setVisibility(View.VISIBLE);
				mergePullRequest.setVisibility(View.VISIBLE);
				deletePullRequestBranch.setVisibility(View.GONE);
			}

			openFilesDiff.setVisibility(View.VISIBLE);
		} else {

			updatePullRequest.setVisibility(View.GONE);
			mergePullRequest.setVisibility(View.GONE);
			deletePullRequestBranch.setVisibility(View.GONE);
		}

		updatePullRequest.setOnClickListener(v -> {
			if(((BaseActivity) requireActivity()).getAccount().requiresVersion("1.16.0")) {
				AlertDialogs.selectPullUpdateStrategy(requireContext(), issue.getRepository().getOwner(), issue.getRepository().getName(),
					String.valueOf(issue.getIssueIndex()));
			} else {
				PullRequestActions.updatePr(requireContext(), issue.getRepository().getOwner(), issue.getRepository().getName(),
					String.valueOf(issue.getIssueIndex()), null);
			}
			dismiss();
		});

		mergePullRequest.setOnClickListener(v13 -> {

			startActivity(new Intent(ctx, MergePullRequestActivity.class));
			dismiss();
		});

		deletePullRequestBranch.setOnClickListener(v -> {

			PullRequestActions.deleteHeadBranch(ctx, issue.getRepository().getOwner(), issue.getRepository().getName(), issue.getPullRequest().getHead().getRef(), true);
			dismiss();
		});

		openFilesDiff.setOnClickListener(v14 -> {

			startActivity(new Intent(ctx, FileDiffActivity.class));
			dismiss();
		});

		editIssue.setOnClickListener(v15 -> {

			startActivity(new Intent(ctx, EditIssueActivity.class));
			dismiss();
		});

		editLabels.setOnClickListener(v16 -> {

			bmListener.onButtonClicked("showLabels");
			dismiss();
		});

		addRemoveAssignees.setOnClickListener(v17 -> {

			bmListener.onButtonClicked("showAssignees");
			dismiss();
		});

		shareIssue.setOnClickListener(v1 -> {

			Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
			sharingIntent.setType("text/plain");
			sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, getResources().getString(R.string.hash) + tinyDB.getString("issueNumber") + " " + tinyDB.getString("issueTitle"));
			sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, tinyDB.getString("singleIssueHtmlUrl"));
			startActivity(Intent.createChooser(sharingIntent, getResources().getString(R.string.hash) + tinyDB.getString("issueNumber") + " " + tinyDB.getString("issueTitle")));

			dismiss();
		});

		copyIssueUrl.setOnClickListener(v12 -> {

			// copy to clipboard
			ClipboardManager clipboard = (ClipboardManager) Objects.requireNonNull(ctx).getSystemService(Context.CLIPBOARD_SERVICE);
			ClipData clip = ClipData.newPlainText("issueUrl", tinyDB.getString("singleIssueHtmlUrl"));
			assert clipboard != null;
			clipboard.setPrimaryClip(clip);

			Toasty.info(ctx, ctx.getString(R.string.copyIssueUrlToastMsg));

			dismiss();
		});

		if(issue.getIssueType().equalsIgnoreCase("Issue")) {

			if(issue.getIssue().getState().equals("open")) { // close issue

				reOpenIssue.setVisibility(View.GONE);
				closeIssue.setVisibility(View.VISIBLE);

				closeIssue.setOnClickListener(closeSingleIssue -> {

					IssueActions.closeReopenIssue(ctx, issue.getIssueIndex(), "closed");
					dismiss();

				});

			}
			else if(issue.getIssue().getState().equals("closed")) {

				closeIssue.setVisibility(View.GONE);
				reOpenIssue.setVisibility(View.VISIBLE);

				reOpenIssue.setOnClickListener(reOpenSingleIssue -> {

					IssueActions.closeReopenIssue(ctx, Integer.parseInt(tinyDB.getString("issueNumber")), "open");
					dismiss();

				});

			}

		}
		else {

			reOpenIssue.setVisibility(View.GONE);
			closeIssue.setVisibility(View.GONE);

		}

		subscribeIssue.setOnClickListener(subscribeToIssue -> {

			IssueActions.subscribe(ctx);
			dismiss();
		});

		unsubscribeIssue.setOnClickListener(unsubscribeToIssue -> {

			IssueActions.unsubscribe(ctx);
			dismiss(); // TODO update values of issuectx
		});

		if(issue.isSubscribed()) {
			subscribeIssue.setVisibility(View.GONE);
			unsubscribeIssue.setVisibility(View.VISIBLE);
		}
		else {
			subscribeIssue.setVisibility(View.VISIBLE);
			unsubscribeIssue.setVisibility(View.GONE);
		}

		return bottomSheetSingleIssueBinding.getRoot();
	}

	@Override
	public void onAttach(@NonNull Context context) {

		super.onAttach(context);

		try {

			bmListener = (BottomSheetListener) context;
		}
		catch(ClassCastException e) {

			throw new ClassCastException(context + " must implement BottomSheetListener");
		}
	}
}
