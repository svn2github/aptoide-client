package openiab.webservices;

import com.google.api.client.http.GenericUrl;

import cm.aptoide.ptdev.webservices.WebserviceOptions;

public class PaidAppPurchaseStatusRequest extends BasePurchaseStatusRequest {
    @Override
    protected GenericUrl getURL(){
        String baseUrl = WebserviceOptions.WebServicesLink+"3/checkProductPayment";
        return new GenericUrl(baseUrl);
    }
}
