package openiab.webservices.json;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;


import java.util.ArrayList;
import java.util.List;

/**
 * Created by j-pac on 19-02-2014.
 */
public class IabSkuDetailsJson {


    private String status;

    public Metadata getMetadata() {
        return metadata;
    }


    private Metadata metadata;


    private PublisherResponse publisher_response;


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

        @JsonProperty("DETAILS_LIST") private List<PurchaseDataObject> details_list;

        public List<PurchaseDataObject> getDetails_list() { return details_list; }

        public static class PurchaseDataObject {
             private String productId;
             private String sku;
             private String price;
             private String title;
             private String description;
             private String developerPayload;




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


                try {
                    return new ObjectMapper().writeValueAsString(PurchaseDataObject.this);
                } catch (JsonProcessingException e) {
                    e.printStackTrace();
                }
                return null;
            }
        }
    }



}
