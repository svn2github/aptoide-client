package cm.aptoidetv.pt.WebServices;

import android.content.Context;

import com.octo.android.robospice.request.retrofit.RetrofitSpiceRequest;

import cm.aptoidetv.pt.R;
import retrofit.http.Body;
import retrofit.http.POST;

/**
 * Created by asantos on 25-11-2014.
 */
public class RequestTV extends RetrofitSpiceRequest<Response, RequestTV.ServiceTV> {
    public interface ServiceTV{

        @POST("/ws2.aptoide.com/api/6/bulkRequest/api_list/getStore,listApps/")
        Response postApk(@Body Api user);
    }
    private final String store;
    private final Context context;

    public RequestTV(String store, Context context) {
        super(Response.class, ServiceTV.class);
        this.store = store;
        this.context = context;
    }

    @Override
    public Response loadDataFromNetwork() throws Exception {
        Api api = new Api();

        api.getApi_global_params().setLang("en");
        api.getApi_global_params().setStore_name(context.getString(R.string.defaultstorename));


        Api.GetStore getStore = new Api.GetStore();
/*        Api.GetStore.CategoriesParams categoriesParams = new Api.GetStore.CategoriesParams();

        categoriesParams.setParent_ref_id("cat_1");*/

        Api.GetStore.WidgetParams widgetParams = new Api.GetStore.WidgetParams();
        widgetParams.setContext(this.store);

        //getStore.getDatasets_params().set(categoriesParams);
        getStore.getDatasets_params().set(widgetParams);

        //getStore.addDataset(categoriesParams.getDatasetName());
        getStore.addDataset(widgetParams.getDatasetName());

        api.getApi_params().set(getStore);


        return getService().postApk(api);

    }

}