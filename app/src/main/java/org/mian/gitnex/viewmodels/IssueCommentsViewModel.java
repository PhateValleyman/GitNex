package org.mian.gitnex.viewmodels;

import android.content.Context;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import org.gitnex.tea4j.v2.models.Comment;
import org.mian.gitnex.clients.RetrofitClient;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Author M M Arif
 */

public class IssueCommentsViewModel extends ViewModel {

    private static MutableLiveData<List<Comment>> issueComments;

    public LiveData<List<Comment>> getIssueCommentList(String token, String owner, String repo, int index, Context ctx) {

        issueComments = new MutableLiveData<>();
        loadIssueComments(token, owner, repo, index, ctx);

        return issueComments;
    }

	public static void loadIssueComments(String token, String owner, String repo, int index, Context ctx) {
		loadIssueComments(token, owner, repo, index, ctx, null);
	}

    public static void loadIssueComments(String token, String owner, String repo, int index, Context ctx, Runnable onLoadingFinished) {

        Call<List<Comment>> call = RetrofitClient
                .getApiInterface(ctx)
                .issueGetComments(owner, repo, (long) index, null, null);

        call.enqueue(new Callback<List<Comment>>() {

            @Override
            public void onResponse(@NonNull Call<List<Comment>> call, @NonNull Response<List<Comment>> response) {

                if(response.isSuccessful()) {

                    issueComments.postValue(response.body());
					if(onLoadingFinished != null) {
						onLoadingFinished.run();
					}

                }
                else {
                    Log.i("onResponse", String.valueOf(response.code()));
                }

            }

            @Override
            public void onFailure(@NonNull Call<List<Comment>> call, Throwable t) {
                Log.i("onFailure", t.toString());
            }

        });

    }

}
