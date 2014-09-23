package com.aptoide.openiab;

import com.aptoide.partners.AptoideConfigurationPartners;

import cm.aptoide.ptdev.Aptoide;
import openiab.PaidAppPurchaseActivity;
import openiab.webservices.PayProductRequestBase;

/**
 * Created by asantos on 22-09-2014.
 */
public class PaidAppPurchaseActivityPartners extends PaidAppPurchaseActivity {

    @Override
    protected void requestsetExtra(PayProductRequestBase pprb) {
        super.requestsetExtra(pprb);
        pprb.setOemId(((AptoideConfigurationPartners) Aptoide.getConfiguration()).PARTNERID);
    }
}
