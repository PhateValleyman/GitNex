package org.mian.gitnex.actions;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import androidx.annotation.NonNull;
import org.gitnex.tea4j.models.UserInfo;
import org.mian.gitnex.R;
import org.mian.gitnex.clients.PicassoService;
import org.mian.gitnex.clients.RetrofitClient;
import org.mian.gitnex.databinding.CustomUserProfileDialogBinding;
import org.mian.gitnex.helpers.AlertDialogs;
import org.mian.gitnex.helpers.AppUtil;
import org.mian.gitnex.helpers.Authorization;
import org.mian.gitnex.helpers.RoundedTransformation;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Author M M Arif
 */

public class ProfileActions {

	private static Dialog userProfileDialog;

	public static ActionResult<Response<?>> showUserProfile(Context context, String username) {

		userProfileDialog = new Dialog(context, R.style.ThemeOverlay_MaterialComponents_Dialog_Alert);
		ActionResult<Response<?>> actionResult = new ActionResult<>();

		Call<UserInfo> call = RetrofitClient
			.getApiInterface(context)
			.getUserProfile(Authorization.get(context), username);

		call.enqueue(new Callback<UserInfo>() {
			@Override
			public void onResponse(@NonNull Call<UserInfo> call, @NonNull retrofit2.Response<UserInfo> response) {

				if(response.isSuccessful() && response.body() != null) {

					switch(response.code()) {
						case 200:
							String username = !response.body().getFullname().isEmpty() ? response.body().getFullname() : response.body().getUsername();
							String email = !response.body().getEmail().isEmpty() ? response.body().getEmail() : "";
							String lang = !response.body().getLang().isEmpty() ? response.body().getLang() : "";
							showUserProfileDialog(context, response.body().getAvatar(), username, email, lang, response.body().getLogin());
							break;

						case 401:
							actionResult.finish(ActionResult.Status.FAILED, response);
							AlertDialogs
								.authorizationTokenRevokedDialog(context, context.getResources().getString(R.string.alertDialogTokenRevokedTitle), context.getResources().getString(R.string.alertDialogTokenRevokedMessage), context.getResources().getString(R.string.alertDialogTokenRevokedCopyNegativeButton), context.getResources().getString(R.string.alertDialogTokenRevokedCopyPositiveButton));
							break;

						default:
							actionResult.finish(ActionResult.Status.FAILED, response);
							break;
					}
				}
			}

			@Override
			public void onFailure(@NonNull Call<UserInfo> call, @NonNull Throwable t) {
				actionResult.finish(ActionResult.Status.FAILED);
			}
		});

		return actionResult;
	}

	private static void showUserProfileDialog(Context ctx, String userAvatar, String username, String email, String lang, String login) {

		int imgRadius = AppUtil.getPixelsFromDensity(ctx, 3);
		if (userProfileDialog.getWindow() != null) {
			userProfileDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
		}

		CustomUserProfileDialogBinding userProfileDialogBinding = CustomUserProfileDialogBinding.inflate(LayoutInflater.from(ctx));
		View view = userProfileDialogBinding.getRoot();
		userProfileDialog.setContentView(view);

		userProfileDialogBinding.username.setText(username);
		userProfileDialogBinding.userEmail.setText(email);
		userProfileDialogBinding.userLang.setText(lang);
		userProfileDialogBinding.userLogin.setText(login);

		PicassoService.getInstance(ctx)
			.get()
			.load(userAvatar)
			.placeholder(R.drawable.loader_animated)
			.transform(new RoundedTransformation(imgRadius, 0))
			.resize(120, 120)
			.centerCrop()
			.into(userProfileDialogBinding.userAvatar);

		userProfileDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
		userProfileDialog.show();
	}
}
