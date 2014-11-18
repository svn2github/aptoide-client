package cm.aptoidetv.pt;

import android.app.DialogFragment;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.squareup.picasso.Picasso;

/**
 * Created by rmateus on 21-03-2014.
 */
public class SplashDialogFragment extends DialogFragment {


    private ImageView imageSplash;
    private RelativeLayout splashBackground;
    private DisplayMetrics mMetrics;
    public static String TAG = "SplashDialog";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NO_TITLE, R.style.Theme_AppCompat_Light);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.splash, container, false);
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    private void startTimer() {
        Handler handler = new Handler(Looper.getMainLooper());
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (isAdded()) {
                    dismissAllowingStateLoss();
                }
            }
        }, 3000);
    }


    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        imageSplash = (ImageView) view.findViewById(R.id.splashscreen);
        splashBackground = (RelativeLayout) view.findViewById(R.id.splash_background);

        String color = getActivity().getString(R.string.splashcolor);
        int parsed_color = 0;
        try{
            parsed_color = Color.parseColor(color);
        }catch(Exception e){
            parsed_color = Color.parseColor("#50FFFFFF");
        }finally{
            splashBackground.setBackgroundColor(parsed_color);
        }

        mMetrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(mMetrics);
        Log.d(TAG, "Color: " + parsed_color);

        if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE){
            Log.d(TAG, "Showing: "+getActivity().getString(R.string.splashscreenland));

            Picasso.with(getActivity())
                    .load(getActivity().getString(R.string.splashscreenland))
                    .resize(mMetrics.widthPixels, mMetrics.heightPixels)
                    .centerCrop()
                    .error(parsed_color)
                    .into(imageSplash);

            startTimer();

        }else{
            Log.d(TAG, "Showing: "+getActivity().getString(R.string.splashscreen));

            Picasso.with(getActivity())
                    .load(getActivity().getString(R.string.splashscreen))
                    .resize(mMetrics.widthPixels, mMetrics.heightPixels)
                    .centerCrop()
                    .error(parsed_color)
                    .into(imageSplash);

            startTimer();

        }




    }


}
