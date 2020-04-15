package org.mian.gitnex.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
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
import org.mian.gitnex.activities.ReplyToIssueActivity;
import org.mian.gitnex.helpers.DiffTextView;
import org.mian.gitnex.models.FileDiffView;
import java.util.ArrayList;
import java.util.List;

/**
 * Author opyale
 */

public class FilesDiffLinesAdapter extends BaseAdapter {

	private static List<View> selectedViews;

	private Context context;
	private List<FileDiffView> fileDiffViews;

	public FilesDiffLinesAdapter(Context context, List<FileDiffView> fileDiffViews) {
		this.context = context;
		this.fileDiffViews = fileDiffViews;

		selectedViews = new ArrayList<>();
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

		convertView = LayoutInflater.from(context).inflate(R.layout.list_files_diffs, null, false);

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

		} else {

			String[] codeLines = data.getFileContents().split("\\R");

			for(String codeLine : codeLines) {

				if(codeLine.length() > 0) {

					DiffTextView diffTextView = new DiffTextView(context);
					diffTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
					diffTextView.setPadding(5, 2, 5, 2);
					diffTextView.setTypeface(Typeface.createFromAsset(context.getAssets(), "fonts/sourcecodeproregular.ttf"));
					diffTextView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

					if(codeLine.startsWith("+")) {

						diffTextView.setText(codeLine);
						diffTextView.setTextColor(context.getResources().getColor(R.color.colorPrimary));
						diffTextView.setBackgroundColor(context.getResources().getColor(R.color.diffAddedColor));

					} else if(codeLine.startsWith("-")) {

						diffTextView.setText(codeLine);
						diffTextView.setTextColor(context.getResources().getColor(R.color.colorPrimary));
						diffTextView.setBackgroundColor(context.getResources().getColor(R.color.diffRemovedColor));

					} else {

						diffTextView.setText(codeLine);
						diffTextView.setTextColor(context.getResources().getColor(R.color.colorPrimary));
						diffTextView.setBackgroundColor(context.getResources().getColor(R.color.white));

					}

					diffTextView.setOnClickListener(v -> {

						if(!selectedViews.contains(v)) {
							selectedViews.add(v);
							v.setBackgroundColor(context.getResources().getColor(R.color.md_grey_200));
						} else {
							selectedViews.remove(v);
							v.setBackgroundColor(((DiffTextView) v).getInitialBackgroundColor());
						}

					});

					diffTextView.setOnLongClickListener(v -> {

						if(((DiffTextView) v).getCurrentBackgroundColor() == context.getResources().getColor(R.color.md_grey_200)) {

							StringBuilder stringBuilder = new StringBuilder();
							stringBuilder.append("```\n");

							for(View view : selectedViews) {

								stringBuilder.append(((DiffTextView) view).getText());
								stringBuilder.append("\n");

							}

							stringBuilder.append("```");

							selectedViews.clear();

							Intent intent = new Intent(context, ReplyToIssueActivity.class);
							intent.putExtra("commentBody", stringBuilder.toString());

							context.startActivity(intent);

						}

						return true;

					});

					diffLines.addView(diffTextView);

				}

			}

		}

		return convertView;

	}

}
