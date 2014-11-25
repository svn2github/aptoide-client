package openiab.webservices.json;



/**
 * Created by j-pac on 21-02-2014.
 */
public class PayProductJson {


    private String status;


    private Response response;

    public String getStatus() {
        return status;
    }

    public Response getResponse() {
        return response;
    }

    public static class Response {


        private String orderId;

        public String getOrderId() {
            return orderId;
        }

    }
}
