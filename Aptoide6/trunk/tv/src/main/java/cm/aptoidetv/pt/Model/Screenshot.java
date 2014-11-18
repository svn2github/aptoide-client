package cm.aptoidetv.pt.Model;

import android.content.Context;
import android.content.Intent;

import cm.aptoidetv.pt.ScreenshotsViewer;


/**
 * Created by tdeus on 10-10-2014.
 */
public class Screenshot implements MediaObject {

    private String url;

    public Screenshot(String path){
        this.url=path;
    }

    public String getImageUrl() {
        return url;
    }

    @Override
    public void startActivity(Context context) {
        Intent intent = new Intent(context, ScreenshotsViewer.class);
        intent.putExtra(ScreenshotsViewer.SCREEN, getImageUrl());
        context.startActivity(intent);
    }

}
