package cm.aptoide.ptdev.fragments;


import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import cm.aptoide.ptdev.Aptoide;
import cm.aptoide.ptdev.EnumStoreTheme;
import cm.aptoide.ptdev.R;
import cm.aptoide.ptdev.database.Database;
import cm.aptoide.ptdev.database.schema.Schema;
import cm.aptoide.ptdev.utils.SimpleCursorLoader;
import com.nostra13.universalimageloader.core.ImageLoader;

/**
 * Created with IntelliJ IDEA.
 * User: rmateus
 * Date: 11-11-2013
 * Time: 17:24
 * To change this template use File | Settings | File Templates.
 */
public class FragmentStoreHeader extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>, FragmentStore{

    TextView banner_store_name;
    TextView banner_description;
    ImageView avatar;
    View view_gradient;

//    FrameLayout store_background;
//    FrameLayout.LayoutParams params;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View header = inflater.inflate(R.layout.header_store_theme, container, false);
        banner_store_name = (TextView) header.findViewById(R.id.banner_store_name);
        banner_description = (TextView) header.findViewById(R.id.store_description);
        avatar = (ImageView) header.findViewById(R.id.banner_store_avatar);
        view_gradient = header.findViewById(R.id.view_gradient);

//        store_background = (FrameLayout) header.findViewById(R.id.banner_background_layout);
//
//        if ((getResources().getConfiguration().screenLayout &
//                Configuration.SCREENLAYOUT_SIZE_MASK) ==
//                Configuration.SCREENLAYOUT_SIZE_LARGE) {
//            params = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, 200);
//            store_background.setLayoutParams(params);
//        }

        //Drawable drawable = getSherlockActivity().getResources().getDrawable(R.drawable.ab_shape);
        //drawable.setAlpha(200);
//        getSherlockActivity().getSupportActionBar().setBackgroundDrawable(drawable);
        Bundle bundle = new Bundle();
        bundle.putLong("storeid",getArguments().getLong("storeid"));
        getLoaderManager().restartLoader(30, bundle, this);

        return header;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, final Bundle args) {
        return new SimpleCursorLoader(getActivity()) {
            @Override
            public Cursor loadInBackground() {
                return new Database(Aptoide.getDb()).getStore(args.getLong("storeid"));
            }
        };
    }



    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if(data.moveToFirst()){
            String url = data.getString(data.getColumnIndex(Schema.Repo.COLUMN_AVATAR));
            String store_name = data.getString(data.getColumnIndex(Schema.Repo.COLUMN_NAME));
            String description = data.getString(data.getColumnIndex(Schema.Repo.COLUMN_DESCRIPTION));

            String theme = data.getString(data.getColumnIndex(Schema.Repo.COLUMN_THEME));
            EnumStoreTheme storeTheme;
            if(theme!=null){
                storeTheme = EnumStoreTheme.valueOf("APTOIDE_STORE_THEME_"+theme.toUpperCase());
            }else{
                storeTheme = EnumStoreTheme.APTOIDE_STORE_THEME_BLACK;
            }
            view_gradient.setBackgroundResource(storeTheme.getStoreViewGradient());

//            store_background.setBackgroundResource( storeTheme.getStoreHeader() );
            if(url!=null && url.length()>0){
                ImageLoader.getInstance().displayImage(url, avatar);
            }else{
                avatar.setImageResource(R.drawable.avatar_apps);
            }
            banner_store_name.setText(store_name);
            banner_description.setText(description);

            ((ActionBarActivity)getActivity()).getSupportActionBar().setDisplayShowTitleEnabled(true);
            ((ActionBarActivity)getActivity()).getSupportActionBar().setTitle(store_name);
        }





    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    @Override
    public void onRefresh() {

    }

    @Override
    public void onError() {




    }

    @Override
    public void setRefreshing(boolean bool) {

    }

    @Override
    public void setListShown(boolean b) {

    }
}
