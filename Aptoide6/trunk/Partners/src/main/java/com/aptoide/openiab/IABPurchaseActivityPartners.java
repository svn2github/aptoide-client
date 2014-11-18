package com.aptoide.openiab;

import com.aptoide.partners.AptoideConfigurationPartners;

import cm.aptoide.ptdev.Aptoide;
import openiab.IABPurchaseActivity;
import openiab.webservices.IabSkuDetailsRequest;
import openiab.webservices.PayProductRequestBase;

public class IABPurchaseActivityPartners extends IABPurchaseActivity {

    @Override
    protected void requestsetExtra(PayProductRequestBase pprb) {
        super.requestsetExtra(pprb);
        pprb.setOemId(((AptoideConfigurationPartners) Aptoide.getConfiguration()).PARTNERID);
    }
    protected IabSkuDetailsRequest BuildIabSkuDetailsRequest(){
        IabSkuDetailsRequest ret = super.BuildIabSkuDetailsRequest();
        ret.setOemid(((AptoideConfigurationPartners) Aptoide.getConfiguration()).PARTNERID);
        return ret;
    }

}
