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

public class MyRepositoriesViewModel extends ViewModel {

    private static MutableLiveData<List<Repository>> myReposList;

    public LiveData<List<Repository>> getCurrentUserRepositories(String username, Context ctx, int page, int limit) {

        //if (myReposList == null) {
        myReposList = new MutableLiveData<>();
        loadMyReposList(username, ctx, page, limit);
        //}

        return myReposList;
    }

    public static void loadMyReposList(String username, Context ctx, int page, int limit) {

        Call<List<Repository>> call = RetrofitClient
                .getApiInterface(ctx)
                .userListRepos(username, page, limit);

        call.enqueue(new Callback<List<Repository>>() {

            @Override
            public void onResponse(@NonNull Call<List<Repository>> call, @NonNull Response<List<Repository>> response) {

                if(response.isSuccessful()) {
                    if(response.code() == 200) {
                        myReposList.postValue(response.body());

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
