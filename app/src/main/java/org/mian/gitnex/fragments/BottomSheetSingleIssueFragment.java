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
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import org.mian.gitnex.R;
import org.mian.gitnex.actions.IssueActions;
import org.mian.gitnex.actions.PullRequestActions;
import org.mian.gitnex.activities.DiffActivity;
import org.mian.gitnex.activities.EditIssueActivity;
import org.mian.gitnex.activities.MergePullRequestActivity;
import org.mian.gitnex.databinding.BottomSheetSingleIssueBinding;
import org.mian.gitnex.helpers.AlertDialogs;
import org.mian.gitnex.helpers.TinyDB;
import org.mian.gitnex.helpers.Toasty;
import org.mian.gitnex.helpers.Version;
import org.mian.gitnex.structs.BottomSheetListener;
import org.mian.gitnex.views.ReactionSpinner;
import java.util.Objects;

/**
 * Author M M Arif
 */

public class BottomSheetSingleIssueFragment extends BottomSheetDialogFragment {

	private BottomSheetListener bmListener;

	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

		BottomSheetSingleIssueBinding binding = BottomSheetSingleIssueBinding.inflate(inflater, container, false);

		final Context ctx = getContext();
		final TinyDB tinyDB = TinyDB.getInstance(ctx);

		Bundle bundle1 = new Bundle();

		String repoFullName = tinyDB.getString("repoFullName");
		String[] parts = repoFullName.split("/");

		Bundle bundle = new Bundle();

		bundle.putString("repoOwner", parts[0]);
		bundle.putString("repoName", parts[1]);
		bundle.putInt("issueId", Integer.parseInt(tinyDB.getString("issueNumber")));

