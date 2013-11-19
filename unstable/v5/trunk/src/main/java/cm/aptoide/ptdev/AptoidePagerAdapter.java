package cm.aptoide.ptdev;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import cm.aptoide.ptdev.fragments.FragmentDownloadManager;
import cm.aptoide.ptdev.fragments.FragmentHome;
import cm.aptoide.ptdev.fragments.FragmentStores;
import cm.aptoide.ptdev.fragments.FragmentUpdates;
import com.astuetz.viewpager.extensions.PagerSlidingTabStrip;

/**
 * Created with IntelliJ IDEA.
 * User: rmateus
 * Date: 28-10-2013
 * Time: 11:30
 * To change this template use File | Settings | File Templates.
 */
public class AptoidePagerAdapter extends FragmentStatePagerAdapter {


    private final String[] TITLES = { "Home", "Stores", "Updates", "Download Manager" };

    public AptoidePagerAdapter(FragmentManager fm) {
        super(fm);
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
