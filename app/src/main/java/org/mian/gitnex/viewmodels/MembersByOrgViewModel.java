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

public class MembersByOrgViewModel extends ViewModel {

    private static MutableLiveData<List<User>> membersList;

    public LiveData<List<User>> getMembersList(String token, String owner, Context ctx) {

        membersList = new MutableLiveData<>();
        loadMembersList(token, owner, ctx);

        return membersList;
    }

    private static void loadMembersList(String token, String owner, Context ctx) {

        Call<List<User>> call = RetrofitClient
                .getApiInterface(ctx)
                .orgListMembers(owner, null, null);

        call.enqueue(new Callback<List<User>>() {

            @Override
            public void onResponse(@NonNull Call<List<User>> call, @NonNull Response<List<User>> response) {

                if (response.isSuccessful()) {
                    membersList.postValue(response.body());
                } else {
                    Log.i("onResponse", String.valueOf(response.code()));
                }

            }

            @Override
            public void onFailure(@NonNull Call<List<User>> call, Throwable t) {
                Log.i("onFailure", t.toString());
            }

        });
    }

}
