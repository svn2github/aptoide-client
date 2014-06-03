package com.aptoide.openiab.webservices.json;

import com.google.api.client.util.Key;

import java.util.ArrayList;

/**
 * Created by j-pac on 19-02-2014.
 */
public class IabPurchaseStatusJson {

    @Key
    private String status;

    @Key
    private PublisherResponse publisher_response;

    public String getStatus() {
        return status;
    }

    public PublisherResponse getPublisherResponse() {
        return publisher_response;
    }

    public static class PublisherResponse {
        @Key("INAPP_PURCHASE_ITEM_LIST")
        private ArrayList<String> item;

        @Key("INAPP_PURCHASE_DATA_LIST")
        private ArrayList<PurchaseDataObject> data;

        @Key("INAAP_DATA_SIGNATURE_LIST")
        private ArrayList<String> signature;

        public ArrayList<String> getItem() {
            return item;
        }

        public ArrayList<PurchaseDataObject> getData() {
            return data;
        }

        public ArrayList<String> getSignature() {
            return signature;
        }

        public static class PurchaseDataObject {
            @Key private int orderId;
            @Key private String packageName;
            @Key private String productId;
            @Key private long purchaseTime;
            @Key private String purchaseState;
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

            public String getPurchaseState() { return purchaseState; }

            public String getPurchaseToken() {
                return purchaseToken;
            }



            public String getJson() {
                return "{"
                        + "\"orderId\":" + orderId
                        + ",\"packageName\":\"" + packageName
                        + "\",\"productId\":\"" + productId
                        + "\",\"purchaseTime\":" + purchaseTime
                        + ",\"purchaseState\":\"" + purchaseState
                        + "\",\"purchaseToken\":\"" + purchaseToken
                        + "\"}";
            }
        }
    }
}
