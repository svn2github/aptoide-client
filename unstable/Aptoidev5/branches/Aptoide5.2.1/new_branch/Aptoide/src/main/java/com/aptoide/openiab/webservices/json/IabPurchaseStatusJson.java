package com.aptoide.openiab.webservices.json;

import android.util.Log;
import com.google.api.client.util.Key;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

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

        @Key("RESPONSE_CODE")
        private int response_code;

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

        public int getResponse_code() {
            return response_code;
        }

        public static class PurchaseDataObject {
            @Key private int orderId;
            @Key private String packageName;
            @Key private String productId;
            @Key private long purchaseTime;
            @Key private String purchaseState;
            @Key private String purchaseToken;
            @Key private String developerPayload;

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

                Map<String, Object> myJSon = new LinkedHashMap<String, Object>();

                myJSon.put("orderId", orderId);
                myJSon.put("packageName", packageName);
                myJSon.put("productId", productId);
                myJSon.put("purchaseTime", purchaseTime);
                myJSon.put("purchaseState", purchaseState);
                myJSon.put("purchaseToken", purchaseToken);
                myJSon.put("developerPayload", developerPayload);


                String json = new Gson().toJson(myJSon);

                Log.d("AptoideJson", json);

                return json;
            }
        }
    }
}
