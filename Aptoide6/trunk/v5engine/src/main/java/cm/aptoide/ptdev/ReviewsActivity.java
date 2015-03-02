package cm.aptoide.ptdev;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.crashlytics.android.Crashlytics;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.octo.android.robospice.SpiceManager;
import com.octo.android.robospice.persistence.DurationInMillis;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.RequestListener;

import org.apache.commons.lang3.concurrent.ConcurrentUtils;

import java.util.ArrayList;
import java.util.concurrent.CopyOnWriteArrayList;

import cm.aptoide.ptdev.adapters.DividerItemDecoration;
import cm.aptoide.ptdev.services.HttpClientSpiceService;
import cm.aptoide.ptdev.utils.AptoideUtils;
import cm.aptoide.ptdev.webservices.GetReviews;
import cm.aptoide.ptdev.webservices.json.reviews.Review;
import cm.aptoide.ptdev.webservices.json.reviews.ReviewListJson;
import cm.aptoide.ptdev.widget.RecyclerView;

/**
 * Created by rmateus on 16-02-2015.
 */
public class ReviewsActivity extends ActionBarActivity {
    SpiceManager spiceManager = new SpiceManager(HttpClientSpiceService.class);

    ArrayList<Review> reviewArrayList = new ArrayList<>();
    private ReviewsAdapter reviewsAdapter;

    private void showLoading(){
        findViewById(android.R.id.empty).setVisibility(View.VISIBLE);
        findViewById(R.id.swipeRefreshLayout).setVisibility(View.GONE);
    }

    private void showContent(){
        findViewById(android.R.id.empty).setVisibility(View.GONE);
        findViewById(R.id.swipeRefreshLayout).setVisibility(View.VISIBLE);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Aptoide.getThemePicker().setAptoideTheme(this);

        super.onCreate(savedInstanceState);

        setContentView(R.layout.app_review_list);
        final SwipeRefreshLayout swipeLayout = (SwipeRefreshLayout) findViewById(R.id.swipeRefreshLayout);


        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recyclerView);

        LinearLayoutManager linearLayoutManager;

        if(getResources().getBoolean(R.bool.landscape)){
            linearLayoutManager = new GridLayoutManager(this, 2);
        }else{
            linearLayoutManager = new LinearLayoutManager(this);
        }

        recyclerView.setLayoutManager(linearLayoutManager);

        reviewsAdapter = new ReviewsAdapter(reviewArrayList);

        float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, getResources().getDisplayMetrics());

        recyclerView.addItemDecoration(new DividerItemDecoration((int) px));

        recyclerView.setAdapter(reviewsAdapter);

        final GetReviews.GetReviewList reviews = new GetReviews.GetReviewList();

        final boolean editors = getIntent().getBooleanExtra("editors", false);
        final int store_id = getIntent().getIntExtra("store_id", 0);
        reviews.setHomePage(editors);
        reviews.setStoreId(store_id);
        reviews.setLimit(50);

        showLoading();
        final RequestListener<ReviewListJson> listener = new RequestListener<ReviewListJson>() {
            @Override
            public void onRequestFailure(SpiceException spiceException) {

            }

            @Override
            public void onRequestSuccess(ReviewListJson reviewListJson) {
                try{
                    reviewArrayList.clear();
                    reviewArrayList.addAll(reviewListJson.getReviews());
                    reviewsAdapter.notifyDataSetChanged();
                    swipeLayout.setRefreshing(false);

                    if(reviewListJson.getReviews().size()>0){
                        showContent();
                    } else{
                        showEmptyList();
                    }
                }catch (Exception e){
                    Crashlytics.logException(e);
                    showEmptyList();
                }

            }
        };
        spiceManager.execute(reviews, "review-list" + editors + store_id, DurationInMillis.ONE_HOUR, listener);

        swipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                spiceManager.execute(reviews, "review-list" + editors + store_id, DurationInMillis.ALWAYS_EXPIRED, listener);
            }
        });

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setWindowTitle(getString(R.string.review_title));
    }

    private void showEmptyList() {
        findViewById(R.id.empty_list).setVisibility(View.VISIBLE);
        findViewById(android.R.id.empty).setVisibility(View.GONE);
    }

    @Override
    protected void onStart() {
        super.onStart();
        spiceManager.start(this);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home || item.getItemId() == R.id.home){
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStop() {
        super.onStop();
        spiceManager.shouldStop();
    }

    public static class ReviewsAdapter extends RecyclerView.Adapter<ReviewsAdapter.ReviewViewHolder>{
        ArrayList<Review> reviewArrayList = new ArrayList<>();

        public ReviewsAdapter(ArrayList<Review> reviewArrayList) {
            this.reviewArrayList = reviewArrayList;
        }

        @Override
        public ReviewViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            Context context = parent.getContext();

            View inflate = LayoutInflater.from(context).inflate(R.layout.row_review, parent, false);
            RecyclerView.LayoutParams layoutParams = (RecyclerView.LayoutParams) inflate.getLayoutParams();
            layoutParams.setMargins(0,0,0,0);
            inflate.setLayoutParams(layoutParams);
            return new ReviewViewHolder(inflate);
        }

        @Override
        public void onBindViewHolder(ReviewViewHolder holder, int position) {

            holder.rating.setText(String.valueOf(reviewArrayList.get(position).getRating()));
            holder.appName.setText(reviewArrayList.get(position).getApk().getTitle());
            holder.description.setText(reviewArrayList.get(position).getFinalVerdict());
            ImageLoader.getInstance().displayImage(reviewArrayList.get(position).getApk().getIcon(), holder.appIcon);
            ImageLoader.getInstance().displayImage(reviewArrayList.get(position).getUser().getAvatar(), holder.avatar);

            holder.reviewer.setText(reviewArrayList.get(position).getUser().getName());

            final Integer id = reviewArrayList.get(position).getId();

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Context context = v.getContext();

                    Intent i = new Intent(context, ReviewActivity.class);
                    i.putExtra("id", id);

                    context.startActivity(i);
                }
            });
        }

        @Override
        public int getItemCount() {
            return reviewArrayList.size();
        }

        public static class ReviewViewHolder extends RecyclerView.ViewHolder{
            ImageView appIcon;
            TextView appName;
            TextView description;
            TextView rating;
            TextView reviewer;
            ImageView avatar;

            public ReviewViewHolder(View itemView) {
                super(itemView);

                avatar = (ImageView) itemView.findViewById(R.id.avatar);
                reviewer = (TextView) itemView.findViewById(R.id.reviewer);
                appIcon = (ImageView) itemView.findViewById(R.id.app_icon);
                appName = (TextView) itemView.findViewById(R.id.app_name);
                description = (TextView) itemView.findViewById(R.id.description);
                rating = (TextView) itemView.findViewById(R.id.rating);
            }
        }
    }
}