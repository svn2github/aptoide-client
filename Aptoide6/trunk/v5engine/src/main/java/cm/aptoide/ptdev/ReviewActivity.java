package cm.aptoide.ptdev;

import android.app.ActionBar;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
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

import net.danlew.android.joda.JodaTimeAndroid;

import org.joda.time.MonthDay;
import org.joda.time.Years;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.text.DateFormat;
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
    private ImageView bigImage;


    DisplayImageOptions options = new DisplayImageOptions.Builder().resetViewBeforeLoading(false).displayer(new FadeInBitmapDisplayer(1000)).build();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Aptoide.getThemePicker().setAptoideTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.app_review);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setWindowTitle("Reviews");
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
                int performance = reviewListJson.getReview().getPerformance();
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

                setValue(speedData, performance);
                setValue(addictiveData, addiction);
                setValue(stabilityData, stability);
                setValue(usabilityData, usability);


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
        bigImage = (ImageView) findViewById(R.id.bigImage);


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


