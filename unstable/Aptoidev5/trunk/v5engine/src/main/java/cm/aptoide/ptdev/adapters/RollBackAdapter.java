package cm.aptoide.ptdev.adapters;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import cm.aptoide.ptdev.R;

/**
 * Created with IntelliJ IDEA.
 * User: tdeus
 * Date: 9/18/13
 * Time: 2:29 PM
 * To change this template use File | Settings | File Templates.
 */
public class RollBackAdapter extends CursorAdapter {

    public RollBackAdapter(Context context) {
        super(context, null, FLAG_REGISTER_CONTENT_OBSERVER);

    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        int type = getItemViewType(cursor.getPosition());
        View v = null;
        switch (type){
            case 0:
                v = LayoutInflater.from(context).inflate(R.layout.separator_rollback, parent, false);
                break;
            case 1:
                v = LayoutInflater.from(context).inflate(R.layout.row_app_rollback, parent, false);
                break;
        }

        return v;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {

    }

    public static class RollBackViewHolder {


        public TextView name;
        public  ImageView icon;
        public  TextView version;
        public  TextView timestamp;

    }
}
