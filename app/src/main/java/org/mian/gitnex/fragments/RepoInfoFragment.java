package org.mian.gitnex.fragments;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import io.noties.markwon.AbstractMarkwonPlugin;
import io.noties.markwon.Markwon;
import io.noties.markwon.core.CorePlugin;
import io.noties.markwon.core.MarkwonTheme;
import io.noties.markwon.ext.strikethrough.StrikethroughPlugin;
import io.noties.markwon.ext.tables.TablePlugin;
import io.noties.markwon.ext.tasklist.TaskListPlugin;
import io.noties.markwon.html.HtmlPlugin;
import io.noties.markwon.image.AsyncDrawable;
import io.noties.markwon.image.DefaultMediaDecoder;
import io.noties.markwon.image.ImageItem;
import io.noties.markwon.image.ImagesPlugin;
import io.noties.markwon.image.SchemeHandler;
import io.noties.markwon.image.gif.GifMediaDecoder;
import io.noties.markwon.image.svg.SvgMediaDecoder;
import io.noties.markwon.linkify.LinkifyPlugin;
import retrofit2.Call;
import retrofit2.Callback;
import android.text.Spanned;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import org.mian.gitnex.R;
import org.mian.gitnex.clients.RetrofitClient;
import org.mian.gitnex.helpers.AlertDialogs;
import org.mian.gitnex.helpers.Authorization;
import org.mian.gitnex.helpers.ClickListener;
import org.mian.gitnex.helpers.TimeHelper;
import org.mian.gitnex.helpers.Toasty;
import org.mian.gitnex.models.UserRepositories;
import org.mian.gitnex.util.AppUtil;
import org.mian.gitnex.util.TinyDB;
import org.ocpsoft.prettytime.PrettyTime;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Collections;
import java.util.Locale;
import java.util.Objects;

/**
 * Author M M Arif
 */

public class RepoInfoFragment extends Fragment {

    private Context ctx = getContext();
    private ProgressBar mProgressBar;
    private LinearLayout pageContent;
    private static String repoNameF = "param2";
    private static String repoOwnerF = "param1";

    private String repoName;
    private String repoOwner;
    private TextView repoMetaName;
    private TextView repoMetaDescription;
    private TextView repoMetaCommits;
    private TextView repoMetaPullRequests;
    private TextView repoMetaSize;
    private TextView repoFileContents;
    private LinearLayout repoMetaFrame;
    private ImageView repoMetaDataExpandCollapse;
    private ImageView repoFilenameExpandCollapse;
    private LinearLayout fileContentsFrameHeader;
    private LinearLayout fileContentsFrame;

    private OnFragmentInteractionListener mListener;

    public RepoInfoFragment() {

    }

