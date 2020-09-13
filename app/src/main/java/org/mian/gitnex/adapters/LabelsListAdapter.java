package org.mian.gitnex.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import org.mian.gitnex.R;
import org.mian.gitnex.models.Labels;
import java.util.List;

/**
 * Author M M Arif
 */

public class LabelsListAdapter extends RecyclerView.Adapter<LabelsListAdapter.LabelsViewHolder> {

	private List<Labels> labels;

	static class LabelsViewHolder extends RecyclerView.ViewHolder {

		private CheckBox labelSelection;

		private LabelsViewHolder(View itemView) {
			super(itemView);

			labelSelection = itemView.findViewById(R.id.labelSelection);

		}
	}

	public LabelsListAdapter(List<Labels> labelsMain) {
		this.labels = labelsMain;
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

		holder.labelSelection.setText(currentItem.getName());

	}

	@Override
	public int getItemCount() {
		return labels.size();
	}
}
