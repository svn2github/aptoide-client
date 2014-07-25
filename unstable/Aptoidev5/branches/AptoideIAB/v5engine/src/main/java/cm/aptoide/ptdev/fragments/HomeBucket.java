package cm.aptoide.ptdev.fragments;

import java.util.ArrayList;

/**
 * Created by rmateus on 09-07-2014.
 */
public class HomeBucket implements Home {
    @Override
    public String getName() {
        return "Name";
    }


    public void setItems(ArrayList<HomeItem> items) {
        this.items = items;
    }

    ArrayList<HomeItem> items = new ArrayList<HomeItem>();



    public ArrayList<HomeItem> getItemsList(){
        return items;
    }
    public void addItem(HomeItem item){
        items.add(item);
    }

    @Override
    public int getItemsSize() {
        return items.size();
    }

    @Override
    public int getSortPriority() {
        return 0;
    }

    @Override
    public int getParentId() {
        return 0;
    }
}
