package org.mian.gitnex.viewmodels;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import org.mian.gitnex.R;
import org.gitnex.tea4j.v2.models.User;
import org.mian.gitnex.clients.RetrofitClient;
import org.mian.gitnex.helpers.Toasty;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * @author M M Arif
 */

public class CollaboratorsViewModel extends ViewModel {

    private static MutableLiveData<List<User>> collaboratorsList;

    public LiveData<List<User>> getCollaboratorsList(String owner, String repo, Context ctx) {

        collaboratorsList = new MutableLiveData<>();
        loadCollaboratorsListList(owner, repo, ctx);

        return collaboratorsList;
    }

    private static void loadCollaboratorsListList(String owner, String repo, Context ctx) {

        Call<List<User>> call = RetrofitClient
                .getApiInterface(ctx)
                .repoListCollaborators(owner, repo, null, null);

        call.enqueue(new Callback<>() {

            @Override
            public void onResponse(@NonNull Call<List<User>> call, @NonNull Response<List<User>> response) {

		        if(response.isSuccessful()) {
			        collaboratorsList.postValue(response.body());
		        }
		        else {
			        Toasty.error(ctx, ctx.getString(R.string.genericError));
		        }
	        }

	        @Override
	        public void onFailure(@NonNull Call<List<User>> call, @NonNull Throwable t) {

		        Toasty.error(ctx, ctx.getString(R.string.genericServerResponseError));
	        }
        });
    }
}
