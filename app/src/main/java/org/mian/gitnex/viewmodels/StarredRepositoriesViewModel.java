package org.mian.gitnex.viewmodels;

import android.content.Context;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import org.gitnex.tea4j.v2.models.Repository;
import org.mian.gitnex.clients.RetrofitClient;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Author M M Arif
 */

public class StarredRepositoriesViewModel extends ViewModel {

    private static MutableLiveData<List<Repository>> reposList;

    public LiveData<List<Repository>> getUserStarredRepositories(Context ctx, int page, int limit) {

        reposList = new MutableLiveData<>();
        loadStarredReposList(ctx, page, limit);

        return reposList;
    }

    public static void loadStarredReposList(Context ctx, int page, int limit) {

        Call<List<Repository>> call = RetrofitClient
                .getApiInterface(ctx)
                .userCurrentListStarred(page, limit);

        call.enqueue(new Callback<List<Repository>>() {

            @Override
            public void onResponse(@NonNull Call<List<Repository>> call, @NonNull Response<List<Repository>> response) {

                if(response.isSuccessful()) {
                    if(response.code() == 200) {
                        reposList.postValue(response.body());

                    }
                }

            }

            @Override
            public void onFailure(@NonNull Call<List<Repository>> call, Throwable t) {
                Log.i("onFailure", t.toString());
            }

        });

    }

}
