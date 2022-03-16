package org.mian.gitnex.activities;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import androidx.annotation.NonNull;
import org.gitnex.tea4j.v2.models.*;
import org.mian.gitnex.R;
import org.mian.gitnex.actions.AssigneesActions;
import org.mian.gitnex.actions.LabelsActions;
import org.mian.gitnex.adapters.AssigneesListAdapter;
import org.mian.gitnex.adapters.LabelsListAdapter;
import org.mian.gitnex.clients.RetrofitClient;
import org.mian.gitnex.databinding.ActivityCreateIssueBinding;
import org.mian.gitnex.databinding.CustomAssigneesSelectionDialogBinding;
import org.mian.gitnex.databinding.CustomLabelsSelectionDialogBinding;
import org.mian.gitnex.fragments.IssuesFragment;
import org.mian.gitnex.helpers.AlertDialogs;
import org.mian.gitnex.helpers.AppUtil;
import org.mian.gitnex.helpers.Constants;
import org.mian.gitnex.helpers.Toasty;
import org.mian.gitnex.helpers.contexts.RepositoryContext;
import java.util.*;
import retrofit2.Call;
import retrofit2.Callback;

/**
 * Author M M Arif
 */

public class CreateIssueActivity extends BaseActivity implements View.OnClickListener, LabelsListAdapter.LabelsListAdapterListener, AssigneesListAdapter.AssigneesListAdapterListener {

	private ActivityCreateIssueBinding viewBinding;
	private CustomLabelsSelectionDialogBinding labelsBinding;
	private CustomAssigneesSelectionDialogBinding assigneesBinding;
    private View.OnClickListener onClickListener;
    private int resultLimit = Constants.resultLimitOldGiteaInstances;
	private Dialog dialogLabels;
	private Dialog dialogAssignees;
	private String labelsSetter;
	private String assigneesSetter;
	private int milestoneId;
	private Date currentDate = null;

	private RepositoryContext repository;

	private LabelsListAdapter labelsAdapter;
	private AssigneesListAdapter assigneesAdapter;

	private List<Integer> labelsIds = new ArrayList<>();
	private final List<Label> labelsList = new ArrayList<>();
	private final List<Milestone> milestonesList = new ArrayList<>();
	private final List<User> assigneesList = new ArrayList<>();
	private List<String> assigneesListData = new ArrayList<>();

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

	    viewBinding = ActivityCreateIssueBinding.inflate(getLayoutInflater());
	    setContentView(viewBinding.getRoot());

        boolean connToInternet = AppUtil.hasNetworkConnection(appCtx);

        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

        repository = RepositoryContext.fromIntent(getIntent());

        // require gitea 1.12 or higher
        if(getAccount().requiresVersion("1.12.0")) {

            resultLimit = Constants.resultLimitNewGiteaInstances;
        }

	    viewBinding.newIssueTitle.requestFocus();
        assert imm != null;
        imm.showSoftInput(viewBinding.newIssueTitle, InputMethodManager.SHOW_IMPLICIT);

	    viewBinding.newIssueDescription.setOnTouchListener((touchView, motionEvent) -> {

		    touchView.getParent().requestDisallowInterceptTouchEvent(true);

		    if ((motionEvent.getAction() & MotionEvent.ACTION_UP) != 0 && (motionEvent.getActionMasked() & MotionEvent.ACTION_UP) != 0) {

			    touchView.getParent().requestDisallowInterceptTouchEvent(false);
		    }
		    return false;
	    });

	    labelsAdapter = new LabelsListAdapter(labelsList, CreateIssueActivity.this, labelsIds);
	    assigneesAdapter = new AssigneesListAdapter(ctx, assigneesList, CreateIssueActivity.this, assigneesListData);

        initCloseListener();
	    viewBinding.close.setOnClickListener(onClickListener);

	    viewBinding.newIssueAssigneesList.setOnClickListener(this);
	    viewBinding.newIssueLabels.setOnClickListener(this);
	    viewBinding.newIssueDueDate.setOnClickListener(this);

        getMilestones(repository.getOwner(), repository.getName(), resultLimit);

        disableProcessButton();

	    viewBinding.newIssueLabels.setOnClickListener(newIssueLabels -> showLabels());

	    viewBinding.newIssueAssigneesList.setOnClickListener(newIssueAssigneesList -> showAssignees());

