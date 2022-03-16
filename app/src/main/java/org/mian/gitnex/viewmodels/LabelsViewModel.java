package org.mian.gitnex.viewmodels;

import android.content.Context;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import org.gitnex.tea4j.v2.models.Label;
import org.mian.gitnex.clients.RetrofitClient;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Author M M Arif
 */

public class LabelsViewModel extends ViewModel {

    private static MutableLiveData<List<Label>> labelsList;

    public LiveData<List<Label>> getLabelsList(String owner, String repo, Context ctx) {

        labelsList = new MutableLiveData<>();
        loadLabelsList(owner, repo, ctx);

        return labelsList;
    }

    public static void loadLabelsList(String owner, String repo, Context ctx) {

        Call<List<Label>> call = RetrofitClient
                .getApiInterface(ctx)
                .issueListLabels(owner, repo, null, null);

        call.enqueue(new Callback<List<Label>>() {

            @Override
            public void onResponse(@NonNull Call<List<Label>> call, @NonNull Response<List<Label>> response) {

                if(response.isSuccessful()) {
                    labelsList.postValue(response.body());
                }
                else {
                    Log.i("onResponse", String.valueOf(response.code()));
                }

            }

            @Override
            public void onFailure(@NonNull Call<List<Label>> call, Throwable t) {
                Log.i("onFailure", t.toString());
            }

        });

    }

}
