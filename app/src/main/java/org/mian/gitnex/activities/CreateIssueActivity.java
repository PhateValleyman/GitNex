package org.mian.gitnex.activities;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import androidx.annotation.NonNull;
import com.google.gson.JsonElement;
import com.hendraanggrian.appcompat.socialview.Mention;
import com.hendraanggrian.appcompat.widget.MentionArrayAdapter;
import org.mian.gitnex.R;
import org.mian.gitnex.adapters.LabelsListAdapter;
import org.mian.gitnex.clients.RetrofitClient;
import org.mian.gitnex.databinding.ActivityCreateIssueBinding;
import org.mian.gitnex.databinding.CustomLabelsSelectionDialogBinding;
import org.mian.gitnex.helpers.AlertDialogs;
import org.mian.gitnex.helpers.AppUtil;
import org.mian.gitnex.helpers.Authorization;
import org.mian.gitnex.helpers.MultiSelectDialog;
import org.mian.gitnex.helpers.StaticGlobalVariables;
import org.mian.gitnex.helpers.TinyDB;
import org.mian.gitnex.helpers.Toasty;
import org.mian.gitnex.helpers.Version;
import org.mian.gitnex.models.Collaborators;
import org.mian.gitnex.models.CreateIssue;
import org.mian.gitnex.models.Labels;
import org.mian.gitnex.models.Milestones;
import org.mian.gitnex.models.MultiSelectModel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Author M M Arif
 */

public class CreateIssueActivity extends BaseActivity implements View.OnClickListener, LabelsListAdapter.LabelsListAdapterListener {

	private ActivityCreateIssueBinding viewBinding;
	private CustomLabelsSelectionDialogBinding labelsBinding;
    private View.OnClickListener onClickListener;
    MultiSelectDialog multiSelectDialog;
    private boolean assigneesFlag;
    final Context ctx = this;
    private Context appCtx;
    private TinyDB tinyDb;
    private int resultLimit = StaticGlobalVariables.resultLimitOldGiteaInstances;
	private Dialog dialogLabels;
	private String labelsSetter;

	private String instanceUrl;
	private String loginUid;
	private String instanceToken;
	private String repoOwner;
	private String repoName;

	private LabelsListAdapter labelsAdapter;

	private ArrayList<Integer> labelsIds = new ArrayList<>();
	List<Labels> labelsList = new ArrayList<>();
    List<Milestones> milestonesList = new ArrayList<>();
    ArrayList<MultiSelectModel> listOfAssignees = new ArrayList<>();
    private ArrayAdapter<Mention> defaultMentionAdapter;

    @Override
    protected int getLayoutResourceId(){
        return R.layout.activity_create_issue;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        appCtx = getApplicationContext();
	    tinyDb = new TinyDB(appCtx);

	    viewBinding = ActivityCreateIssueBinding.inflate(getLayoutInflater());
	    View view = viewBinding.getRoot();
	    setContentView(view);

        boolean connToInternet = AppUtil.hasNetworkConnection(appCtx);

        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

        instanceUrl = tinyDb.getString("instanceUrl");
        loginUid = tinyDb.getString("loginUid");
        final String loginFullName = tinyDb.getString("userFullname");
        String repoFullName = tinyDb.getString("repoFullName");
        String[] parts = repoFullName.split("/");
        repoOwner = parts[0];
        repoName = parts[1];
        instanceToken = "token " + tinyDb.getString(loginUid + "-token");

        // require gitea 1.12 or higher
        if(new Version(tinyDb.getString("giteaVersion")).higherOrEqual("1.12.0")) {

            resultLimit = StaticGlobalVariables.resultLimitNewGiteaInstances;
        }

	    viewBinding.newIssueTitle.requestFocus();
        assert imm != null;
        imm.showSoftInput(viewBinding.newIssueTitle, InputMethodManager.SHOW_IMPLICIT);

        defaultMentionAdapter = new MentionArrayAdapter<>(this);
        loadCollaboratorsList();

	    labelsAdapter = new LabelsListAdapter(labelsList, CreateIssueActivity.this);

	    viewBinding.newIssueDescription.setMentionAdapter(defaultMentionAdapter);

        initCloseListener();
	    viewBinding.close.setOnClickListener(onClickListener);

	    viewBinding.newIssueAssigneesList.setOnClickListener(this);
	    viewBinding.newIssueLabels.setOnClickListener(this);
	    viewBinding.newIssueDueDate.setOnClickListener(this);

        getMilestones(instanceUrl, instanceToken, repoOwner, repoName, loginUid, resultLimit);

        getCollaborators(instanceUrl, instanceToken, repoOwner, repoName, loginUid, loginFullName);

        disableProcessButton();

	    viewBinding.newIssueLabels.setOnClickListener(newIssueLabels ->
		    showLabels()
	    );

        if(!connToInternet) {

	        viewBinding.createNewIssueButton.setEnabled(false);
        }
        else {

	        viewBinding.createNewIssueButton.setOnClickListener(this);
        }

    }

