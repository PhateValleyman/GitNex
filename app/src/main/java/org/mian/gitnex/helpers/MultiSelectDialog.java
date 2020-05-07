package org.mian.gitnex.helpers;

import android.app.AlertDialog;
import android.content.Context;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SearchView;
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
    private List<Integer> selectedItems;

    private boolean searchViewEnabled;

    protected MultiSelectDialog(Context context) {

        super(context);
        this.context = context;
    }

    protected MultiSelectDialog(Context context, int themeResId) {

        super(context, themeResId);
        this.context = context;
    }

    @Override
    public AlertDialog create() {

        LinearLayout linearLayout = new LinearLayout(context);
        linearLayout.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        if(searchViewEnabled) {

            SearchView searchView = new SearchView(context);
            searchView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
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

            linearLayout.addView(searchView);

        }

        multiSelectAdapter = new MultiSelectAdapter(context, multiSelectModels, selectedItems);

        ListView listView = new ListView(context);
        listView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        listView.setAdapter(multiSelectAdapter);

        setView(linearLayout);
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

        this.multiSelectModels = multiSelectModels;
    }

    public void setSelectedItems(List<Integer> selectedItems) {

        this.selectedItems = selectedItems;
    }

    public List<Integer> getSelectedItems() {

        return selectedItems;
    }

}