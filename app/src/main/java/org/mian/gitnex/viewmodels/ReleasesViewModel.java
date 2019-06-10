package org.mian.gitnex.viewmodels;

import android.util.Log;
import org.mian.gitnex.clients.RetrofitClient;
import org.mian.gitnex.models.Releases;
import java.util.List;
import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Author M M Arif
 */

public class ReleasesViewModel extends ViewModel {

    private static MutableLiveData<List<Releases>> releasesList;

    public LiveData<List<Releases>> getReleasesList(String instanceUrl, String token, String owner, String repo) {

        releasesList = new MutableLiveData<>();
        loadReleasesList(instanceUrl, token, owner, repo);

        return releasesList;
    }

    public static void loadReleasesList(String instanceUrl, String token, String owner, String repo) {

        Call<List<Releases>> call = RetrofitClient
                .getInstance(instanceUrl)
                .getApiInterface()
                .getReleases(token, owner, repo);

        call.enqueue(new Callback<List<Releases>>() {

            @Override
            public void onResponse(@NonNull Call<List<Releases>> call, @NonNull Response<List<Releases>> response) {

                if (response.isSuccessful()) {
                    releasesList.postValue(response.body());
                } else {
                    Log.i("onResponse", String.valueOf(response.code()));
                }

            }

            @Override
            public void onFailure(@NonNull Call<List<Releases>> call, Throwable t) {
                Log.i("onFailure", t.toString());
            }

        });
    }

}
