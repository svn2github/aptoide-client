package com.aptoide.partners;

import android.text.TextUtils;

import cm.aptoide.ptdev.fragments.FragmentListTopApps;
import cm.aptoide.ptdev.model.Login;

/**
 * Created by rmateus on 15-01-2015.
 */
public class FragmentListTopAppsPartners extends FragmentListTopApps {


    @Override
    public String getContext(){
        return "top_oem";
    }

    @Override
    public Login getLogin() {
        return ((StartPartner)getActivity()).getLogin();
    }

}
