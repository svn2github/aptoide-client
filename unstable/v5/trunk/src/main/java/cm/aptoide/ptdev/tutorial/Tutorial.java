package cm.aptoide.ptdev.tutorial;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import cm.aptoide.ptdev.R;
import cm.aptoide.ptdev.tutorial.Wizard.WizardCallback;
import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.app.SherlockFragmentActivity;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
    private ArrayList<SherlockFragment> wizard_fragments;
    private ArrayList<Action> actionsToExecute = new ArrayList<Action>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tutorial_layout);

        wizard_fragments = Wizard.getWizard();
        if (wizard_fragments.isEmpty()) {
            Log.e("Wizard", "The wizard doesn't have fragments");
            finish();
        }

        lastFragment = wizard_fragments.size() - 1;

        /*wAdapter = new WizardAdapter(getSupportFragmentManager(), wizard_fragments);

        wPager = (ViewPager) findViewById(R.id.pager);

        wPager.setAdapter(wAdapter);
        */

        ((Button) findViewById(R.id.next)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentFragment != lastFragment) {
                    changeFragment(++currentFragment);
                }
            }
        });

        ((Button) findViewById(R.id.back)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentFragment != 0) {
                    changeFragment(--currentFragment);
                }
            }
        });

        ((Button) findViewById(R.id.finish)).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (currentFragment == lastFragment) {
                    getFragmentsActions();

                    runFragmentsActions();
                }

                finish();
            }
        });


        if (savedInstanceState == null) {
            Fragment firstFragment = wizard_fragments.get(0);
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.add(R.id.wizard_fragment, firstFragment);
            ft.commit();

        }

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

    }

    private void changeFragment(int toPage) {
        Fragment fragment = wizard_fragments.get(toPage);

        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.wizard_fragment, fragment);
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        //ft.addToBackStack(null);
        ft.commit();

    }

    private void getFragmentsActions() {
        Iterator<SherlockFragment> iterator = wizard_fragments.iterator();
        WizardCallback wizardCallback;
        while (iterator.hasNext()) {
            wizardCallback = (WizardCallback) iterator.next();
            wizardCallback.getActions(actionsToExecute);
        }
    }

    private void runFragmentsActions() {
        ExecutorService run_actions_thread = Executors.newSingleThreadExecutor();

        Iterator<Action> iterator = actionsToExecute.iterator();
        Action action;
        while (iterator.hasNext()) {
            action = iterator.next();
            run_actions_thread.submit(action);
        }

        run_actions_thread.shutdown();
    }

    /*
    public class WizardAdapter extends FragmentPagerAdapter {

        ArrayList<SherlockFragment> fragments;

        public WizardAdapter(FragmentManager fm, ArrayList<SherlockFragment> fragments) {
            super(fm);
            this.fragments = fragments;
        }

        @Override
        public Fragment getItem(int i) {
            return fragments.get(i);
        }

        @Override
        public int getCount() {
            return fragments.size();
        }

    } */

}
