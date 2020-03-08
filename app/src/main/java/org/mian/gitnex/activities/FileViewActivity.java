package org.mian.gitnex.activities;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import com.github.barteksc.pdfviewer.PDFView;
import com.github.barteksc.pdfviewer.util.FitPolicy;
import com.github.chrisbanes.photoview.PhotoView;
import com.pddstudio.highlightjs.HighlightJsView;
import com.pddstudio.highlightjs.models.Theme;
import org.apache.commons.io.FilenameUtils;
import org.mian.gitnex.R;
import org.mian.gitnex.clients.RetrofitClient;
import org.mian.gitnex.helpers.AlertDialogs;
import org.mian.gitnex.helpers.Toasty;
import org.mian.gitnex.models.Files;
import org.mian.gitnex.util.AppUtil;
import org.mian.gitnex.util.TinyDB;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import retrofit2.Call;
import retrofit2.Callback;

/**
 * Author M M Arif
 */

public class FileViewActivity extends BaseActivity {

    private View.OnClickListener onClickListener;
    private TextView singleFileContents;
    private LinearLayout singleFileContentsFrame;
    private HighlightJsView singleCodeContents;
    private PhotoView imageView;
    final Context ctx = this;
    private ProgressBar mProgressBar;
    private byte[] imageData;
    private PDFView pdfView;
    private LinearLayout pdfViewFrame;
    private byte[] decodedPdf;
    private Boolean pdfNightMode;

    @Override
    protected int getLayoutResourceId(){
        return R.layout.activity_file_view;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final TinyDB tinyDb = new TinyDB(getApplicationContext());
        String repoFullName = tinyDb.getString("repoFullName");
        String[] parts = repoFullName.split("/");
        final String repoOwner = parts[0];
        final String repoName = parts[1];
        final String instanceUrl = tinyDb.getString("instanceUrl");
        final String loginUid = tinyDb.getString("loginUid");
        final String instanceToken = "token " + tinyDb.getString(loginUid + "-token");

        ImageView closeActivity = findViewById(R.id.close);
        singleFileContents = findViewById(R.id.singleFileContents);
        singleCodeContents = findViewById(R.id.singleCodeContents);
        imageView = findViewById(R.id.imageView);
        mProgressBar = findViewById(R.id.progress_bar);
        pdfView = findViewById(R.id.pdfView);
        pdfViewFrame = findViewById(R.id.pdfViewFrame);
        singleFileContentsFrame = findViewById(R.id.singleFileContentsFrame);

        String singleFileName = getIntent().getStringExtra("singleFileName");

        TextView toolbar_title = findViewById(R.id.toolbar_title);
        toolbar_title.setMovementMethod(new ScrollingMovementMethod());

        initCloseListener();
        closeActivity.setOnClickListener(onClickListener);

        try {

            singleFileName = URLDecoder.decode(singleFileName, "UTF-8");
            singleFileName = singleFileName.replaceAll("//", "/");
            singleFileName = singleFileName.startsWith("/") ? singleFileName.substring(1) : singleFileName;

        }
        catch (UnsupportedEncodingException e) {

            assert singleFileName != null;
            Log.i("singleFileName", singleFileName);

        }

        toolbar_title.setText(singleFileName);

        getSingleFileContents(instanceUrl, instanceToken, repoOwner, repoName, singleFileName);

    }

