package org.mian.gitnex.fragments.profile;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import org.gitnex.tea4j.models.UserInfo;
import org.mian.gitnex.R;
import org.mian.gitnex.clients.PicassoService;
import org.mian.gitnex.clients.RetrofitClient;
import org.mian.gitnex.databinding.FragmentProfileDetailBinding;
import org.mian.gitnex.helpers.AlertDialogs;
import org.mian.gitnex.helpers.AppUtil;
import org.mian.gitnex.helpers.Authorization;
import org.mian.gitnex.helpers.ClickListener;
import org.mian.gitnex.helpers.RoundedTransformation;
import org.mian.gitnex.helpers.TimeHelper;
import org.mian.gitnex.helpers.TinyDB;
import org.mian.gitnex.helpers.Toasty;
import org.ocpsoft.prettytime.PrettyTime;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;
import retrofit2.Call;
import retrofit2.Callback;

/**
 * Author M M Arif
 */

public class DetailFragment extends Fragment {

	private Context ctx;
	private FragmentProfileDetailBinding binding;
	Locale locale;
	TinyDB tinyDb;

	private static final String usernameBundle = "";
	private String username;

	public DetailFragment() {}

	public static DetailFragment newInstance(String username) {
		DetailFragment fragment = new DetailFragment();
		Bundle args = new Bundle();
		args.putString(usernameBundle, username);
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (getArguments() != null) {
			username = getArguments().getString(usernameBundle);
		}
	}

	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		binding = FragmentProfileDetailBinding.inflate(inflater, container, false);
		tinyDb = TinyDB.getInstance(getContext());
		ctx = getContext();
		locale = getResources().getConfiguration().locale;

		getProfileDetail(ctx, username);

		return binding.getRoot();
	}

	public void getProfileDetail(Context context, String username) {

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

							int imgRadius = AppUtil.getPixelsFromDensity(ctx, 3);
							String timeFormat = tinyDb.getString("dateFormat");

							binding.username.setText(username);
							binding.userEmail.setText(email);
							binding.userLang.setText(lang);
							binding.userLogin.setText(response.body().getLogin());

							PicassoService.getInstance(ctx)
								.get()
								.load(response.body().getAvatar())
								.placeholder(R.drawable.loader_animated)
								.transform(new RoundedTransformation(imgRadius, 0))
								.resize(120, 120)
								.centerCrop()
								.into(binding.userAvatar);

							switch(timeFormat) {
								case "pretty": {
									PrettyTime prettyTime = new PrettyTime(locale);
									String createdTime = prettyTime.format(response.body().getCreated());
									binding.userJoinedOn.setText(createdTime);
									binding.userJoinedOn.setOnClickListener(new ClickListener(TimeHelper.customDateFormatForToastDateFormat(response.body().getCreated()), context));
									break;
								}
								case "normal": {
									DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd '" + context.getResources().getString(R.string.timeAtText) + "' HH:mm", locale);
									String createdTime = formatter.format(response.body().getCreated());
									binding.userJoinedOn.setText(createdTime);
									break;
								}
								case "normal1": {
									DateFormat formatter = new SimpleDateFormat("dd-MM-yyyy '" + context.getResources().getString(R.string.timeAtText) + "' HH:mm", locale);
									String createdTime = formatter.format(response.body().getCreated());
									binding.userJoinedOn.setText(createdTime);
									break;
								}
							}

							break;

						case 401:
							AlertDialogs
								.authorizationTokenRevokedDialog(context, context.getResources().getString(R.string.alertDialogTokenRevokedTitle), context.getResources().getString(R.string.alertDialogTokenRevokedMessage), context.getResources().getString(R.string.alertDialogTokenRevokedCopyNegativeButton), context.getResources().getString(R.string.alertDialogTokenRevokedCopyPositiveButton));
							break;

						default:
							break;
					}
				}
			}

			@Override
			public void onFailure(@NonNull Call<UserInfo> call, @NonNull Throwable t) {
				Toasty.error(ctx, ctx.getResources().getString(R.string.genericError));
			}
		});
	}
}
