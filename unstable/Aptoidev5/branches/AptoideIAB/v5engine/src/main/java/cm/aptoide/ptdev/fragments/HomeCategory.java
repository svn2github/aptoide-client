package cm.aptoide.ptdev.fragments;

/**
 * Created by rmateus on 09-07-2014.
 */
public class HomeCategory implements Home {
    private String categoryName;

    public HomeCategory(String categoryName) {

        this.categoryName = categoryName;
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
}
