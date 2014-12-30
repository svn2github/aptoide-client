package cm.aptoidetv.pt.Model;

import android.content.Context;

/**
 * Created by tdeus on 03-10-2014.
 */
public interface BindInterface {

    public boolean isEditorsChoice();
    public String getText(Context context);
    public String getName(Context context);
    public String getVersion();
    public String getDownloads();
    public String getImage();
    public void startActivity(Context context);
    public String getDownloadUrl();

}
