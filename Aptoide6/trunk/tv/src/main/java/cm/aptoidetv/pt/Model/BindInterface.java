package cm.aptoidetv.pt.Model;

import android.content.Context;

/**
 * Created by tdeus on 03-10-2014.
 */
public interface BindInterface {

    public String getName();
    public String getVersion();
    public String getImage();
    public String getCategory();
    public void startActivity(Context context);
    public String getDownloadUrl();

}
