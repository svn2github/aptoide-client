package cm.aptoide.ptdev.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import cm.aptoide.ptdev.AppViewActivity;
import cm.aptoide.ptdev.R;
import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.app.SherlockListFragment;

/**
 * Created with IntelliJ IDEA.
 * User: rmateus
 * Date: 15-11-2013
 * Time: 14:34
 * To change this template use File | Settings | File Templates.
 */
public class FragmentAppView extends SherlockFragment {



    public static class FragmentAppViewDetails extends SherlockFragment{
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

            return inflater.inflate(R.layout.fragment_app_view_details, container, false);
        }

        @Override
        public void onViewCreated(View view, Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
            view.findViewById(R.id.add_comment).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(new Intent(getSherlockActivity(), AppViewActivity.class));
                }
            });
        }
    }



    public static class FragmentScreenShots extends SherlockListFragment {

    }

    public static class FragmentRelatedApps extends SherlockListFragment{

    }

}
