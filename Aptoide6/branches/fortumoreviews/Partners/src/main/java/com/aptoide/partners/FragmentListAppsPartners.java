package com.aptoide.partners;

import android.database.Cursor;
import android.text.TextUtils;

import cm.aptoide.ptdev.Aptoide;
import cm.aptoide.ptdev.database.Database;
import cm.aptoide.ptdev.fragments.FragmentListApps;
import cm.aptoide.ptdev.model.Login;

/**
 * Created by rmateus on 04-02-2015.
 */
public class FragmentListAppsPartners extends FragmentListApps {

    @Override
    public Login getLogin() {

        return ((StartPartner)getActivity()).getLogin();

    }

    @Override
    public boolean isAdultEnabled() {
        return getActivity().getResources().getBoolean(R.bool.maturecontentswitch);
    }
}
