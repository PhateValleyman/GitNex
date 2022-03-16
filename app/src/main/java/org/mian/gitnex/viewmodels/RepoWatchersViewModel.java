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

public class RepoWatchersViewModel extends ViewModel {

    private static MutableLiveData<List<User>> watchersList;

    public LiveData<List<User>> getRepoWatchers(String repoOwner, String repoName, Context ctx) {

        watchersList = new MutableLiveData<>();
        loadRepoWatchers(repoOwner, repoName, ctx);

        return watchersList;
    }

    private static void loadRepoWatchers(String repoOwner, String repoName, Context ctx) {

        Call<List<User>> call = RetrofitClient
                .getApiInterface(ctx)
                .repoListSubscribers(repoOwner, repoName, null, null);

        call.enqueue(new Callback<List<User>>() {

            @Override
            public void onResponse(@NonNull Call<List<User>> call, @NonNull Response<List<User>> response) {

                if(response.isSuccessful()) {
                    if(response.code() == 200) {
                        watchersList.postValue(response.body());

                    }
                }

            }

            @Override
            public void onFailure(@NonNull Call<List<User>> call, Throwable t) {
                Log.i("onFailure", t.toString());
            }

        });

    }
}
