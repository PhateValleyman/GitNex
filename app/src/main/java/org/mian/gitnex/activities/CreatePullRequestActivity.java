package org.mian.gitnex.activities;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import org.gitnex.tea4j.v2.models.*;
import org.mian.gitnex.R;
import org.mian.gitnex.actions.LabelsActions;
import org.mian.gitnex.adapters.LabelsListAdapter;
import org.mian.gitnex.clients.RetrofitClient;
import org.mian.gitnex.databinding.ActivityCreatePrBinding;
import org.mian.gitnex.databinding.CustomLabelsSelectionDialogBinding;
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

public class CreatePullRequestActivity extends BaseActivity implements LabelsListAdapter.LabelsListAdapterListener {

	private View.OnClickListener onClickListener;
	private ActivityCreatePrBinding viewBinding;
	private int resultLimit = Constants.resultLimitOldGiteaInstances;
	private Dialog dialogLabels;
	private List<Integer> labelsIds = new ArrayList<>();
	private final List<String> assignees = new ArrayList<>();
	private int milestoneId;
	private Date currentDate = null;

	private RepositoryContext repository;

	private LabelsListAdapter labelsAdapter;

	LinkedHashMap<String, Milestone> milestonesList = new LinkedHashMap<>();
	List<String> branchesList = new ArrayList<>();
	List<Label> labelsList = new ArrayList<>();

	@SuppressLint("ClickableViewAccessibility")
	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		viewBinding = ActivityCreatePrBinding.inflate(getLayoutInflater());
		setContentView(viewBinding.getRoot());

		repository = RepositoryContext.fromIntent(getIntent());

		// require gitea 1.12 or higher
		if(getAccount().requiresVersion("1.12.0")) {

			resultLimit = Constants.resultLimitNewGiteaInstances;
		}

		viewBinding.prBody.setOnTouchListener((touchView, motionEvent) -> {

			touchView.getParent().requestDisallowInterceptTouchEvent(true);

			if ((motionEvent.getAction() & MotionEvent.ACTION_UP) != 0 && (motionEvent.getActionMasked() & MotionEvent.ACTION_UP) != 0) {

				touchView.getParent().requestDisallowInterceptTouchEvent(false);
			}
			return false;
		});

		labelsAdapter =  new LabelsListAdapter(labelsList, CreatePullRequestActivity.this, labelsIds);

		ImageView closeActivity = findViewById(R.id.close);

		initCloseListener();
		closeActivity.setOnClickListener(onClickListener);

		viewBinding.prDueDate.setOnClickListener(dueDate ->
			setDueDate()
		);

		disableProcessButton();

		getMilestones(repository.getOwner(), repository.getName(), resultLimit);
		getBranches(repository.getOwner(), repository.getName());

		viewBinding.prLabels.setOnClickListener(prLabels -> showLabels());

		viewBinding.createPr.setOnClickListener(createPr -> processPullRequest());

