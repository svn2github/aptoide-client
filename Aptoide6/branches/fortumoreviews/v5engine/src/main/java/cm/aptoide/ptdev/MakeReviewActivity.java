package cm.aptoide.ptdev;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.octo.android.robospice.SpiceManager;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.RequestListener;

import java.util.ArrayList;
import java.util.List;

import cm.aptoide.ptdev.SpiceStuff.AlmostGenericResponseV2RequestListener;
import cm.aptoide.ptdev.events.BusProvider;
import cm.aptoide.ptdev.services.HttpClientSpiceService;
import cm.aptoide.ptdev.webservices.MakeReviewRequest;
import cm.aptoide.ptdev.webservices.json.GenericResponseV2;

/**
 * Created by asantos on 04-03-2015.
 */
public class MakeReviewActivity extends Activity {
    private static final int RATINGS_COUNT = 4;
    private static final int RATINGS_SPEED = 0;
    private static final int RATINGS_USABILITY = 1;
    private static final int RATINGS_ADDICTIVE = 2;
    private static final int RATINGS_STABILITY = 3;
    private static final String RATINGS_VALUES = "RV";
    private static final String RATINGS_AVG = "AVG";

    private static final int[] PRO_IDS = {R.id.Pro1,R.id.Pro2,R.id.Pro3};
    private static final int[] CON_IDS = {R.id.Con1,R.id.Con2,R.id.Con3};

    public static final String EXTRA_PACKAGE = "PACKAGE";
    public static final String EXTRA_REPO = "ERS";
    public static final String EXTRA_ICON = "EICON";
    public static final String EXTRA_APP_NAME = "EAPPNAME";
    public static final String EXTRA_SIZE = "ESIZE";
    public static final String EXTRA_DOWNLOADS = "EDOWNLOADS";
    public static final String EXTRA_STARS = "ESTARS";

    int[] ratingValues = new int[RATINGS_COUNT];
    double avg;

    private TextView scoreTV;
    private String scoreString;


    private SpiceManager spiceManager = new SpiceManager(HttpClientSpiceService.class);

    @Override
    protected void onPause() {
        super.onPause();
        spiceManager.shouldStop();
    }
    @Override
    protected void onResume() {
        super.onResume();
        spiceManager.start(this);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_make_review);
        scoreTV = (TextView) findViewById(R.id.finalScore);
        scoreString = getString(R.string.review_final_score);

        ImageLoader.getInstance().displayImage(
                getIntent().getStringExtra(EXTRA_ICON),
                (ImageView)findViewById(R.id.app_icon));

        ((TextView) findViewById(R.id.app_name)).setText(getIntent().getStringExtra(EXTRA_APP_NAME));
        String text = getString(R.string.size) + ":" + getIntent().getLongExtra(EXTRA_SIZE,0);
        ((TextView) findViewById(R.id.text1)).setText(text);
        text = getString(R.string.downloads) + ":" + getIntent().getIntExtra(EXTRA_DOWNLOADS,0);
        ((TextView) findViewById(R.id.text2)).setText(text);

        ((RatingBar) findViewById(R.id.app_rating)).setRating(getIntent().getFloatExtra(EXTRA_STARS,0.0f));

