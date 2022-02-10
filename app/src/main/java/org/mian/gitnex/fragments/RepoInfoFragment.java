package org.mian.gitnex.fragments;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import org.apache.commons.io.FileUtils;
import org.gitnex.tea4j.models.UserRepositories;
import org.mian.gitnex.R;
import org.mian.gitnex.activities.RepoDetailActivity;
import org.mian.gitnex.activities.RepoStargazersActivity;
import org.mian.gitnex.activities.RepoWatchersActivity;
import org.mian.gitnex.clients.RetrofitClient;
import org.mian.gitnex.databinding.FragmentRepoInfoBinding;
import org.mian.gitnex.helpers.AlertDialogs;
import org.mian.gitnex.helpers.Authorization;
import org.mian.gitnex.helpers.ClickListener;
import org.mian.gitnex.helpers.Markdown;
import org.mian.gitnex.helpers.TimeHelper;
import org.mian.gitnex.helpers.TinyDB;
import org.mian.gitnex.helpers.Toasty;
import org.mian.gitnex.helpers.contexts.RepositoryContext;
import java.util.Locale;
import retrofit2.Call;
import retrofit2.Callback;

/**
 * Author M M Arif
 */

public class RepoInfoFragment extends Fragment {

	private Context ctx;
	private LinearLayout pageContent;

	private FragmentRepoInfoBinding binding;

	private RepositoryContext repository;

	private OnFragmentInteractionListener mListener;

	public RepoInfoFragment() {}

	public static RepoInfoFragment newInstance(RepositoryContext repository) {
		RepoInfoFragment fragment = new RepoInfoFragment();
		fragment.setArguments(repository.getBundle());
		return fragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		repository = RepositoryContext.fromBundle(requireArguments());
	}

	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		binding = FragmentRepoInfoBinding.inflate(inflater, container, false);
		TinyDB tinyDb = TinyDB.getInstance(getContext());
		ctx = getContext();
		Locale locale = getResources().getConfiguration().locale;

		pageContent = binding.repoInfoLayout;
		pageContent.setVisibility(View.GONE);

		binding.repoMetaFrame.setVisibility(View.GONE);

		setRepoInfo(locale, tinyDb.getString("dateFormat", "pretty"));
		getFileContents(Authorization.get(getContext()), repository.getOwner(), repository.getName(), getResources().getString(R.string.defaultFilename));

		if(isExpandViewVisible()) {
			toggleExpandView();
		}

		if(!isExpandViewMetaVisible()) {
			toggleExpandViewMeta();
		}

		binding.fileContentsFrameHeader.setOnClickListener(v1 -> toggleExpandView());
		binding.repoMetaFrameHeader.setOnClickListener(v12 -> toggleExpandViewMeta());

		binding.repoMetaStarsFrame.setOnClickListener(metaStars -> ctx.startActivity(repository.getIntent(ctx, RepoStargazersActivity.class)));

		binding.repoMetaWatchersFrame.setOnClickListener(metaWatchers -> ctx.startActivity(repository.getIntent(ctx, RepoWatchersActivity.class)));

		binding.repoMetaPullRequestsFrame.setOnClickListener(metaPR -> ((RepoDetailActivity) requireActivity()).mViewPager.setCurrentItem(3));

