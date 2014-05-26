package com.aptoide.openiab.webservices.json;

import com.google.api.client.util.Key;

import java.util.List;

/**
 * Created by j-pac on 19-02-2014.
 */
public class IabPurchasesJson {

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

            public String getProductId() {
                return productId;
            }

            public long getPurchaseTime() {
                return purchaseTime;
            }

            public String getPurchaseStatus() {
                return purchaseState;
            }

            public String getPurchaseToken() {
                return purchaseToken;
            }

            public String getPurchaseState() {
                return purchaseState;
            }

            public String getDeveloperPayload() {
                return developerPayload;
            }

            public String getToken() {
                return token;
            }

            public String getJson() {
                return "{"
                        + "\"orderId\":" + orderId
                        + ",\"packageName\":\"" + packageName
                        + "\",\"productId\":\"" + productId
                        + "\",\"purchaseTime\":" + purchaseTime
                        //+ ",\"purchaseState\":\"" + purchaseState
                        + ",\"developerPayload\":\"" + (developerPayload == null || developerPayload.equals("") ? "" : developerPayload)
                        //+ "\",\"token\":\"" + (token == null || token.equals("") ? "" : token)
                        + "\",\"purchaseToken\":\"" + purchaseToken
                        + "\"}";
            }
        }
    }
}
