package com.aptoide.partners;

import cm.aptoide.ptdev.MoreActivity;
import cm.aptoide.ptdev.model.Login;

/**
 * Created by rmateus on 05-02-2015.
 */
public class MoreActivityPartners extends MoreActivity{

    @Override
    public Login getLogin() {
        return ((AptoidePartner)getApplication()).getLogin();
    }

}
