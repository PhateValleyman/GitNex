package org.mian.gitnex.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
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

    private List<FileDiffView> dataList;
    private Context ctx;

    static class FilesDiffViewHolder extends RecyclerView.ViewHolder {

        private TextView fileContents;
        private TextView fileName;
        private TextView fileInfo;
        private ImageView fileImage;
        private HorizontalScrollView fileContentsView;

        private FilesDiffViewHolder(View itemView) {
            super(itemView);

            fileContents = itemView.findViewById(R.id.fileContents);
            fileName = itemView.findViewById(R.id.fileName);
            fileInfo = itemView.findViewById(R.id.fileInfo);
            fileImage = itemView.findViewById(R.id.fileImage);
            fileContentsView = itemView.findViewById(R.id.fileContentsView);

        }
    }

    public FilesDiffAdapter(List<FileDiffView> dataListMain, Context ctx) {
        this.dataList = dataListMain;
        this.ctx = ctx;
    }

    @NonNull
    @Override
    public FilesDiffAdapter.FilesDiffViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.files_diffs_list, parent, false);
        return new FilesDiffAdapter.FilesDiffViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull FilesDiffAdapter.FilesDiffViewHolder holder, int position) {

        FileDiffView data = dataList.get(position);

        if(data.isFileType()) {

            holder.fileName.setText(data.getFileName());

            holder.fileInfo.setVisibility(View.GONE);

            //byte[] imageData = Base64.decode(data.getFileContents(), Base64.DEFAULT);
            //Drawable imageDrawable = new BitmapDrawable(ctx.getResources(), BitmapFactory.decodeByteArray(imageData, 0, imageData.length));
            //holder.fileImage.setImageDrawable(imageDrawable);
            holder.fileContentsView.setVisibility(View.GONE);

        }
        else {

            holder.fileName.setText(data.getFileName());
            if(!data.getFileInfo().equals("")) {
                holder.fileInfo.setText(ctx.getResources().getString(R.string.fileDiffInfoChanges, data.getFileInfo()));
            }
            else {
                holder.fileInfo.setVisibility(View.GONE);
            }
            holder.fileContents.setText(data.getFileContents());

        }

    }

    @Override
    public int getItemCount() {
        return dataList.size();
    }

}