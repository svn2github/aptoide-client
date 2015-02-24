package cm.aptoide.ptdev;

import android.content.Context;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.octo.android.robospice.SpiceManager;

import cm.aptoide.ptdev.adapters.DividerItemDecoration;
import cm.aptoide.ptdev.services.HttpClientSpiceService;
import cm.aptoide.ptdev.utils.AptoideUtils;
import cm.aptoide.ptdev.webservices.GetReviews;
import cm.aptoide.ptdev.widget.RecyclerView;

/**
 * Created by rmateus on 16-02-2015.
 */
public class ReviewsActivity extends ActionBarActivity {


    SpiceManager spiceManager = new SpiceManager(HttpClientSpiceService.class);


    @Override
    public void onCreate(Bundle savedInstanceState) {
        Aptoide.getThemePicker().setAptoideTheme(this);

        super.onCreate(savedInstanceState);

        RecyclerView recyclerView = new RecyclerView(this);

        addContentView(recyclerView, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        LinearLayoutManager linearLayoutManager;

        if(getResources().getBoolean(R.bool.landscape)){
            linearLayoutManager = new GridLayoutManager(this, 2);
        }else{
            linearLayoutManager = new LinearLayoutManager(this);
        }

        recyclerView.setLayoutManager(linearLayoutManager);

        float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, getResources().getDisplayMetrics());
        recyclerView.addItemDecoration(new DividerItemDecoration((int) px));
        recyclerView.setAdapter(new ReviewsAdapter());

        GetReviews.GetReviewList reviews = new GetReviews.GetReviewList();




    }



    public static class ReviewsAdapter extends RecyclerView.Adapter<ReviewsAdapter.ReviewViewHolder>{


        @Override
        public ReviewViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

            Context context = parent.getContext();

            View inflate = LayoutInflater.from(context).inflate(R.layout.row_review, parent, false);

            return new ReviewViewHolder(inflate);
        }

        @Override
        public void onBindViewHolder(ReviewViewHolder holder, int position) {

        }

        @Override
        public int getItemCount() {
            return 10;
        }

        public static class ReviewViewHolder extends RecyclerView.ViewHolder{

            public ReviewViewHolder(View itemView) {
                super(itemView);
            }
        }

    }


}
