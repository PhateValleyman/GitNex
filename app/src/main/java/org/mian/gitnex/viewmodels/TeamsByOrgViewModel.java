package org.mian.gitnex.viewmodels;

import android.content.Context;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import org.mian.gitnex.clients.RetrofitClient;
import org.mian.gitnex.models.Teams;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Author M M Arif
 */

public class TeamsByOrgViewModel extends ViewModel {

    private static MutableLiveData<List<Teams>> teamsList;

    public static void loadTeamsByOrgList(String instanceUrl, String token, String orgName, Context ctx) {

        Call<List<Teams>> call = RetrofitClient
                .getInstance(instanceUrl, ctx)
                .getApiInterface()
                .getTeamsByOrg(token, orgName);

        call.enqueue(new Callback<List<Teams>>() {

            @Override
            public void onResponse(@NonNull Call<List<Teams>> call, @NonNull Response<List<Teams>> response) {

                if (response.isSuccessful()) {
                    if (response.code() == 200) {
                        teamsList.postValue(response.body());

                    }
                }

            }

            @Override
            public void onFailure(@NonNull Call<List<Teams>> call, Throwable t) {
                Log.i("onFailure", t.toString());
            }

        });

    }

    public LiveData<List<Teams>> getTeamsByOrg(String instanceUrl, String token, String orgName, Context ctx) {

        teamsList = new MutableLiveData<>();
        loadTeamsByOrgList(instanceUrl, token, orgName, ctx);

        return teamsList;
    }

}
