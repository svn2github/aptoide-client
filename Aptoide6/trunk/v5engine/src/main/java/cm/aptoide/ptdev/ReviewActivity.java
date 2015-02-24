package cm.aptoide.ptdev;

import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.octo.android.robospice.SpiceManager;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.RequestListener;

import java.util.ArrayList;

import cm.aptoide.ptdev.services.HttpClientSpiceService;
import cm.aptoide.ptdev.webservices.GetReviews;
import cm.aptoide.ptdev.webservices.json.reviews.ReviewJson;
import lecho.lib.hellocharts.model.PieChartData;
import lecho.lib.hellocharts.model.SliceValue;
import lecho.lib.hellocharts.view.PieChartView;

/**
 * Created by rmateus on 16-02-2015.
 */
public class ReviewActivity extends ActionBarActivity {


    SpiceManager manager = new SpiceManager(HttpClientSpiceService.class);


    private ImageView speedScreenshot;
    private ImageView usabilityScreenshot;
    private ImageView addictiveScreenshot;
    private ImageView stabilityScreenshot;


    private PieChartData speedData;
    private PieChartView speedChart;

    private PieChartData usabilityData;
    private PieChartView usabilityChart;

    private PieChartData addictiveData;
    private PieChartView addictiveChart;

    private PieChartData stabilityData;
    private PieChartView stabilityChart;
    private TextView title;
    private TextView finalVeredict;
    private TextView reviewer;
    private TextView rating;
    private ImageView appIcon;
    private ImageView avatar;
    private LinearLayout consContainer;
    private LinearLayout prosContainer;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.app_review);

        init();
        setTitle("Reviews");

        GetReviews.GetReview reviewsRequest = new GetReviews.GetReview();

        reviewsRequest.setId(3);

        manager.execute(reviewsRequest, new RequestListener<ReviewJson>() {
            @Override
            public void onRequestFailure(SpiceException spiceException) {

            }

            @Override
            public void onRequestSuccess(ReviewJson reviewListJson) {
                Log.d("AptoideReview", reviewListJson.toString()) ;
                int addiction = reviewListJson.getReview().getAddiction();
                int performance = reviewListJson.getReview().getPerformance();
                int stability = reviewListJson.getReview().getStability();
                int usability = reviewListJson.getReview().getUsability();

                setValue(speedData, performance);
                setValue(addictiveData, addiction);
                setValue(stabilityData, stability);
                setValue(usabilityData, usability);


                float ratingValue = (performance + addiction + stability + usability) / 4;
                rating.setText(String.valueOf(ratingValue));
                title.setText(reviewListJson.getReview().getApk().getTitle());

                finalVeredict.setText(reviewListJson.getReview().getFinalVerdict());

                int i = 0;
                for (ImageView screenshot : screenshots) {
                    ImageLoader.getInstance().displayImage(reviewListJson.getReview().getApk().getScreenshots().get(i++).getUrl(), screenshot);
                }

                ImageLoader.getInstance().displayImage(reviewListJson.getReview().getApk().getIcon(), appIcon);
                ImageLoader.getInstance().displayImage(reviewListJson.getReview().getUser().getAvatar(), avatar);

                reviewer.setText("Review by - " + reviewListJson.getReview().getUser().getName());

                LayoutInflater layoutInflater = LayoutInflater.from(ReviewActivity.this);
                for (String pro : reviewListJson.getReview().getPros()) {
                    TextView proTv = (TextView) layoutInflater.inflate(R.layout.review_pro, prosContainer, false);
                    proTv.setText(pro);
                    prosContainer.addView(proTv);
                }


                for (String con : reviewListJson.getReview().getCons()) {

                    TextView conTv = (TextView) layoutInflater.inflate(R.layout.review_con, consContainer, false);
                    conTv.setText(con);
                    consContainer.addView(conTv);

                }

                speedChart.startDataAnimation();
                usabilityChart.startDataAnimation();
                addictiveChart.startDataAnimation();
                stabilityChart.startDataAnimation();

            }
        });

    }

    private void setValue(PieChartData data, int score){
        data.getValues().get(0).setTarget(score);
        data.getValues().get(1).setTarget(10-score);
        data.setCenterText1(score + "/10");
    }

    ArrayList<ImageView> screenshots = new ArrayList<>();

    private void init() {

        speedChart = (PieChartView) findViewById(R.id.speed_chart).findViewById(R.id.chart);
        usabilityChart = (PieChartView) findViewById(R.id.usability_chart).findViewById(R.id.chart);
        addictiveChart = (PieChartView) findViewById(R.id.addictive_chart).findViewById(R.id.chart);
        stabilityChart = (PieChartView) findViewById(R.id.stability_chart).findViewById(R.id.chart);

        speedScreenshot = (ImageView) findViewById(R.id.speed_chart).findViewById(R.id.screenshot);
        usabilityScreenshot = (ImageView) findViewById(R.id.usability_chart).findViewById(R.id.screenshot);
        addictiveScreenshot = (ImageView) findViewById(R.id.addictive_chart).findViewById(R.id.screenshot);
        stabilityScreenshot = (ImageView) findViewById(R.id.stability_chart).findViewById(R.id.screenshot);


        TextView speedLabel = (TextView) findViewById(R.id.speed_chart).findViewById(R.id.designation);
        TextView usabilityLabel = (TextView) findViewById(R.id.usability_chart).findViewById(R.id.designation);
        TextView addictiveLabel = (TextView) findViewById(R.id.addictive_chart).findViewById(R.id.designation);
        TextView stabilityLabel = (TextView) findViewById(R.id.stability_chart).findViewById(R.id.designation);

        speedLabel.setText("Speed");
        usabilityLabel.setText("Usability");
        addictiveLabel.setText("Addicitive");
        stabilityLabel.setText("Stability");

        speedLabel.setBackgroundColor(Color.parseColor("#ff3037"));
        usabilityLabel.setBackgroundColor(Color.parseColor("#d9d31a"));
        stabilityLabel.setBackgroundColor(Color.parseColor("#00c81b"));
        addictiveLabel.setBackgroundColor(Color.parseColor("#ff6600"));

        screenshots.add(speedScreenshot);
        screenshots.add(usabilityScreenshot);
        screenshots.add(addictiveScreenshot);
        screenshots.add(stabilityScreenshot);

        title = (TextView) findViewById(R.id.app_name);
        finalVeredict = (TextView) findViewById(R.id.final_veredict);
        reviewer = (TextView) findViewById(R.id.reviewer);
        rating = (TextView) findViewById(R.id.rating);

        appIcon = (ImageView) findViewById(R.id.app_icon);
        avatar = (ImageView) findViewById(R.id.avatar);
        
        speedData = new PieChartData();
        usabilityData = new PieChartData();
        addictiveData = new PieChartData();
        stabilityData = new PieChartData();

        consContainer = (LinearLayout) findViewById(R.id.cons_container);
        prosContainer = (LinearLayout) findViewById(R.id.pros_container);

        setGraph(speedChart, speedData);
        setGraph(usabilityChart, usabilityData);
        setGraph(addictiveChart, addictiveData);
        setGraph(stabilityChart, stabilityData);

    }

    @Override
    protected void onStart() {
        super.onStart();
        manager.start(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        manager.shouldStop();
    }

    public void setGraph(PieChartView graph, PieChartData data){

        ArrayList<SliceValue> sliceValues = new ArrayList<>();
        SliceValue sliceValue2 = new SliceValue(1);
        SliceValue sliceValue = new SliceValue(0);


        sliceValues.add(sliceValue);
        sliceValues.add(sliceValue2);

        sliceValue.setColor(getResources().getColor(R.color.greenapple));
        sliceValue2.setColor(getResources().getColor(R.color.dark_custom_gray));
        data.setHasCenterCircle(true);
        data.setCenterCircleColor(Color.BLACK);
        data.setValues(sliceValues);
        data.setCenterText1FontSize(12);
        data.setCenterText1Typeface(Typeface.DEFAULT_BOLD);
        data.setCenterText1Color(Color.WHITE);
        graph.setPieChartData(data);
        graph.setChartRotationEnabled(false);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }


}


