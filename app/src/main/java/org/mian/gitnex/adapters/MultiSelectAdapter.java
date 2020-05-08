package org.mian.gitnex.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.TextView;
import org.mian.gitnex.R;
import org.mian.gitnex.models.MultiSelectModel;
import java.util.List;

/**
 * Author opyale
 */

public class MultiSelectAdapter extends BaseAdapter {

	private Context context;
	private List<MultiSelectModel> multiSelectModels;
	private List<Integer> selectedItemsIds;

	public MultiSelectAdapter(Context context, List<MultiSelectModel> multiSelectModels, List<Integer> selectedItemsIds) {

		this.context = context;
		this.multiSelectModels = multiSelectModels;
		this.selectedItemsIds = selectedItemsIds;

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

	@SuppressLint("ViewHolder")
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		MultiSelectModel multiSelectModel = (MultiSelectModel) getItem(position);
		convertView = LayoutInflater.from(context).inflate(R.layout.list_multi_select_item, parent, false);

		TextView textView = convertView.findViewById(R.id.textView);
		textView.setText(multiSelectModel.getName());

		CheckBox checkBox = convertView.findViewById(R.id.checkBox);
		checkBox.setChecked(multiSelectModel.isSelected());
		checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {

			if(isChecked) {
				multiSelectModel.setSelected(true);
				// selectedItemsIds.remove((Object) position);
			} else {
				multiSelectModel.setSelected(false);
				selectedItemsIds.add(multiSelectModel.getId());
			}

		});

		return convertView;

	}

}