    private void getSingleFileContents(String instanceUrl, String token, final String owner, String repo, final String filename) {

        final TinyDB tinyDb = new TinyDB(getApplicationContext());

        Call<Files> call = RetrofitClient
                .getInstance(instanceUrl, getApplicationContext())
                .getApiInterface()
                .getSingleFileContents(token, owner, repo, filename);

        call.enqueue(new Callback<Files>() {

            @Override
            public void onResponse(@NonNull Call<Files> call, @NonNull retrofit2.Response<Files> response) {

                if (response.code() == 200) {

                    AppUtil appUtil = new AppUtil();
                    assert response.body() != null;

                    if(!response.body().getContent().equals("")) {

                        String fileExtension = FilenameUtils.getExtension(filename);
                        mProgressBar.setVisibility(View.GONE);

                        if(appUtil.imageExtension(fileExtension)) { // file is image

                            singleFileContentsFrame.setVisibility(View.GONE);
                            singleCodeContents.setVisibility(View.GONE);
                            pdfViewFrame.setVisibility(View.GONE);
                            imageView.setVisibility(View.VISIBLE);

                            imageData = Base64.decode(response.body().getContent(), Base64.DEFAULT);
                            Drawable imageDrawable = new BitmapDrawable(getResources(), BitmapFactory.decodeByteArray(imageData, 0, imageData.length));
                            imageView.setImageDrawable(imageDrawable);

                        }
                        else if (appUtil.sourceCodeExtension(fileExtension)) { // file is sourcecode

                            imageView.setVisibility(View.GONE);
                            singleFileContentsFrame.setVisibility(View.GONE);
                            pdfViewFrame.setVisibility(View.GONE);
                            singleCodeContents.setVisibility(View.VISIBLE);

                            singleCodeContents.setTheme(Theme.GRUVBOX_DARK);
                            singleCodeContents.setShowLineNumbers(true);
                            singleCodeContents.setSource(appUtil.decodeBase64(response.body().getContent()));

                        }
                        else if (appUtil.pdfExtension(fileExtension)) { // file is pdf

                            imageView.setVisibility(View.GONE);
                            singleFileContentsFrame.setVisibility(View.GONE);
                            singleCodeContents.setVisibility(View.GONE);
                            pdfViewFrame.setVisibility(View.VISIBLE);

                            pdfNightMode = tinyDb.getBoolean("enablePdfMode");

                            decodedPdf = Base64.decode(response.body().getContent(), Base64.DEFAULT);
                            pdfView.fromBytes(decodedPdf)
                                    .enableSwipe(true)
                                    .swipeHorizontal(false)
                                    .enableDoubletap(true)
                                    .defaultPage(0)
                                    .enableAnnotationRendering(false)
                                    .password(null)
                                    .scrollHandle(null)
                                    .enableAntialiasing(true)
                                    .spacing(0)
                                    .autoSpacing(true)
                                    .pageFitPolicy(FitPolicy.WIDTH)
                                    .fitEachPage(true)
                                    .pageSnap(false)
                                    .pageFling(true)
                                    .nightMode(pdfNightMode)
                                    .load();

                        }
                        else { // file type not known - plain text view

                            imageView.setVisibility(View.GONE);
                            singleCodeContents.setVisibility(View.GONE);
                            pdfViewFrame.setVisibility(View.GONE);
                            singleFileContentsFrame.setVisibility(View.VISIBLE);

                            singleFileContents.setText(appUtil.decodeBase64(response.body().getContent()));

                        }

                    }
                    else {
                        singleFileContents.setText("");
                        mProgressBar.setVisibility(View.GONE);
                    }

                }
                else if(response.code() == 401) {

                    AlertDialogs.authorizationTokenRevokedDialog(ctx, getResources().getString(R.string.alertDialogTokenRevokedTitle),
                            getResources().getString(R.string.alertDialogTokenRevokedMessage),
                            getResources().getString(R.string.alertDialogTokenRevokedCopyNegativeButton),
                            getResources().getString(R.string.alertDialogTokenRevokedCopyPositiveButton));

                }
                else if(response.code() == 403) {

                    Toasty.info(ctx, ctx.getString(R.string.authorizeError));

                }
                else if(response.code() == 404) {

                    Toasty.info(ctx, ctx.getString(R.string.apiNotFound));

                }
                else {

                    Toasty.info(getApplicationContext(), getString(R.string.labelGeneralError));

                }

            }

            @Override
            public void onFailure(@NonNull Call<Files> call, @NonNull Throwable t) {
                Log.e("onFailure", t.toString());
            }
        });

    }

    private void initCloseListener() {
        onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getIntent().removeExtra("singleFileName");
                finish();
            }
        };
    }

}
