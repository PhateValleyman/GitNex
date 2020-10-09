package org.mian.gitnex.fragments;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.hendraanggrian.appcompat.socialview.Mention;
import com.hendraanggrian.appcompat.widget.MentionArrayAdapter;
import com.hendraanggrian.appcompat.widget.SocialAutoCompleteTextView;
import org.mian.gitnex.R;
import org.mian.gitnex.actions.ActionResult;
import org.mian.gitnex.actions.CollaboratorActions;
import org.mian.gitnex.actions.IssueActions;
import org.mian.gitnex.activities.MainActivity;
import org.mian.gitnex.database.api.DraftsApi;
import org.mian.gitnex.helpers.StaticGlobalVariables;
import org.mian.gitnex.helpers.TinyDB;
import org.mian.gitnex.helpers.Toasty;
import org.mian.gitnex.models.Collaborators;
import java.util.Objects;

/**
 * @author opyale
 */

public class BottomSheetReplyFragment extends BottomSheetDialogFragment {

	private TinyDB tinyDB;
	private DraftsApi draftsApi;

	private int repositoryId;
	private int currentActiveAccountId;
	private int issueNumber;
	private long draftId;

	private ArrayAdapter<Mention> mentionArrayAdapter;
	private TextView draftsHint;

	@Override
	public void onAttach(@NonNull Context context) {

		tinyDB = new TinyDB(context);
		draftsApi = new DraftsApi(context);

		repositoryId = (int) tinyDB.getLong("repositoryId", 0);
		currentActiveAccountId = tinyDB.getInt("currentActiveAccountId");
		issueNumber = Integer.parseInt(tinyDB.getString("issueNumber"));

		mentionArrayAdapter = new MentionArrayAdapter<>(context);

		CollaboratorActions.getCollaborators(context).accept((status, result) -> {

			assert result != null;

			for(Collaborators collaborators : result) {

				Mention mention = new Mention(collaborators.getUsername(), collaborators.getFull_name(), collaborators.getAvatar_url());
				mentionArrayAdapter.add(mention);

			}
		});

		super.onAttach(context);

	}

	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

		View view = inflater.inflate(R.layout.bottom_sheet_reply_layout, container, false);
		Bundle arguments = requireArguments();

		draftsHint = view.findViewById(R.id.drafts_hint);

		SocialAutoCompleteTextView socialAutoCompleteTextView = view.findViewById(R.id.comment);
		TextView toolbarTitle = view.findViewById(R.id.toolbar_title);
		ImageButton close = view.findViewById(R.id.close);
		ImageButton drafts = view.findViewById(R.id.drafts);
		ImageButton send = view.findViewById(R.id.send);

		if(Objects.equals(arguments.getString("commentAction"), "edit")) {
			send.setVisibility(View.GONE);
		}

		if(arguments.getString("draftId") != null) {
			draftId = Long.parseLong(arguments.getString("draftId"));
		}

		if(!tinyDB.getString("issueTitle").isEmpty()) {
			toolbarTitle.setText(tinyDB.getString("issueTitle"));
		} else if(arguments.getString("draftTitle") != null) {
			toolbarTitle.setText(arguments.getString("draftTitle"));
		}

		if(arguments.getString("commentBody") != null) {

			send.setEnabled(true);
			send.setAlpha(1f);

			socialAutoCompleteTextView.setText(arguments.getString("commentBody"));

			if(arguments.getBoolean("cursorToEnd", false)) {
				socialAutoCompleteTextView.setSelection(socialAutoCompleteTextView.length());
			}
		}

		socialAutoCompleteTextView.requestFocus();
		socialAutoCompleteTextView.addTextChangedListener(new TextWatcher() {

			@Override
			public void afterTextChanged(Editable s) {

				String text = socialAutoCompleteTextView.getText().toString();

				if(text.isEmpty()) {

					send.setEnabled(false);
					send.setAlpha(0.5f);
					saveDraft(null, true);

				} else {

					send.setEnabled(true);
					send.setAlpha(1f);
					saveDraft(text, false);

				}
			}

			@Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
			@Override public void onTextChanged(CharSequence s, int start, int before, int count) {}

		});

		close.setOnClickListener(v -> dismiss());

		drafts.setOnClickListener(v -> {

			Intent intent = new Intent(getContext(), MainActivity.class);
			intent.putExtra("launchFragment", "drafts");
			startActivity(intent);

			dismiss();

		});

		send.setOnClickListener(v -> IssueActions
			.reply(getContext(), socialAutoCompleteTextView.getText().toString(), issueNumber)
			.accept((status, result) -> {

				if(status == ActionResult.Status.SUCCESS) {

					Toasty.success(getContext(), getString(R.string.commentSuccess));

					tinyDB.putBoolean("commentPosted", true);
					tinyDB.putBoolean("resumeIssues", true);
					tinyDB.putBoolean("resumePullRequests", true);

					if(draftId != 0 && tinyDB.getBoolean("draftsCommentsDeletionEnabled")) {
						draftsApi.deleteSingleDraft((int) draftId);
					}

					dismiss();

				} else {

					Toasty.error(getContext(), getString(R.string.commentError));
					dismiss();

				}
			}));

		return view;

	}

	private void saveDraft(String text, boolean remove) {

		ValueAnimator valueAnimator = ValueAnimator.ofFloat(0f, 1f);
		valueAnimator.setDuration(500);
		valueAnimator.addUpdateListener(animation -> {

			float value = (Float) animation.getAnimatedValue();

			if(value == 0f)  {
				draftsHint.setVisibility((remove) ? View.GONE : View.VISIBLE);
			}

			draftsHint.setAlpha(value);

		});

		if(remove) {

			draftsApi.deleteSingleDraft((int) draftId);
			draftId = 0;

			valueAnimator.reverse();

		} else {

			if(draftId == 0) {
				draftId = draftsApi.insertDraft(repositoryId, currentActiveAccountId, issueNumber, text, StaticGlobalVariables.draftTypeComment, "TODO");
			} else {
				DraftsApi.updateDraft(text, (int) draftId, "TODO");
			}

			draftsHint.setVisibility(View.VISIBLE);
			valueAnimator.start();

		}
	}

	public static BottomSheetReplyFragment newInstance(Bundle bundle) {

		BottomSheetReplyFragment fragment = new BottomSheetReplyFragment();
		fragment.setArguments(bundle);

		return fragment;

	}
}
