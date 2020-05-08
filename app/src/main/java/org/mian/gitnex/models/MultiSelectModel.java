package org.mian.gitnex.models;

import java.util.ArrayList;
import java.util.List;

/**
 * Author com.github.abumoallim, modified by M M Arif, modified by opyale
 */

public class MultiSelectModel {

    private int id;
    private String name;
    private boolean isSelected;

    public MultiSelectModel(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }

    public static String resolveList(List<MultiSelectModel> multiSelectModels) {

    	StringBuilder stringBuilder = new StringBuilder();

    	for(int i=0; i<multiSelectModels.size(); i++) {

		    stringBuilder.append(multiSelectModels.get(i).getName());

    		if(i != (multiSelectModels.size() - 1)) {

			    stringBuilder.append(", ");
		    }
	    }

    	return stringBuilder.toString();

    }

    public static List<String> extractNamesFromList(List<MultiSelectModel> multiSelectModels) {

	    ArrayList<String> arrayList = new ArrayList<>();

    	for(MultiSelectModel multiSelectModel : multiSelectModels) {

    		arrayList.add(multiSelectModel.getName());
	    }

    	return arrayList;

    }

}
