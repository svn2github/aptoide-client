package cm.aptoidetv.pt.Model;

import android.content.Context;
import android.content.Intent;

import cm.aptoidetv.pt.ScreenshotsViewer;


/**
 * Created by tdeus on 10-10-2014.
 */
public class Screenshot implements MediaObject {

    private String url;
    private String orient;

    public Screenshot(String path, String orient){
        this.url=path;
        this.orient = orient;
    }

    public String getImageUrl() {
        return url;
    }

    public String getOrient() {
        return orient;
    }

    @Override
    public void startActivity(Context context) {
        Intent intent = new Intent(context, ScreenshotsViewer.class);
        intent.putExtra(ScreenshotsViewer.SCREEN, getImageUrl());
        context.startActivity(intent);
    }

}
