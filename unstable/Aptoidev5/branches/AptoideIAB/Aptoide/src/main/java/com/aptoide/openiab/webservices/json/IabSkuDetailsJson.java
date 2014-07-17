package com.aptoide.openiab.webservices.json;

import com.google.api.client.util.Key;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by j-pac on 19-02-2014.
 */
public class IabSkuDetailsJson {

    @Key
    private String status;

    public Metadata getMetadata() {
        return metadata;
    }

    @Key
    private Metadata metadata;

    @Key
    private PublisherResponse publisher_response;

    @Key
    private ArrayList<PaymentServices> payment_services;

    public String getStatus() {
        return status;
    }

    public PublisherResponse getPublisher_response() {
        return publisher_response;
    }

    public ArrayList<PaymentServices> getPayment_services() {
        return payment_services;
    }





    public static class PublisherResponse {

        @Key("DETAILS_LIST") private List<PurchaseDataObject> details_list;

        public List<PurchaseDataObject> getDetails_list() { return details_list; }

        public static class PurchaseDataObject {
            @Key private String productId;
            @Key private String sku;
            @Key private String price;
            @Key private String title;
            @Key private String description;
            @Key private String developerPayload;




            public String getPrice() {
                return price;
            }

            public String getTitle() {
                return title;
            }

            public String getDescription() {
                return description;
            }

            public String getSku() {
                return sku;
            }

            public String getProductId() {
                return productId;
            }



            public String getJson() {

                Gson gson = new Gson();




                return gson.toJson(PurchaseDataObject.this);
            }
        }
    }



}
