package android.support.v4.app;

import android.os.Bundle;
import android.view.ViewGroup;

/**
 * Created with IntelliJ IDEA.
 * User: rmateus
 * Date: 18-11-2013
 * Time: 17:46
 * To change this template use File | Settings | File Templates.
 */
public abstract class FixedFragmentStatePagerAdapter extends FragmentStatePagerAdapter {

    public FixedFragmentStatePagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        Fragment f = (Fragment)super.instantiateItem(container, position);
        Bundle savedFragmentState = f.mSavedFragmentState;

        if (savedFragmentState != null) {
            savedFragmentState.setClassLoader(((Object)f).getClass().getClassLoader());
        }

        return f;
    }
}
