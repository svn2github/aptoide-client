package cm.aptoide.ptdev.adapters;

import android.content.Context;
import android.support.v4.app.FragmentActivity;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import cm.aptoide.ptdev.EnumDownloadStates;
import cm.aptoide.ptdev.R;
import cm.aptoide.ptdev.downloadmanager.EnumDownloadFailReason;
import cm.aptoide.ptdev.downloadmanager.state.EnumState;
import cm.aptoide.ptdev.model.Download;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;

/**
 * Created by rmateus on 12-12-2013.
 */
public class NotOngoingAdapter extends ArrayAdapter<Download>{

    @Override
    public long getItemId(int position) {
        return getItem(position).getId();
    }

    public NotOngoingAdapter(Context context, ArrayList<Download> notOngoingList) {
        super(context, 0, notOngoingList);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View v;
        if(convertView==null){
            v = LayoutInflater.from(getContext()).inflate(R.layout.row_app_downloading, parent, false);
        }else{
            v = convertView;
        }
        final Download download = getItem(position);

        ((TextView)v.findViewById(R.id.app_name)).setText(Html.fromHtml(download.getName()).toString());


        v.findViewById(R.id.manage_icon).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                download.getParent().remove();
            }
        });
        ImageLoader.getInstance().displayImage(download.getIcon(), (ImageView) v.findViewById(R.id.app_icon));

        ProgressBar pb = (ProgressBar) v.findViewById(R.id.downloading_progress);
        pb.setVisibility(View.GONE);

        EnumState downloadState = download.getDownloadState();
        switch(downloadState){
            case ERROR:
                ((TextView)v.findViewById(R.id.app_error)).setText(download.getParent().getFailReason().toString(getContext()));
                v.findViewById(R.id.app_error).setVisibility(View.VISIBLE);
                break;
            case COMPLETE:
                v.findViewById(R.id.app_error).setVisibility(View.GONE);
                break;
        }

        if(v==null){
            Log.e("Aptoide-Section", "View is null");
        }
        return v;
    }
}
