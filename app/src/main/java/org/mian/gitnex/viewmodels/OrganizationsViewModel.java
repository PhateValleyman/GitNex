package org.mian.gitnex.viewmodels;

import android.content.Context;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import org.gitnex.tea4j.v2.models.Organization;
import org.mian.gitnex.adapters.OrganizationsListAdapter;
import org.mian.gitnex.clients.RetrofitClient;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * @author M M Arif
 */

public class OrganizationsViewModel extends ViewModel {

    private static MutableLiveData<List<Organization>> orgList;

    public LiveData<List<Organization>> getUserOrg(int page, int resultLimit, Context ctx) {

	    orgList = new MutableLiveData<>();
    	loadOrgList(page, resultLimit, ctx);

        return orgList;
    }

    public static void loadOrgList(int page, int resultLimit, Context ctx) {

        Call<List<Organization>> call = RetrofitClient
                .getApiInterface(ctx)
                .orgListCurrentUserOrgs(page, resultLimit);

        call.enqueue(new Callback<>() {

	        @Override
	        public void onResponse(@NonNull Call<List<Organization>> call, @NonNull Response<List<Organization>> response) {

		        if(response.isSuccessful()) {
			        if(response.code() == 200) {
				        orgList.postValue(response.body());
			        }
		        }
	        }

	        @Override
	        public void onFailure(@NonNull Call<List<Organization>> call, @NonNull Throwable t) {

		        Log.e("onFailure", t.toString());
	        }

        });
    }

	public static void loadMoreOrgList(int page, int resultLimit, Context ctx, OrganizationsListAdapter adapter) {

		Call<List<Organization>> call = RetrofitClient
			.getApiInterface(ctx)
			.orgListCurrentUserOrgs(page, resultLimit);

		call.enqueue(new Callback<>() {

			@Override
			public void onResponse(@NonNull Call<List<Organization>> call, @NonNull Response<List<Organization>> response) {

				if(response.isSuccessful()) {
					List<Organization> list = orgList.getValue();
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
					Log.e("onResponse", String.valueOf(response.code()));
				}
			}

			@Override
			public void onFailure(@NonNull Call<List<Organization>> call, @NonNull Throwable t) {

				Log.e("onFailure", t.toString());
			}

		});
	}
}
