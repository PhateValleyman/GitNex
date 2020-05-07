package org.mian.gitnex.adapters;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CheckedTextView;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import org.mian.gitnex.models.MultiSelectModel;
import java.util.List;

/**
 * Author opyale
 */

public class MultiSelectAdapter extends BaseAdapter {

	private Context context;
	private List<MultiSelectModel> multiSelectModels;
	private List<Integer> selectedItems;

	MultiSelectAdapter(Context context, List<MultiSelectModel> multiSelectModels, List<Integer> selectedItems) {

		this.context = context;
		this.multiSelectModels = multiSelectModels;
		this.selectedItems = selectedItems;

	}

	@Override
	public int getCount() {

		return multiSelectModels.size();
	}

	@Override
	public Object getItem(int position) {

		return multiSelectModels.get(position);
	}

	@Override
	public long getItemId(int position) {

		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		MultiSelectModel multiSelectModel = (MultiSelectModel) getItem(position);

		convertView = new LinearLayout(context);
		convertView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
		convertView.setPadding(15, 15, 15, 15);

		TextView textView = new TextView(context);
		textView.setText(multiSelectModel.getName());

		CheckBox checkBox = new CheckBox(context);
		checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {

			if(isChecked) {
				selectedItems.remove(multiSelectModel.getId());
			} else {
				selectedItems.add(multiSelectModel.getId());
			}

		});

		return convertView;

	}

}
