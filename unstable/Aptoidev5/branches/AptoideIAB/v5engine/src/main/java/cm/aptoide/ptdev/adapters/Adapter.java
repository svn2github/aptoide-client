package cm.aptoide.ptdev.adapters;

import android.content.Context;
import android.content.Intent;
import android.database.DataSetObserver;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import cm.aptoide.ptdev.Aptoide;
import cm.aptoide.ptdev.MoreEditorsChoiceActitivy;
import cm.aptoide.ptdev.MoreTopAppsActivity;
import cm.aptoide.ptdev.R;
import cm.aptoide.ptdev.fragments.Home;
import cm.aptoide.ptdev.fragments.HomeBucket;
import cm.aptoide.ptdev.fragments.HomeCategory;
import cm.aptoide.ptdev.fragments.HomeFooter;
import cm.aptoide.ptdev.fragments.HomeItem;
import cm.aptoide.ptdev.utils.IconSizes;

/**
 * Created by rmateus on 17-07-2014.
 */
public class Adapter extends BaseAdapter {

    protected float getScreenWidthInDip() {
        WindowManager wm = ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE));
        DisplayMetrics dm = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(dm);
        int screenWidth_in_pixel = dm.widthPixels;

        return screenWidth_in_pixel / dm.density;
    }

    private final int bucketSize;
    private final LayoutInflater inflater;


    Set<Integer> set = new HashSet<Integer>();
    int maxCount;
    public Adapter(Context context) {
        this.context = context;
        float screenWidth = getScreenWidthInDip();

        if (120 >= screenWidth) {
            this.bucketSize = 1;
        } else {
            this.bucketSize = (int) (screenWidth / 120);;
        }

        inflater = LayoutInflater.from(context);

        registerDataSetObserver(new DataSetObserver() {
            @Override
            public void onChanged() {
                maxCount = 0;
                for(Home item: items){

                    int count = item.getItemsSize() + 1;

                    if(count > maxCount){
                        maxCount = count;
                    }

                    set.add(count);
                }

                //Log.d("yuuup", set + " maxcount is: " + maxCount);

            }
        });
    }



    private Context context;

    public void setItems(ArrayList<Home> items) {
        this.items = items;
    }

    public ArrayList<Home> getItems() {
        return items;
    }

    ArrayList<Home> items = new ArrayList<Home>();


    @Override
    public int getViewTypeCount() {
        return maxCount + 1;
    }



    @Override
    public int getItemViewType(int position) {

        int result;

        if(getItem(position) instanceof HomeCategory){
            result =0;
        }else if(getItem(position) instanceof HomeFooter){
            result =1;
        }else {
            result= getItem(position).getItemsSize() + 1 ;
        }

        //Log.d("yuuuup", "itemType" + result);

        return result;

    }

    @Override
    public boolean areAllItemsEnabled() {
        return false;
    }

    @Override
    public boolean isEnabled(int position) {
        return false;
    }

    @Override
    public int getCount() {
        return  items.size();
    }

    @Override
    public Home getItem(int position) {
        return items.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        View item = null;
        //Log.d("yuuup", "cenas");
        switch (getItemViewType(position)){

            case 1:
                if(convertView==null){
                    item = View.inflate(context, R.layout.separator_home_footer, null);
                }else{
                    item = convertView;
                }

                item.findViewById(R.id.more).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {



                        HomeFooter item = (HomeFooter) getItem(position);
                        Intent i;
                        if(item.getParentId() > -2){
                            i = new Intent(Aptoide.getContext(), MoreEditorsChoiceActitivy.class);
                            i.putExtra("parentId", item.getParentId());
                        }else{
                            i = new Intent(Aptoide.getContext(), MoreTopAppsActivity.class);
                        }


                        context.startActivity(i);
                    }
                });

                break;
            default :
                item = createRow(position, convertView);
                break;
            case 0:
                //Toast.makeText(context, "Inflating Footer", Toast.LENGTH_LONG).show();
                if(convertView==null){
                    item = View.inflate(context, R.layout.separator_home_header, null);
                }else{
                    item = convertView;
                }
                ((TextView)item.findViewById(R.id.separator_label)).setText(getItem(position).getName());
                break;
        }

        return item;
    }

    private Class appViewClass = Aptoide.getConfiguration().getAppViewActivityClass();


    private View createRow(int position, View convertView){

        View view;
        if(convertView==null){
            //Log.d("Sup", "Init view " );
            view = new LinearLayout(context);
            LinearLayout layout = (LinearLayout) view;
            layout.setOrientation(LinearLayout.HORIZONTAL);
            layout.setLayoutParams(new AbsListView.LayoutParams(AbsListView.LayoutParams.MATCH_PARENT,AbsListView.LayoutParams.MATCH_PARENT));

            int itemCount = getItem(position).getItemsSize();



            for(int i = 0 ;  i < itemCount  ; i++ ){
                layout.addView(inflater.inflate(R.layout.row_app_home, layout, false));
            }



        }else{
            //Log.d("Sup", "Reusing view ");
            view = convertView;
        }

        for(int i = 0;  i < getItem(position).getItemsSize(); i++ ){

            View root = ((LinearLayout) view).getChildAt(i);
            CellViewHolder holder = (CellViewHolder) root.getTag();
            final HomeItem item = ((HomeBucket) getItem(position)).getItemsList().get(i);

            if(holder == null){
                holder = new CellViewHolder();
                holder.tv = (TextView) (root.findViewById(R.id.app_name));
                holder.icon = (ImageView) (root.findViewById(R.id.app_icon));
                root.setTag(holder);
            }

            String icon = item.getIcon();
            if (icon.contains("_icon")) {
                String[] splittedUrl = icon.split("\\.(?=[^\\.]+$)");
                icon = splittedUrl[0] + "_" + IconSizes.generateSizeString(Aptoide.getContext()) + "." + splittedUrl[1];
            }

            root.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent i = new Intent(context, appViewClass);
                    long id = item.getId();
                    i.putExtra("id", id);
                    context.startActivity(i);
                }
            });
            ImageLoader.getInstance().displayImage(icon, holder.icon);
            holder.tv.setText(item.getName());

        }

        return view;
    }

    public int getBucketSize() {
        return bucketSize;
    }


    public static class CellViewHolder{
        public TextView tv;
        public ImageView icon;
    }

}
