package cm.aptoide.pt.model;

import android.util.Log;

/**
 * Created with IntelliJ IDEA.
 * User: rmateus
 * Date: 07-10-2013
 * Time: 16:13
 * To change this template use File | Settings | File Templates.
 */
public class MyCustomClass {



    @Override
    public void finalize() throws Throwable {
        Log.d("TAG", "Garbage Collecting CustomClass");
        super.finalize();
    }
}
