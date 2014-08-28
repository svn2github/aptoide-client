package com.aptoide.openiab.webservices.json;

import com.google.api.client.util.Key;

/**
 * Created by j-pac on 21-02-2014.
 */
public class PayProductJson {

    @Key
    private String status;

    @Key
    private Response response;

    public String getStatus() {
        return status;
    }

    public Response getResponse() {
        return response;
    }

    public static class Response {

        @Key
        private String orderId;

        public String getOrderId() {
            return orderId;
        }

    }
}
