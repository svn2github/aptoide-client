package cm.aptoidetv.pt.Model;

import android.content.Context;

import cm.aptoidetv.pt.CardPresenter;

/**
 * Created by tdeus on 03-10-2014.
 */
public interface BindInterface {


    public String getText(Context context);
    public String getName(Context context);
    public String getVersion();
    public String getDownloads();
    //public String getImage();
    public void startActivity(Context context);
    public String getDownloadUrl();

    public void setImage(int iconWidth, int iconHeight, CardPresenter.PicassoImageCardViewTarget picassoImageCardViewTarget);

    int getWidth();
    int getHeight();
}
