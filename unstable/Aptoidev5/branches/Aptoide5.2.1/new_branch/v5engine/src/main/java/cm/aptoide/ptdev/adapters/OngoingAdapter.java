package cm.aptoide.ptdev.adapters;

import android.content.Context;
import android.os.Build;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import cm.aptoide.ptdev.R;
import cm.aptoide.ptdev.downloadmanager.DownloadInfo;
import cm.aptoide.ptdev.downloadmanager.Utils;
import cm.aptoide.ptdev.model.Download;
import cm.aptoide.ptdev.utils.AptoideUtils;

import com.flurry.android.FlurryAgent;
import com.mopub.mobileads.HtmlBanner;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;

/**
 * Created by rmateus on 12-12-2013.
 */
public class OngoingAdapter extends ArrayAdapter<Download> {


    private Context context;
    private final ArrayList<Download> list;

    public OngoingAdapter(Context context, ArrayList<Download> ongoingList) {
        super(context, 0, ongoingList);
        this.context = context;
        this.list = ongoingList;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Download getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return getItem(position).getId();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Log.d("Aptoide", "Updating view2");
        View v;
        if(convertView==null){
            v = LayoutInflater.from(context).inflate(R.layout.row_app_downloading, parent, false);
        }else{
            v = convertView;
        }

        final Download download = getItem(position);

        if(download.getName()!=null){
            ((TextView) v.findViewById(R.id.app_name)).setText(Html.fromHtml(download.getName()).toString());
        }else{
            ((TextView) v.findViewById(R.id.app_name)).setText("");
        }
        ImageLoader.getInstance().displayImage(download.getIcon(), (ImageView) v.findViewById(R.id.app_icon));
        ProgressBar pb = (ProgressBar) v.findViewById(R.id.downloading_progress);
        pb.setIndeterminate(false);

        pb.setProgress(download.getProgress());


        View downloadsLayout = v.findViewById(R.id.download_details_layout);
        downloadsLayout.setVisibility(View.VISIBLE);
        ((TextView) downloadsLayout.findViewById(R.id.speed)).setText(Utils.formatBits((long) download.getSpeed())+"/s");
        ((TextView) downloadsLayout.findViewById(R.id.eta)).setText(Utils.formatEta(download.getTimeLeft(), context.getString(R.string.remaining_time)));
        ((TextView) downloadsLayout.findViewById(R.id.progress)).setText(download.getProgress()+"%");


        v.findViewById(R.id.manage_icon).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FlurryAgent.logEvent("Download_Manager_Clicked_On_Stop_Button");
                download.getParent().remove(false);
            }
        });
        if(v==null){
            Log.e("Aptoide-Section", "View is null");
        }
        return v;
    }
}
