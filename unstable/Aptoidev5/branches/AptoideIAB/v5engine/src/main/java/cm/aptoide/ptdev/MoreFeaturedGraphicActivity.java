package cm.aptoide.ptdev;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.widget.CursorAdapter;
import android.support.v7.app.ActionBarActivity;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import cm.aptoide.ptdev.database.Database;
import cm.aptoide.ptdev.fragments.HomeItem;
import cm.aptoide.ptdev.utils.SimpleCursorLoader;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

/**
 * Created by rmateus on 27-06-2014.
 */
public class MoreFeaturedGraphicActivity extends ActionBarActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        Aptoide.getThemePicker().setAptoideTheme(this);


        super.onCreate(savedInstanceState);
        setContentView(R.layout.page_store);

        FeaturedGraphicFragment featuredGraphicFragment = new FeaturedGraphicFragment();

        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, featuredGraphicFragment).commit();
    }

    public static class FeaturedGraphicAdapter extends CursorAdapter{
        DisplayImageOptions options = new DisplayImageOptions.Builder().cacheInMemory(true).cacheOnDisc(true).resetViewBeforeLoading(true).build();
        public FeaturedGraphicAdapter(Context context, Cursor c) {
            super(context, c, 0);
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            return LayoutInflater.from(context).inflate(R.layout.row_app_home_featured, parent, false);
        }

        @Override
        public void bindView(View viewById, Context context, Cursor cursor) {
            TextView tv = (TextView) viewById.findViewById(R.id.app_name);
            tv.setText(cursor.getString(cursor.getColumnIndex("name")));
            ImageView iv = (ImageView) viewById.findViewById(R.id.app_icon);
            ImageLoader.getInstance().displayImage(cursor.getString(cursor.getColumnIndex("featgraph")), iv, options);
        }
    }

    public static class FeaturedGraphicFragment extends ListFragment implements LoaderManager.LoaderCallbacks<Cursor>{

        CursorAdapter cursorAdapter ;


        @Override
        public void onAttach(Activity activity) {
            super.onAttach(activity);
            cursorAdapter = new FeaturedGraphicAdapter(activity, null);
        }

        @Override
        public void onResume() {
            super.onResume();
            setListAdapter(cursorAdapter);
            getLoaderManager().initLoader(90, null, this);
        }

        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle args) {
            return new SimpleCursorLoader(getActivity()) {
                @Override
                public Cursor loadInBackground() {
                    return new Database(Aptoide.getDb()).getAllFeaturedGraphics();
                }
            };
        }

        @Override
        public void onViewCreated(View view, Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
            getListView().setItemsCanFocus(true);
        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
            cursorAdapter.swapCursor(data);

        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader) {
            cursorAdapter.swapCursor(null);
        }
    }
}
