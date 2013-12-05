package cm.aptoide.ptdev;

import android.app.ListFragment;
import android.os.Bundle;
import android.support.v4.app.FixedFragmentStatePagerAdapter;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import cm.aptoide.ptdev.fragments.FragmentAppView;
import com.actionbarsherlock.app.SherlockFragmentActivity;

/**
 * Created with IntelliJ IDEA.
 * User: rmateus
 * Date: 15-11-2013
 * Time: 15:04
 * To change this template use File | Settings | File Templates.
 */
public class AppViewActivity extends SherlockFragmentActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.page_app_view);


        ViewPager pager = (ViewPager) findViewById(R.id.pager);

        if(pager == null){

        }else{
            PagerAdapter adapter = new AppViewPager(getSupportFragmentManager());

            pager.setAdapter(adapter);
        }


    }

    public class AppViewPager extends FixedFragmentStatePagerAdapter{

        public AppViewPager(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int i) {


            switch (i){
                case 0:
                return new FragmentAppView.FragmentAppViewDetails();
                case 1:
                    return new FragmentAppView.FragmentAppViewRating();
                case 2:
                    return new FragmentAppView.FragmentAppViewRelated();
                case 3:
                    return new FragmentAppView.FragmentAppViewSpecs();
                default:
                    return null;
            }



        }

        @Override
        public int getCount() {
            return 4;
        }
    }
}
