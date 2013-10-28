package cm.aptoide.ptdev.tutorial;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import cm.aptoide.ptdev.R;

/**
 * Created with IntelliJ IDEA.
 * User: brutus
 * Date: 28-10-2013
 * Time: 13:11
 * To change this template use File | Settings | File Templates.
 */
public class Tutorial extends Activity {

    private int tutorial_level;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tutorial_layout);

        ((Button)findViewById(R.id.next)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addFragment();
            }
        });

        ((Button)findViewById(R.id.previous)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    getFragmentManager().popBackStack();
            }
        });

        ((Button)findViewById(R.id.finish)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        if(savedInstanceState == null) {
            TutorialFragment firstFragment = TutorialFragment.newInstance(tutorial_level);
            FragmentTransaction ft = getFragmentManager().beginTransaction();
            ft.add(R.id.tutorial_fragment, firstFragment);
            ft.commit();
        } else {
            tutorial_level = savedInstanceState.getInt("LEVEL");
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("LEVEL", tutorial_level);
    }

    public void addFragment() {
        tutorial_level++;

        TutorialFragment tutorial_fragment = TutorialFragment.newInstance(tutorial_level);

        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.replace(R.id.tutorial_fragment, tutorial_fragment);
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        ft.addToBackStack(null);
        ft.commit();
    }

    public static class TutorialFragment extends Fragment {

        private int num;

        public static TutorialFragment newInstance(int num) {
            TutorialFragment tutorial_page = new TutorialFragment();

            Bundle args = new Bundle();
            args.putInt("num", num);

            return tutorial_page;
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            num = (getArguments() != null) ? getArguments().getInt("num") : 1;

        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

            View view = inflater.inflate(R.layout.tutorial_fragment_layout, container, false);
            ((TextView) view.findViewById(R.id.tutorialpage)).setText("#page: " + num);

            return view;
        }


    }
}
