package cm.aptoidetv.pt.WebServices.old;

import android.content.Context;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.octo.android.robospice.request.retrofit.RetrofitSpiceRequest;

import java.util.ArrayList;
import java.util.HashMap;

import cm.aptoidetv.pt.Model.GetApkInfoJson;
import retrofit.RestAdapter;
import retrofit.converter.Converter;
import retrofit.converter.JacksonConverter;
import retrofit.http.FieldMap;
import retrofit.http.FormUrlEncoded;
import retrofit.http.POST;

/**
 * Created by asantos on 07-08-2014.
 */
public abstract class  GetApkInfoRequest extends RetrofitSpiceRequest<GetApkInfoJson, GetApkInfoRequest.Webservice> {


    public interface Webservice{
        @POST("/webservices.aptoide.com/webservices/3/getApkInfo")
        @FormUrlEncoded

        GetApkInfoJson getApkInfo(@FieldMap HashMap<String, String> args);
    }

    protected String repoName;
    protected String packageName;
    protected Context context;
    protected int cmtlimit;
    public void setCmtlimit(int limit){cmtlimit=limit;}

    public GetApkInfoRequest(Context context) {
        super(GetApkInfoJson.class, Webservice.class);
        this.context = context;
        cmtlimit=5;
    }

    protected Converter createConverter() {

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        return new JacksonConverter(objectMapper);
    }

    protected abstract ArrayList<WebserviceOptions> fillWithExtraOptions(ArrayList<WebserviceOptions> options);
    protected abstract HashMap<String, String > getParameters();
    @Override
    public GetApkInfoJson loadDataFromNetwork() throws Exception{
        ArrayList<WebserviceOptions> options = getoptions();
        //token = SecurePreferences.getInstance().getString("access_token", null);
        fillWithExtraOptions(options);
        HashMap<String, String > parameters = getParameters();
        parameters.put("options", buildOptions(options));
        parameters.put("mode", "json");

        RestAdapter adapter = new RestAdapter.Builder().setEndpoint("http://").setConverter(createConverter()).build();
        setService(adapter.create(getRetrofitedInterfaceClass()));


        return getService().getApkInfo(parameters);

    };

    public void setRepoName(String repoName) {
        this.repoName = repoName;
    }

    public String getRepoName() {
        return repoName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getPackageName() {
        return packageName;
    }



    protected ArrayList<WebserviceOptions> getoptions(){
        ArrayList<WebserviceOptions> options = new ArrayList<WebserviceOptions>();
        options.add(new WebserviceOptions("cmtlimit", String.valueOf(cmtlimit)));
        //options.add(new WebserviceOptions("payinfo", String.valueOf(payinfo)));
       /* options.add(new WebserviceOptions("q", AptoideUtils.filters(context)));
        options.add(new WebserviceOptions("lang", AptoideUtils.getMyCountryCode(context)));*/
        return options;
    }

    protected String buildOptions(ArrayList<WebserviceOptions> options){
        StringBuilder sb = new StringBuilder();
        sb.append("(");
        for(WebserviceOptions option: options){
            sb.append(option);
            sb.append(";");
        }
        sb.append(")");
        return sb.toString();
    }
}
