package org.mian.gitnex.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Typeface;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import org.mian.gitnex.R;
import org.mian.gitnex.models.FileDiffView;
import java.util.List;

/**
 * Author opyale
 */

public class FilesDiffLinesAdapter extends BaseAdapter {

	private Context context;
	private List<FileDiffView> fileDiffViews;

	public FilesDiffLinesAdapter(Context context, List<FileDiffView> fileDiffViews) {
		this.context = context;
		this.fileDiffViews = fileDiffViews;
	}

	@Override
	public int getCount() {
		return fileDiffViews.size();
	}

	@Override
	public Object getItem(int position) {
		return fileDiffViews.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@SuppressLint({"ViewHolder", "InflateParams"})
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		convertView = LayoutInflater.from(context).inflate(R.layout.list_files_diffs_new, null, false);

		TextView headerFileName = convertView.findViewById(R.id.headerFileName);
		TextView headerFileInfo = convertView.findViewById(R.id.headerFileInfo);
		ImageView footerImage = convertView.findViewById(R.id.footerImage);
		LinearLayout diffLines = convertView.findViewById(R.id.diffLines);

		FileDiffView data = (FileDiffView) getItem(position);
		headerFileName.setText(data.getFileName());
		headerFileInfo.setText(data.getFileInfo());

		if(data.isFileType()) {

			TextView textView = new TextView(context);
			textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
			textView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

			textView.setText("Binary file cannot be shown.");
			diffLines.addView(textView);

			// holder.fileInfo.setVisibility(View.GONE);

			// byte[] imageData = Base64.decode(data.getFileContents(), Base64.DEFAULT);
			// Drawable imageDrawable = new BitmapDrawable(ctx.getResources(), BitmapFactory.decodeByteArray(imageData, 0, imageData.length));
			// holder.fileImage.setImageDrawable(imageDrawable);
			// holder.fileContentsView.setVisibility(View.GONE);

		} else {

			String[] codeLines = data.getFileContents().split("\\R");

			for(String codeLine : codeLines) {

				if(codeLine.length() > 0) {

					TextView textView = new TextView(context);
					textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
					textView.setPadding(5, 2, 5, 2);
					textView.setTypeface(Typeface.createFromAsset(context.getAssets(), "fonts/sourcecodeproregular.ttf"));
					textView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

					if(codeLine.startsWith("+")) {

						textView.setText(codeLine);
						textView.setTextColor(context.getResources().getColor(R.color.colorPrimary));
						textView.setBackgroundColor(context.getResources().getColor(R.color.diffAddedColor));

					} else if(codeLine.startsWith("-")) {

						textView.setText(codeLine);
						textView.setTextColor(context.getResources().getColor(R.color.colorPrimary));
						textView.setBackgroundColor(context.getResources().getColor(R.color.diffRemovedColor));

					} else {

						textView.setText(codeLine);
						textView.setTextColor(context.getResources().getColor(R.color.colorPrimary));
						textView.setBackgroundColor(context.getResources().getColor(R.color.white));

					}

					/*

					textView.setOnFocusChangeListener(new View.OnFocusChangeListener() {

						@Override
						public void onFocusChange(View v, boolean hasFocus) {

							if(hasFocus) {
								v.setBackgroundColor(Color.GRAY);
							} else {

							}

						}
					});

					textView.setOnClickListener(new View.OnClickListener() {

						@Override
						public void onClick(View v) {

						}
					});

					 */

					diffLines.addView(textView);

				}

			}

		}

		return convertView;

	}

}
