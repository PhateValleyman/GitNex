package org.mian.gitnex.helpers;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SearchView;
import androidx.annotation.NonNull;
import org.mian.gitnex.R;
import org.mian.gitnex.adapters.MultiSelectAdapter;
import org.mian.gitnex.models.MultiSelectModel;
import java.util.ArrayList;
import java.util.List;

/**
 * Author opyale
 */

public class MultiSelectDialog extends AlertDialog.Builder {

    private Context context;
	private MultiSelectAdapter multiSelectAdapter;

    private List<MultiSelectModel> multiSelectModels;
    private List<Integer> selectedItemIds;

    private boolean searchViewEnabled = true;

    public MultiSelectDialog(Context context) {

        super(context);

	    this.multiSelectModels = new ArrayList<>();
	    this.selectedItemIds = new ArrayList<>();
	    this.context = context;
    }

    protected MultiSelectDialog(Context context, int themeResId) {

        super(context, themeResId);

        this.multiSelectModels = new ArrayList<>();
        this.selectedItemIds = new ArrayList<>();
        this.context = context;
    }

    @Override
    public AlertDialog create() {

	    View mainView = LayoutInflater.from(context).inflate(R.layout.layout_multi_select_dialog, null);
	    setView(mainView);

        if(searchViewEnabled) {

            SearchView searchView = mainView.findViewById(R.id.searchView);
            searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

	            @Override
	            public boolean onQueryTextSubmit(String query) {

		            return false;
	            }

	            @Override
	            public boolean onQueryTextChange(String newText) {

	            	multiSelectModels = filterItems(newText);
					multiSelectAdapter.notifyDataSetChanged();

		            return false;

	            }
            });

        }

        multiSelectAdapter = new MultiSelectAdapter(context, multiSelectModels, selectedItemIds);

        ListView listView = mainView.findViewById(R.id.listView);
        listView.setAdapter(multiSelectAdapter);
	    listView.setOnItemClickListener(null);
	    listView.setDivider(null);

        return super.create();

    }

	private List<MultiSelectModel> filterItems(String query) {

    	List<MultiSelectModel> multiSelectModels = new ArrayList<>();

    	for(MultiSelectModel multiSelectModel : this.multiSelectModels) {

    		if(multiSelectModel.getName().startsWith(query)) {
    			multiSelectModels.add(multiSelectModel);
		    }
	    }

    	return multiSelectModels;

    }

    public void enableSearchView(boolean searchViewEnabled) {

        this.searchViewEnabled = searchViewEnabled;
    }

    public void setItems(List<MultiSelectModel> multiSelectModels) {

	    this.multiSelectModels.addAll(multiSelectModels);
    }

    public void setSelectedItemIds(List<Integer> selectedItemIds) {

	    this.selectedItemIds.addAll(selectedItemIds);

	    for(MultiSelectModel multiSelectModel : multiSelectModels) {

		    for(Integer selectedItemId : selectedItemIds) {

			    if(multiSelectModel.getId() == selectedItemId) {

				    multiSelectModel.setSelected(true);
			    } else {

				    break;
			    }
		    }
	    }

    }

	public List<MultiSelectModel> getSelectedModels() {

    	List<MultiSelectModel> multiSelectModels = new ArrayList<>();

    	for(MultiSelectModel multiSelectModel : this.multiSelectModels) {

    		if(selectedItemIds.contains(multiSelectModel.getId())) {

    			multiSelectModels.add(multiSelectModel);
		    }
	    }

        return multiSelectModels;
    }

	public List<Integer> getSelectedItemIds() {

		return selectedItemIds;
	}

}