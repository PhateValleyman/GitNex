package org.mian.gitnex.adapters;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import org.mian.gitnex.R;
import org.mian.gitnex.models.Issues;
import org.mian.gitnex.models.Labels;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

/**
 * Author M M Arif
 */

public class LabelsListAdapter extends RecyclerView.Adapter<LabelsListAdapter.LabelsViewHolder> {

	private ArrayList<Integer> currentLabelsIds;
	private List<Labels> labels;
	private ArrayList<String> labelsStrings = new ArrayList<>();
	private ArrayList<Integer> labelsIds = new ArrayList<>();

	private LabelsListAdapterListener labelsListener;

	public interface LabelsListAdapterListener {

		void labelsStringData(ArrayList<String> data);
		void labelsIdsData(ArrayList<Integer> data);
	}

	public LabelsListAdapter(List<Labels> labelsMain, LabelsListAdapterListener labelsListener, ArrayList<Integer> currentLabelsIds) {

		this.labels = labelsMain;
		this.labelsListener = labelsListener;
		this.currentLabelsIds = currentLabelsIds;
	}

	static class LabelsViewHolder extends RecyclerView.ViewHolder {

		private CheckBox labelSelection;
		private TextView labelText;
		private ImageView labelColor;

		private LabelsViewHolder(View itemView) {
			super(itemView);

			labelSelection = itemView.findViewById(R.id.labelSelection);
			labelText = itemView.findViewById(R.id.labelText);
			labelColor = itemView.findViewById(R.id.labelColor);

		}
	}

	@NonNull
	@Override
	public LabelsListAdapter.LabelsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

		View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.custom_labels_list, parent, false);
		return new LabelsListAdapter.LabelsViewHolder(v);
	}

	@Override
	public void onBindViewHolder(@NonNull LabelsListAdapter.LabelsViewHolder holder, int position) {

		Labels currentItem = labels.get(position);

		String labelColor = currentItem.getColor();
		int color = Color.parseColor("#" + labelColor);

		holder.labelText.setText(currentItem.getName());
		holder.labelColor.setBackgroundColor(color);

		for(int i = 0; i < labelsIds.size(); i++) {

			if(labelsStrings.contains(currentItem.getName())) {

				holder.labelSelection.setChecked(true);
			}
		}

		currentLabelsIds = new ArrayList<>(new LinkedHashSet<>(currentLabelsIds));
		for(int i = 0; i < currentLabelsIds.size(); i++) {

			if(currentLabelsIds.contains(currentItem.getId())) {

				holder.labelSelection.setChecked(true);
				labelsIds.add(currentLabelsIds.get(i));
			}
		}
		labelsListener.labelsIdsData(labelsIds);

		holder.labelSelection.setOnCheckedChangeListener((buttonView, isChecked) -> {

			if(isChecked) {

				labelsStrings.add(currentItem.getName());
				labelsIds.add(currentItem.getId());
			}
			else {

				labelsStrings.remove(currentItem.getName());
				labelsIds.remove(Integer.valueOf(currentItem.getId()));
			}

			labelsListener.labelsStringData(labelsStrings);
			labelsListener.labelsIdsData(labelsIds);
		});

		labelsIds = new ArrayList<>(new LinkedHashSet<>(labelsIds));
	}

	@Override
	public int getItemCount() {
		return labels.size();
	}

	public void updateList(ArrayList<Integer> list) {

		currentLabelsIds = list;
		notifyDataSetChanged();
	}
}
