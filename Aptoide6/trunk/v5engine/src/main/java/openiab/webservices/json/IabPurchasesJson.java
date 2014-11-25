package openiab.webservices.json;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gson.Gson;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by j-pac on 19-02-2014.
 */
public class    IabPurchasesJson {


    private String status;


    private PublisherResponse publisher_response;

    public String getStatus() { return status; }

    public PublisherResponse getPublisher_response() {
        return publisher_response;
    }

    public static class PublisherResponse {
        @JsonProperty("INAPP_PURCHASE_ITEM_LIST") private List<String> itemList;

        @JsonProperty("INAPP_PURCHASE_DATA_LIST") private List<PurchaseDataObject> purchaseDataList;

        @JsonProperty("INAAP_DATA_SIGNATURE_LIST") private List<String> signatureList;

        @JsonProperty("INAPP_CONTINUATION_TOKEN") private String inapp_continuation_token;


        public List<String> getItemList() {
            return itemList;
        }

        public List<PurchaseDataObject> getPurchaseDataList() { return purchaseDataList; }

        public List<String> getSignatureList() {
            return signatureList;
        }

        public String getInapp_continuation_token() {
            return inapp_continuation_token;
        }

        public static class PurchaseDataObject {
             private int orderId;
             private String packageName;
             private String productId;
             private long purchaseTime;
             private String purchaseState;
             private String developerPayload;
             private String token;
             private String purchaseToken;

            public int getOrderId() { return orderId; }

            public String getPackageName() {
                return packageName;
            }

     

            public String getToken() {
                return token;
            }

            public String getJson() {



                Map<String, Object> myJson = new LinkedHashMap<String, Object>();

                myJson.put("orderId", orderId);
                myJson.put("packageName", packageName);
                myJson.put("productId", productId);
                myJson.put("purchaseTime", purchaseTime);
                myJson.put("purchaseToken", purchaseToken);
                if(developerPayload!=null) myJson.put("developerPayload", developerPayload);



                return new Gson().toJson(myJson);
            }
        }
    }
}
