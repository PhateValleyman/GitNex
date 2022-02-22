package org.mian.gitnex.fragments;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import org.mian.gitnex.R;
import org.mian.gitnex.actions.IssueActions;
import org.mian.gitnex.actions.PullRequestActions;
import org.mian.gitnex.activities.DiffActivity;
import org.mian.gitnex.activities.BaseActivity;
import org.mian.gitnex.activities.EditIssueActivity;
import org.mian.gitnex.activities.IssueDetailActivity;
import org.mian.gitnex.activities.MergePullRequestActivity;
import org.mian.gitnex.databinding.BottomSheetSingleIssueBinding;
import org.mian.gitnex.helpers.AlertDialogs;
import org.mian.gitnex.helpers.Toasty;
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
	private final String issueCreator;

	public BottomSheetSingleIssueFragment(IssueContext issue, String username) {
		this.issue = issue;
		issueCreator = username;
	}

	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

		BottomSheetSingleIssueBinding binding = BottomSheetSingleIssueBinding.inflate(inflater, container, false);

		final Context ctx = getContext();

		boolean userIsCreator = issueCreator.equals(((BaseActivity) requireActivity()).getAccount().getAccount().getUserName());
		boolean isRepoAdmin = issue.getRepository().getPermissions().isAdmin();
		boolean canPush = issue.getRepository().getPermissions().canPush();
		boolean archived = issue.getRepository().getRepository().isArchived();

		Bundle bundle = new Bundle();
		bundle.putString("repoOwner", issue.getRepository().getOwner());
		bundle.putString("repoName", issue.getRepository().getName());
		bundle.putInt("issueId", issue.getIssueIndex());

		TextView loadReactions = new TextView(ctx);
		loadReactions.setText(Objects.requireNonNull(ctx).getString(R.string.genericWaitFor));
		loadReactions.setGravity(Gravity.CENTER);
		loadReactions.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 80));
		binding.commentReactionButtons.addView(loadReactions);

		ReactionSpinner reactionSpinner = new ReactionSpinner(ctx, bundle);
		reactionSpinner.setOnInteractedListener(() -> {

			((IssueDetailActivity) requireActivity()).singleIssueUpdate = true;

			bmListener.onButtonClicked("onResume");
			dismiss();
		});
		reactionSpinner.setOnLoadingFinishedListener(() -> {
			reactionSpinner.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 160));
			binding.commentReactionButtons.removeView(loadReactions);
			binding.commentReactionButtons.addView(reactionSpinner);
		});

		if(issue.getIssueType().equalsIgnoreCase("Pull")) {

			binding.editIssue.setText(R.string.editPrText);
			binding.copyIssueUrl.setText(R.string.copyPrUrlText);
			binding.shareIssue.setText(R.string.sharePr);

			boolean canPushPullSource = issue.getPullRequest().getHead().getRepo().getPermissions().isPush();
			if(issue.getPullRequest().isMerged() || issue.getIssue().getState().equals("closed")) {
				binding.updatePullRequest.setVisibility(View.GONE);
				binding.mergePullRequest.setVisibility(View.GONE);
				if(canPushPullSource) {
					binding.deletePrHeadBranch.setVisibility(View.VISIBLE);
				}
				else {
					if(!canPush) {
						binding.editIssue.setVisibility(View.GONE);
					}
					binding.deletePrHeadBranch.setVisibility(View.GONE);
				}
			}
			else {
				if(canPushPullSource) {
					binding.updatePullRequest.setVisibility(View.VISIBLE);
				}
				else {
					binding.updatePullRequest.setVisibility(View.GONE);
				}
				if(!userIsCreator && !canPush) {
					binding.editIssue.setVisibility(View.GONE);
				}
				if(canPush && !issue.getPullRequest().isMergeable()) {
					binding.mergePullRequest.setVisibility(View.VISIBLE);
				}
				else {
					binding.mergePullRequest.setVisibility(View.GONE);
				}
				binding.deletePrHeadBranch.setVisibility(View.GONE);
			}

			binding.openFilesDiff.setVisibility(View.VISIBLE);
		} else {
			if(!userIsCreator && !canPush) {
				binding.editIssue.setVisibility(View.GONE);
			}
			binding.updatePullRequest.setVisibility(View.GONE);
			binding.mergePullRequest.setVisibility(View.GONE);
			binding.deletePrHeadBranch.setVisibility(View.GONE);
		}

		binding.updatePullRequest.setOnClickListener(v -> {
			if(((BaseActivity) requireActivity()).getAccount().requiresVersion("1.16.0")) {
				AlertDialogs.selectPullUpdateStrategy(requireContext(), issue.getRepository().getOwner(), issue.getRepository().getName(),
					String.valueOf(issue.getIssueIndex()));
			} else {
				PullRequestActions.updatePr(requireContext(), issue.getRepository().getOwner(), issue.getRepository().getName(),
					String.valueOf(issue.getIssueIndex()), null);
			}
			dismiss();
		});

		binding.mergePullRequest.setOnClickListener(v13 -> {

			startActivity(issue.getIntent(ctx, MergePullRequestActivity.class));
			dismiss();
		});

		binding.deletePrHeadBranch.setOnClickListener(v -> {

			PullRequestActions.deleteHeadBranch(ctx, issue.getRepository().getOwner(), issue.getRepository().getName(), issue.getPullRequest().getHead().getRef(), true);
			dismiss();
		});

		binding.openFilesDiff.setOnClickListener(v14 -> {
			startActivity(issue.getIntent(ctx, DiffActivity.class));
			dismiss();
		});

		binding.editIssue.setOnClickListener(v15 -> {
			((IssueDetailActivity) requireActivity()).editIssueLauncher.launch(issue.getIntent(ctx, EditIssueActivity.class));
			dismiss();
		});

		binding.editLabels.setOnClickListener(v16 -> {
			bmListener.onButtonClicked("showLabels");
			dismiss();
		});

		binding.addRemoveAssignees.setOnClickListener(v17 -> {
			bmListener.onButtonClicked("showAssignees");
			dismiss();
		});

		binding.shareIssue.setOnClickListener(v1 -> {

			Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
			sharingIntent.setType("text/plain");
			sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, getResources().getString(R.string.hash) + issue.getIssueIndex() + " " + issue.getIssue().getTitle());
			sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, issue.getIssue().getHtml_url());
			startActivity(Intent.createChooser(sharingIntent, getResources().getString(R.string.hash) + issue.getIssueIndex() + " " + issue.getIssue().getTitle()));

			dismiss();
		});

		binding.copyIssueUrl.setOnClickListener(v12 -> {

			// copy to clipboard
			ClipboardManager clipboard = (ClipboardManager) Objects.requireNonNull(ctx).getSystemService(Context.CLIPBOARD_SERVICE);
			ClipData clip = ClipData.newPlainText("issueUrl", issue.getIssue().getHtml_url());
			assert clipboard != null;
			clipboard.setPrimaryClip(clip);

			Toasty.info(ctx, ctx.getString(R.string.copyIssueUrlToastMsg));

			dismiss();
		});

		if(issue.getIssue().getState().equals("open")) { // close issue
			if(!userIsCreator && !canPush) {
				binding.closeIssue.setVisibility(View.GONE);
				binding.dividerCloseReopenIssue.setVisibility(View.GONE);
			}
			else if(issue.getIssueType().equalsIgnoreCase("Pull")) {
				binding.closeIssue.setText(R.string.closePr);
			}
			binding.closeIssue.setOnClickListener(closeSingleIssue -> {
				IssueActions.closeReopenIssue(ctx, "closed", issue);
				dismiss();
			});
		}
		else if(issue.getIssue().getState().equals("closed")) {
			if(userIsCreator || canPush) {
				if(issue.getIssueType().equalsIgnoreCase("Pull")) {
					binding.closeIssue.setText(R.string.reopenPr);
				}
				else {
					binding.closeIssue.setText(R.string.reOpenIssue);
				}
			}
			else {
				binding.closeIssue.setVisibility(View.GONE);
				binding.dividerCloseReopenIssue.setVisibility(View.GONE);
			}
			binding.closeIssue.setOnClickListener(closeSingleIssue -> {
				IssueActions.closeReopenIssue(ctx, "open", issue);
				dismiss();
			});
		}

		binding.subscribeIssue.setOnClickListener(subscribeToIssue -> {

			IssueActions.subscribe(ctx, issue);
			issue.setSubscribed(true);
			dismiss();
		});

		binding.unsubscribeIssue.setOnClickListener(unsubscribeToIssue -> {

			IssueActions.unsubscribe(ctx, issue);
			issue.setSubscribed(false);
			dismiss();
		});

		if(issue.isSubscribed()) {
			binding.subscribeIssue.setVisibility(View.GONE);
			binding.unsubscribeIssue.setVisibility(View.VISIBLE);
		}
		else {
			binding.subscribeIssue.setVisibility(View.VISIBLE);
			binding.unsubscribeIssue.setVisibility(View.GONE);
		}

		if(archived) {
			binding.subscribeIssue.setVisibility(View.GONE);
			binding.unsubscribeIssue.setVisibility(View.GONE);
			binding.editIssue.setVisibility(View.GONE);
			binding.editLabels.setVisibility(View.GONE);
			binding.closeIssue.setVisibility(View.GONE);
			binding.dividerCloseReopenIssue.setVisibility(View.GONE);
			binding.addRemoveAssignees.setVisibility(View.GONE);
			binding.commentReactionButtons.setVisibility(View.GONE);
			binding.shareDivider.setVisibility(View.GONE);
		}

		return binding.getRoot();
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
