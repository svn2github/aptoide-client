package cm.aptoide.ptdev.webservices;

import com.octo.android.robospice.request.retrofit.RetrofitSpiceRequest;

import java.util.HashMap;

import cm.aptoide.ptdev.webservices.json.reviews.ReviewJson;
import cm.aptoide.ptdev.webservices.json.reviews.ReviewListJson;
import retrofit.http.FieldMap;
import retrofit.http.FormUrlEncoded;
import retrofit.http.POST;

/**
 * Created by rmateus on 23-02-2015.
 */
public abstract class GetReviews<T> extends RetrofitSpiceRequest<T,GetReviews.GetReviewListWebservice> {


    private int store_id;
    private int offset;




    public int getStore_id() {
        return store_id;
    }

    public void setStore_id(int store_id) {
        this.store_id = store_id;
    }

    public int getOffset() {
        return offset;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    public GetReviews(Class<T> clazz) {
        super(clazz, GetReviewListWebservice.class );
    }

    @Override
    public T loadDataFromNetwork() throws Exception {

        HashMap<String, String > args = new HashMap<>();

        args.put("mode","json");
        args.put("status","active");
        args.put("repo_id", String.valueOf(store_id));
        if(offset>0){
            args.put("offset", String.valueOf(offset));
        }



        return response(args);
    }

    public abstract T response(HashMap<String, String> args);

    public interface GetReviewListWebservice{
        @POST("/webservices.aptoide.com/webservices/3/getReviewList")
        @FormUrlEncoded
        ReviewListJson getReviewList(@FieldMap HashMap<String, String> map);

        @POST("/webservices.aptoide.com/webservices/3/getReview")
        @FormUrlEncoded
        ReviewJson getReview(@FieldMap HashMap<String, String> map);
    }

    public static class GetReview extends GetReviews<ReviewJson>{
        public void setId(int id) {
            this.id = id;
        }

        private int id;

        public GetReview() {
            super(ReviewJson.class);
        }

        @Override
        public ReviewJson response(HashMap<String, String> args) {

            if(id>0){
                args.put("id", String.valueOf(id));
            }

            return getService().getReview(args);
        }
    }

    public static class GetReviewList extends GetReviews<ReviewListJson>{
        private int id;

        public GetReviewList() {
            super(ReviewListJson.class);
        }

        @Override
        public ReviewListJson response(HashMap<String, String> args) {
            return getService().getReviewList(args);
        }

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }
    }


}
