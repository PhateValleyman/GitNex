package org.mian.gitnex.viewmodels;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import org.gitnex.tea4j.v2.models.Issue;
import org.mian.gitnex.R;
import org.mian.gitnex.activities.BaseActivity;
import org.mian.gitnex.adapters.ExploreIssuesAdapter;
import org.mian.gitnex.clients.RetrofitClient;
import org.mian.gitnex.helpers.Constants;
import org.mian.gitnex.helpers.Toasty;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * @author M M Arif
 */

public class IssuesViewModel extends ViewModel {

	private static MutableLiveData<List<Issue>> issuesList;
	private static int resultLimit = Constants.resultLimitOldGiteaInstances;

	public LiveData<List<Issue>> getIssuesList(String searchKeyword, String type, Boolean created, String state, Context ctx) {

		issuesList = new MutableLiveData<>();

		// if gitea is 1.12 or higher use the new limit
		if(((BaseActivity) ctx).getAccount().requiresVersion("1.12.0")) {
			resultLimit = Constants.resultLimitNewGiteaInstances;
		}

		loadIssuesList(searchKeyword, type, created, state, ctx);

		return issuesList;
	}

	public static void loadIssuesList(String searchKeyword, String type, Boolean created, String state, Context ctx) {

		Call<List<Issue>> call = RetrofitClient
			.getApiInterface(ctx)
			.issueSearchIssues(state, null, null, searchKeyword, null, type, null, null,
				null, created, null, null, null, null, 1, resultLimit);

		call.enqueue(new Callback<>() {

			@Override
			public void onResponse(@NonNull Call<List<Issue>> call, @NonNull Response<List<Issue>> response) {

				if(response.isSuccessful()) {
					issuesList.postValue(response.body());
				}
				else {
					Toasty.error(ctx, ctx.getString(R.string.genericError));
				}
			}

			@Override
			public void onFailure(@NonNull Call<List<Issue>> call, @NonNull Throwable t) {

				Toasty.error(ctx, ctx.getString(R.string.genericServerResponseError));
			}
		});
	}

	public static void loadMoreIssues(String searchKeyword, String type, Boolean created, String state, int page, Context ctx, ExploreIssuesAdapter adapter) {

		Call<List<Issue>> call = RetrofitClient
			.getApiInterface(ctx)
			.issueSearchIssues(state, null, null, searchKeyword, null, type, null, null,
				null, created, null, null, null, null, page, resultLimit);

		call.enqueue(new Callback<>() {

			@Override
			public void onResponse(@NonNull Call<List<Issue>> call, @NonNull Response<List<Issue>> response) {

				if(response.isSuccessful()) {

					List<Issue> list = issuesList.getValue();
					assert list != null;
					assert response.body() != null;

					if(response.body().size() != 0) {
						list.addAll(response.body());
						adapter.updateList(list);
					}
					else {
						adapter.setMoreDataAvailable(false);
					}
				}
				else {
					Toasty.error(ctx, ctx.getString(R.string.genericError));
				}
			}

			@Override
			public void onFailure(@NonNull Call<List<Issue>> call, @NonNull Throwable t) {

				Toasty.error(ctx, ctx.getString(R.string.genericServerResponseError));
			}
		});
	}
}
