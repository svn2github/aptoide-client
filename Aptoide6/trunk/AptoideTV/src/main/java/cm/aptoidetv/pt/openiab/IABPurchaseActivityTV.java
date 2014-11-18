package cm.aptoidetv.pt.openiab;

import cm.aptoidetv.pt.AptoideConfigurationTV;
import cm.aptoidetv.pt.AptoideTV;
import openiab.IABPurchaseActivity;
import openiab.webservices.IabSkuDetailsRequest;
import openiab.webservices.PayProductRequestBase;

public class IABPurchaseActivityTV extends IABPurchaseActivity {

    @Override
    protected void requestsetExtra(PayProductRequestBase pprb) {
        super.requestsetExtra(pprb);
        pprb.setOemId(((AptoideConfigurationTV) AptoideTV.getConfiguration()).PARTNERID);
    }
    protected IabSkuDetailsRequest BuildIabSkuDetailsRequest(){
        IabSkuDetailsRequest ret = super.BuildIabSkuDetailsRequest();
        ret.setOemid(((AptoideConfigurationTV) AptoideTV.getConfiguration()).PARTNERID);
        return ret;
    }

}
