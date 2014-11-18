package cm.aptoidetv.pt.Model;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

/**
 * Created by tdeus on 2/12/14.
 */
public class Video implements MediaObject {

    private String thumb;
    private String url;
    private String type;

    public Video(String thumb, String url){
        this.thumb=thumb;
        this.url=url;
    }

    public String getVideoUrl() { return url; }

    public String getImageUrl() {
        return thumb;
    }

    @Override
    public void startActivity(Context context) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(getVideoUrl()));
        context.startActivity(intent);
    }


}
