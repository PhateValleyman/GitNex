package org.mian.gitnex.activities;

import androidx.annotation.NonNull;
import retrofit2.Call;
import retrofit2.Callback;
import android.app.DatePickerDialog;
import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import org.mian.gitnex.R;
import org.mian.gitnex.clients.RetrofitClient;
import org.mian.gitnex.helpers.AlertDialogs;
import org.mian.gitnex.helpers.Authorization;
import org.mian.gitnex.helpers.VersionCheck;
import org.mian.gitnex.helpers.Toasty;
import org.mian.gitnex.models.Milestones;
import org.mian.gitnex.util.AppUtil;
import org.mian.gitnex.util.TinyDB;
import java.util.Calendar;

/**
 * Author M M Arif
 */

public class CreateMilestoneActivity extends BaseActivity implements View.OnClickListener {

    private EditText milestoneDueDate;
    private View.OnClickListener onClickListener;
    private EditText milestoneTitle;
    private EditText milestoneDescription;
    private Button createNewMilestoneButton;
    final Context ctx = this;
    final Context appCtx = getApplicationContext();

    @Override
    protected int getLayoutResourceId(){
        return R.layout.activity_new_milestone;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        boolean connToInternet = AppUtil.haveNetworkConnection(appCtx);

        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

        milestoneDueDate = findViewById(R.id.milestoneDueDate);
        ImageView closeActivity = findViewById(R.id.close);
        createNewMilestoneButton = findViewById(R.id.createNewMilestoneButton);
        milestoneTitle = findViewById(R.id.milestoneTitle);
        milestoneDescription = findViewById(R.id.milestoneDescription);

        milestoneTitle.requestFocus();
        assert imm != null;
        imm.showSoftInput(milestoneTitle, InputMethodManager.SHOW_IMPLICIT);

        initCloseListener();
        closeActivity.setOnClickListener(onClickListener);
        milestoneDueDate.setOnClickListener(this);

        if(!connToInternet) {

            createNewMilestoneButton.setEnabled(false);
            GradientDrawable shape =  new GradientDrawable();
            shape.setCornerRadius( 8 );
            shape.setColor(getResources().getColor(R.color.hintColor));
            createNewMilestoneButton.setBackground(shape);

        } else {

            createNewMilestoneButton.setOnClickListener(createMilestoneListener);

        }

    }

    private View.OnClickListener createMilestoneListener = new View.OnClickListener() {
        public void onClick(View v) {
            processNewMilestone();
        }
    };

    private void processNewMilestone() {

        boolean connToInternet = AppUtil.haveNetworkConnection(appCtx);
        AppUtil appUtil = new AppUtil();
        TinyDB tinyDb = new TinyDB(appCtx);
        String repoFullName = tinyDb.getString("repoFullName");
        String[] parts = repoFullName.split("/");
        final String repoOwner = parts[0];
        final String repoName = parts[1];
        final String instanceUrl = tinyDb.getString("instanceUrl");
        final String loginUid = tinyDb.getString("loginUid");
        final String instanceToken = "token " + tinyDb.getString(loginUid + "-token");
        //String appLocale = tinyDb.getString("locale");

        String newMilestoneTitle = milestoneTitle.getText().toString();
        String newMilestoneDescription = milestoneDescription.getText().toString();
        String newMilestoneDueDate = milestoneDueDate.getText().toString();

        if(!connToInternet) {

            Toasty.info(ctx, getResources().getString(R.string.checkNetConnection));
            return;

        }

        if(newMilestoneTitle.equals("")) {

            Toasty.info(ctx, getString(R.string.milestoneNameErrorEmpty));
            return;

        }

        if(!newMilestoneDescription.equals("")) {
            if (appUtil.charactersLength(newMilestoneDescription) > 255) {

                Toasty.info(ctx, getString(R.string.milestoneDescError));
                return;

            }
        }

        String finalMilestoneDueDate = null;
        if(!newMilestoneDueDate.isEmpty()) {
            finalMilestoneDueDate = (AppUtil.customDateCombine(AppUtil.customDateFormat(newMilestoneDueDate)));
        } else if (VersionCheck.compareVersion("1.10.0", tinyDb.getString("giteaVersion")) > 1) {
            // if Gitea version is less than 1.10.0 DueDate is required
            Toasty.info(ctx, getString(R.string.milestoneDateEmpty));
            return;
        }

        disableProcessButton();
        createNewMilestone(instanceUrl, Authorization.returnAuthentication(ctx, loginUid, instanceToken), repoOwner, repoName, newMilestoneTitle, newMilestoneDescription, finalMilestoneDueDate);

    }

