package cm.aptoidetv.pt.openiab;

import cm.aptoidetv.pt.AptoideConfigurationTV;
import cm.aptoidetv.pt.AptoideTV;
import openiab.PaidAppPurchaseActivity;
import openiab.webservices.PayProductRequestBase;

/**
 * Created by asantos on 22-09-2014.
 */
public class PaidAppPurchaseActivityTV extends PaidAppPurchaseActivity {

    @Override
    protected void requestsetExtra(PayProductRequestBase pprb) {
        super.requestsetExtra(pprb);
        pprb.setOemId(((AptoideConfigurationTV) AptoideTV.getConfiguration()).PARTNERID);
    }
}
