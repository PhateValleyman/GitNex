package org.mian.gitnex.activities;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;
import androidx.annotation.NonNull;
import org.gitnex.tea4j.v2.models.EditIssueOption;
import org.gitnex.tea4j.v2.models.Issue;
import org.gitnex.tea4j.v2.models.Milestone;
import org.mian.gitnex.R;
import org.mian.gitnex.clients.RetrofitClient;
import org.mian.gitnex.databinding.ActivityEditIssueBinding;
import org.mian.gitnex.fragments.IssuesFragment;
import org.mian.gitnex.fragments.PullRequestsFragment;
import org.mian.gitnex.helpers.AlertDialogs;
import org.mian.gitnex.helpers.AppUtil;
import org.mian.gitnex.helpers.Constants;
import org.mian.gitnex.helpers.Toasty;
import org.mian.gitnex.helpers.contexts.IssueContext;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import retrofit2.Call;
import retrofit2.Callback;

/**
 * Author M M Arif
 */

public class EditIssueActivity extends BaseActivity implements View.OnClickListener {

    private View.OnClickListener onClickListener;
    private int resultLimit = Constants.resultLimitOldGiteaInstances;

    private EditText editIssueTitle;
    private EditText editIssueDescription;
    private TextView editIssueDueDate;
    private Button editIssueButton;
    private AutoCompleteTextView editIssueMilestoneSpinner;

    private final String msState = "open";
    private int milestoneId = 0;
	private Date currentDate = null;

    private LinkedHashMap<String, Milestone> milestonesList = new LinkedHashMap<>();

	private IssueContext issue;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

	    ActivityEditIssueBinding activityEditIssueBinding = ActivityEditIssueBinding.inflate(getLayoutInflater());
	    setContentView(activityEditIssueBinding.getRoot());

        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

        issue = IssueContext.fromIntent(getIntent());

        ImageView closeActivity = activityEditIssueBinding.close;
        editIssueButton = activityEditIssueBinding.editIssueButton;
        TextView toolbar_title = activityEditIssueBinding.toolbarTitle;
        editIssueTitle = activityEditIssueBinding.editIssueTitle;
        editIssueDescription = activityEditIssueBinding.editIssueDescription;
        editIssueDueDate = activityEditIssueBinding.editIssueDueDate;

        // if gitea is 1.12 or higher use the new limit
        if(getAccount().requiresVersion("1.12.0")) {

            resultLimit = Constants.resultLimitNewGiteaInstances;
        }

        editIssueTitle.requestFocus();
        assert imm != null;
        imm.showSoftInput(editIssueTitle, InputMethodManager.SHOW_IMPLICIT);

	    editIssueDescription.setOnTouchListener((touchView, motionEvent) -> {

		    touchView.getParent().requestDisallowInterceptTouchEvent(true);

		    if ((motionEvent.getAction() & MotionEvent.ACTION_UP) != 0 && (motionEvent.getActionMasked() & MotionEvent.ACTION_UP) != 0) {

			    touchView.getParent().requestDisallowInterceptTouchEvent(false);
		    }
		    return false;
	    });

        editIssueMilestoneSpinner = findViewById(R.id.editIssueMilestoneSpinner);

        initCloseListener();
        closeActivity.setOnClickListener(onClickListener);

        editIssueDueDate.setOnClickListener(this);
        editIssueButton.setOnClickListener(this);

        if(issue.getIssueType().equalsIgnoreCase("Pull")) {

            toolbar_title.setText(getString(R.string.editPrNavHeader, String.valueOf(issue.getIssueIndex())));
        }
        else {

            toolbar_title.setText(getString(R.string.editIssueNavHeader, String.valueOf(issue.getIssueIndex())));
        }

        disableProcessButton();
        getIssue(issue.getRepository().getOwner(), issue.getRepository().getName(), issue.getIssueIndex(), resultLimit);

