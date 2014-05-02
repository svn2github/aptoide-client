package cm.aptoide.ptdev.adapters;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import cm.aptoide.ptdev.model.MultiStoreItem;

/**
 * Created by rmateus on 14-04-2014.
 */
public class StoreSpinnerAdapter extends ArrayAdapter<MultiStoreItem> {

    public StoreSpinnerAdapter(Context context, MultiStoreItem[] objects) {
        super(context, android.R.layout.simple_list_item_1,android.R.id.text1, objects);
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        TextView v;

        if(convertView == null){
            v = new TextView(getContext());
        }else{
            v = (TextView) convertView;
        }

        v.setText(getItem(position).getName());

        return v;
    }
}
