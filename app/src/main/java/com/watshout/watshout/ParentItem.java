package com.watshout.watshout;

import java.util.ArrayList;
import java.util.List;

public class ParentItem {

    private List<ChildItem> childItemList;

    public ParentItem() {
        childItemList = new ArrayList<ChildItem>();
    }

    public List<ChildItem> getChildItemList() {
        return childItemList;
    }
}