		if(!repository.getPermissions().isPush()) {
			viewBinding.prDueDateLayout.setVisibility(View.GONE);
			viewBinding.prLabelsLayout.setVisibility(View.GONE);
			viewBinding.milestonesSpinnerLayout.setVisibility(View.GONE);
		}
	}

	private void processPullRequest() {

		String prTitle = String.valueOf(viewBinding.prTitle.getText());
		String prDescription = String.valueOf(viewBinding.prBody.getText());
		String mergeInto = viewBinding.mergeIntoBranchSpinner.getText().toString();
		String pullFrom = viewBinding.pullFromBranchSpinner.getText().toString();

		assignees.add("");

		if (labelsIds.size() == 0) {

			labelsIds.add(0);
		}

		if(prTitle.matches("")) {

			Toasty.error(ctx, getString(R.string.titleError));
		}
		else if(mergeInto.matches("")) {

			Toasty.error(ctx, getString(R.string.mergeIntoError));
		}
		else if(pullFrom.matches("")) {

			Toasty.error(ctx, getString(R.string.pullFromError));
		}
		else if(pullFrom.equals(mergeInto)) {

			Toasty.error(ctx, getString(R.string.sameBranchesError));
		}
		else {

			createPullRequest(prTitle, prDescription, mergeInto, pullFrom, milestoneId, assignees);
		}
	}

	private void createPullRequest(String prTitle, String prDescription, String mergeInto, String pullFrom, int milestoneId, List<String> assignees) {

		ArrayList<Long> labelIds = new ArrayList<>();
		for(Integer i : labelsIds) {
			labelIds.add((long) i);
		}

		CreatePullRequestOption createPullRequest = new CreatePullRequestOption();
		createPullRequest.setTitle(prTitle);
		createPullRequest.setMilestone((long) milestoneId);
		createPullRequest.setAssignees(assignees);
		createPullRequest.setBody(prDescription);
		createPullRequest.setBase(mergeInto);
		createPullRequest.setHead(pullFrom);
		createPullRequest.setLabels(labelIds);
		createPullRequest.setDueDate(currentDate);

		Call<PullRequest> transferCall = RetrofitClient
			.getApiInterface(ctx)
			.repoCreatePullRequest(repository.getOwner(), repository.getName(), createPullRequest);

		transferCall.enqueue(new Callback<PullRequest>() {

			@Override
			public void onResponse(@NonNull Call<PullRequest> call, @NonNull retrofit2.Response<PullRequest> response) {

				disableProcessButton();

				if (response.code() == 201) {

					Toasty.success(ctx, getString(R.string.prCreateSuccess));
					finish();
				}
				else if (response.code() == 409 && response.message().equals("Conflict")) {

					enableProcessButton();
					Toasty.error(ctx, getString(R.string.prAlreadyExists));
				}
				else if (response.code() == 404) {

					enableProcessButton();
					Toasty.error(ctx, getString(R.string.apiNotFound));
				}
				else {

					enableProcessButton();
					Toasty.error(ctx, getString(R.string.genericError));
				}
			}

			@Override
			public void onFailure(@NonNull Call<PullRequest> call, @NonNull Throwable t) {

				enableProcessButton();
				Toasty.error(ctx, getString(R.string.genericServerResponseError));
			}
		});
	}

	@Override
	public void labelsInterface(List<String> data) {

		String labelsSetter = String.valueOf(data);
		viewBinding.prLabels.setText(labelsSetter.replace("]", "").replace("[", ""));
	}

	@Override
	public void labelsIdsInterface(List<Integer> data) {

		labelsIds = data;
	}

	private void showLabels() {

		dialogLabels = new Dialog(ctx, R.style.ThemeOverlay_MaterialComponents_Dialog_Alert);

		if (dialogLabels.getWindow() != null) {

			dialogLabels.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
		}

		org.mian.gitnex.databinding.CustomLabelsSelectionDialogBinding labelsBinding = CustomLabelsSelectionDialogBinding
			.inflate(LayoutInflater.from(ctx));

		View view = labelsBinding.getRoot();
		dialogLabels.setContentView(view);

		labelsBinding.save.setOnClickListener(editProperties -> dialogLabels.dismiss());

		dialogLabels.show();
		LabelsActions.getRepositoryLabels(ctx, repository.getOwner(), repository.getName(), labelsList, dialogLabels, labelsAdapter, labelsBinding);
	}

	private void getBranches(String repoOwner, String repoName) {

		Call<List<Branch>> call = RetrofitClient
			.getApiInterface(ctx)
			.repoListBranches(repoOwner, repoName, null, null);

		call.enqueue(new Callback<List<Branch>>() {

			@Override
			public void onResponse(@NonNull Call<List<Branch>> call, @NonNull retrofit2.Response<List<Branch>> response) {

				if(response.isSuccessful()) {

					if(response.code() == 200) {

						List<Branch> branchesList_ = response.body();
						assert branchesList_ != null;

						for(Branch i : branchesList_) {
							branchesList.add(i.getName());
						}

						ArrayAdapter<String> adapter = new ArrayAdapter<>(CreatePullRequestActivity.this,
							R.layout.list_spinner_items, branchesList);

						viewBinding.mergeIntoBranchSpinner.setAdapter(adapter);
						viewBinding.pullFromBranchSpinner.setAdapter(adapter);
						enableProcessButton();

					}
				}

			}

			@Override
			public void onFailure(@NonNull Call<List<Branch>> call, @NonNull Throwable t) {

				Toasty.error(ctx, getString(R.string.genericServerResponseError));
			}
		});

	}

	private void getMilestones(String repoOwner, String repoName, int resultLimit) {

		String msState = "open";
		Call<List<Milestone>> call = RetrofitClient
			.getApiInterface(ctx)
			.issueGetMilestonesList(repoOwner, repoName, msState, null, 1, resultLimit);

		call.enqueue(new Callback<List<Milestone>>() {

			@Override
			public void onResponse(@NonNull Call<List<Milestone>> call, @NonNull retrofit2.Response<List<Milestone>> response) {

				if(response.code() == 200) {

					List<Milestone> milestonesList_ = response.body();

					milestonesList.put(getString(R.string.issueCreatedNoMilestone), new Milestone().id(0L).title(getString(R.string.issueCreatedNoMilestone)));
					assert milestonesList_ != null;

					if(milestonesList_.size() > 0) {

						for(Milestone milestone : milestonesList_) {

							//Don't translate "open" is a enum
							if(milestone.getState().equals("open")) {
								milestonesList.put(milestone.getTitle(), milestone);
							}
						}
					}

					ArrayAdapter<String> adapter = new ArrayAdapter<>(CreatePullRequestActivity.this,
						R.layout.list_spinner_items, new ArrayList<>(milestonesList.keySet()));

					viewBinding.milestonesSpinner.setAdapter(adapter);
					enableProcessButton();

					viewBinding.milestonesSpinner.setOnItemClickListener ((parent, view, position, id) -> {
							if(position == 0) {
								milestoneId = 0;
							}
							else if(view instanceof TextView) {
								milestoneId = Math.toIntExact(
									Objects.requireNonNull(milestonesList.get(((TextView) view).getText().toString())).getId());
							}
						}
					);

				}
			}

			@Override
			public void onFailure(@NonNull Call<List<Milestone>> call, @NonNull Throwable t) {

				Toasty.error(ctx, getString(R.string.genericServerResponseError));
			}
		});

	}

	private void setDueDate() {

		final Calendar c = Calendar.getInstance();
		int mYear = c.get(Calendar.YEAR);
		final int mMonth = c.get(Calendar.MONTH);
		final int mDay = c.get(Calendar.DAY_OF_MONTH);

		DatePickerDialog datePickerDialog = new DatePickerDialog(this,
			(view, year, monthOfYear, dayOfMonth) -> {
			viewBinding.prDueDate.setText(getString(R.string.setDueDate, year, (monthOfYear + 1), dayOfMonth));
			currentDate = new Date(year - 1900, monthOfYear, dayOfMonth);
			}, mYear, mMonth, mDay);
		datePickerDialog.show();
	}

	private void initCloseListener() {

		onClickListener = view -> finish();
	}

	private void disableProcessButton() {

		viewBinding.createPr.setEnabled(false);
	}

	private void enableProcessButton() {

		viewBinding.createPr.setEnabled(true);
	}

	@Override
	public void onResume() {
		super.onResume();
		repository.checkAccountSwitch(this);
	}
}
