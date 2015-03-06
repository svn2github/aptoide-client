package cm.aptoide.ptdev.webservices;

import android.database.Cursor;
import android.preference.PreferenceManager;
import android.text.TextUtils;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.octo.android.robospice.request.retrofit.RetrofitSpiceRequest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import cm.aptoide.ptdev.Aptoide;
import cm.aptoide.ptdev.database.Database;
import cm.aptoide.ptdev.database.schema.Schema;
import cm.aptoide.ptdev.utils.AptoideUtils;
import cm.aptoide.ptdev.webservices.json.SearchJson;
import retrofit.converter.Converter;
import retrofit.converter.JacksonConverter;
import retrofit.http.FieldMap;
import retrofit.http.FormUrlEncoded;
import retrofit.http.POST;

/**
 * Created by rmateus on 08-07-2014.
 */

public class ListSearchApkRequest extends RetrofitSpiceRequest<SearchJson, ListSearchApkRequest.Webservice> {


    private String searchString;

    private List<String> stores;
    private int offset;

    public ListSearchApkRequest() {
        super(SearchJson.class, Webservice.class);
    }

    public interface Webservice{
        @POST("/webservices.aptoide.com/webservices/2/listSearchApks")
        @FormUrlEncoded
        SearchJson listSearchApk(@FieldMap HashMap<String, String> args);
    }

    protected Converter createConverter() {

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        return new JacksonConverter(objectMapper);
    }


    @Override
    public SearchJson loadDataFromNetwork() throws Exception {

        StringBuilder repos = new StringBuilder();
        //GenericUrl url = new GenericUrl(WebserviceOptions.WebServicesLink+"2/listSearchApks");

        final ArrayList<String> stores = new ArrayList<String>();

        Cursor c = new Database(Aptoide.getDb()).getServers();
        for (c.moveToFirst(); !c.isAfterLast(); c.moveToNext()) {
            stores.add(c.getString(c.getColumnIndex(Schema.Repo.COLUMN_NAME)));
        }
        c.close();

        Iterator<String> it = stores.iterator();

        while (it.hasNext()) {
            String next = it.next();
            repos.append(next);
            if (it.hasNext()) {
                repos.append(",");
            }
        }

        HashMap<String, String > parameters = new HashMap<String, String>();
        ArrayList<WebserviceOptions> options = new ArrayList<WebserviceOptions>();
        options.add(new WebserviceOptions("q", AptoideUtils.filters(Aptoide.getContext())));
        if(!TextUtils.isEmpty(repos))options.add(new WebserviceOptions("repo", repos.toString()));
        options.add(new WebserviceOptions("lang", AptoideUtils.getMyCountryCode(Aptoide.getContext())));
        options.add(new WebserviceOptions("u_limit", "4"));
        options.add(new WebserviceOptions("limit", "15"));

        if(PreferenceManager.getDefaultSharedPreferences(Aptoide.getContext()).getBoolean("matureChkBox", true)){
            options.add(new WebserviceOptions("mature", "0"));
        }

        if(offset>0) options.add(new WebserviceOptions("offset", String.valueOf(offset)));



        StringBuilder sb = new StringBuilder();
        sb.append("(");
        for(WebserviceOptions option: options){
            sb.append(option);
            sb.append(";");
        }
        sb.append(")");

        parameters.put("mode", "json");
        parameters.put("search", searchString);
        parameters.put("options", sb.toString());

        //RestAdapter adapter = new RestAdapter.Builder().setEndpoint("http://").setConverter(createConverter()).build();
        //  setService(adapter.create(getRetrofitedInterfaceClass()));

        return getService().listSearchApk(parameters);

//        HttpContent content = new UrlEncodedContent(parameters);
//
//        HttpRequest request = getHttpRequestFactory().buildPostRequest(url, content);
//
//        request.setParser(new JacksonFactory().createJsonObjectParser());
//
//        return request.execute().parseAs( getResultType() );
    }

    public void setSearchString(String searchString) {
        this.searchString = searchString;
    }


    public void setOffset(int offset) {
        this.offset = offset;
    }
}