		return binding.getRoot();
	}

	public void onButtonPressed(Uri uri) {
		if (mListener != null) {
			mListener.onFragmentInteraction(uri);
		}
	}

	@Override
	public void onDetach() {
		super.onDetach();
		mListener = null;
	}

	public interface OnFragmentInteractionListener {
		void onFragmentInteraction(Uri uri);
	}

	private void toggleExpandView() {

		if (binding.repoFileContents.getVisibility() == View.GONE) {
			binding.repoFilenameExpandCollapse.setImageResource(R.drawable.ic_chevron_up);
			binding.repoFileContents.setVisibility(View.VISIBLE);
			//Animation slide_down = AnimationUtils.loadAnimation(getContext(), R.anim.slide_down);
			//binding.fileContentsFrame.startAnimation(slide_down);
		}
		else {
			binding.repoFilenameExpandCollapse.setImageResource(R.drawable.ic_chevron_down);
			binding.repoFileContents.setVisibility(View.GONE);
			//Animation slide_up = AnimationUtils.loadAnimation(getContext(), R.anim.slide_up);
			//binding.fileContentsFrame.startAnimation(slide_up);
		}
	}

	private boolean isExpandViewVisible() {
		return binding.repoFileContents.getVisibility() == View.VISIBLE;
	}

	private void toggleExpandViewMeta() {

		if (binding.repoMetaFrame.getVisibility() == View.GONE) {
			binding.repoMetaDataExpandCollapse.setImageResource(R.drawable.ic_chevron_up);
			binding.repoMetaFrame.setVisibility(View.VISIBLE);
			//Animation slide_down = AnimationUtils.loadAnimation(getContext(), R.anim.slide_down);
			//binding.repoMetaFrame.startAnimation(slide_down);
		}
		else {
			binding.repoMetaDataExpandCollapse.setImageResource(R.drawable.ic_chevron_down);
			binding.repoMetaFrame.setVisibility(View.GONE);
			//Animation slide_up = AnimationUtils.loadAnimation(getContext(), R.anim.slide_up);
			//binding.repoMetaFrame.startAnimation(slide_up);
		}
	}

	private boolean isExpandViewMetaVisible() {
		return binding.repoMetaFrame.getVisibility() == View.VISIBLE;
	}

	private void setRepoInfo(Locale locale, final String timeFormat) {
		UserRepositories repoInfo = repository.getRepository();

		if (isAdded()) {
			assert repoInfo != null;
			binding.repoMetaName.setText(repoInfo.getName());

			if(!repoInfo.getDescription().isEmpty()) {
				Markdown.render(ctx, repoInfo.getDescription(), binding.repoMetaDescription);
			}
			else {
				binding.repoMetaDescription.setText(getString(R.string.noDataDescription));
			}

			binding.repoMetaStars.setText(repoInfo.getStars_count());

			if(repoInfo.getOpen_pull_count() != null) {
				binding.repoMetaPullRequests.setText(repoInfo.getOpen_pull_count());
			}
			else {
				binding.repoMetaPullRequestsFrame.setVisibility(View.GONE);
			}

			binding.repoMetaForks.setText(repoInfo.getForks_count());
			binding.repoMetaWatchers.setText(repoInfo.getWatchers_count());
			binding.repoMetaSize.setText(FileUtils.byteCountToDisplaySize((int) repoInfo.getSize() * 1024));

			binding.repoMetaCreatedAt.setText(TimeHelper.formatTime(repoInfo.getCreated_at(), locale, timeFormat, ctx));
			if(timeFormat.equals("pretty")) {
				binding.repoMetaCreatedAt.setOnClickListener(new ClickListener(TimeHelper.customDateFormatForToastDateFormat(repoInfo.getCreated_at()), ctx));
			}

			String repoMetaUpdatedAt = TimeHelper.formatTime(repoInfo.getUpdated_at(), locale, timeFormat, ctx);

			String website = (repoInfo.getWebsite().isEmpty()) ? getResources().getString(R.string.noDataWebsite) : repoInfo.getWebsite();
			binding.repoMetaWebsite.setText(website);

			binding.repoAdditionalButton.setOnClickListener(v -> {

				View view = LayoutInflater.from(ctx).inflate(R.layout.layout_repo_more_info, null);

				TextView defaultBranchHeader = view.findViewById(R.id.defaultBranchHeader);
				TextView defaultBranchContent = view.findViewById(R.id.defaultBranchContent);

				TextView lastUpdatedHeader = view.findViewById(R.id.lastUpdatedHeader);
				TextView lastUpdatedContent = view.findViewById(R.id.lastUpdatedContent);

				TextView sshUrlHeader = view.findViewById(R.id.sshUrlHeader);
				TextView sshUrlContent = view.findViewById(R.id.sshUrlContent);

				TextView cloneUrlHeader = view.findViewById(R.id.cloneUrlHeader);
				TextView cloneUrlContent = view.findViewById(R.id.cloneUrlContent);

				TextView repoUrlHeader = view.findViewById(R.id.repoUrlHeader);
				TextView repoUrlContent = view.findViewById(R.id.repoUrlContent);

				defaultBranchHeader.setText(getString(R.string.infoTabRepoDefaultBranch));
				defaultBranchContent.setText(repoInfo.getDefault_branch());

				lastUpdatedHeader.setText(getString(R.string.infoTabRepoUpdatedAt));
				lastUpdatedContent.setText(repoMetaUpdatedAt);

				sshUrlHeader.setText(getString(R.string.infoTabRepoSshUrl));
				sshUrlContent.setText(repoInfo.getSsh_url());

				cloneUrlHeader.setText(getString(R.string.infoTabRepoCloneUrl));
				cloneUrlContent.setText(repoInfo.getClone_url());

				repoUrlHeader.setText(getString(R.string.infoTabRepoRepoUrl));
				repoUrlContent.setText(repoInfo.getHtml_url());

				AlertDialog.Builder alertDialog = new AlertDialog.Builder(ctx);

				alertDialog.setTitle(getResources().getString(R.string.infoMoreInformation));
				alertDialog.setView(view);
				alertDialog.setPositiveButton(getString(R.string.okButton), null);
				alertDialog.create().show();

			});

			if(repoInfo.isArchived()) {
				binding.repoIsArchived.setVisibility(View.VISIBLE);
			}
			else {
				binding.repoIsArchived.setVisibility(View.GONE);
			}

			pageContent.setVisibility(View.VISIBLE);

		}
	}

	private void getFileContents(String token, final String owner, String repo, final String filename) {

		Call<String> call = RetrofitClient
				.getApiInterface(getContext())
				.getFileContents(token, owner, repo, filename);

		call.enqueue(new Callback<String>() {

			@Override
			public void onResponse(@NonNull Call<String> call, @NonNull retrofit2.Response<String> response) {

				if (isAdded()) {

					switch(response.code()) {

						case 200:
							Markdown.render(ctx, response.body(), binding.repoFileContents);
							break;

						case 401:
							AlertDialogs.authorizationTokenRevokedDialog(ctx, getResources().getString(R.string.alertDialogTokenRevokedTitle),
								getResources().getString(R.string.alertDialogTokenRevokedMessage),
								getResources().getString(R.string.cancelButton),
								getResources().getString(R.string.navLogout));
							break;

						case 403:
							Toasty.error(ctx, ctx.getString(R.string.authorizeError));
							binding.fileContentsFrameHeader.setVisibility(View.GONE);
							binding.fileContentsFrame.setVisibility(View.GONE);
							break;

						case 404:
							binding.fileContentsFrameHeader.setVisibility(View.GONE);
							binding.fileContentsFrame.setVisibility(View.GONE);
							break;

						default:
							Toasty.error(getContext(), getString(R.string.genericError));
							binding.fileContentsFrameHeader.setVisibility(View.GONE);
							binding.fileContentsFrame.setVisibility(View.GONE);
							break;

					}
				}
			}

			@Override
			public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
				Log.e("onFailure", t.toString());
			}

		});

	}

}
