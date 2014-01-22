package cm.aptoide.ptdev.tutorial;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;
import cm.aptoide.ptdev.R;

import java.util.ArrayList;

/**
 * Created with IntelliJ IDEA.
 * User: brutus
 * Date: 30-10-2013
 * Time: 12:06
 * To change this template use File | Settings | File Templates.
 */
public class Wizard {

    public static ArrayList<Fragment> getWizardNewToAptoide() {
        ArrayList<Fragment> wizard = new ArrayList<Fragment>();
        wizard.add(NewToAptoide1.newInstace());
        wizard.add(NewToAptoide2.newInstace());
        wizard.add(NewToAptoide3.newInstace());
        return wizard;
    }

    public static ArrayList<Fragment> getWizardUpdate() {
        ArrayList<Fragment> wizard = new ArrayList<Fragment>();
        wizard.add(NewFeature1.newInstace());
        wizard.add(NewFeature2.newInstace());
        wizard.add(NewFeature3.newInstace());
        wizard.add(NewFeature4.newInstace());
        return wizard;
    }

    public interface WizardCallback {

        public void getActions(ArrayList<Action> actions);
    }

    public static class NewToAptoide1 extends Fragment implements WizardCallback {

        public static NewToAptoide1 newInstace() {
            NewToAptoide1 fragment = new NewToAptoide1();
            Bundle args = new Bundle();
            args.putString("name", "NewToAptoide1");
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View view = inflater.inflate(R.layout.fragment_tutorial_new_to_aptoide, container, false);
            return view;
        }

        @Override
        public void onViewCreated(View view, Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
            TextView new_to_aptoide_description = (TextView) view.findViewById(R.id.new_to_aptoide_description);
            new_to_aptoide_description.setText("1 - Welcome to Aptoide");
        }

        @Override
        public void getActions(ArrayList<Action> actions) {

        }
    }

    public static class NewToAptoide2 extends Fragment implements WizardCallback {

        CheckBox cb_add_apps;
        TextView new_to_aptoide_description;

        public static NewToAptoide2 newInstace() {
            NewToAptoide2 fragment = new NewToAptoide2();

            Bundle args = new Bundle();
            args.putString("name", "NewToAptoide2");
            fragment.setArguments(args);

            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View view = inflater.inflate(R.layout.fragment_tutorial_new_to_aptoide, container, false);
            return view;
        }

        @Override
        public void onViewCreated(View view, Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);

            cb_add_apps = (CheckBox) view.findViewById(R.id.cb_add_apps);
            cb_add_apps.setVisibility(View.VISIBLE);
            cb_add_apps.setChecked(true);
            new_to_aptoide_description = (TextView) view.findViewById(R.id.new_to_aptoide_description);
            new_to_aptoide_description.setText("2 - Add more stores");
        }

        @Override
        public void getActions(ArrayList<Action> actions) {
            if (cb_add_apps.isChecked()) {
                actions.add(new Action() {
                    @Override
                    public void run() {
                        Log.d("Wizard", getArguments().getString("name") + " CheckBox1 was checked!");
                    }
                });
            }

        }
    }

    public static class NewToAptoide3 extends Fragment implements WizardCallback {

        public static NewToAptoide3 newInstace() {
            NewToAptoide3 fragment = new NewToAptoide3();

            Bundle args = new Bundle();
            args.putString("name", "NewToAptoide3");
            fragment.setArguments(args);

            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View view = inflater.inflate(R.layout.fragment_tutorial_new_to_aptoide, container, false);
            return view;
        }

        @Override
        public void onViewCreated(View view, Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
            TextView new_to_aptoide_description = (TextView) view.findViewById(R.id.new_to_aptoide_description);
            new_to_aptoide_description.setText("3 - Search thousands of apps");
        }

        @Override
        public void getActions(ArrayList<Action> actions) {

        }
    }

    public static class NewFeature1 extends Fragment implements WizardCallback {

        public static NewFeature1 newInstace() {
            NewFeature1 fragment = new NewFeature1();
            Bundle args = new Bundle();
            args.putString("name", "NewFeature1");
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View view = inflater.inflate(R.layout.fragment_tutorial_new_feature, container, false);
            return view;
        }

        @Override
        public void onViewCreated(View view, Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
            TextView main_description = (TextView) view.findViewById(R.id.main_description);
            main_description.setText("1- New Layout");
        }

        @Override
        public void getActions(ArrayList<Action> actions) {

        }
    }

    public static class NewFeature2 extends Fragment implements WizardCallback {

        public static NewFeature2 newInstace() {
            NewFeature2 fragment = new NewFeature2();
            Bundle args = new Bundle();
            args.putString("name", "NewFeature2");
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View view = inflater.inflate(R.layout.fragment_tutorial_new_feature, container, false);
            return view;
        }

        @Override
        public void onViewCreated(View view, Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);

            TextView main_description = (TextView) view.findViewById(R.id.main_description);
            main_description.setText("2- Rollback");
        }

        @Override
        public void getActions(ArrayList<Action> actions) {

        }
    }

    public static class NewFeature3 extends Fragment implements WizardCallback {

        public static NewFeature3 newInstace() {
            NewFeature3 fragment = new NewFeature3();
            Bundle args = new Bundle();
            args.putString("name", "NewFeature3");
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View view = inflater.inflate(R.layout.fragment_tutorial_new_feature, container, false);
            return view;
        }

        @Override
        public void onViewCreated(View view, Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);

            TextView main_description = (TextView) view.findViewById(R.id.main_description);
            main_description.setText("2- Widget");
        }

        @Override
        public void getActions(ArrayList<Action> actions) {

        }
    }

    public static class NewFeature4 extends Fragment implements WizardCallback {

        public static NewFeature4 newInstace() {
            NewFeature4 fragment = new NewFeature4();
            Bundle args = new Bundle();
            args.putString("name", "NewFeature4");
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View view = inflater.inflate(R.layout.fragment_tutorial_new_feature, container, false);
            return view;
        }

        @Override
        public void onViewCreated(View view, Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
            TextView main_description = (TextView) view.findViewById(R.id.main_description);
            main_description.setText("4- Account Manager");
        }

        @Override
        public void getActions(ArrayList<Action> actions) {

        }
    }
}
