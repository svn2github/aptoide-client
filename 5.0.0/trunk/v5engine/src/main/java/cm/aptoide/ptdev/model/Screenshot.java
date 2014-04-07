package cm.aptoide.ptdev.model;

/**
 * Created by tdeus on 2/12/14.
 */
public class Screenshot implements MediaObject {

    private String url;
    private String orient;

    private boolean isHd;

    public Screenshot(String path, String orient, boolean isHd){
        this.url=path;
        this.orient = orient;
        this.isHd = isHd;
    }



    public String getImageUrl() {
        return url;
    }

    public String getOrient() {
        return orient;
    }


    public boolean isHd() {
        return isHd;
    }
}