    private void createNewMilestone(final String instanceUrl, final String token, String repoOwner, String repoName, String newMilestoneTitle, String newMilestoneDescription, String newMilestoneDueDate) {

        Milestones createMilestone = new Milestones(newMilestoneDescription, newMilestoneTitle, newMilestoneDueDate);

        Call<Milestones> call;

        call = RetrofitClient
                .getInstance(instanceUrl, ctx)
                .getApiInterface()
                .createMilestone(token, repoOwner, repoName, createMilestone);

        call.enqueue(new Callback<Milestones>() {

            @Override
            public void onResponse(@NonNull Call<Milestones> call, @NonNull retrofit2.Response<Milestones> response) {

                if(response.isSuccessful()) {
                    if(response.code() == 201) {

                        TinyDB tinyDb = new TinyDB(appCtx);
                        tinyDb.putBoolean("milestoneCreated", true);
                        Toasty.info(ctx, getString(R.string.milestoneCreated));
                        enableProcessButton();
                        finish();

                    }
                }
                else if(response.code() == 401) {

                    enableProcessButton();
                    AlertDialogs.authorizationTokenRevokedDialog(ctx, getResources().getString(R.string.alertDialogTokenRevokedTitle),
                            getResources().getString(R.string.alertDialogTokenRevokedMessage),
                            getResources().getString(R.string.alertDialogTokenRevokedCopyNegativeButton),
                            getResources().getString(R.string.alertDialogTokenRevokedCopyPositiveButton));

                }
                else {

                    enableProcessButton();
                    Toasty.info(ctx, getString(R.string.milestoneCreatedError));

                }

            }

            @Override
            public void onFailure(@NonNull Call<Milestones> call, @NonNull Throwable t) {
                Log.e("onFailure", t.toString());
                enableProcessButton();
            }
        });

    }

    @Override
    public void onClick(View v) {

        if (v == milestoneDueDate) {

            final Calendar c = Calendar.getInstance();
            int mYear = c.get(Calendar.YEAR);
            final int mMonth = c.get(Calendar.MONTH);
            final int mDay = c.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                    new DatePickerDialog.OnDateSetListener() {

                        @Override
                        public void onDateSet(DatePicker view, int year,
                                              int monthOfYear, int dayOfMonth) {

                            milestoneDueDate.setText(getString(R.string.setDueDate, year, (monthOfYear + 1), dayOfMonth));

                        }
                    }, mYear, mMonth, mDay);
            datePickerDialog.show();
        }

    }

    private void initCloseListener() {
        onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        };
    }

    private void disableProcessButton() {

        createNewMilestoneButton.setEnabled(false);
        GradientDrawable shape =  new GradientDrawable();
        shape.setCornerRadius( 8 );
        shape.setColor(getResources().getColor(R.color.hintColor));
        createNewMilestoneButton.setBackground(shape);

    }

    private void enableProcessButton() {

        createNewMilestoneButton.setEnabled(true);
        GradientDrawable shape =  new GradientDrawable();
        shape.setCornerRadius( 8 );
        shape.setColor(getResources().getColor(R.color.btnBackground));
        createNewMilestoneButton.setBackground(shape);

    }

}
