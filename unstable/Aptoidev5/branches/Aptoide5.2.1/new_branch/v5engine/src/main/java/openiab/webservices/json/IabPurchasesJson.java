package openiab.webservices.json;

import com.google.api.client.util.Key;
import com.google.gson.Gson;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by j-pac on 19-02-2014.
 */
public class    IabPurchasesJson {

    @Key
    private String status;

    @Key
    private PublisherResponse publisher_response;

    public String getStatus() { return status; }

    public PublisherResponse getPublisher_response() {
        return publisher_response;
    }

    public static class PublisherResponse {
        @Key("INAPP_PURCHASE_ITEM_LIST") private List<String> itemList;

        @Key("INAPP_PURCHASE_DATA_LIST") private List<PurchaseDataObject> purchaseDataList;

        @Key("INAAP_DATA_SIGNATURE_LIST") private List<String> signatureList;

        @Key("INAPP_CONTINUATION_TOKEN") private String inapp_continuation_token;


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
            @Key private int orderId;
            @Key private String packageName;
            @Key private String productId;
            @Key private long purchaseTime;
            @Key private String purchaseState;
            @Key private String developerPayload;
            @Key private String token;
            @Key private String purchaseToken;

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
