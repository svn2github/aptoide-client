package cm.aptoide.ptdev.adapters;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.*;
import android.util.Log;
import android.view.ViewGroup;
import cm.aptoide.ptdev.R;
import cm.aptoide.ptdev.fragments.*;

import java.lang.reflect.Field;

/**
 * Created with IntelliJ IDEA.
 * User: rmateus
 * Date: 28-10-2013
 * Time: 11:30
 * To change this template use File | Settings | File Templates.
 */
public class AptoidePagerAdapter extends FragmentStatePagerAdapter {


    private final boolean timeline;
    private String[] TITLES;

    public AptoidePagerAdapter(FragmentManager fm, Context context, boolean timeline) {
        super(fm);
        this.timeline = timeline;

        if(timeline){
            TITLES = new String[] { context.getString(R.string.home), context.getString(R.string.stores), context.getString(R.string.updates_tab),context.getString(R.string.social_timeline), context.getString(R.string.download_manager)};
        }else{
            TITLES = new String[] { context.getString(R.string.home), context.getString(R.string.stores), context.getString(R.string.updates_tab), context.getString(R.string.download_manager)};
        }

    }

    @Override
    public CharSequence getPageTitle(int position) {
        return TITLES[position];
    }

    @Override
    public int getCount() {
        return TITLES.length;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        final Object fragment = super.instantiateItem(container, position);
        try {
            final Field saveFragmentStateField = Fragment.class.getDeclaredField("mSavedFragmentState");
            saveFragmentStateField.setAccessible(true);
            final Bundle savedFragmentState = (Bundle) saveFragmentStateField.get(fragment);
            if (savedFragmentState != null) {
                savedFragmentState.setClassLoader(Fragment.class.getClassLoader());
            }
        } catch (Exception e) {
            Log.w("CustomFragmentStatePagerAdapter", "Could not get mSavedFragmentState field: " + e);
        }
        return fragment;
    }

    @Override
    public Fragment getItem(int position) {


        if(timeline) {

            switch (position) {
                case 0:
                    return new FragmentHome();
                case 1:
                    return new FragmentStores();
                case 2:
                    return new FragmentUpdates();
                case 3:
                    return new FragmentSocialTimeline();
                case 4:
                    return new FragmentDownloadManager();
            }
        }else{
            switch (position) {
                case 0:
                    return new FragmentHome();
                case 1:
                    return new FragmentStores();
                case 2:
                    return new FragmentUpdates();
                case 3:
                    return new FragmentDownloadManager();
            }
        }

        return null;
    }


}
