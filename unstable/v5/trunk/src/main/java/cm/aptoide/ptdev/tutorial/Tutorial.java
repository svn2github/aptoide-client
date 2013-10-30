package cm.aptoide.ptdev.tutorial;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import cm.aptoide.ptdev.R;
import com.actionbarsherlock.app.SherlockFragmentActivity;

import java.util.ArrayList;

/**
 * Created with IntelliJ IDEA.
 * User: brutus
 * Date: 28-10-2013
 * Time: 13:11
 * To change this template use File | Settings | File Templates.
 */
public class Tutorial extends SherlockFragmentActivity implements Wizard.WizardCallback {

    public static final String FRAGMENTS_INTENT_KEY = "tutorial_fragments";
    private ArrayList<Fragment> tutorial_fragments;
    private int currentFragment;
    private int lastFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tutorial_layout);

        /*
        if (getIntent().hasExtra(FRAGMENTS_INTENT_KEY)) {
            tutorial_fragments = (ArrayList<TutorialFragment>) getIntent().getParcelableArrayListExtra(FRAGMENTS_INTENT_KEY);
        } else {
            finish();
        }
        */
        tutorial_fragments = Wizard.getWizard();
        if (tutorial_fragments.isEmpty()) {
            Log.e("Tutorial", "The wizard doesn't have fragments");
            finish();
        }

        lastFragment = tutorial_fragments.size() - 1;

        ((Button) findViewById(R.id.next)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentFragment != lastFragment) {
                    changeFragment(++currentFragment);
                }
            }
        });

        ((Button) findViewById(R.id.previous)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               if (currentFragment != 0) {
                    changeFragment(--currentFragment);
                    //getFragmentManager().popBackStack();
               }
            }
        });

        ((Button) findViewById(R.id.finish)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        if (savedInstanceState == null) {
            Fragment firstFragment = tutorial_fragments.get(0);
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.add(R.id.tutorial_fragment, firstFragment, "0");
            ft.commit();

        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

    }

    private void changeFragment(int toPage) {


        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.tutorial_fragment, tutorial_fragments.get(toPage));
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        //ft.addToBackStack(null);
        ft.commit();

    }


    @Override
    public void clicked(String which_fragment) {
        Toast.makeText(this, "Callback from: " + which_fragment, Toast.LENGTH_SHORT).show();
    }
}
