package cm.aptoide.pt.dev;

import android.app.Application;
import android.content.Context;

/**
 * Created with IntelliJ IDEA.
 * User: rmateus
 * Date: 07-10-2013
 * Time: 15:19
 * To change this template use File | Settings | File Templates.
 */
public class Aptoide extends Application {

    private static Aptoide context;

    @Override
    public void onCreate() {
        super.onCreate();
        context = this;
    }

    public static Context getContext(){
        return context;
    }

}
