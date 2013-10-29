package cm.aptoide.ptdev.tutorial;

import android.app.Activity;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import cm.aptoide.ptdev.R;

import java.util.ArrayList;

/**
 * Created with IntelliJ IDEA.
 * User: brutus
 * Date: 28-10-2013
 * Time: 13:11
 * To change this template use File | Settings | File Templates.
 */
public class Tutorial extends Activity {

    public static final String FRAGMENTS_INTENT_KEY = "tutorial_fragments";

    private ArrayList<TutorialFragment> tutorial_fragments;
    private int currentFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tutorial_layout);

        if (getIntent().hasExtra(FRAGMENTS_INTENT_KEY)) {
            tutorial_fragments = (ArrayList<TutorialFragment>) getIntent().getParcelableArrayListExtra(FRAGMENTS_INTENT_KEY);
        } else {
            finish();
        }

        ((Button) findViewById(R.id.next)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeFragment(++currentFragment);
            }
        });

        ((Button) findViewById(R.id.previous)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentFragment--;
                getFragmentManager().popBackStack();
            }
        });

        ((Button) findViewById(R.id.finish)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        if (savedInstanceState == null) {
            TutorialFragment firstFragment = tutorial_fragments.get(0);
            FragmentTransaction ft = getFragmentManager().beginTransaction();
            ft.add(R.id.tutorial_fragment, firstFragment);
            ft.commit();
        }

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

    }

    private void changeFragment(int toPage) {
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.replace(R.id.tutorial_fragment, tutorial_fragments.get(toPage));
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        ft.addToBackStack(null);
        ft.commit();
    }

}
