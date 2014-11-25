package openiab.webservices.json;


import openiab.webservices.BaseRequest;

/**
 * Created by j-pac on 19-02-2014.
 */
public class IabAvailableJson  {


    private String status;


    private Response response;

    public String getStatus() {
        return status;
    }

    public Response getResponse() { return response; }

    public static class Response {

        private String iabavailable;

        public String getIabavailable() {
            return iabavailable;
        }
    }
}
