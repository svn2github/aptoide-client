package cm.aptoide.ptdev.adapters;

import android.app.Activity;
import android.content.Intent;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import cm.aptoide.ptdev.AppViewActivity;
import cm.aptoide.ptdev.Aptoide;
import cm.aptoide.ptdev.R;
import cm.aptoide.ptdev.utils.AptoideUtils;
import cm.aptoide.ptdev.utils.IconSizes;
import cm.aptoide.ptdev.webservices.json.RelatedApkJson;
import com.nostra13.universalimageloader.core.ImageLoader;
import org.joda.time.*;
import org.w3c.dom.Text;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: rmateus
 * Date: 27-11-2013
 * Time: 15:26
 * To change this template use File | Settings | File Templates.
 */
public class RelatedBucketAdapter extends BucketListAdapter<RelatedApkJson.Item> {

    private final String sizeString;
    private Class appViewClass = Aptoide.getConfiguration().getAppViewActivityClass();

    public RelatedBucketAdapter(Activity ctx, List<RelatedApkJson.Item> elements) {

        super(ctx, elements);
        enableAutoMeasure(120);
        sizeString = IconSizes.generateSizeString(ctx);

    }

    @Override
    public int getBucketSize() {
        return super.getBucketSize();
    }

    @Override
    public boolean isEnabled(int position) {
        return false;
    }

    final SimpleDateFormat dateFormater = new SimpleDateFormat("yyyy-MM-dd HH:mm");

    @Override
    protected View bindBucketElement(int position, RelatedApkJson.Item currentElement, View convertView, ViewGroup parent) {
        final View v;


        ViewHolder holder;

        if (convertView == null) {
            holder = new ViewHolder();
            v = LayoutInflater.from(ctx).inflate(R.layout.row_app_related, parent, false);

            holder.version = (TextView) v.findViewById(R.id.app_version);
            holder.name = (TextView) v.findViewById(R.id.app_name);
            holder.icon = (ImageView) v.findViewById(R.id.app_icon);
            holder.repo = (TextView) v.findViewById(R.id.app_repo);
            holder.timestamp = (TextView) v.findViewById(R.id.app_timestamp);
            v.setTag(holder);
        } else {
            v = convertView;
            holder = (ViewHolder) v.getTag();
        }

        final RelatedApkJson.Item item = getItem(position);
        Log.d("Aptoide-Related", "adding item " + item.getName() + " " + item.getVername() + " " + item.getRepo());


        holder.name.setText(item.getName());
        holder.version.setText(item.getVername());
        holder.repo.setText(getContext().getString(R.string.store)+": "+item.getRepo());

        try {
            Date date = dateFormater.parse(item.getTimestamp());
            holder.timestamp.setText(AptoideUtils.DateDiffUtils.getDiffDate(getContext(), date));
        } catch (ParseException e) {
            e.printStackTrace();
        }

        Log.d("Aptoide-Related", " item " + item.getName() + " " + item.getVername() + " " + item.getRepo());


        String icon = item.getIcon();

        if (icon.contains("_icon")) {
            String[] splittedUrl = icon.split("\\.(?=[^\\.]+$)");
            icon = splittedUrl[0] + "_" + sizeString + "." + splittedUrl[1];
        }

        ImageLoader.getInstance().displayImage(icon , holder.icon);

        v.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getContext(), appViewClass);
                i.putExtra("fromRelated", true);
                i.putExtra("appName", item.getName());

                i.putExtra("repoName", item.getRepo());
                i.putExtra("md5sum", item.getMd5sum());
                getContext().startActivity(i);
            }
        });

        return v;
    }

    static class ViewHolder{
        TextView name;
        TextView version;
        ImageView icon;
        TextView repo;
        TextView timestamp;
    }

}
