package openiab.webservices;

import com.google.api.client.http.GenericUrl;

import cm.aptoide.ptdev.webservices.WebserviceOptions;

public class IabPurchaseStatusRequest extends BasePurchaseStatusRequest {
    @Override
    protected GenericUrl getURL(){
        String baseUrl = WebserviceOptions.WebServicesLink+"3/processInAppBilling";
        return new GenericUrl(baseUrl);
    }

    String getReqType(){
        return "iabpurchasestatus";
    }
}
