package cm.aptoide.ptdev.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.facebook.widget.LoginButton;

import cm.aptoide.ptdev.R;

/**
 * Created by rmateus on 22-10-2014.
 */
public class FragmentSocialTimelineLogin extends Fragment {


    public static final String LOGOUT_FIRST_ARG = "logoutFirst";

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        boolean logoutFirst = getArguments().getBoolean(LOGOUT_FIRST_ARG, false);
        if(logoutFirst){
            return inflater.inflate(R.layout.page_timeline_logout_and_login, container, false);
        }else {
            return inflater.inflate(R.layout.page_timeline_not_logged_in, container, false);
        }
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        LoginButton fb_login_button = (LoginButton) view.findViewById(R.id.fb_login_button);
        fb_login_button.setFragment(getParentFragment());
    }
}
