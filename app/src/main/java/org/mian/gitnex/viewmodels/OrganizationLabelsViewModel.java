package org.mian.gitnex.viewmodels;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import org.gitnex.tea4j.v2.models.Label;
import org.mian.gitnex.clients.RetrofitClient;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Author M M Arif
 */

public class OrganizationLabelsViewModel extends ViewModel {

	private static MutableLiveData<List<Label>> orgLabelsList;

	public LiveData<List<Label>> getOrgLabelsList(String token, String owner, Context ctx, ProgressBar progressBar, TextView noData) {

		orgLabelsList = new MutableLiveData<>();
		loadOrgLabelsList(token, owner, ctx, progressBar, noData);

		return orgLabelsList;
	}

	public static void loadOrgLabelsList(String token, String owner, Context ctx, ProgressBar progressBar, TextView noData) {

		Call<List<Label>> call = RetrofitClient
			.getApiInterface(ctx)
			.orgListLabels(owner, null, null);

		call.enqueue(new Callback<List<Label>>() {

			@Override
			public void onResponse(@NonNull Call<List<Label>> call, @NonNull Response<List<Label>> response) {

				if(response.isSuccessful()) {

					orgLabelsList.postValue(response.body());
				}
				else {

					progressBar.setVisibility(View.GONE);
					noData.setVisibility(View.VISIBLE);
					Log.i("onResponse-org-labels", String.valueOf(response.code()));
				}
			}

			@Override
			public void onFailure(@NonNull Call<List<Label>> call, @NonNull Throwable t) {
				Log.i("onFailure", t.toString());
			}

		});

	}
}
