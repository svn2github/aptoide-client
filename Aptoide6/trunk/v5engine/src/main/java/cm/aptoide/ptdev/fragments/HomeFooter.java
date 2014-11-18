package cm.aptoide.ptdev.fragments;

/**
 * Created by rmateus on 09-07-2014.
 */
public class HomeFooter implements Home {
    private int parentId;

    public HomeFooter(Integer integer) {
        this.parentId = integer.intValue();
    }

    @Override
    public String getName() {
        return "Name";
    }

    @Override
    public int getItemsSize() {
        return 0;
    }

    @Override
    public int getSortPriority() {
        return 0;
    }

    public int getParentId() {
        return parentId;
    }

    public int isParentId() {
        return parentId;
    }


}
