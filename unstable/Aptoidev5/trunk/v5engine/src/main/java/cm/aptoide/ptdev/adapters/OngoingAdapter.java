package cm.aptoide.ptdev.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ProgressBar;
import cm.aptoide.ptdev.R;
import cm.aptoide.ptdev.downloadmanager.DownloadInfo;
import cm.aptoide.ptdev.model.Download;

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

        View v;
        if(convertView==null){
            v = LayoutInflater.from(context).inflate(R.layout.row_app_downloading, parent, false);
        }else{
            v = convertView;
        }
        Download download = getItem(position);

        ProgressBar pb = (ProgressBar) v.findViewById(R.id.downloading_progress);
        pb.setIndeterminate(false);

        pb.setProgress(download.getProgress());



        return v;
    }
}
