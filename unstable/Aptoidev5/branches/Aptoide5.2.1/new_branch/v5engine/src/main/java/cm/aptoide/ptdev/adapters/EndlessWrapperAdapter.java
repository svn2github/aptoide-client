package cm.aptoide.ptdev.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.RotateAnimation;
import android.widget.BaseAdapter;
import android.widget.ListAdapter;

import com.commonsware.cwac.endless.EndlessAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import cm.aptoide.ptdev.R;
import cm.aptoide.ptdev.TimelineActivity;
import cm.aptoide.ptdev.fragments.FragmentSocialTimeline;
import cm.aptoide.ptdev.webservices.timeline.json.TimelineListAPKsJson;

/**
 * Created by rmateus on 25-09-2014.
 */

public class EndlessWrapperAdapter extends EndlessAdapter {

    private final Callback callback;

    public interface Callback{
        public void runRequest();
    }

    public EndlessWrapperAdapter(FragmentSocialTimeline.SubFragmentSocialTimeline callback, Context context, ArrayList<TimelineListAPKsJson.UserApk> list) {
        super(context, new TimelineAdapter(callback, context, list), 0);
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


}
