package cm.aptoide.ptdev.webservices;

import com.octo.android.robospice.request.retrofit.RetrofitSpiceRequest;

import cm.aptoide.ptdev.webservices.json.GetUserRepoSubscriptionJson;

/**
 * Created by rmateus on 16-02-2015.
 */
public class GetUserRepoSubscriptions extends RetrofitSpiceRequest<GetUserRepoSubscriptions.GetUserRepoSubscriptionWebservice, GetUserRepoSubscriptionJson> {

    public GetUserRepoSubscriptions() {
        super(GetUserRepoSubscriptionWebservice.class, GetUserRepoSubscriptionJson.class);
    }

    @Override
    public GetUserRepoSubscriptionWebservice loadDataFromNetwork() throws Exception {
        return null;
    }

    public interface GetUserRepoSubscriptionWebservice{

    }

}
