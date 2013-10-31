package cm.aptoide.ptdev.tutorial;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import cm.aptoide.ptdev.R;
import cm.aptoide.ptdev.tutorial.Wizard.WizardCallback;
import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.app.SherlockFragmentActivity;

import java.util.ArrayList;

/**
 * Created with IntelliJ IDEA.
 * User: brutus
 * Date: 28-10-2013
 * Time: 13:11
 * To change this template use File | Settings | File Templates.
 */
public class Tutorial extends SherlockFragmentActivity {

    private int currentFragment;
    private int lastFragment;
    private WizardCallback currentFragmentListener;
    private WizardAdapter wAdapter;
    private ViewPager wPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tutorial_layout);

        final ArrayList<SherlockFragment> wizard_fragments = Wizard.getWizard();
        if (wizard_fragments.isEmpty()) {
            Log.e("Wizard", "The wizard doesn't have fragments");
            finish();
        }

        lastFragment = wizard_fragments.size() - 1;

        wAdapter = new WizardAdapter(getSupportFragmentManager(), wizard_fragments);

        wPager = (ViewPager) findViewById(R.id.pager);
        wPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i2) {
                //((WizardCallback) wizard_fragments.get(i)).checkActionEvents();

            }

            @Override
            public void onPageSelected(int i) {

                if (i != 0) {
                    ((WizardCallback) wizard_fragments.get(i - 1)).checkActionEvents();
                }
            }

            @Override
            public void onPageScrollStateChanged(int i) {
                //To change body of implemented methods use File | Settings | File Templates.
            }
        });
        wPager.setAdapter(wAdapter);

        ((Button) findViewById(R.id.next)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentFragment != lastFragment) {
                    wPager.setCurrentItem(++currentFragment);
                }
            }
        });

        ((Button) findViewById(R.id.previous)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentFragment != 0) {
                    wPager.setCurrentItem(--currentFragment);
                }
            }
        });

        ((Button) findViewById(R.id.finish)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        /*
        if (savedInstanceState == null) {
            Fragment firstFragment = tutorial_fragments.get(0);
            currentFragmentListener = (Wizard.WizardCallback) firstFragment;
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.add(R.id.tutorial_fragment, firstFragment);
            ft.commit();

        }*/
        if (savedInstanceState == null) {
            wPager.setCurrentItem(0);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

    }

    /*
    private void changeFragment(int toPage) {
        Fragment nextFragment = tutorial_fragments.get(toPage);
        currentFragmentListener = (Wizard.WizardCallback) nextFragment;

        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();

        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        //ft.addToBackStack(null);
        ft.commit();

    }*/

    public class WizardAdapter extends FragmentStatePagerAdapter {

        ArrayList<SherlockFragment> fragments;

        public WizardAdapter(FragmentManager fm, ArrayList<SherlockFragment> fragments) {
            super(fm);
            this.fragments = fragments;
        }

        @Override
        public Fragment getItem(int i) {
            SherlockFragment sf = fragments.get(i);
            currentFragmentListener = (WizardCallback) sf;
            return sf;
        }

        @Override
        public int getCount() {
            return fragments.size();
        }

    }

}