        if(!connToInternet) {

	        viewBinding.createNewIssueButton.setEnabled(false);
        }
        else {

	        viewBinding.createNewIssueButton.setOnClickListener(this);
        }

        if(!repository.getPermissions().isPush()) {
        	viewBinding.newIssueAssigneesListLayout.setVisibility(View.GONE);
        	viewBinding.newIssueMilestoneSpinnerLayout.setVisibility(View.GONE);
        	viewBinding.newIssueLabelsLayout.setVisibility(View.GONE);
        	viewBinding.newIssueDueDateLayout.setVisibility(View.GONE);
        }

    }

	@Override
	public void assigneesInterface(List<String> data) {

		assigneesSetter = String.valueOf(data);
		viewBinding.newIssueAssigneesList.setText(assigneesSetter.replace("]", "").replace("[", ""));
		assigneesListData = data;
	}

	@Override
	public void labelsInterface(List<String> data) {

		labelsSetter = String.valueOf(data);
		viewBinding.newIssueLabels.setText(labelsSetter.replace("]", "").replace("[", ""));
	}

	@Override
	public void labelsIdsInterface(List<Integer> data) {

		labelsIds = data;
	}

	private void showAssignees() {

		dialogAssignees = new Dialog(ctx, R.style.ThemeOverlay_MaterialComponents_Dialog_Alert);

		if (dialogAssignees.getWindow() != null) {

			dialogAssignees.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
		}

		assigneesBinding = CustomAssigneesSelectionDialogBinding.inflate(LayoutInflater.from(ctx));

		View view = assigneesBinding.getRoot();
		dialogAssignees.setContentView(view);

		assigneesBinding.cancel.setOnClickListener(assigneesBinding_ -> dialogAssignees.dismiss());

		dialogAssignees.show();
		AssigneesActions.getRepositoryAssignees(ctx, repository.getOwner(), repository.getName(), assigneesList, dialogAssignees, assigneesAdapter, assigneesBinding);
	}

	private void showLabels() {

		dialogLabels = new Dialog(ctx, R.style.ThemeOverlay_MaterialComponents_Dialog_Alert);

		if (dialogLabels.getWindow() != null) {

			dialogLabels.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
		}

		labelsBinding = CustomLabelsSelectionDialogBinding.inflate(LayoutInflater.from(ctx));

		View view = labelsBinding.getRoot();
		dialogLabels.setContentView(view);

		labelsBinding.cancel.setOnClickListener(labelsBinding_ -> dialogLabels.dismiss());

		dialogLabels.show();
		LabelsActions.getRepositoryLabels(ctx, repository.getOwner(), repository.getName(), labelsList, dialogLabels, labelsAdapter, labelsBinding);
	}

    private void processNewIssue() {

        boolean connToInternet = AppUtil.hasNetworkConnection(appCtx);

        String newIssueTitleForm = Objects.requireNonNull(viewBinding.newIssueTitle.getText()).toString();
        String newIssueDescriptionForm = Objects.requireNonNull(viewBinding.newIssueDescription.getText()).toString();
        String newIssueDueDateForm = Objects.requireNonNull(viewBinding.newIssueDueDate.getText()).toString();

        if(!connToInternet) {

            Toasty.error(ctx, getResources().getString(R.string.checkNetConnection));
            return;
        }

        if (newIssueTitleForm.equals("")) {

            Toasty.error(ctx, getString(R.string.issueTitleEmpty));
            return;
        }

	    disableProcessButton();
        createNewIssueFunc(repository.getOwner(), repository.getName(), newIssueDescriptionForm, milestoneId, newIssueTitleForm);
    }

    private void createNewIssueFunc(String repoOwner, String repoName, String newIssueDescriptionForm, int newIssueMilestoneIdForm, String newIssueTitleForm) {

		ArrayList<Long> labelIds = new ArrayList<>();
	    for(Integer i : labelsIds) {
		    labelIds.add((long) i);
	    }

        CreateIssueOption createNewIssueJson = new CreateIssueOption();
		createNewIssueJson.setBody(newIssueDescriptionForm);
		createNewIssueJson.setMilestone((long) newIssueMilestoneIdForm);
		createNewIssueJson.setDueDate(currentDate);
		createNewIssueJson.setTitle(newIssueTitleForm);
		createNewIssueJson.setAssignees(assigneesListData);
		createNewIssueJson.setLabels(labelIds);

        Call<Issue> call3 = RetrofitClient
                .getApiInterface(ctx)
                .issueCreateIssue(repoOwner, repoName, createNewIssueJson);

        call3.enqueue(new Callback<Issue>() {

            @Override
            public void onResponse(@NonNull Call<Issue> call, @NonNull retrofit2.Response<Issue> response2) {

				if(response2.code() == 201) {

                    IssuesFragment.resumeIssues = true;

                    Toasty.success(ctx, getString(R.string.issueCreated));
                    enableProcessButton();
                    finish();
                }
                else if(response2.code() == 401) {

                    enableProcessButton();
                    AlertDialogs.authorizationTokenRevokedDialog(ctx, getResources().getString(R.string.alertDialogTokenRevokedTitle),
                            getResources().getString(R.string.alertDialogTokenRevokedMessage),
                            getResources().getString(R.string.cancelButton),
                            getResources().getString(R.string.navLogout));
                }
                else {

                    Toasty.error(ctx, getString(R.string.issueCreatedError));
                    enableProcessButton();
                }

            }

            @Override
            public void onFailure(@NonNull Call<Issue> call, @NonNull Throwable t) {

	            Toasty.error(ctx, getString(R.string.genericServerResponseError));
                enableProcessButton();
            }
        });

    }

    private void initCloseListener() {

        onClickListener = view -> finish();
    }

    private void getMilestones(String repoOwner, String repoName, int resultLimit) {

        String msState = "open";
        Call<List<Milestone>> call = RetrofitClient
                .getApiInterface(ctx)
                .issueGetMilestonesList(repoOwner, repoName, msState, null, 1, resultLimit);

        call.enqueue(new Callback<List<Milestone>>() {

            @Override
            public void onResponse(@NonNull Call<List<Milestone>> call, @NonNull retrofit2.Response<List<Milestone>> response) {

                if(response.isSuccessful()) {

                    if(response.code() == 200) {

                        List<Milestone> milestonesList_ = response.body();

	                    Milestone ms = new Milestone();
						ms.setId(0L);
						ms.setTitle(getString(R.string.issueCreatedNoMilestone));
                        milestonesList.add(ms);
                        assert milestonesList_ != null;

                        if(milestonesList_.size() > 0) {

                            for (int i = 0; i < milestonesList_.size(); i++) {

                                //Don't translate "open" is a enum
                                if(milestonesList_.get(i).getState().equals("open")) {
                                    milestonesList.add(milestonesList_.get(i));
                                }
                            }
                        }

                        ArrayAdapter<Milestone> adapter = new ArrayAdapter<>(CreateIssueActivity.this,
                                R.layout.list_spinner_items, milestonesList);

	                    viewBinding.newIssueMilestoneSpinner.setAdapter(adapter);
                        enableProcessButton();

	                    viewBinding.newIssueMilestoneSpinner.setOnItemClickListener ((parent, view, position, id) ->

		                    milestoneId = Math.toIntExact(milestonesList.get(position).getId())
	                    );

                    }
                }

            }

            @Override
            public void onFailure(@NonNull Call<List<Milestone>> call, @NonNull Throwable t) {

	            Toasty.error(ctx, getString(R.string.genericServerResponseError));
            }
        });

    }

    @Override
    public void onClick(View v) {

        if (v == viewBinding.newIssueDueDate) {

            final Calendar c = Calendar.getInstance();
            int mYear = c.get(Calendar.YEAR);
            final int mMonth = c.get(Calendar.MONTH);
            final int mDay = c.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(this,
	            (view, year, monthOfYear, dayOfMonth) -> {
				viewBinding.newIssueDueDate.setText(getString(R.string.setDueDate, year, (monthOfYear + 1), dayOfMonth));
				currentDate = new Date(year - 1900, monthOfYear, dayOfMonth);
				}, mYear, mMonth, mDay);
            datePickerDialog.show();
        }
        else if(v == viewBinding.createNewIssueButton) {

            processNewIssue();
        }
    }

    private void disableProcessButton() {

	    viewBinding.createNewIssueButton.setEnabled(false);
    }

    private void enableProcessButton() {

	    viewBinding.createNewIssueButton.setEnabled(true);
    }

	@Override
	public void onResume() {
		super.onResume();
		repository.checkAccountSwitch(this);
	}
}
