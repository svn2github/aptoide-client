package cm.aptoidetv.pt;

import android.app.DialogFragment;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.squareup.picasso.RequestCreator;

public class SplashDialogFragment extends DialogFragment {
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
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        int parsed_color;
        try{
            parsed_color = Color.parseColor(getActivity().getString(R.string.splashcolor));
        }catch(Exception e){
            parsed_color = Color.parseColor("#50FFFFFF");
        }
        RelativeLayout splashBackground = (RelativeLayout) view.findViewById(R.id.splash_background);
        splashBackground.setBackgroundColor(parsed_color);
        DisplayMetrics mMetrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(mMetrics);
        int splashscreenID= getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE?
            R.string.splashscreenland:R.string.splashscreen;
        String link = getActivity().getString(splashscreenID);
        RequestCreator img = link.length()==0 ?
                AppTV.getPicasso().load(R.drawable.splashscreen_land):
                AppTV.getPicasso().load(link);
        img.resize(mMetrics.widthPixels, mMetrics.heightPixels)
            .centerCrop()
            .error(parsed_color)
            .into((ImageView) view.findViewById(R.id.splashscreen));
    }
}
