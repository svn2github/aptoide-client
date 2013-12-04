package cm.aptoide.ptdev.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import cm.aptoide.ptdev.R;
import cm.aptoide.ptdev.fragments.callbacks.DownloadManagerCallback;
import com.actionbarsherlock.app.SherlockListFragment;

/**
 * Created with IntelliJ IDEA.
 * User: rmateus
 * Date: 28-10-2013
 * Time: 11:40
 * To change this template use File | Settings | File Templates.
 */
public class FragmentDownloadManager extends SherlockListFragment {


    DownloadManagerCallback callback;


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.callback = (DownloadManagerCallback) activity;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        this.callback = null;
    }


}
