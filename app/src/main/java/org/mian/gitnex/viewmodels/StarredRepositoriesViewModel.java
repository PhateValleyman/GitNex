package org.mian.gitnex.viewmodels;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import org.mian.gitnex.clients.RetrofitClient;
import org.mian.gitnex.models.UserRepositories;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Author M M Arif
 */

public class StarredRepositoriesViewModel extends ViewModel {

    private static MutableLiveData<List<UserRepositories>> reposList;

    public static void loadStarredReposList(String instanceUrl, String token, Context ctx) {

        Call<List<UserRepositories>> call = RetrofitClient
                .getInstance(instanceUrl, ctx)
                .getApiInterface()
                .getUserStarredRepos(token);

        call.enqueue(new Callback<List<UserRepositories>>() {

            @Override
            public void onResponse(@NonNull Call<List<UserRepositories>> call, @NonNull Response<List<UserRepositories>> response) {

                if (response.isSuccessful()) {
                    if (response.code() == 200) {
                        reposList.postValue(response.body());

                    }
                }

            }

            @Override
            public void onFailure(@NonNull Call<List<UserRepositories>> call, Throwable t) {
                Log.i("onFailure", t.toString());
            }

        });

    }

    public LiveData<List<UserRepositories>> getUserStarredRepositories(String instanceUrl, String token, Context ctx) {

        reposList = new MutableLiveData<>();
        loadStarredReposList(instanceUrl, token, ctx);

        return reposList;
    }

}
