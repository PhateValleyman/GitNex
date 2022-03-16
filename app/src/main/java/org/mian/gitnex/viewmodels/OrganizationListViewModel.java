package org.mian.gitnex.viewmodels;

import android.content.Context;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import org.gitnex.tea4j.v2.models.Organization;
import org.mian.gitnex.clients.RetrofitClient;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Author M M Arif
 */

public class OrganizationListViewModel extends ViewModel {

    private static MutableLiveData<List<Organization>> orgsList;

    public LiveData<List<Organization>> getUserOrgs(Context ctx) {

        //if (orgsList == null) {
            orgsList = new MutableLiveData<>();
            loadOrgsList(ctx);
        //}

        return orgsList;
    }

    public static void loadOrgsList(Context ctx) {

        Call<List<Organization>> call = RetrofitClient
                .getApiInterface(ctx)
                .orgListCurrentUserOrgs(1, 50);

        call.enqueue(new Callback<List<Organization>>() {

            @Override
            public void onResponse(@NonNull Call<List<Organization>> call, @NonNull Response<List<Organization>> response) {

                if(response.isSuccessful()) {
                    if(response.code() == 200) {
                        orgsList.postValue(response.body());

                    }
                }

            }

            @Override
            public void onFailure(@NonNull Call<List<Organization>> call, Throwable t) {
                Log.i("onFailure", t.toString());
            }

        });

    }

}
