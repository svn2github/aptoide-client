package cm.aptoide.ptdev.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;

import cm.aptoide.ptdev.Aptoide;
import cm.aptoide.ptdev.R;
import cm.aptoide.ptdev.utils.AptoideUtils;
import cm.aptoide.ptdev.webservices.timeline.json.ApkInstallComments;

public class TimelineCommentsAdapter extends BaseAdapter {

	private final List<ApkInstallComments.Comment> list;
	protected Context ctx;

    public TimelineCommentsAdapter(Context ctx, List<ApkInstallComments.Comment> list) {
        this.list = list;
        this.ctx = ctx;
    }


    @Override
    public int getCount() {
	    if (list == null) return 0;
        return list.size();
    }

    @Override
    public ApkInstallComments.Comment getItem(int position) { return list.get(position); }

	@Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent)
    {
		class ViewHolder {
			public TextView personName;
			public TextView comment;
			public TextView timestamp;
		}

	    ViewHolder holder;

	    if(convertView == null){
		    convertView = LayoutInflater.from(ctx).inflate(R.layout.row_latest_comments, null);
			convertView.findViewById(R.id.comment_app_name).setVisibility(View.GONE);

			holder = new ViewHolder();
		    holder.personName = (TextView) convertView.findViewById(R.id.user_comment);
			holder.comment = (TextView) convertView.findViewById(R.id.comment);
		    holder.timestamp = (TextView) convertView.findViewById(R.id.comment_time);

		    convertView.setTag(holder);
	    }else{
		    holder = (ViewHolder)convertView.getTag();
	    }


        ApkInstallComments.Comment comment = getItem(position);

	    holder.personName.setText(comment.getUsername());
		holder.comment.setText(comment.getText());
	    holder.timestamp.setText(getTime(comment.getTimestamp()));

//	    holder.appIcon.setVisibility(View.GONE);        // Hide an element

        return convertView;
    }

	private static String getTime(String time) {
		String result = "";
		final SimpleDateFormat dateFormater = new SimpleDateFormat("yyyy-MM-dd HH:mm");
		try {
			result = AptoideUtils.DateTimeUtils.getInstance(Aptoide.getContext()).getTimeDiffString(dateFormater.parse(time).getTime());
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return result;
	}
}
