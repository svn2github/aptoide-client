package cm.aptoide.ptdev.tutorial;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;
import cm.aptoide.ptdev.R;
import com.actionbarsherlock.app.SherlockFragment;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created with IntelliJ IDEA.
 * User: brutus
 * Date: 30-10-2013
 * Time: 12:06
 * To change this template use File | Settings | File Templates.
 */
public class Wizard {

    public static ArrayList<SherlockFragment> getWizard() {
        ArrayList<SherlockFragment> wizard = new ArrayList<SherlockFragment>();

        /*
        Class[] fragments = Wizard.class.getClasses();
        for (int i = 0; i < fragments.length; i++) {
            try {
                wizard.add((Fragment) fragments[i].newInstance());
                Log.d("wizard", fragments[i].getSimpleName());
            } catch (InstantiationException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            } catch (IllegalAccessException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        }
        */
        wizard.add(Fragment1.newInstace());
        wizard.add(Fragment2.newInstace());
        wizard.add(Fragment3.newInstace());
        wizard.add(Fragment4.newInstace());
        wizard.add(Fragment5.newInstace());


        return wizard;
    }

    public interface WizardCallback {
        public void checkActionEvents();
    }

    public static class Fragment1 extends SherlockFragment implements WizardCallback {

        CheckBox checkBox1;
        CheckBox checkBox2;
        CheckBox checkBox3;

        public static Fragment1 newInstace() {
            Fragment1 fragment = new Fragment1();

            Bundle args = new Bundle();
            args.putString("name", "Fragment 1");
            fragment.setArguments(args);

            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View view = inflater.inflate(R.layout.tutorial_fragment_layout, container, false);
            return view;
        }


        @Override
        public void onViewCreated(View view, Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
            ((TextView) view.findViewById(R.id.tutorialpage)).setText("Fragment 1");
            checkBox1 = (CheckBox) view.findViewById(R.id.checkbox1);
            checkBox2 = (CheckBox) view.findViewById(R.id.checkbox2);
            checkBox3 = (CheckBox) view.findViewById(R.id.checkbox3);
        }

