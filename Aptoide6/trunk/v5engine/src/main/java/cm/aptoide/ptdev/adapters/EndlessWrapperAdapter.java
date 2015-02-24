package cm.aptoide.ptdev.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.commonsware.cwac.endless.EndlessAdapter;
import com.facebook.ads.NativeAd;

import java.util.ArrayList;

import cm.aptoide.ptdev.R;
import cm.aptoide.ptdev.fragments.FragmentSocialTimeline;
import cm.aptoide.ptdev.webservices.timeline.json.TimelineListAPKsJson;

/**
 * Created by rmateus on 25-09-2014.
 */

public class EndlessWrapperAdapter extends EndlessAdapter {

    private final Callback callback;
    TimelineAdapter tla;

    public interface Callback{
        public void runRequest();
    }

    public EndlessWrapperAdapter(TimelineAdapter tla,FragmentSocialTimeline.SubFragmentSocialTimeline callback, Context context) {
        super(context, tla, 0);
        this.tla=tla;
        this.callback = callback;
    }

    @Override
    protected View getPendingView(ViewGroup parent) {
        return LayoutInflater.from(getContext()).inflate(R.layout.progress_bar, parent, false);
    }

    @Override
    protected boolean cacheInBackground() throws Exception {
        callback.runRequest();
        return true;
    }


    @Override
    protected void appendCachedData() {}

    public synchronized TimelineListAPKsJson.UserApk addNativeAd(NativeAd ad,Context context,ArrayList<TimelineListAPKsJson.UserApk> list) {
        return tla.addNativeAd(ad,context,list);
    }

}
