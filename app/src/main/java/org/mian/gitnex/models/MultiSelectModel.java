package org.mian.gitnex.models;

/**
 * Author com.github.abumoallim, modified by M M Arif, modified by opyale
 */

public class MultiSelectModel {

    private Integer id;
    private String name;
    private Boolean isSelected;

    public MultiSelectModel(Integer id, String name) {
        this.id = id;
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Boolean isSelected() {
        return isSelected;
    }

    public void setSelected(Boolean selected) {
        isSelected = selected;
    }

}