        @Override
        public void checkActionEvents() {
            if (checkBox1.isChecked()) {
                Toast.makeText(getSherlockActivity(), getArguments().getString("name") + "CheckBox1 was checked!", Toast.LENGTH_SHORT).show();
            }
            if (checkBox2.isChecked()) {
                Toast.makeText(getSherlockActivity(), getArguments().getString("name") + "CheckBox2 was checked!", Toast.LENGTH_SHORT).show();
            }
            if (checkBox3.isChecked()) {
                Toast.makeText(getSherlockActivity(), getArguments().getString("name") + "CheckBox3 was checked!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public static class Fragment2 extends SherlockFragment implements WizardCallback{

        CheckBox checkBox1;
        CheckBox checkBox2;
        CheckBox checkBox3;

        public static Fragment2 newInstace() {
            Fragment2 fragment = new Fragment2();

            Bundle args = new Bundle();
            args.putString("name", "Fragment 2");
            fragment.setArguments(args);

            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View view = inflater.inflate(R.layout.tutorial_fragment_layout, container, false);
            return view;
        }

        @Override
        public void onViewCreated(View view, Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
            ((TextView) view.findViewById(R.id.tutorialpage)).setText("Fragment 2");

            checkBox1 = (CheckBox) view.findViewById(R.id.checkbox1);
            checkBox2 = (CheckBox) view.findViewById(R.id.checkbox2);
            checkBox3 = (CheckBox) view.findViewById(R.id.checkbox3);
        }

        @Override
        public void checkActionEvents() {
            if (checkBox1.isChecked()) {
                Toast.makeText(getSherlockActivity(), getArguments().getString("name") + "CheckBox1 was checked!", Toast.LENGTH_SHORT).show();
            }
            if (checkBox2.isChecked()) {
                Toast.makeText(getSherlockActivity(), getArguments().getString("name") + "CheckBox2 was checked!", Toast.LENGTH_SHORT).show();
            }
            if (checkBox3.isChecked()) {
                Toast.makeText(getSherlockActivity(), getArguments().getString("name") + "CheckBox3 was checked!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public static class Fragment3 extends SherlockFragment implements WizardCallback{

        CheckBox checkBox1;
        CheckBox checkBox2;
        CheckBox checkBox3;

        public static Fragment3 newInstace() {
            Fragment3 fragment = new Fragment3();

            Bundle args = new Bundle();
            args.putString("name", "Fragment 3");
            fragment.setArguments(args);

            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

            View view = inflater.inflate(R.layout.tutorial_fragment_layout, container, false);
            return view;
        }

        @Override
        public void onViewCreated(View view, Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
            ((TextView) view.findViewById(R.id.tutorialpage)).setText("Fragment 3");

            checkBox1 = (CheckBox) view.findViewById(R.id.checkbox1);
            checkBox2 = (CheckBox) view.findViewById(R.id.checkbox2);
            checkBox3 = (CheckBox) view.findViewById(R.id.checkbox3);
        }

        @Override
        public void checkActionEvents() {
            if (checkBox1.isChecked()) {
                Toast.makeText(getSherlockActivity(), getArguments().getString("name") + "CheckBox1 was checked!", Toast.LENGTH_SHORT).show();
            }
            if (checkBox2.isChecked()) {
                Toast.makeText(getSherlockActivity(), getArguments().getString("name") + "CheckBox2 was checked!", Toast.LENGTH_SHORT).show();
            }
            if (checkBox3.isChecked()) {
                Toast.makeText(getSherlockActivity(), getArguments().getString("name") + "CheckBox3 was checked!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public static class Fragment4 extends SherlockFragment implements WizardCallback{

        private CheckBox checkBox1;
        private CheckBox checkBox2;
        private CheckBox checkBox3;

        public static Fragment4 newInstace() {
            Fragment4 fragment = new Fragment4();

            Bundle args = new Bundle();
            args.putString("name", "Fragment 4");
            fragment.setArguments(args);

            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View view = inflater.inflate(R.layout.tutorial_fragment_layout, container, false);
            return view;
        }

        @Override
        public void onViewCreated(View view, Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
            ((TextView) view.findViewById(R.id.tutorialpage)).setText("Fragment 4");

            checkBox1 = (CheckBox) view.findViewById(R.id.checkbox1);
            checkBox2 = (CheckBox) view.findViewById(R.id.checkbox2);
            checkBox3 = (CheckBox) view.findViewById(R.id.checkbox3);
        }

        @Override
        public void checkActionEvents() {
            if (checkBox1.isChecked()) {
                Toast.makeText(getSherlockActivity(), getArguments().getString("name") + "CheckBox1 was checked!", Toast.LENGTH_SHORT).show();
            }
            if (checkBox2.isChecked()) {
                Toast.makeText(getSherlockActivity(), getArguments().getString("name") + "CheckBox2 was checked!", Toast.LENGTH_SHORT).show();
            }
            if (checkBox3.isChecked()) {
                Toast.makeText(getSherlockActivity(), getArguments().getString("name") + "CheckBox3 was checked!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public static class Fragment5 extends SherlockFragment implements WizardCallback {
        private CheckBox checkBox1;
        private CheckBox checkBox2;
        private CheckBox checkBox3;

        public static Fragment5 newInstace() {
            Fragment5 fragment = new Fragment5();

            Bundle args = new Bundle();
            args.putString("name", "Fragment 5");
            fragment.setArguments(args);

            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View view = inflater.inflate(R.layout.tutorial_fragment_layout, container, false);
            return view;
        }

        @Override
        public void onViewCreated(View view, Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
            ((TextView) view.findViewById(R.id.tutorialpage)).setText("Fragment 5");

            checkBox1 = (CheckBox) view.findViewById(R.id.checkbox1);
            checkBox2 = (CheckBox) view.findViewById(R.id.checkbox2);
            checkBox3 = (CheckBox) view.findViewById(R.id.checkbox3);
        }

        @Override
        public void checkActionEvents() {
            if (checkBox1.isChecked()) {
                Toast.makeText(getSherlockActivity(), getArguments().getString("name") + "CheckBox1 was checked!", Toast.LENGTH_SHORT).show();
            }
            if (checkBox2.isChecked()) {
                Toast.makeText(getSherlockActivity(), getArguments().getString("name") + "CheckBox2 was checked!", Toast.LENGTH_SHORT).show();
            }
            if (checkBox3.isChecked()) {
                Toast.makeText(getSherlockActivity(), getArguments().getString("name") + "CheckBox3 was checked!", Toast.LENGTH_SHORT).show();
            }
        }
    }

}
