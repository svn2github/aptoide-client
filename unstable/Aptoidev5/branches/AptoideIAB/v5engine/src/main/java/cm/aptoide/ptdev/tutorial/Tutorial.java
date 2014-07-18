package cm.aptoide.ptdev.tutorial;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.flurry.android.FlurryAgent;

import cm.aptoide.ptdev.Aptoide;
import cm.aptoide.ptdev.AptoideThemePicker;
import cm.aptoide.ptdev.R;
import cm.aptoide.ptdev.tutorial.Wizard.WizardCallback;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * Created with IntelliJ IDEA.
 * User: brutus
 * Date: 28-10-2013
 * Time: 13:11
 * To change this template use File | Settings | File Templates.
 */
public class Tutorial extends ActionBarActivity {

    private int currentFragment;
    private int lastFragment;
    private ArrayList<Fragment> wizard_fragments;
    private ArrayList<Action> actionsToExecute = new ArrayList<Action>();
    private Button next, back;
    private boolean addDefaultRepo = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Aptoide.getThemePicker().setAptoideTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.page_tutorial);

        if(getIntent().hasExtra("isUpdate")){
            wizard_fragments = Wizard.getWizardUpdate();
            addDefaultRepo = false;
        }else{
            wizard_fragments = Wizard.getWizardNewToAptoide();
        }

        if (wizard_fragments.isEmpty()) {
            Log.e("Wizard", "The wizard doesn't have fragments");
            finish();
        }


        lastFragment = wizard_fragments.size() - 1;

        /*wAdapter = new WizardAdapter(getSupportFragmentManager(), wizard_fragments);

        wPager = (ViewPager) findViewById(R.id.pager);

        wPager.setAdapter(wAdapter);
        */

        next = (Button) findViewById(R.id.next);
        next.setOnClickListener(getNextListener());

        back = (Button) findViewById(R.id.back);
        back.setOnClickListener(getBackListener());

//        ((Button) findViewById(R.id.finish)).setOnClickListener(new View.OnClickListener() {
//
//            @Override
//            public void onClick(View v) {
//                if (currentFragment == lastFragment) {
//                    getFragmentsActions();
//
//                    runFragmentsActions();
//                }
//
//                finish();
//            }
//        });


        if (savedInstanceState == null) {
            Fragment firstFragment = wizard_fragments.get(0);
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.add(R.id.wizard_fragment, firstFragment);
            ft.commit();

        }

        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setTitle(R.string.app_name);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(Build.VERSION.SDK_INT >= 10) FlurryAgent.onStartSession(this, "X89WPPSKWQB2FT6B8F3X");

    }

    @Override
    public void finish() {

        if(addDefaultRepo){
            if(Build.VERSION.SDK_INT >= 10) FlurryAgent.logEvent("Wizard_Added_Apps_As_Default_Store");
            Intent data = new Intent();
            data.putExtra("addDefaultRepo", true);
            setResult(RESULT_OK, data);
            Log.d("Tutorial-addDefaultRepo","true");
        }else{
            if(Build.VERSION.SDK_INT >= 10) FlurryAgent.logEvent("Wizard_Did_Not_Add_Apps_As_Default_Store");
        }

        super.finish();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(Build.VERSION.SDK_INT >= 10) FlurryAgent.onEndSession(this);
    }

    private View.OnClickListener getBackListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(Build.VERSION.SDK_INT >= 10) FlurryAgent.logEvent("Wizard_Clicked_On_Back_Button");
                if (currentFragment != 0) {
                    changeFragment(--currentFragment);
                }
            }
        };
    }

    private View.OnClickListener getNextListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(Build.VERSION.SDK_INT >= 10) FlurryAgent.logEvent("Wizard_Clicked_On_Next_Button");
                if (currentFragment != lastFragment) {
                    changeFragment(++currentFragment);
                }else{
                    finish();
                }
            }
        };
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
        Iterator<Fragment> iterator = wizard_fragments.iterator();
        WizardCallback wizardCallback;
        while (iterator.hasNext()) {
            wizardCallback = (WizardCallback) iterator.next();
            wizardCallback.getActions(actionsToExecute);
        }
    }

    private void runFragmentsActions() {

        Iterator<Action> iterator = actionsToExecute.iterator();
        Action action;
        while (iterator.hasNext()) {
            action = iterator.next();
            action.run();
        }

    }


    public void setAddDefaultRepo(boolean addDefaultRepo){
        this.addDefaultRepo = addDefaultRepo;
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_wizard, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int i = item.getItemId();
        if (i == R.id.menu_skip) {
            if(Build.VERSION.SDK_INT >= 10) FlurryAgent.logEvent("Wizard_Skipped_Initial_Wizard");

            if (currentFragment == lastFragment) {
                getFragmentsActions();
                runFragmentsActions();
            }
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
}
