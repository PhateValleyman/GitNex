package org.mian.gitnex.adapters;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import org.mian.gitnex.R;
import org.mian.gitnex.models.FileDiffView;
import java.util.List;

/**
 * Author M M Arif
 */

public class FilesDiffAdapter extends RecyclerView.Adapter<FilesDiffAdapter.FilesDiffViewHolder> {

    private static final int COLOR_REMOVED = Color.RED;
    private static final int COLOR_ADDED = Color.GREEN;
    private static final int COLOR_CHANGED = Color.YELLOW;

    private List<FileDiffView> dataList;
    private Context ctx;

    static class FilesDiffViewHolder extends RecyclerView.ViewHolder {

        private TextView headerFileName;
        // private TextView fileInfo;
        private ImageView footerImage;
        private LinearLayout diffLines;

        private FilesDiffViewHolder(View itemView) {
            super(itemView);

            headerFileName = itemView.findViewById(R.id.headerFileName);
            // fileInfo = itemView.findViewById(R.id.fileInfo);
            footerImage = itemView.findViewById(R.id.footerImage);
            diffLines = itemView.findViewById(R.id.diffLines);

        }
    }

    public FilesDiffAdapter(List<FileDiffView> dataListMain, Context ctx) {
        this.dataList = dataListMain;
        this.ctx = ctx;
    }

    @NonNull
    @Override
    public FilesDiffAdapter.FilesDiffViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_files_diffs_new, parent, false);
        return new FilesDiffAdapter.FilesDiffViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull FilesDiffViewHolder holder, int position) {

        FileDiffView data = dataList.get(position);

        if(data.isFileType()) {

            holder.headerFileName.setText(data.getFileName());

            TextView textView = new TextView(ctx);
            textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
            textView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

            textView.setText("Filetype is binary and cannot be shown.");
            holder.diffLines.addView(textView);

            // holder.fileInfo.setVisibility(View.GONE);

            // byte[] imageData = Base64.decode(data.getFileContents(), Base64.DEFAULT);
            // Drawable imageDrawable = new BitmapDrawable(ctx.getResources(), BitmapFactory.decodeByteArray(imageData, 0, imageData.length));
            // holder.fileImage.setImageDrawable(imageDrawable);
            // holder.fileContentsView.setVisibility(View.GONE);

        }
        else {

            String[] splitData = data.getFileContents().split("\\R");

            for(int i=0; i<splitData.length; i++) {

                LinearLayout linearLayout = new LinearLayout(ctx);
                linearLayout.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

                TextView textLine = new TextView(ctx);
                textLine.setGravity(0);
                textLine.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
                textLine.setPadding(5, 2, 5, 2);
                textLine.setTypeface(Typeface.createFromAsset(ctx.getAssets(), "fonts/sourcecodeproregular.ttf"));
                textLine.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

                String line = splitData[i];

                if (line.startsWith("+")) {

                    textLine.setText(line);
                    textLine.setTextColor(ctx.getResources().getColor(R.color.colorPrimary));
                    textLine.setBackgroundColor(ctx.getResources().getColor(R.color.diffAddedColor));

                }
                else if (line.startsWith("-")) {

                    textLine.setText(line);
                    textLine.setTextColor(ctx.getResources().getColor(R.color.colorPrimary));
                    textLine.setBackgroundColor(ctx.getResources().getColor(R.color.diffRemovedColor));

                }
                else {

                    if(line.length() > 0) {

                        textLine.setText(line);
                        textLine.setTextColor(ctx.getResources().getColor(R.color.colorPrimary));
                        textLine.setBackgroundColor(ctx.getResources().getColor(R.color.white));

                    }

                }

                linearLayout.addView(textLine);

                holder.diffLines.addView(linearLayout);

            }

            holder.headerFileName.setText(data.getFileName());

            /*
            if(!data.getFileInfo().equals("")) {
                holder.fileInfo.setText(ctx.getResources().getString(R.string.fileDiffInfoChanges, data.getFileInfo()));
            }
            else {
                holder.fileInfo.setVisibility(View.GONE);
            }
             */

        }

    }

    @Override
    public int getItemCount() {
        return dataList.size();
    }

}