package cm.aptoide.ptdev;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import cm.aptoide.ptdev.utils.AptoideUtils;
import com.actionbarsherlock.app.SherlockFragment;

/**
 * Created with IntelliJ IDEA.
 * User: rmateus
 * Date: 11-11-2013
 * Time: 17:24
 * To change this template use File | Settings | File Templates.
 */
public class StoreHeaderFragment extends SherlockFragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View header = inflater.inflate(R.layout.header_store_theme, container, false);

        RelativeLayout store_background = (RelativeLayout) header.findViewById(R.id.banner_background_layout);
        store_background.setBackgroundResource( EnumStoreTheme.values()[getArguments().getInt("theme")].getStoreHeader() );

        Drawable drawable = getSherlockActivity().getResources().getDrawable(R.drawable.ab_shape);
        drawable.setAlpha(200);
        getSherlockActivity().getSupportActionBar().setBackgroundDrawable(drawable);

        return header;
    }


}
