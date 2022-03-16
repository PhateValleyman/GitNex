package org.mian.gitnex.viewmodels;

import android.content.Context;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import org.gitnex.tea4j.v2.models.ContentsResponse;
import org.mian.gitnex.R;
import org.mian.gitnex.clients.RetrofitClient;
import org.mian.gitnex.helpers.Toasty;
import java.util.Collections;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Author M M Arif
 */

public class FilesViewModel extends ViewModel {

    private static MutableLiveData<List<ContentsResponse>> filesList;
    private static MutableLiveData<List<ContentsResponse>> filesList2;

    public LiveData<List<ContentsResponse>> getFilesList(String owner, String repo, String ref, Context ctx, ProgressBar progressBar, TextView noDataFiles) {

        filesList = new MutableLiveData<>();
        loadFilesList(owner, repo, ref, ctx, progressBar, noDataFiles);

        return filesList;
    }

    private static void loadFilesList(String owner, String repo, String ref, final Context ctx, ProgressBar progressBar, TextView noDataFiles) {

        Call<List<ContentsResponse>> call = RetrofitClient
                .getApiInterface(ctx)
                .repoGetContentsList(owner, repo, ref);

        call.enqueue(new Callback<List<ContentsResponse>>() {
            @Override
            public void onResponse(@NonNull Call<List<ContentsResponse>> call, @NonNull Response<List<ContentsResponse>> response) {

	            if(response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
	                Collections.sort(response.body(), (byType1, byType2) -> byType1.getType().compareTo(byType2.getType()));
                    filesList.postValue(response.body());
                }
                else {
	                progressBar.setVisibility(View.GONE);
	                noDataFiles.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<ContentsResponse>> call, @NonNull Throwable t) {
	            Toasty.error(ctx, ctx.getString(R.string.errorOnLogin));
            }
        });
    }

    public LiveData<List<ContentsResponse>> getFilesList2(String owner, String repo, String filesDir, String ref, Context ctx, ProgressBar progressBar, TextView noDataFiles) {

        filesList2 = new MutableLiveData<>();
        loadFilesList2(owner, repo, filesDir, ref, ctx, progressBar, noDataFiles);

        return filesList2;
    }

    private static void loadFilesList2(String owner, String repo, String filesDir, String ref, final Context ctx, ProgressBar progressBar, TextView noDataFiles) {

        Call<List<ContentsResponse>> call = RetrofitClient
                .getApiInterface(ctx)
                .repoGetContentsList(owner, repo, filesDir, ref);

        call.enqueue(new Callback<List<ContentsResponse>>() {
            @Override
            public void onResponse(@NonNull Call<List<ContentsResponse>> call, @NonNull Response<List<ContentsResponse>> response) {

	            if(response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
	                Collections.sort(response.body(), (byType1, byType2) -> byType1.getType().compareTo(byType2.getType()));
	                filesList2.postValue(response.body());
                }
                else {
	                progressBar.setVisibility(View.GONE);
	                noDataFiles.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<ContentsResponse>> call, @NonNull Throwable t) {
	            Toasty.error(ctx, ctx.getString(R.string.errorOnLogin));
            }
        });
    }

}
