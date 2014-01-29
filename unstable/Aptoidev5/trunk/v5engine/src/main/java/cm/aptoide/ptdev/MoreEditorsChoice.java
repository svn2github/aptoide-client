package cm.aptoide.ptdev;


import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.Fragment;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.GridLayout;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.AdapterView;

import android.widget.LinearLayout;
import android.widget.ListView;
import cm.aptoide.ptdev.adapters.HomeLayoutAdapter;
import cm.aptoide.ptdev.database.Database;
import cm.aptoide.ptdev.fragments.HomeItem;
import cm.aptoide.ptdev.model.Collection;

import java.util.AbstractCollection;
import java.util.ArrayList;
import java.util.Random;

/**
 * Created by rmateus on 28-01-2014.
 */
public class MoreEditorsChoice extends ActionBarActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.page_store);

        Fragment fragment = new MoreEditorsChoiceFragment();
        fragment.setArguments(getIntent().getExtras());
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, fragment).commit();

    }


    public static class MoreEditorsChoiceFragment extends ListFragment implements LoaderManager.LoaderCallbacks<ArrayList<Collection>> {
        private boolean mWasEndedAlready;



        private HomeLayoutAdapter adapter;
        private ArrayList<Collection> editorsChoice;

        @Override
        public void onViewCreated(View view, Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);

            editorsChoice = new ArrayList<Collection>();
            adapter = new HomeLayoutAdapter(getActivity(), editorsChoice, false);
            getLoaderManager().initLoader(0, getArguments(), this);

        }

        @Override
        public void onListItemClick(ListView l, View v, int position, long id) {
            super.onListItemClick(l, v, position, id);





            //collection.setExpanded(!collection.isExpanded());

        }

        @Override
        public Loader<ArrayList<Collection>> onCreateLoader(final int id, final Bundle args) {
            AsyncTaskLoader<ArrayList<Collection>> asyncTaskLoader = new AsyncTaskLoader<ArrayList<Collection>>(getActivity()) {
                @Override
                public ArrayList<Collection> loadInBackground() {
                    Log.d("Aptoide-Home", String.valueOf(args.getInt("parentId")));
                    return new Database(Aptoide.getDb()).getSpecificFeatured(args.getInt("parentId"), 3);

                }
            };

            asyncTaskLoader.forceLoad();
            return asyncTaskLoader;
        }

        @Override
        public void onLoadFinished(Loader<ArrayList<Collection>> loader, ArrayList<Collection> data) {

            editorsChoice.clear();
            editorsChoice.addAll(data);
            adapter.notifyDataSetChanged();
            setListAdapter(adapter);

        }

        @Override
        public void onLoaderReset(Loader<ArrayList<Collection>> loader) {

        }

    }



}