        if(savedInstanceState!=null){
            ratingValues=savedInstanceState.getIntArray(RATINGS_VALUES);
            avg=savedInstanceState.getDouble(RATINGS_AVG);
            if(avg<1) updateScoreUI();
        }else{
            for (int i = 0; i < RATINGS_COUNT; i++) {
                ratingValues[i]=10;
            }
        }
        setupSeekBar(RATINGS_SPEED,R.string.review_speed,R.id.Seek_Bar_Speed);
        setupSeekBar(RATINGS_USABILITY,R.string.review_usability,R.id.Seek_Bar_Usability);
        setupSeekBar(RATINGS_ADDICTIVE,R.string.review_addictive,R.id.Seek_Bar_Funny);
        setupSeekBar(RATINGS_STABILITY,R.string.review_stability,R.id.Seek_Bar_Stability);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putIntArray(RATINGS_VALUES,ratingValues);
        outState.putDouble(RATINGS_AVG,avg);
    }

    private void setupSeekBar(final int valuesPos,int title, int id){
        View v = findViewById(id);
        SeekBar seekbar = (SeekBar) v.findViewById(R.id.seek_bar_on_row);
        TextView titleTV = (TextView) v.findViewById(R.id.seek_bar_name);
        final TextView seek_bar_value = (TextView) v.findViewById(R.id.seek_bar_value);
        titleTV.setText(title);
        seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                progress+=1;
                if(fromUser){
                    seek_bar_value.setText(String.valueOf(progress));
                    ratingValues[valuesPos]=progress;
                    updateScore();
                }else if(ratingValues[valuesPos] !=0){
                    seekBar.setProgress(ratingValues[valuesPos]);
                }
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        if(ratingValues[valuesPos] !=0){
            seekbar.setProgress(ratingValues[valuesPos]);
            seek_bar_value.setText(String.valueOf(ratingValues[valuesPos]));
        }
    }

    private void updateScore(){
        int sum = 0;
        for (int i = 0; i < RATINGS_COUNT; i++) {
            sum+= ratingValues[i];
        }
        avg = (double) sum/RATINGS_COUNT;
        updateScoreUI();
    }

    private void updateScoreUI(){
        if(scoreTV==null){
            scoreTV = (TextView) findViewById(R.id.finalScore);
        }
        scoreTV.setText(String.format("%s %.1f",scoreString, avg));
    }

    private List<String> getStrings(int [] ids){
        ArrayList<String> ret = new ArrayList<>(ids.length);
        for (int i = 0; i < ids.length; i++) {
            EditText et = (EditText) findViewById(ids[i]);
            String s = et.getText().toString();
            if(s.length()>0)ret.add(s);
        }
        return ret;
    }

    public void finishButtonClick(View view){
        //TODO Strings
        Log.d("pois", "finishButtonClick" );
        Log.d("pois", "avg:"+avg );
        if(avg<1) {
            /*If true the sliders were never moved...*/
            Toast.makeText(Aptoide.getContext(), "Set the Sliders", Toast.LENGTH_LONG).show();
            return;
        }

        List<String> pros = getStrings(PRO_IDS);
        if(pros.size()==0){
            Toast.makeText(Aptoide.getContext(), "Write at least one pro", Toast.LENGTH_LONG).show();
            return;
        }
        List<String> cons = getStrings(CON_IDS);
        if(cons.size()==0){
            Toast.makeText(Aptoide.getContext(), "Write at least one con", Toast.LENGTH_LONG).show();
            return;
        }
        String final_verdict = ((EditText) findViewById(R.id.make_review_final_verdict)).getText().toString();
        if(final_verdict.length()<=1){
            Toast.makeText(Aptoide.getContext(), "Write Your final Verdict", Toast.LENGTH_LONG).show();
            return;
        }



        for(String pro : pros){
            Log.d("pois", "pro: "+pro );
        }
        for(String pro : cons){
            Log.d("pois", "con: "+pro );
        }


        for (int i = 0; i < ratingValues.length; i++) {
            Log.d("pois", "values ["+i+"] =   "+ ratingValues[i] );
        }

        MakeReviewRequest request = new MakeReviewRequest();
        request.setPackage_name(getIntent().getStringExtra(EXTRA_PACKAGE));
        request.setRepoID(getIntent().getLongExtra(EXTRA_REPO, -1));
        request.setPerformance(ratingValues[RATINGS_SPEED]);
        request.setUsability(ratingValues[RATINGS_USABILITY]);
        request.setAddiction(ratingValues[RATINGS_ADDICTIVE]);
        request.setStability(ratingValues[RATINGS_STABILITY]);

        MakeReviewRequest.ReviewPost.Locale en_GB = new MakeReviewRequest.ReviewPost.Locale("en_GB");
        en_GB.setCons(cons);
        en_GB.setPros(pros);
        en_GB.setFinalVerdict(final_verdict);
        request.addLocale(en_GB);

        spiceManager.execute(request,new RequestListener<GenericResponseV2>() {
            @Override
            public void onRequestFailure(SpiceException spiceException) {
                Log.d("pois", "Fail");
            }

            @Override
            public void onRequestSuccess(GenericResponseV2 genericResponseV2) {
                Log.d("pois", "Success");
            }
        });
    }

}
