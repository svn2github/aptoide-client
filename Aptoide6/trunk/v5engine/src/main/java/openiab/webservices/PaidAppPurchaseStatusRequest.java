package openiab.webservices;


import java.util.HashMap;

import openiab.webservices.json.IabPurchaseStatusJson;

public class PaidAppPurchaseStatusRequest extends BasePurchaseStatusRequest {

    @Override
    IabPurchaseStatusJson executeRequest(Webservice webervice, HashMap<String, String> parameters) {
        return webervice.checkProductPayment(parameters);
    }

    String getReqType(){
        return "apkpurchasestatus";
    }

}