		TextView loadReactions = new TextView(ctx);
		loadReactions.setText(Objects.requireNonNull(ctx).getString(R.string.genericWaitFor));
		loadReactions.setGravity(Gravity.CENTER);
		loadReactions.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 160));
		binding.commentReactionButtons.addView(loadReactions);

		ReactionSpinner reactionSpinner = new ReactionSpinner(ctx, bundle1);
		reactionSpinner.setOnInteractedListener(() -> {

			tinyDB.putBoolean("singleIssueUpdate", true);

			bmListener.onButtonClicked("onResume");
			dismiss();
		});

		Handler handler = new Handler();
		handler.postDelayed(() -> {
			binding.commentReactionButtons.removeView(loadReactions);
			reactionSpinner.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 160));
			binding.commentReactionButtons.addView(reactionSpinner);
		}, 2500);

		if(tinyDB.getString("issueType").equalsIgnoreCase("Pull")) {

			binding.editIssue.setText(R.string.editPrText);
			binding.copyIssueUrl.setText(R.string.copyPrUrlText);
			binding.shareIssue.setText(R.string.sharePr);

			if(tinyDB.getBoolean("prMerged") || tinyDB.getString("repoPrState").equals("closed")) {
				binding.mergePullRequest.setVisibility(View.GONE);
				binding.updatePullRequest.setVisibility(View.GONE);
				binding.mergePullRequest.setVisibility(View.GONE);
				binding.deletePrHeadBranch.setVisibility(View.VISIBLE);
			}
			else {
				binding.mergePullRequest.setVisibility(View.VISIBLE);
				binding.updatePullRequest.setVisibility(View.VISIBLE);
				binding.mergePullRequest.setVisibility(View.VISIBLE);
				binding.deletePrHeadBranch.setVisibility(View.GONE);
			}

			if(new Version(tinyDB.getString("giteaVersion")).higherOrEqual("1.13.0")) {
				binding.openFilesDiff.setVisibility(View.VISIBLE);
			}
			else if(tinyDB.getString("repoType").equals("public")) {
				binding.openFilesDiff.setVisibility(View.VISIBLE);
			}
			else {
				binding.openFilesDiff.setVisibility(View.GONE);
			}

		}
		else {

			binding.mergePullRequest.setVisibility(View.GONE);
			binding.updatePullRequest.setVisibility(View.GONE);
			binding.mergePullRequest.setVisibility(View.GONE);
			binding.deletePrHeadBranch.setVisibility(View.GONE);
		}

		binding.updatePullRequest.setOnClickListener(v -> {
			if(new Version(tinyDB.getString("giteaVersion")).higherOrEqual("1.16.0")) {
				AlertDialogs.selectPullUpdateStrategy(requireContext(), parts[0], parts[1], tinyDB.getString("issueNumber"));
			}
			else {
				PullRequestActions.updatePr(requireContext(), parts[0], parts[1], tinyDB.getString("issueNumber"), null);
			}
			dismiss();
		});

		binding.mergePullRequest.setOnClickListener(v13 -> {
			startActivity(new Intent(ctx, MergePullRequestActivity.class));
			dismiss();
		});

		binding.openFilesDiff.setOnClickListener(v14 -> {
			startActivity(new Intent(ctx, DiffActivity.class));
			dismiss();
		});

		binding.deletePrHeadBranch.setOnClickListener(v -> {

			PullRequestActions.deleteHeadBranch(ctx, parts[0], parts[1], tinyDB.getString("prHeadBranch"), true);
			dismiss();
		});



		binding.editIssue.setOnClickListener(v15 -> {
			startActivity(new Intent(ctx, EditIssueActivity.class));
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
			sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, getResources().getString(R.string.hash) + tinyDB.getString("issueNumber") + " " + tinyDB.getString("issueTitle"));
			sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, tinyDB.getString("singleIssueHtmlUrl"));
			startActivity(Intent.createChooser(sharingIntent, getResources().getString(R.string.hash) + tinyDB.getString("issueNumber") + " " + tinyDB.getString("issueTitle")));

			dismiss();
		});

		binding.copyIssueUrl.setOnClickListener(v12 -> {

			// copy to clipboard
			ClipboardManager clipboard = (ClipboardManager) Objects.requireNonNull(ctx).getSystemService(Context.CLIPBOARD_SERVICE);
			ClipData clip = ClipData.newPlainText("issueUrl", tinyDB.getString("singleIssueHtmlUrl"));
			assert clipboard != null;
			clipboard.setPrimaryClip(clip);

			Toasty.info(ctx, ctx.getString(R.string.copyIssueUrlToastMsg));

			dismiss();
		});

		if(tinyDB.getString("issueType").equalsIgnoreCase("Issue")) {

			if(tinyDB.getString("issueState").equals("open")) { // close issue

				binding.reOpenIssue.setVisibility(View.GONE);
				binding.closeIssue.setVisibility(View.VISIBLE);

				binding.closeIssue.setOnClickListener(closeSingleIssue -> {

					IssueActions.closeReopenIssue(ctx, Integer.parseInt(tinyDB.getString("issueNumber")), "closed");
					dismiss();

				});

			}
			else if(tinyDB.getString("issueState").equals("closed")) {

				binding.closeIssue.setVisibility(View.GONE);
				binding.reOpenIssue.setVisibility(View.VISIBLE);

				binding.reOpenIssue.setOnClickListener(reOpenSingleIssue -> {

					IssueActions.closeReopenIssue(ctx, Integer.parseInt(tinyDB.getString("issueNumber")), "open");
					dismiss();

				});

			}

		}
		else {

			binding.reOpenIssue.setVisibility(View.GONE);
			binding.closeIssue.setVisibility(View.GONE);

		}

		binding.subscribeIssue.setOnClickListener(subscribeToIssue -> {

			IssueActions.subscribe(ctx);
			dismiss();
		});

		binding.unsubscribeIssue.setOnClickListener(unsubscribeToIssue -> {

			IssueActions.unsubscribe(ctx);
			dismiss();
		});

		if(new Version(tinyDB.getString("giteaVersion")).less("1.12.0")) {
			binding.subscribeIssue.setVisibility(View.GONE);
			binding.unsubscribeIssue.setVisibility(View.GONE);
		}
		else if(tinyDB.getBoolean("issueSubscribed")) {
			binding.subscribeIssue.setVisibility(View.GONE);
			binding.unsubscribeIssue.setVisibility(View.VISIBLE);
		}
		else {
			binding.subscribeIssue.setVisibility(View.VISIBLE);
			binding.unsubscribeIssue.setVisibility(View.GONE);
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

			throw new ClassCastException(context.toString() + " must implement BottomSheetListener");
		}
	}
}