    public static RepoInfoFragment newInstance(String param1, String param2) {
        RepoInfoFragment fragment = new RepoInfoFragment();
        Bundle args = new Bundle();
        args.putString(repoOwnerF, param1);
        args.putString(repoNameF, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            repoName = getArguments().getString(repoNameF);
            repoOwner = getArguments().getString(repoOwnerF);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_repo_info, container, false);

        TinyDB tinyDb = new TinyDB(getContext());
        final String instanceUrl = tinyDb.getString("instanceUrl");
        final String loginUid = tinyDb.getString("loginUid");
        final String instanceToken = "token " + tinyDb.getString(loginUid + "-token");
        final String locale = tinyDb.getString("locale");
        final String timeFormat = tinyDb.getString("dateFormat");

        pageContent = v.findViewById(R.id.repoInfoLayout);
        pageContent.setVisibility(View.GONE);

        mProgressBar = v.findViewById(R.id.progress_bar);
        repoMetaName = v.findViewById(R.id.repoMetaName);
        repoMetaDescription = v.findViewById(R.id.repoMetaDescription);
        repoMetaCommits = v.findViewById(R.id.repoMetaCommits);
        repoMetaPullRequests = v.findViewById(R.id.repoMetaPullRequests);
        repoMetaSize = v.findViewById(R.id.repoMetaSize);
        repoFileContents = v.findViewById(R.id.repoFileContents);
        repoMetaFrame = v.findViewById(R.id.repoMetaFrame);
        LinearLayout repoMetaFrameHeader = v.findViewById(R.id.repoMetaFrameHeader);
        repoMetaDataExpandCollapse = v.findViewById(R.id.repoMetaDataExpandCollapse);
        repoFilenameExpandCollapse = v.findViewById(R.id.repoFilenameExpandCollapse);
        fileContentsFrameHeader = v.findViewById(R.id.fileContentsFrameHeader);
        fileContentsFrame = v.findViewById(R.id.fileContentsFrame);

        repoMetaFrame.setVisibility(View.GONE);

        getRepoInfo(instanceUrl, Authorization.returnAuthentication(getContext(), loginUid, instanceToken), repoOwner, repoName, locale, timeFormat);
        getFileContents(instanceUrl, Authorization.returnAuthentication(getContext(), loginUid, instanceToken), repoOwner, repoName, getResources().getString(R.string.defaultFilename));

        fileContentsFrameHeader.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                collapseExpandView();
            }
        });

        repoMetaFrameHeader.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                collapseExpandViewMeta();
            }
        });

        return v;
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

    private void collapseExpandView() {

        if (repoFileContents.getVisibility() == View.GONE) {
            repoFilenameExpandCollapse.setImageResource(R.drawable.ic_arrow_up);
            repoFileContents.setVisibility(View.VISIBLE);
            //Animation slide_down = AnimationUtils.loadAnimation(getContext(), R.anim.slide_down);
            //fileContentsFrame.startAnimation(slide_down);
        }
        else {
            repoFilenameExpandCollapse.setImageResource(R.drawable.ic_arrow_down);
            repoFileContents.setVisibility(View.GONE);
            //Animation slide_up = AnimationUtils.loadAnimation(getContext(), R.anim.slide_up);
            //fileContentsFrame.startAnimation(slide_up);
        }

    }

    private void collapseExpandViewMeta() {
        if (repoMetaFrame.getVisibility() == View.GONE) {
            repoMetaDataExpandCollapse.setImageResource(R.drawable.ic_arrow_up);
            repoMetaFrame.setVisibility(View.VISIBLE);
            //Animation slide_down = AnimationUtils.loadAnimation(getContext(), R.anim.slide_down);
            //repoMetaFrame.startAnimation(slide_down);
        }
        else {
            repoMetaDataExpandCollapse.setImageResource(R.drawable.ic_arrow_down);
            repoMetaFrame.setVisibility(View.GONE);
            //Animation slide_up = AnimationUtils.loadAnimation(getContext(), R.anim.slide_up);
            //repoMetaFrame.startAnimation(slide_up);
        }

    }

    private void getRepoInfo(String instanceUrl, String token, final String owner, String repo, final String locale, final String timeFormat) {

        final TinyDB tinyDb = new TinyDB(getContext());

        Call<UserRepositories> call = RetrofitClient
                .getInstance(instanceUrl, getContext())
                .getApiInterface()
                .getUserRepository(token, owner, repo);

        call.enqueue(new Callback<UserRepositories>() {

            @Override
            public void onResponse(@NonNull Call<UserRepositories> call, @NonNull retrofit2.Response<UserRepositories> response) {

                UserRepositories repoInfo = response.body();

                if (isAdded()) {

                    if (response.isSuccessful()) {

                        if (response.code() == 200) {

                            assert repoInfo != null;
                            repoMetaName.setText(repoInfo.getName());
                            repoMetaDescription.setText(repoInfo.getDescription());
                            repoMetaCommits.setText("20");
                            repoMetaPullRequests.setText("20");
                            repoMetaSize.setText(AppUtil.formatFileSize(repoInfo.getSize()));

                            if(repoInfo.getHas_issues() != null) {
                                tinyDb.putBoolean("hasIssues", repoInfo.getHas_issues());
                            }
                            else {
                                tinyDb.putBoolean("hasIssues", true);
                            }

                            /*
                            switch (timeFormat) {
                                case "pretty": {
                                    PrettyTime prettyTime = new PrettyTime(new Locale(locale));
                                    String createdTime = prettyTime.format(repoInfo.getCreated_at());
                                    repoCreatedAtInfo.setText(createdTime);
                                    repoCreatedAtInfo.setOnClickListener(new ClickListener(TimeHelper.customDateFormatForToastDateFormat(repoInfo.getCreated_at()), getContext()));
                                    break;
                                }
                                case "normal": {
                                    DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd '" + getResources().getString(R.string.timeAtText) + "' HH:mm", new Locale(locale));
                                    String createdTime = formatter.format(repoInfo.getCreated_at());
                                    repoCreatedAtInfo.setText(createdTime);
                                    break;
                                }
                                case "normal1": {
                                    DateFormat formatter = new SimpleDateFormat("dd-MM-yyyy '" + getResources().getString(R.string.timeAtText) + "' HH:mm", new Locale(locale));
                                    String createdTime = formatter.format(repoInfo.getCreated_at());
                                    repoCreatedAtInfo.setText(createdTime);
                                    break;
                                }
                            }
                            */

                            mProgressBar.setVisibility(View.GONE);
                            pageContent.setVisibility(View.VISIBLE);

                        }

                    }
                    else {
                        Log.e("onFailure", String.valueOf(response.code()));
                    }

                }

            }

            @Override
            public void onFailure(@NonNull Call<UserRepositories> call, @NonNull Throwable t) {
                Log.e("onFailure", t.toString());
            }
        });

    }

    private void getFileContents(String instanceUrl, String token, final String owner, String repo, final String filename) {

        final TinyDB tinyDb = new TinyDB(getContext());

        Call<String> call = RetrofitClient
                .getInstance(instanceUrl, getContext())
                .getApiInterface()
                .getFileContents(token, owner, repo, filename);

        call.enqueue(new Callback<String>() {

            @Override
            public void onResponse(@NonNull Call<String> call, @NonNull retrofit2.Response<String> response) {

                if (isAdded()) {

                    if (response.code() == 200) {

                        final Markwon markwon = Markwon.builder(Objects.requireNonNull(getContext()))
                            .usePlugin(CorePlugin.create())
                            .usePlugin(ImagesPlugin.create(new ImagesPlugin.ImagesConfigure() {
                                @Override
                                public void configureImages(@NonNull ImagesPlugin plugin) {
                                    plugin.addSchemeHandler(new SchemeHandler() {
                                        @NonNull
                                        @Override
                                        public ImageItem handle(@NonNull String raw, @NonNull Uri uri) {

                                            final int resourceId = getContext().getResources().getIdentifier(
                                                    raw.substring("drawable://".length()),
                                                    "drawable",
                                                    getContext().getPackageName());

                                            final Drawable drawable = getContext().getDrawable(resourceId);

                                            assert drawable != null;
                                            return ImageItem.withResult(drawable);
                                        }

                                        @NonNull
                                        @Override
                                        public Collection<String> supportedSchemes() {
                                            return Collections.singleton("drawable");
                                        }
                                    });
                                    plugin.placeholderProvider(new ImagesPlugin.PlaceholderProvider() {
                                        @Nullable
                                        @Override
                                        public Drawable providePlaceholder(@NonNull AsyncDrawable drawable) {
                                            return null;
                                        }
                                    });
                                    plugin.addMediaDecoder(GifMediaDecoder.create(false));
                                    plugin.addMediaDecoder(SvgMediaDecoder.create(getContext().getResources()));
                                    plugin.addMediaDecoder(SvgMediaDecoder.create());
                                    plugin.defaultMediaDecoder(DefaultMediaDecoder.create(getContext().getResources()));
                                    plugin.defaultMediaDecoder(DefaultMediaDecoder.create());
                                }
                            }))
                            .usePlugin(new AbstractMarkwonPlugin() {
                                @Override
                                public void configureTheme(@NonNull MarkwonTheme.Builder builder) {
                                    builder
                                            .codeTextColor(tinyDb.getInt("codeBlockColor"))
                                            .codeBackgroundColor(tinyDb.getInt("codeBlockBackground"))
                                            .linkColor(getResources().getColor(R.color.lightBlue));
                                }
                            })
                            .usePlugin(TablePlugin.create(getContext()))
                            .usePlugin(TaskListPlugin.create(getContext()))
                            .usePlugin(HtmlPlugin.create())
                            .usePlugin(StrikethroughPlugin.create())
                            .usePlugin(LinkifyPlugin.create())
                            .build();

                        Spanned bodyWithMD = null;
                        if (response.body() != null) {
                            bodyWithMD = markwon.toMarkdown(response.body());
                        }
                        assert bodyWithMD != null;
                        markwon.setParsedMarkdown(repoFileContents, bodyWithMD);


                    } else if (response.code() == 401) {

                        AlertDialogs.authorizationTokenRevokedDialog(ctx, getResources().getString(R.string.alertDialogTokenRevokedTitle),
                                getResources().getString(R.string.alertDialogTokenRevokedMessage),
                                getResources().getString(R.string.alertDialogTokenRevokedCopyNegativeButton),
                                getResources().getString(R.string.alertDialogTokenRevokedCopyPositiveButton));

                    } else if (response.code() == 403) {

                        Toasty.info(ctx, ctx.getString(R.string.authorizeError));

                    } else if (response.code() == 404) {

                        fileContentsFrameHeader.setVisibility(View.GONE);
                        fileContentsFrame.setVisibility(View.GONE);

                    } else {

                        Toasty.info(getContext(), getString(R.string.genericError));

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
