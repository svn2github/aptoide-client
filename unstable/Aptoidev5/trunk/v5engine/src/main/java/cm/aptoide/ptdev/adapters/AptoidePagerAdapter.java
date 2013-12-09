package cm.aptoide.ptdev.adapters;

import android.content.Context;
import android.support.v4.app.FixedFragmentStatePagerAdapter;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import cm.aptoide.ptdev.R;
import cm.aptoide.ptdev.fragments.FragmentDownloadManager;
import cm.aptoide.ptdev.fragments.FragmentHome;
import cm.aptoide.ptdev.fragments.FragmentStores;
import cm.aptoide.ptdev.fragments.FragmentUpdates;

/**
 * Created with IntelliJ IDEA.
 * User: rmateus
 * Date: 28-10-2013
 * Time: 11:30
 * To change this template use File | Settings | File Templates.
 */
public class AptoidePagerAdapter extends FixedFragmentStatePagerAdapter {


    private String[] TITLES;

    public AptoidePagerAdapter(FragmentManager fm, Context context) {
        super(fm);
        TITLES = new String[] { context.getString(R.string.home), context.getString(R.string.stores), context.getString(R.string.updates), context.getString(R.string.download_manager)};

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
    public Fragment getItem(int position) {

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

        return null;
    }


}