        if(!issue.getRepository().getPermissions().isPush()) {
			findViewById(R.id.editIssueMilestoneSpinnerLayout).setVisibility(View.GONE);
			findViewById(R.id.editIssueDueDateLayout).setVisibility(View.GONE);
        }
    }

    private void initCloseListener() {

        onClickListener = view -> finish();
    }

    private void processEditIssue() {

        boolean connToInternet = AppUtil.hasNetworkConnection(appCtx);

        String editIssueTitleForm = editIssueTitle.getText().toString();
        String editIssueDescriptionForm = editIssueDescription.getText().toString();

	    if(!connToInternet) {

            Toasty.error(ctx, getResources().getString(R.string.checkNetConnection));
            return;
        }

        if (editIssueTitleForm.equals("")) {

            Toasty.error(ctx, getString(R.string.issueTitleEmpty));
            return;
        }

	    disableProcessButton();
        editIssue(issue.getRepository().getOwner(), issue.getRepository().getName(), issue.getIssueIndex(), editIssueTitleForm, editIssueDescriptionForm,
	        milestoneId);
    }

    private void editIssue(String repoOwner, String repoName, int issueIndex, String title, String description, int milestoneId) {

        EditIssueOption issueData = new EditIssueOption();
		issueData.setTitle(title);
		issueData.setBody(description);
		issueData.setDueDate(currentDate);
		issueData.setMilestone((long) milestoneId);

        Call<Issue> call = RetrofitClient
                .getApiInterface(ctx)
                .issueEditIssue(repoOwner, repoName, (long) issueIndex, issueData);

        call.enqueue(new Callback<Issue>() {

            @Override
            public void onResponse(@NonNull Call<Issue> call, @NonNull retrofit2.Response<Issue> response) {

                if(response.code() == 201) {

                    if(issue.getIssueType().equalsIgnoreCase("Pull")) {

                        Toasty.success(ctx, getString(R.string.editPrSuccessMessage));
                    }
                    else {

                        Toasty.success(ctx, getString(R.string.editIssueSuccessMessage));
                    }

                    Intent result = new Intent();
                    result.putExtra("issueEdited", true);
	                IssuesFragment.resumeIssues = issue.getIssue().getPullRequest() == null;
	                PullRequestsFragment.resumePullRequests = issue.getIssue().getPullRequest() != null;
					setResult(200, result);
                    finish();
                }
                else if(response.code() == 401) {

                    enableProcessButton();
                    AlertDialogs.authorizationTokenRevokedDialog(ctx, getResources().getString(R.string.alertDialogTokenRevokedTitle),
                            getResources().getString(R.string.alertDialogTokenRevokedMessage),
                            getResources().getString(R.string.cancelButton),
                            getResources().getString(R.string.navLogout));
                }
                else {

                    enableProcessButton();
                    Toasty.error(ctx, getString(R.string.genericError));
                }
            }

            @Override
            public void onFailure(@NonNull Call<Issue> call, @NonNull Throwable t) {

                Log.e("onFailure", t.toString());
                enableProcessButton();
            }
        });

    }

    @Override
    public void onClick(View v) {

        if (v == editIssueDueDate) {

            final Calendar c = Calendar.getInstance();
            int mYear = c.get(Calendar.YEAR);
            final int mMonth = c.get(Calendar.MONTH);
            final int mDay = c.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(this,
	            (view, year, monthOfYear, dayOfMonth) -> {
				editIssueDueDate.setText(getString(R.string.setDueDate, year, (monthOfYear + 1), dayOfMonth));
				currentDate = new Date(year - 1900, monthOfYear, dayOfMonth);
	            }, mYear, mMonth, mDay);
            datePickerDialog.show();
        }
        else if(v == editIssueButton) {

            processEditIssue();
        }

    }

    private void getIssue(final String repoOwner, final String repoName, int issueIndex, int resultLimit) {

        Call<Issue> call = RetrofitClient
                .getApiInterface(ctx)
                .issueGetIssue(repoOwner, repoName, (long) issueIndex);

        call.enqueue(new Callback<>() {

	        @Override
	        public void onResponse(@NonNull Call<Issue> call, @NonNull retrofit2.Response<Issue> response) {

		        if(response.code() == 200) {

			        assert response.body() != null;
			        editIssueTitle.setText(response.body().getTitle());
			        editIssueDescription.setText(response.body().getBody());

			        Milestone currentMilestone = response.body().getMilestone();

			        // get milestones list
			        if(response.body().getId() > 0) {

				        Call<List<Milestone>> call_ = RetrofitClient.getApiInterface(ctx).issueGetMilestonesList(repoOwner, repoName, msState, null, 1, resultLimit);

				        call_.enqueue(new Callback<>() {

					        @Override
					        public void onResponse(@NonNull Call<List<Milestone>> call, @NonNull retrofit2.Response<List<Milestone>> response_) {

						        if(response_.code() == 200) {

							        List<Milestone> milestonesList_ = response_.body();

							        assert milestonesList_ != null;

							        Milestone ms = new Milestone();
							        ms.setId(0L);
							        ms.setTitle(getString(R.string.issueCreatedNoMilestone));
							        milestonesList.put(ms.getTitle(), ms);

							        if(milestonesList_.size() > 0) {

								        for(Milestone milestone : milestonesList_) {

									        //Don't translate "open" is a enum
									        if(milestone.getState().equals("open")) {
										        milestonesList.put(milestone.getTitle(), milestone);
									        }
								        }
							        }

							        ArrayAdapter<String> adapter = new ArrayAdapter<>(EditIssueActivity.this, R.layout.list_spinner_items, new ArrayList<>(milestonesList.keySet()));

							        editIssueMilestoneSpinner.setAdapter(adapter);

							        editIssueMilestoneSpinner.setOnItemClickListener((parent, view, position, id) -> {
								        if(position == 0) {
									        milestoneId = 0;
								        }
								        else if(view instanceof TextView) {
									        milestoneId = Math.toIntExact(
										        Objects.requireNonNull(milestonesList.get(((TextView) view).getText().toString())).getId());
								        }
							        });

							        new Handler(Looper.getMainLooper()).postDelayed(() -> {
										if(currentMilestone != null) {
											milestoneId = Math.toIntExact(currentMilestone.getId());
											editIssueMilestoneSpinner.setText(currentMilestone.getTitle(), false);
										} else {
											milestoneId = 0;
											editIssueMilestoneSpinner.setText(getString(R.string.issueCreatedNoMilestone), false);
										}
							        }, 500);

							        enableProcessButton();
						        }
					        }

					        @Override
					        public void onFailure(@NonNull Call<List<Milestone>> call, @NonNull Throwable t) {

						        Log.e("onFailure", t.toString());
					        }
				        });

			        }
			        // get milestones list

			        if(response.body().getDueDate() != null) {

				        @SuppressLint("SimpleDateFormat") DateFormat formatter = new SimpleDateFormat("yyyy-M-dd");
				        String dueDate = formatter.format(response.body().getDueDate());
				        editIssueDueDate.setText(dueDate);
			        }
			        //enableProcessButton();

		        }
		        else if(response.code() == 401) {

			        AlertDialogs.authorizationTokenRevokedDialog(ctx, getResources().getString(R.string.alertDialogTokenRevokedTitle), getResources().getString(R.string.alertDialogTokenRevokedMessage),
				        getResources().getString(R.string.cancelButton), getResources().getString(R.string.navLogout));
		        }
		        else {

			        Toasty.error(ctx, getString(R.string.genericError));
		        }
	        }

	        @Override
	        public void onFailure(@NonNull Call<Issue> call, @NonNull Throwable t) {

		        Log.e("onFailure", t.toString());
	        }
        });

    }

    private void disableProcessButton() {

        editIssueButton.setEnabled(false);
    }

    private void enableProcessButton() {

        editIssueButton.setEnabled(true);
    }

	@Override
	public void onResume() {
		super.onResume();
		issue.getRepository().checkAccountSwitch(this);
	}

}
