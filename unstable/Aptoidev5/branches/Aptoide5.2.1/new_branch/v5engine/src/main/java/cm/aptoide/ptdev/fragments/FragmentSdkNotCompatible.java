package cm.aptoide.ptdev.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import cm.aptoide.ptdev.R;

/**
 * Created by rmateus on 27-10-2014.
 */
public class FragmentSdkNotCompatible extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.page_timeline_not_compatible, container, false);
    }
}
