package org.mian.gitnex.actions;

import android.app.Dialog;
import android.content.Context;
import android.util.Log;
import android.view.View;
import androidx.annotation.NonNull;
import org.mian.gitnex.R;
import org.mian.gitnex.adapters.LabelsListAdapter;
import org.mian.gitnex.clients.RetrofitClient;
import org.mian.gitnex.databinding.CustomLabelsSelectionDialogBinding;
import org.mian.gitnex.helpers.Authorization;
import org.mian.gitnex.helpers.Toasty;
import org.mian.gitnex.models.Labels;
import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;

/**
 * Author M M Arif
 */

public class LabelsActions {

	public static void getCurrentIssueLabels(Context ctx, String instanceUrl, String loginUid, String instanceToken, String repoOwner, String repoName, int issueIndex, ArrayList<Integer> currentLabelsIds) {

		Call<List<Labels>> callSingleIssueLabels = RetrofitClient
			.getInstance(instanceUrl, ctx)
			.getApiInterface()
			.getIssueLabels(Authorization.returnAuthentication(ctx, loginUid, instanceToken), repoOwner, repoName, issueIndex);

		callSingleIssueLabels.enqueue(new Callback<List<Labels>>() {

			@Override
			public void onResponse(@NonNull Call<List<Labels>> call, @NonNull retrofit2.Response<List<Labels>> response) {

				if(response.code() == 200) {

					List<Labels> issueLabelsList = response.body();

					assert issueLabelsList != null;

					if(issueLabelsList.size() > 0) {

						for (int i = 0; i < issueLabelsList.size(); i++) {

							currentLabelsIds.add(issueLabelsList.get(i).getId());
						}
					}

				}
			}

			@Override
			public void onFailure(@NonNull Call<List<Labels>> call, @NonNull Throwable t) {

				Log.e("onFailure", t.toString());
			}

		});
	}

	public static void getRepositoryLabels(Context ctx, String instanceUrl, String instanceToken, String repoOwner, String repoName, List<Labels> labelsList, Dialog dialogLabels, LabelsListAdapter labelsAdapter, CustomLabelsSelectionDialogBinding labelsBinding) {

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

						dialogLabels.show();

						for (int i = 0; i < labelsList_.size(); i++) {

							labelsList.add(new Labels(labelsList_.get(i).getId(), labelsList_.get(i).getName(), labelsList_.get(i).getColor()));
						}
					}
					else {

						dialogLabels.dismiss();
						Toasty.warning(ctx, ctx.getResources().getString(R.string.noLabelsFound));
					}

					labelsBinding.labelsRecyclerView.setAdapter(labelsAdapter);

				}
				else {

					Toasty.error(ctx, ctx.getResources().getString(R.string.genericError));
				}

			}

			@Override
			public void onFailure(@NonNull Call<List<Labels>> call, @NonNull Throwable t) {

				Toasty.error(ctx, ctx.getResources().getString(R.string.genericServerResponseError));
			}
		});

	}

}
