package org.mian.gitnex.viewmodels;

import android.content.Context;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import org.gitnex.tea4j.v2.models.Email;
import org.mian.gitnex.clients.RetrofitClient;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Author M M Arif
 */

public class ProfileEmailsViewModel extends ViewModel {

    private static MutableLiveData<List<Email>> emailsList;

    public LiveData<List<Email>> getEmailsList(Context ctx) {

        emailsList = new MutableLiveData<>();
        loadEmailsList(ctx);

        return emailsList;
    }

    public static void loadEmailsList(Context ctx) {

        Call<List<Email>> call = RetrofitClient
                .getApiInterface(ctx)
                .userListEmails();

        call.enqueue(new Callback<List<Email>>() {

            @Override
            public void onResponse(@NonNull Call<List<Email>> call, @NonNull Response<List<Email>> response) {

                if (response.isSuccessful()) {
                    emailsList.postValue(response.body());
                } else {
                    Log.i("onResponse", String.valueOf(response.code()));
                }

            }

            @Override
            public void onFailure(@NonNull Call<List<Email>> call, @NonNull Throwable t) {
                Log.i("onFailure", t.toString());
            }

        });
    }

}
