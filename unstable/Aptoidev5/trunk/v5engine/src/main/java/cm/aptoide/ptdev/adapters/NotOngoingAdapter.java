package cm.aptoide.ptdev.adapters;

import android.content.Context;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import cm.aptoide.ptdev.R;
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
        Download download = getItem(position);

        ((TextView)v.findViewById(R.id.app_name)).setText(download.getName());
        ImageLoader.getInstance().displayImage(download.getIcon(), (ImageView) v.findViewById(R.id.app_icon));

        ProgressBar pb = (ProgressBar) v.findViewById(R.id.downloading_progress);
        pb.setVisibility(View.GONE);

        return v;
    }
}