	@Override
	public void labelsStringData(ArrayList<String> data) {

		labelsSetter = String.valueOf(data);
		viewBinding.newIssueLabels.setText(labelsSetter.replace("]", "").replace("[", ""));
	}

	@Override
	public void labelsIdsData(ArrayList<Integer> data) {

		labelsIds = data;
	}

	private void showLabels() {

		dialogLabels = new Dialog(ctx, R.style.ThemeOverlay_MaterialComponents_Dialog_Alert);
		dialogLabels.setCancelable(false);

		if (dialogLabels.getWindow() != null) {
			dialogLabels.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
		}

		labelsBinding = CustomLabelsSelectionDialogBinding.inflate(LayoutInflater.from(ctx));

		View view = labelsBinding.getRoot();
		dialogLabels.setContentView(view);

		labelsBinding.cancel.setOnClickListener(editProperties ->
			dialogLabels.dismiss()
		);

		Call<List<Labels>> call = RetrofitClient
			.getInstance(instanceUrl, ctx)
			.getApiInterface()
			.getlabels(instanceToken, repoOwner, repoName);

		call.enqueue(new Callback<List<Labels>>() {

			@Override
			public void onResponse(@NonNull Call<List<Labels>> call, @NonNull retrofit2.Response<List<Labels>> response) {

				labelsList.clear();
				List<Labels> labelsList_ = response.body();

				labelsBinding.progressBar.setVisibility(View.GONE);
				labelsBinding.dialogFrame.setVisibility(View.VISIBLE);

				if (response.code() == 200) {

					assert labelsList_ != null;
					if(labelsList_.size() > 0) {
						for (int i = 0; i < labelsList_.size(); i++) {

							labelsList.add(new Labels(labelsList_.get(i).getId(), labelsList_.get(i).getName(), labelsList_.get(i).getColor()));

						}
					}
					else {

						dialogLabels.dismiss();
						Toasty.warning(ctx, getString(R.string.noLabelsFound));
					}

					labelsBinding.labelsRecyclerView.setAdapter(labelsAdapter);

				}
				else {

					Toasty.error(ctx, getString(R.string.genericError));
				}

			}

			@Override
			public void onFailure(@NonNull Call<List<Labels>> call, @NonNull Throwable t) {

				Toasty.error(ctx, getString(R.string.genericServerResponseError));
			}
		});

		dialogLabels.show();

	}

    private void processNewIssue() {

        boolean connToInternet = AppUtil.hasNetworkConnection(appCtx);
        TinyDB tinyDb = new TinyDB(appCtx);
        final String instanceUrl = tinyDb.getString("instanceUrl");
        final String loginUid = tinyDb.getString("loginUid");
        String repoFullName = tinyDb.getString("repoFullName");
        String[] parts = repoFullName.split("/");
        final String repoOwner = parts[0];
        final String repoName = parts[1];
        final String instanceToken = "token " + tinyDb.getString(loginUid + "-token");

        Milestones mModel = (Milestones) viewBinding.newIssueMilestoneSpinner.getSelectedItem();

        int newIssueMilestoneIdForm = mModel.getId();
        String newIssueTitleForm = viewBinding.newIssueTitle.getText().toString();
        String newIssueDescriptionForm = viewBinding.newIssueDescription.getText().toString();
        String newIssueAssigneesListForm = viewBinding.newIssueAssigneesList.getText().toString();
        String newIssueDueDateForm = viewBinding.newIssueDueDate.getText().toString();

        if(!connToInternet) {

            Toasty.error(ctx, getResources().getString(R.string.checkNetConnection));
            return;

        }

        if (newIssueTitleForm.equals("")) {

            Toasty.error(ctx, getString(R.string.issueTitleEmpty));
            return;

        }

        if (newIssueDueDateForm.equals("")) {
            newIssueDueDateForm = null;
        }
        else {
            newIssueDueDateForm = (AppUtil.customDateCombine(AppUtil.customDateFormat(newIssueDueDateForm)));
        }

        List<String> newIssueAssigneesListForm_ = new ArrayList<>(Arrays.asList(newIssueAssigneesListForm.split(",")));

        for (int i = 0; i < newIssueAssigneesListForm_.size(); i++) {
            newIssueAssigneesListForm_.set(i, newIssueAssigneesListForm_.get(i).trim());
        }

        //Log.i("FormData", String.valueOf(newIssueLabelsForm));
        disableProcessButton();
        createNewIssueFunc(instanceUrl, instanceToken, repoOwner, repoName, loginUid, newIssueDescriptionForm, newIssueDueDateForm, newIssueMilestoneIdForm, newIssueTitleForm, newIssueAssigneesListForm_);

    }

