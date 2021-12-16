package org.mian.gitnex.actions;

import android.content.Context;
import android.util.Log;
import androidx.annotation.NonNull;
import com.google.gson.JsonElement;
import org.gitnex.tea4j.models.Milestones;
import org.mian.gitnex.R;
import org.mian.gitnex.clients.RetrofitClient;
import org.mian.gitnex.helpers.AlertDialogs;
import org.mian.gitnex.helpers.TinyDB;
import org.mian.gitnex.helpers.Toasty;
import retrofit2.Call;
import retrofit2.Callback;

/**
 * Author M M Arif
 */

public class MilestoneActions {

	static final private String TAG = "MilestoneActions : ";

	public static void closeMilestone(final Context ctx, int milestoneId_) {

		final TinyDB tinyDB = TinyDB.getInstance(ctx);

		String repoFullName = tinyDB.getString("repoFullName");
		String[] parts = repoFullName.split("/");
		final String repoOwner = parts[0];
		final String repoName = parts[1];
		final String loginUid = tinyDB.getString("loginUid");
		final String token = "token " + tinyDB.getString(loginUid + "-token");

		Milestones milestoneStateJson = new Milestones("closed");
		Call<JsonElement> call;

		call = RetrofitClient
				.getApiInterface(ctx)
				.closeReopenMilestone(token, repoOwner, repoName, milestoneId_, milestoneStateJson);

		call.enqueue(new Callback<JsonElement>() {

			@Override
			public void onResponse(@NonNull Call<JsonElement> call, @NonNull retrofit2.Response<JsonElement> response) {

				if(response.isSuccessful()) {

					Toasty.success(ctx, ctx.getString(R.string.milestoneStatusUpdate));

				}
				else if(response.code() == 401) {

					AlertDialogs.authorizationTokenRevokedDialog(ctx, ctx.getResources().getString(R.string.alertDialogTokenRevokedTitle),
							ctx.getResources().getString(R.string.alertDialogTokenRevokedMessage),
							ctx.getResources().getString(R.string.cancelButton),
							ctx.getResources().getString(R.string.navLogout));

				}
				else {

					Toasty.error(ctx, ctx.getString(R.string.genericError));

				}

			}

			@Override
			public void onFailure(@NonNull Call<JsonElement> call, @NonNull Throwable t) {

				Log.e(TAG, t.toString());

			}

		});


	}

	public static void openMilestone(final Context ctx, int milestoneId_) {

		final TinyDB tinyDB = TinyDB.getInstance(ctx);

		String repoFullName = tinyDB.getString("repoFullName");
		String[] parts = repoFullName.split("/");
		final String repoOwner = parts[0];
		final String repoName = parts[1];
		final String loginUid = tinyDB.getString("loginUid");
		final String token = "token " + tinyDB.getString(loginUid + "-token");

		Milestones milestoneStateJson = new Milestones("open");
		Call<JsonElement> call;

		call = RetrofitClient
				.getApiInterface(ctx)
				.closeReopenMilestone(token, repoOwner, repoName, milestoneId_, milestoneStateJson);

		call.enqueue(new Callback<JsonElement>() {

			@Override
			public void onResponse(@NonNull Call<JsonElement> call, @NonNull retrofit2.Response<JsonElement> response) {

				if(response.isSuccessful()) {

					Toasty.success(ctx, ctx.getString(R.string.milestoneStatusUpdate));

				}
				else if(response.code() == 401) {

					AlertDialogs.authorizationTokenRevokedDialog(ctx, ctx.getResources().getString(R.string.alertDialogTokenRevokedTitle),
							ctx.getResources().getString(R.string.alertDialogTokenRevokedMessage),
							ctx.getResources().getString(R.string.cancelButton),
							ctx.getResources().getString(R.string.navLogout));

				}
				else {

					Toasty.error(ctx, ctx.getString(R.string.genericError));

				}

			}

			@Override
			public void onFailure(@NonNull Call<JsonElement> call, @NonNull Throwable t) {

				Log.e(TAG, t.toString());

			}

		});

	}

}
