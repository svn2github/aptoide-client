package cm.aptoide.ptdev.webservices;

/**
 * Created by rmateus on 29-07-2014.
 */
//public class RegisterAdRequest extends RetrofitSpiceRequest<GenericResponseV2> {
//
//
//
//    public interface Webservice{
//        @POST()
//        @FormUrlEncoded
//        GenericResponseV2 registerAdsRequest(@FieldMap HashMap<String, String> args);
//    }
//    final static String url;
//
//    public RegisterAdRequest(Context context, String cpiUrl) {
//        super(GenericResponseV2.class);
//        //this.context = context;
//        url = cpiUrl;
//    }
//
//
//
//
//
//    @Override
//    public GenericResponseV2 loadDataFromNetwork() throws Exception {
//
//        HashMap<String, String> parameters = new HashMap<String, String>();
//
//        String oemid = Aptoide.getConfiguration().getExtraId();
//
//        if(!TextUtils.isEmpty(oemid)){
//            parameters.put("oemid", oemid);
//        }
//        GenericUrl url = new GenericUrl(this.url);
//
//        HttpContent content = new UrlEncodedContent(parameters);
//
//        HttpRequest request = getHttpRequestFactory().buildPostRequest(url, content);
//        request.setParser(new JacksonFactory().createJsonObjectParser());
//
//        return request.execute().parseAs( getResultType() );
//    }
//
//}