    public void loadCollaboratorsList() {

        final TinyDB tinyDb = new TinyDB(appCtx);

        final String instanceUrl = tinyDb.getString("instanceUrl");
        final String loginUid = tinyDb.getString("loginUid");
        final String instanceToken = "token " + tinyDb.getString(loginUid + "-token");
        String repoFullName = tinyDb.getString("repoFullName");
        String[] parts = repoFullName.split("/");
        final String repoOwner = parts[0];
        final String repoName = parts[1];

        Call<List<Collaborators>> call = RetrofitClient
                .getInstance(instanceUrl, ctx)
                .getApiInterface()
                .getCollaborators(Authorization.returnAuthentication(ctx, loginUid, instanceToken), repoOwner, repoName);

        call.enqueue(new Callback<List<Collaborators>>() {

            @Override
            public void onResponse(@NonNull Call<List<Collaborators>> call, @NonNull Response<List<Collaborators>> response) {

                if (response.isSuccessful()) {

                    assert response.body() != null;
                    String fullName = "";
                    for (int i = 0; i < response.body().size(); i++) {
                        if(!response.body().get(i).getFull_name().equals("")) {
                            fullName = response.body().get(i).getFull_name();
                        }
                        defaultMentionAdapter.add(
                                new Mention(response.body().get(i).getUsername(), fullName, response.body().get(i).getAvatar_url()));
                    }

                } else {

                    Log.i("onResponse", String.valueOf(response.code()));

                }

            }

            @Override
            public void onFailure(@NonNull Call<List<Collaborators>> call, @NonNull Throwable t) {
                Log.i("onFailure", t.toString());
            }

        });
    }

    private void createNewIssueFunc(final String instanceUrl, final String instanceToken, String repoOwner, String repoName, String loginUid, String newIssueDescriptionForm, String newIssueDueDateForm, int newIssueMilestoneIdForm, String newIssueTitleForm, List<String> newIssueAssigneesListForm) {

        CreateIssue createNewIssueJson = new CreateIssue(loginUid, newIssueDescriptionForm, false, newIssueDueDateForm, newIssueMilestoneIdForm, newIssueTitleForm, newIssueAssigneesListForm, labelsIds);

        Call<JsonElement> call3;

        call3 = RetrofitClient
                .getInstance(instanceUrl, ctx)
                .getApiInterface()
                .createNewIssue(Authorization.returnAuthentication(ctx, loginUid, instanceToken), repoOwner, repoName, createNewIssueJson);

        call3.enqueue(new Callback<JsonElement>() {

            @Override
            public void onResponse(@NonNull Call<JsonElement> call, @NonNull retrofit2.Response<JsonElement> response2) {

                if(response2.isSuccessful()) {
                    if(response2.code() == 201) {

                        //Log.i("isSuccessful1", String.valueOf(response2.body()));
                        TinyDB tinyDb = new TinyDB(appCtx);
                        tinyDb.putBoolean("resumeIssues", true);

                        Toasty.success(ctx, getString(R.string.issueCreated));
                        enableProcessButton();
                        finish();

                    }

                }
                else if(response2.code() == 401) {

                    enableProcessButton();
                    AlertDialogs.authorizationTokenRevokedDialog(ctx, getResources().getString(R.string.alertDialogTokenRevokedTitle),
                            getResources().getString(R.string.alertDialogTokenRevokedMessage),
                            getResources().getString(R.string.alertDialogTokenRevokedCopyNegativeButton),
                            getResources().getString(R.string.alertDialogTokenRevokedCopyPositiveButton));

                }
                else {

                    Toasty.error(ctx, getString(R.string.issueCreatedError));
                    enableProcessButton();
                    //Log.i("isSuccessful2", String.valueOf(response2.body()));

                }

            }

            @Override
            public void onFailure(@NonNull Call<JsonElement> call, @NonNull Throwable t) {
                Log.e("onFailure", t.toString());
                enableProcessButton();
            }
        });

    }

    private void initCloseListener() {

        onClickListener = view -> finish();
    }

