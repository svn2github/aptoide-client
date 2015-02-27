package cm.aptoide.ptdev;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.octo.android.robospice.SpiceManager;
import com.octo.android.robospice.persistence.DurationInMillis;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.RequestListener;

import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import cm.aptoide.ptdev.services.HttpClientSpiceService;
import cm.aptoide.ptdev.utils.Configs;
import cm.aptoide.ptdev.webservices.GetReviews;
import cm.aptoide.ptdev.webservices.json.reviews.ReviewJson;
import cm.aptoide.ptdev.webservices.json.reviews.Screenshot;
import lecho.lib.hellocharts.model.PieChartData;
import lecho.lib.hellocharts.model.SliceValue;
import lecho.lib.hellocharts.view.PieChartView;

/**
 * Created by rmateus on 16-02-2015.
 */
public class ReviewActivity extends ActionBarActivity {

    SpiceManager manager = new SpiceManager(HttpClientSpiceService.class);

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
    private ImageView bigImage;

    ArrayList<ImageView> screenshots;

    DisplayImageOptions options = new DisplayImageOptions.Builder().resetViewBeforeLoading(false).displayer(new FadeInBitmapDisplayer(1000)).build();
    private TextView speedLabel;
    private TextView usabilityLabel;
    private TextView addictiveLabel;
    private TextView stabilityLabel;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Aptoide.getThemePicker().setAptoideTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.app_review);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setWindowTitle(getString(R.string.review_title));
        init();

        getWindow().getDecorView().setBackgroundColor(Color.WHITE);

        GetReviews.GetReview reviewsRequest = new GetReviews.GetReview();
        int id = getIntent().getIntExtra("id", 0);
        reviewsRequest.setId(id);

        manager.execute(reviewsRequest, "review-id-" + id, DurationInMillis.ONE_DAY,new RequestListener<ReviewJson>() {
            @Override
            public void onRequestFailure(SpiceException spiceException) {

            }

            @Override
            public void onRequestSuccess(final ReviewJson reviewListJson) {
                Log.d("AptoideReview", reviewListJson.toString()) ;
                int addiction = reviewListJson.getReview().getAddiction();
                int speed = reviewListJson.getReview().getPerformance();
                int stability = reviewListJson.getReview().getStability();
                int usability = reviewListJson.getReview().getUsability();

                TextView vername = (TextView) findViewById(R.id.vername_date);

                Date date = null;
                try {
                    date = Configs.TIME_STAMP_FORMAT.parse(reviewListJson.getReview().getAddedTimestamp());
                    DateTimeFormatter dateTimeFormatter = DateTimeFormat.forPattern("MMMM y");
                    vername.setText("Version: " + reviewListJson.getReview().getApk().getVername() + " - " + dateTimeFormatter.print(date.getTime()));
                } catch (ParseException e) {
                    e.printStackTrace();
                    vername.setText(reviewListJson.getReview().getApk().getVername());
                }

                findViewById(R.id.getapp).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(v.getContext(), Aptoide.getConfiguration().getAppViewActivityClass());
                        intent.putExtra("fromApkInstaller", true);
                        intent.putExtra("id", reviewListJson.getReview().getApk().getId().longValue());
                        startActivity(intent);
                    }
                });

                setValue(speedData, speed,speedLabel,speedChart);
                setValue(addictiveData, addiction,addictiveLabel,addictiveChart);
                setValue(stabilityData, stability,stabilityLabel,stabilityChart);
                setValue(usabilityData, usability,usabilityLabel,usabilityChart);

                rating.setText(String.valueOf(reviewListJson.getReview().getRating()));
                title.setText(reviewListJson.getReview().getApk().getTitle());

                finalVeredict.setText(reviewListJson.getReview().getFinalVerdict());

                List<Screenshot> reviewScreenshots = reviewListJson.getReview().getApk().getScreenshots();
                int i = 0;
                if(i<reviewScreenshots.size()){
                    ImageLoader.getInstance().displayImage(reviewScreenshots.get(i++).getUrl(), bigImage, options);
                }

                for (ImageView screenshot : screenshots) {
                    if(i<reviewScreenshots.size()){
                        ImageLoader.getInstance().displayImage(reviewScreenshots.get(i++).getUrl(), screenshot, options);
                    }
                }

                ImageLoader.getInstance().displayImage(reviewListJson.getReview().getApk().getIcon(), appIcon, options);
                ImageLoader.getInstance().displayImage(reviewListJson.getReview().getUser().getAvatar(), avatar, options);

                reviewer.setText("Review by:\n" + reviewListJson.getReview().getUser().getName());

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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if(item.getItemId() == android.R.id.home || item.getItemId() == R.id.home){
            finish();
        }

        return super.onOptionsItemSelected(item);
    }

    private void setValue(PieChartData data, int score, TextView label,PieChartView chartView){
        int color = getColorBasedOnScore(score);
        setGraph(chartView, data,color);
        label.setBackgroundColor(color);
        data.getValues().get(0).setTarget(score);
        data.getValues().get(1).setTarget(10-score);
        data.setCenterText1(score + "/10");
    }

    private int getColorBasedOnScore(int score){
        String hexColor;
        if(score >=9){
            hexColor = "#00c81b";
        }else if(score >= 7 ){
            hexColor= "#d9d31a";
        }else if(score>=5){
            hexColor = "#ff6600";
        }else{
            hexColor= "#ff3037";
        }
        return Color.parseColor(hexColor);
    }

    private void init() {
        final View speed_chart = findViewById(R.id.speed_chart);
        final View usability_chart = findViewById(R.id.usability_chart);
        final View addictive_chart = findViewById(R.id.addictive_chart);
        final View stability_chart = findViewById(R.id.stability_chart);

        speedChart = (PieChartView) speed_chart.findViewById(R.id.chart);
        usabilityChart = (PieChartView) usability_chart.findViewById(R.id.chart);
        addictiveChart = (PieChartView) addictive_chart.findViewById(R.id.chart);
        stabilityChart = (PieChartView) stability_chart.findViewById(R.id.chart);

        screenshots = new ArrayList<>();
        screenshots.add((ImageView) speed_chart.findViewById(R.id.screenshot));
        screenshots.add((ImageView) usability_chart.findViewById(R.id.screenshot));
        screenshots.add((ImageView) addictive_chart.findViewById(R.id.screenshot));
        screenshots.add((ImageView) stability_chart.findViewById(R.id.screenshot));

        bigImage = (ImageView) findViewById(R.id.bigImage);

        speedLabel = (TextView) speed_chart.findViewById(R.id.designation);
        usabilityLabel = (TextView) usability_chart.findViewById(R.id.designation);
        addictiveLabel = (TextView) addictive_chart.findViewById(R.id.designation);
        stabilityLabel = (TextView) stability_chart.findViewById(R.id.designation);

        speedLabel.setText(R.string.review_speed);
        usabilityLabel.setText(R.string.review_usability);
        addictiveLabel.setText(R.string.review_addictive);
        stabilityLabel.setText(R.string.review_stability);

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

    private void setGraph(PieChartView graph, PieChartData data,int color){
        ArrayList<SliceValue> sliceValues = new ArrayList<>();
        SliceValue sliceValue2 = new SliceValue(1);
        SliceValue sliceValue = new SliceValue(0);

        sliceValues.add(sliceValue);
        sliceValues.add(sliceValue2);

        sliceValue.setColor(color);
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

}


