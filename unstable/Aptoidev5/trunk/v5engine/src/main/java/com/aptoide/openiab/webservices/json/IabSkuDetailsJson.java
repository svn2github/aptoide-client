package com.aptoide.openiab.webservices.json;

import com.google.api.client.util.Key;

import java.util.List;

/**
 * Created by j-pac on 19-02-2014.
 */
public class IabSkuDetailsJson {

    @Key
    private String status;

    @Key
    private PublisherResponse publisher_response;

    public String getStatus() {
        return status;
    }

    public PublisherResponse getPublisher_response() {
        return publisher_response;
    }

    public static class PublisherResponse {

        @Key("DETAILS_LIST") private List<PurchaseDataObject> details_list;

        public List<PurchaseDataObject> getDetails_list() { return details_list; }

        public static class PurchaseDataObject {
            @Key private String productId;
            @Key private String sku;
            @Key private String type;
            @Key private String price;
            @Key private String title;
            @Key private String description;
            @Key private int aptoideProductId;

            public String getType() {
                return type;
            }

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

            public int getAptoideProductId() {
                return aptoideProductId;
            }

            public String getJson() {
                return "{"
                        + "\"productId\":\"" + productId
                        + "\",\"type\":\"" + type
                        + "\",\"price\":\"" + price
                        + "\",\"title\":\"" + title
                        + "\",\"description\":\"" + description
                        + "\",\"aptoideProductId\":" + aptoideProductId
                        + "}";
            }
        }
    }



}
