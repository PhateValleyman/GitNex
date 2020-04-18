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
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentSkipListMap;

/**
 * Author opyale
 */

public class FilesDiffAdapter extends BaseAdapter {

	private static Map<Long, View> SELECTED_VIEWS;
	private static final int MAXIMUM_LINES = 5000;

	private static int COLOR_ADDED;
	private static int COLOR_REMOVED;
	private static int COLOR_NORMAL;
	private static int COLOR_SELECTED;

	private Context context;
	private List<FileDiffView> fileDiffViews;

	public FilesDiffAdapter(Context context, List<FileDiffView> fileDiffViews) {

		this.context = context;
		this.fileDiffViews = fileDiffViews;

		SELECTED_VIEWS = new ConcurrentSkipListMap<>();
		COLOR_ADDED = context.getResources().getColor(R.color.diffAddedColor);
		COLOR_REMOVED = context.getResources().getColor(R.color.diffRemovedColor);
		COLOR_NORMAL = context.getResources().getColor(R.color.white);
		COLOR_SELECTED = context.getResources().getColor(R.color.md_grey_300);

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

			diffLines.addView(getMessageView(context.getResources().getString(R.string.binaryFileError)));

		} else {

			String[] codeLines = getLines(data.getFileContents());

			if(MAXIMUM_LINES > codeLines.length) {

				for(int l=0; l<codeLines.length; l++) {

					if(codeLines[l].length() > 0) {

						int uniquePosition = l + (position * MAXIMUM_LINES);

						DiffTextView diffTextView = new DiffTextView(context);
						diffTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
						diffTextView.setPadding(15, 2, 15, 2);
						diffTextView.setTypeface(Typeface.createFromAsset(context.getAssets(), "fonts/sourcecodeproregular.ttf"));
						diffTextView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
						diffTextView.setPosition(uniquePosition);

						boolean check = true;

						for(View view : SELECTED_VIEWS.values()) {

							if(((DiffTextView) view).getPosition() == uniquePosition) {

								diffTextView.setBackgroundColor(COLOR_SELECTED);
								check = false;
								break;

							}

						}


						if(codeLines[l].startsWith("+")) {

							diffTextView.setText(codeLines[l]);
							diffTextView.setTextColor(context.getResources().getColor(R.color.colorPrimary));

							if(check) {
								diffTextView.setInitialBackgroundColor(COLOR_ADDED);
							}

						} else if(codeLines[l].startsWith("-")) {

							diffTextView.setText(codeLines[l]);
							diffTextView.setTextColor(context.getResources().getColor(R.color.colorPrimary));

							if(check) {
								diffTextView.setInitialBackgroundColor(COLOR_REMOVED);
							}

						} else {

							diffTextView.setText(codeLines[l]);
							diffTextView.setTextColor(context.getResources().getColor(R.color.colorPrimary));

							if(check) {
								diffTextView.setInitialBackgroundColor(COLOR_NORMAL);
							}

						}


						diffTextView.setOnClickListener(v -> {

							if(((DiffTextView) v).getCurrentBackgroundColor() != COLOR_SELECTED) {

								SELECTED_VIEWS.put(((DiffTextView) v).getPosition(), v);
								v.setBackgroundColor(COLOR_SELECTED);

							} else {

								SELECTED_VIEWS.remove(((DiffTextView) v).getPosition());
								v.setBackgroundColor(((DiffTextView) v).getInitialBackgroundColor());

							}

						});


						diffTextView.setOnLongClickListener(v -> {

							if(((DiffTextView) v).getCurrentBackgroundColor() == COLOR_SELECTED) {

								StringBuilder stringBuilder = new StringBuilder();
								stringBuilder.append("```\n");

								for(View view : SELECTED_VIEWS.values()) {

									stringBuilder.append(((DiffTextView) view).getText());
									stringBuilder.append("\n");

								}

								stringBuilder.append("```\n\n");

								SELECTED_VIEWS.clear();

								Intent intent = new Intent(context, ReplyToIssueActivity.class);
								intent.putExtra("commentBody", stringBuilder.toString());
								intent.putExtra("cursorToEnd", true);
								intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

								context.startActivity(intent);

							}

							return true;

						});

						diffLines.addView(diffTextView);

					}

				}

			} else {

				diffLines.addView(getMessageView(context.getResources().getString(R.string.fileTooLarge)));

			}

		}

		return convertView;

	}

	private TextView getMessageView(String message) {

		TextView textView = new TextView(context);
		textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
		textView.setPadding(15, 15, 15, 15);
		textView.setTypeface(Typeface.DEFAULT);
		textView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
		textView.setText(message);

		return textView;

	}

	private String[] getLines(String content) {

		return content.split("\\R");

	}

}
