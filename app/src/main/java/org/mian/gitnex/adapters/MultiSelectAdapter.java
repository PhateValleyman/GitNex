package org.mian.gitnex.adapters;

import android.content.Context;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
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

	public MultiSelectAdapter(Context context, List<MultiSelectModel> multiSelectModels, List<Integer> selectedItems) {

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

		LinearLayout linearLayout = new LinearLayout(context);
		linearLayout.setOrientation(LinearLayout.HORIZONTAL);
		linearLayout.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
		linearLayout.setPadding(15, 15, 15, 15);

		TextView textView = new TextView(context);
		textView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
		textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
		textView.setPadding(15, 0, 0, 0);
		textView.setText(multiSelectModel.getName());

		CheckBox checkBox = new CheckBox(context);
		checkBox.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
		checkBox.setChecked(multiSelectModel.isSelected());
		checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {

			if(isChecked) {
				multiSelectModel.setSelected(true);
				selectedItems.remove(multiSelectModel.getId());
			} else {
				multiSelectModel.setSelected(false);
				selectedItems.add(multiSelectModel.getId());
			}

		});

		linearLayout.addView(checkBox);
		linearLayout.addView(textView);

		return linearLayout;

	}

}
