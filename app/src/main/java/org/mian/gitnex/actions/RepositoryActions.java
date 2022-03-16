package org.mian.gitnex.actions;

import android.content.Context;
import android.util.Log;
import androidx.annotation.NonNull;
import org.gitnex.tea4j.v2.models.WatchInfo;
import org.mian.gitnex.R;
import org.mian.gitnex.activities.MainActivity;
import org.mian.gitnex.clients.RetrofitClient;
import org.mian.gitnex.helpers.AlertDialogs;
import org.mian.gitnex.helpers.Toasty;
import org.mian.gitnex.helpers.contexts.RepositoryContext;
import retrofit2.Call;
import retrofit2.Callback;

/**
 * Author M M Arif
 */

public class RepositoryActions {

    public static void starRepository(final Context context, RepositoryContext repository) {

        Call<Void> call = RetrofitClient
                .getApiInterface(context)
                .userCurrentPutStar(repository.getOwner(), repository.getName());

        call.enqueue(new Callback<Void>() {

            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull retrofit2.Response<Void> response) {

                if(response.isSuccessful()) {
                    if(response.code() == 204) {

	                    MainActivity.repoCreated = true;
                        Toasty.success(context, context.getString(R.string.starRepositorySuccess));

                    }
                }
                else if(response.code() == 401) {

                    AlertDialogs.authorizationTokenRevokedDialog(context, context.getResources().getString(R.string.alertDialogTokenRevokedTitle),
                            context.getResources().getString(R.string.alertDialogTokenRevokedMessage),
                            context.getResources().getString(R.string.cancelButton),
                            context.getResources().getString(R.string.navLogout));

                }
                else if(response.code() == 403) {

                    Toasty.error(context, context.getString(R.string.authorizeError));

                }
                else if(response.code() == 404) {

                    Toasty.warning(context, context.getString(R.string.apiNotFound));

                }
                else {

                    Toasty.error(context, context.getString(R.string.genericError));

                }

            }

            @Override
            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                Log.e("onFailure", t.toString());
            }
        });

    }

    public static void unStarRepository(final Context context, RepositoryContext repository) {

        Call<Void> call = RetrofitClient
                .getApiInterface(context)
                .userCurrentDeleteStar(repository.getOwner(), repository.getName());

        call.enqueue(new Callback<Void>() {

            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull retrofit2.Response<Void> response) {

                if(response.isSuccessful()) {
                    if(response.code() == 204) {

	                    MainActivity.repoCreated = true;
                        Toasty.success(context, context.getString(R.string.unStarRepositorySuccess));

                    }
                }
                else if(response.code() == 401) {

                    AlertDialogs.authorizationTokenRevokedDialog(context, context.getResources().getString(R.string.alertDialogTokenRevokedTitle),
                            context.getResources().getString(R.string.alertDialogTokenRevokedMessage),
                            context.getResources().getString(R.string.cancelButton),
                            context.getResources().getString(R.string.navLogout));

                }
                else if(response.code() == 403) {

                    Toasty.error(context, context.getString(R.string.authorizeError));

                }
                else if(response.code() == 404) {

                    Toasty.warning(context, context.getString(R.string.apiNotFound));

                }
                else {

                    Toasty.error(context, context.getString(R.string.genericError));

                }

            }

            @Override
            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                Log.e("onFailure", t.toString());
            }
        });

    }

    public static void watchRepository(final Context context, RepositoryContext repository) {

        Call<WatchInfo> call = RetrofitClient
                .getApiInterface(context)
                .userCurrentPutSubscription(repository.getOwner(), repository.getName());

        call.enqueue(new Callback<WatchInfo>() {

            @Override
            public void onResponse(@NonNull Call<WatchInfo> call, @NonNull retrofit2.Response<WatchInfo> response) {

                if(response.isSuccessful()) {
                    if(response.code() == 200) {

                        Toasty.success(context, context.getString(R.string.watchRepositorySuccess));

                    }
                }
                else if(response.code() == 401) {

                    AlertDialogs.authorizationTokenRevokedDialog(context, context.getResources().getString(R.string.alertDialogTokenRevokedTitle),
                            context.getResources().getString(R.string.alertDialogTokenRevokedMessage),
                            context.getResources().getString(R.string.cancelButton),
                            context.getResources().getString(R.string.navLogout));

                }
                else if(response.code() == 403) {

                    Toasty.error(context, context.getString(R.string.authorizeError));

                }
                else if(response.code() == 404) {

                    Toasty.warning(context, context.getString(R.string.apiNotFound));

                }
                else {

                    Toasty.error(context, context.getString(R.string.genericError));

                }

            }

            @Override
            public void onFailure(@NonNull Call<WatchInfo> call, @NonNull Throwable t) {
                Log.e("onFailure", t.toString());
            }
        });

    }

    public static void unWatchRepository(final Context context, RepositoryContext repository) {

        Call<Void> call = RetrofitClient
                .getApiInterface(context)
                .userCurrentDeleteStar(repository.getOwner(), repository.getName());

        call.enqueue(new Callback<Void>() {

            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull retrofit2.Response<Void> response) {

                if(response.code() == 204) {

                    Toasty.success(context, context.getString(R.string.unWatchRepositorySuccess));

                }
                else if(response.code() == 401) {

                    AlertDialogs.authorizationTokenRevokedDialog(context, context.getResources().getString(R.string.alertDialogTokenRevokedTitle),
                            context.getResources().getString(R.string.alertDialogTokenRevokedMessage),
                            context.getResources().getString(R.string.cancelButton),
                            context.getResources().getString(R.string.navLogout));

                }
                else if(response.code() == 403) {

                    Toasty.error(context, context.getString(R.string.authorizeError));

                }
                else if(response.code() == 404) {

                    Toasty.warning(context, context.getString(R.string.apiNotFound));

                }
                else {

                    Toasty.error(context, context.getString(R.string.genericError));

                }

            }

            @Override
            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                Log.e("onFailure", t.toString());
            }
        });

    }

}
