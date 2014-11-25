package openiab.webservices;



import cm.aptoide.ptdev.webservices.WebserviceOptions;

public class IabPurchaseStatusRequest extends BasePurchaseStatusRequest {


    String getReqType(){
        return "iabpurchasestatus";
    }
}
