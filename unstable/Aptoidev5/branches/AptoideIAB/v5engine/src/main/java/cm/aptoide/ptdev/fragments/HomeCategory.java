package cm.aptoide.ptdev.fragments;

/**
 * Created by rmateus on 09-07-2014.
 */
public class HomeCategory implements Home {
    private String categoryName;
    private final int parentId;

    public HomeCategory(String categoryName, int parentId) {

        this.categoryName = categoryName;
        this.parentId = parentId;
    }

    @Override
    public String getName() {
        return categoryName;
    }

    @Override
    public int getItemsSize() {
        return 0;
    }

    @Override
    public int getSortPriority() {
        return 0;
    }

    @Override
    public int getParentId() {
        return parentId;
    }
}
