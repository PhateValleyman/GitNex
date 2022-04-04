package org.mian.gitnex.viewmodels;

import android.content.Context;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import org.gitnex.tea4j.v2.models.User;
import org.mian.gitnex.clients.RetrofitClient;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Author M M Arif
 */

public class RepoStargazersViewModel extends ViewModel {

    private static MutableLiveData<List<User>> stargazersList;

    public LiveData<List<User>> getRepoStargazers(String repoOwner, String repoName, Context ctx) {

        stargazersList = new MutableLiveData<>();
        loadRepoStargazers(repoOwner, repoName, ctx);

        return stargazersList;
    }

    private static void loadRepoStargazers(String repoOwner, String repoName, Context ctx) {

        Call<List<User>> call = RetrofitClient
                .getApiInterface(ctx)
                .repoListStargazers(repoOwner, repoName, null, null);

        call.enqueue(new Callback<List<User>>() {

            @Override
            public void onResponse(@NonNull Call<List<User>> call, @NonNull Response<List<User>> response) {

                if(response.isSuccessful()) {
                	stargazersList.postValue(response.body());
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<User>> call, Throwable t) {
                Log.i("onFailure", t.toString());
            }

        });
    }
}
