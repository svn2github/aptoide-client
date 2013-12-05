package cm.aptoide.ptdev.fragments;

/**
 * Created with IntelliJ IDEA.
 * User: rmateus
 * Date: 27-11-2013
 * Time: 15:39
 * To change this template use File | Settings | File Templates.
 */
public class HomeItem {


    public long getId() {
        return id;
    }

    private final long id;

    public String getCategory() {
        return category;
    }

    public String getIcon() {
        return icon;
    }

    public String getName() {
        return name;
    }

    private String name;
    private String icon;
    private String category;


    public HomeItem( String name, String category, String icon, long id) {
        this.category = category;
        this.icon = icon;
        this.name = name;
        this.id = id;
    }
}
