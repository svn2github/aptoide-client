package cm.aptoide.ptdev;



/**
 * Created with IntelliJ IDEA.
 * User: rmateus
 * Date: 12-11-2013
 * Time: 15:17
 * To change this template use File | Settings | File Templates.
 */
public class Category {


    private String name;
    private int appsNumber;
    private String type;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    private long id;


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setAppsNumber(int appsNumber) {
        this.appsNumber = appsNumber;
    }

    public int getAppsNumber() {
        return appsNumber;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