    private void getMilestones(String instanceUrl, String instanceToken, String repoOwner, String repoName, String loginUid, int resultLimit) {

        String msState = "open";
        Call<List<Milestones>> call = RetrofitClient
                .getInstance(instanceUrl, ctx)
                .getApiInterface()
                .getMilestones(Authorization.returnAuthentication(ctx, loginUid, instanceToken), repoOwner, repoName, 1, resultLimit, msState);

        call.enqueue(new Callback<List<Milestones>>() {

            @Override
            public void onResponse(@NonNull Call<List<Milestones>> call, @NonNull retrofit2.Response<List<Milestones>> response) {

                if(response.isSuccessful()) {
                    if(response.code() == 200) {

                        List<Milestones> milestonesList_ = response.body();

                        milestonesList.add(new Milestones(0,getString(R.string.issueCreatedNoMilestone)));
                        assert milestonesList_ != null;
                        if(milestonesList_.size() > 0) {
                            for (int i = 0; i < milestonesList_.size(); i++) {

                                //Don't translate "open" is a enum
                                if(milestonesList_.get(i).getState().equals("open")) {
                                    Milestones data = new Milestones(
                                            milestonesList_.get(i).getId(),
                                            milestonesList_.get(i).getTitle()
                                    );
                                    milestonesList.add(data);
                                }

                            }
                        }

                        ArrayAdapter<Milestones> adapter = new ArrayAdapter<>(CreateIssueActivity.this,
                                R.layout.spinner_item, milestonesList);

                        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
	                    viewBinding.newIssueMilestoneSpinner.setAdapter(adapter);
                        enableProcessButton();

                    }
                }

            }

            @Override
            public void onFailure(@NonNull Call<List<Milestones>> call, @NonNull Throwable t) {
                Log.e("onFailure", t.toString());
            }
        });

    }

    private void getCollaborators(String instanceUrl, String instanceToken, String repoOwner, String repoName, String loginUid, String loginFullName) {

        Call<List<Collaborators>> call = RetrofitClient
                .getInstance(instanceUrl, ctx)
                .getApiInterface()
                .getCollaborators(Authorization.returnAuthentication(ctx, loginUid, instanceToken), repoOwner, repoName);

        listOfAssignees.add(new MultiSelectModel(-1, loginFullName));

        call.enqueue(new Callback<List<Collaborators>>() {

            @Override
            public void onResponse(@NonNull Call<List<Collaborators>> call, @NonNull retrofit2.Response<List<Collaborators>> response) {

                if(response.isSuccessful()) {
                    if(response.code() == 200) {

                        List<Collaborators> assigneesList_ = response.body();

                        assert assigneesList_ != null;
                        if(assigneesList_.size() > 0) {
                            for (int i = 0; i < assigneesList_.size(); i++) {

                                /*String assigneesCopy;
                                if(!assigneesList_.get(i).getFull_name().equals("")) {
                                    assigneesCopy = getString(R.string.dialogAssignessText, assigneesList_.get(i).getFull_name(), assigneesList_.get(i).getLogin());
                                }
                                else {
                                    assigneesCopy = assigneesList_.get(i).getLogin();
                                }*/
                                listOfAssignees.add(new MultiSelectModel(assigneesList_.get(i).getId(), assigneesList_.get(i).getLogin().trim()));

                            }
                            assigneesFlag = true;
                        }

                        multiSelectDialog = new MultiSelectDialog()
                                .title(getResources().getString(R.string.newIssueSelectAssigneesListTitle))
                                .titleSize(25)
                                .positiveText(getResources().getString(R.string.okButton))
                                .negativeText(getResources().getString(R.string.cancelButton))
                                .setMinSelectionLimit(0)
                                .setMaxSelectionLimit(listOfAssignees.size())
                                .multiSelectList(listOfAssignees)
                                .onSubmit(new MultiSelectDialog.SubmitCallbackListener() {
                                    @Override
                                    public void onSelected(List<Integer> selectedIds, List<String> selectedNames, String dataString) {

	                                    viewBinding.newIssueAssigneesList.setText(dataString);

                                    }

                                    @Override
                                    public void onCancel() {
                                        //Log.d("multiSelect","Dialog cancelled");

                                    }
                                });

                    }
                }

            }

            @Override
            public void onFailure(@NonNull Call<List<Collaborators>> call, @NonNull Throwable t) {
                Log.e("onFailure", t.toString());
            }
        });

    }

    @Override
    public void onClick(View v) {
        if (v == viewBinding.newIssueAssigneesList) {
            if(assigneesFlag) {
                multiSelectDialog.show(getSupportFragmentManager(), "multiSelectDialog");
            }
            else {
                Toasty.warning(ctx, getResources().getString(R.string.noAssigneesFound));
            }
        }
        else if (v == viewBinding.newIssueDueDate) {

            final Calendar c = Calendar.getInstance();
            int mYear = c.get(Calendar.YEAR);
            final int mMonth = c.get(Calendar.MONTH);
            final int mDay = c.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                    new DatePickerDialog.OnDateSetListener() {

                        @Override
                        public void onDateSet(DatePicker view, int year,
                                              int monthOfYear, int dayOfMonth) {

	                        viewBinding.newIssueDueDate.setText(getString(R.string.setDueDate, year, (monthOfYear + 1), dayOfMonth));

                        }
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
}
