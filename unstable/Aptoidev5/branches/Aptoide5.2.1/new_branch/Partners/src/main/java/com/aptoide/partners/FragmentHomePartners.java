package com.aptoide.partners;

import android.util.Log;

import cm.aptoide.ptdev.fragments.FragmentHome;

/**
 * Created with IntelliJ IDEA.
 * User: rmateus
 * Date: 28-10-2013
 * Time: 11:40
 * To change this template use File | Settings | File Templates.
 */
public class FragmentHomePartners extends FragmentHome {

    @Override
    protected void getSwitchView() {
        if(((AptoideConfigurationPartners)AptoidePartner.getConfiguration()).getMatureContentSwitch()){
            mergeAdapter.setActive(adultSwitchView, true);
        }else {
            Log.d("pois", "don't show adult content switch");
            mergeAdapter.setActive(adultSwitchView, false);
        }
    }
}
