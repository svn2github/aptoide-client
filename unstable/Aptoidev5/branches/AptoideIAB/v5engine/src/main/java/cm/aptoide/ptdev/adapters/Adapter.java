package cm.aptoide.ptdev.adapters;

import android.content.Context;
import android.content.Intent;
import android.database.DataSetObserver;
import android.os.Build;
import android.support.v7.widget.PopupMenu;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.flurry.android.FlurryAgent;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import cm.aptoide.ptdev.Aptoide;
import cm.aptoide.ptdev.DownloadInterface;
import cm.aptoide.ptdev.EnumCategories;
import cm.aptoide.ptdev.MoreEditorsChoiceActitivy;
import cm.aptoide.ptdev.MoreTopAppsActivity;
import cm.aptoide.ptdev.R;
import cm.aptoide.ptdev.fragments.Home;
import cm.aptoide.ptdev.fragments.HomeBucket;
import cm.aptoide.ptdev.fragments.HomeCategory;
import cm.aptoide.ptdev.fragments.HomeFooter;
import cm.aptoide.ptdev.fragments.HomeItem;
import cm.aptoide.ptdev.utils.IconSizes;

import static cm.aptoide.ptdev.utils.AptoideUtils.withSuffix;

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

                Log.d("yuuup", set + " maxcount is: " + maxCount);

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

//        Log.d("yuuuup", "itemType" + result);

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
//        Log.d("yuuup", "cenas");
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
                            if(Build.VERSION.SDK_INT >= 10) FlurryAgent.logEvent("Home_Page_Clicked_On_More_Editors_Choice_Button");
                        }else{
                            if(Build.VERSION.SDK_INT >= 10) FlurryAgent.logEvent("Home_Page_Clicked_On_More_Top_Apps_Button");
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

                String categoryName;
                try {
                    categoryName = context.getString(EnumCategories.getCategoryName(getItem(position).getParentId()));
//                Log.d("HomeLayoutAdapter-categ", "Category Name: " + categoryName);
                } catch (Exception e) {
                    categoryName = getItem(position).getName();
//                Log.d("HomeLayoutAdapter-categ", "Untranslated Category Name: " + categoryName);
                }

                ((TextView)item.findViewById(R.id.separator_label)).setText(categoryName);
                break;
        }

        return item;
    }

    private Class appViewClass = Aptoide.getConfiguration().getAppViewActivityClass();


    private View createRow(int position, View convertView){

        View view;
        if(convertView==null){
//            Log.d("Sup", "Init view " );
            view = new LinearLayout(context);
            LinearLayout layout = (LinearLayout) view;
            layout.setOrientation(LinearLayout.HORIZONTAL);
            layout.setLayoutParams(new AbsListView.LayoutParams(AbsListView.LayoutParams.MATCH_PARENT,AbsListView.LayoutParams.MATCH_PARENT));

            int itemCount = getItem(position).getItemsSize();



            for(int i = 0 ;  i < itemCount  ; i++ ){
                layout.addView(inflater.inflate(R.layout.row_app_home, layout, false));
            }



        }else{
//            Log.d("Sup", "Reusing view ");
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
                holder.category = (TextView) (root.findViewById(R.id.app_category));
                holder.overflow = (ImageView) (root.findViewById(R.id.ic_action));
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
                    i.putExtra("whereFrom", "editorsChoice");
                    context.startActivity(i);
                }
            });
            ImageLoader.getInstance().displayImage(icon, holder.icon);
            holder.tv.setText(item.getName());

            String category;
            try {
                int cat = Integer.parseInt(item.getCategory());

                if(cat > 0){
                    category = context.getString(EnumCategories.getCategoryName(cat));
                }else{
                    category = context.getString(R.string.X_download_number, withSuffix(item.getDownloads()));
                }


            }catch (Exception e){
                category = item.getCategoryString();
            }
            holder.category.setText(category);

            holder.overflow.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    FlurryAgent.logEvent("Home_Page_Opened_Popup_Menu");
                    showPopup(v, item.getId());
                }
            });
        }

        return view;
    }

    public int getBucketSize() {
        return bucketSize;
    }


    public static class CellViewHolder{
        public TextView tv;
        public ImageView icon;
        public TextView category;
        public ImageView overflow;
    }

    public void showPopup(View v, long id) {
        PopupMenu popup = new PopupMenu(context, v);
        popup.setOnMenuItemClickListener(new MenuListener(context, id));
        popup.inflate(R.menu.menu_actions);
        popup.show();
    }

    static class MenuListener implements PopupMenu.OnMenuItemClickListener{

        Context context;
        long id;

        MenuListener(Context context, long id) {
            this.context = context;
            this.id = id;


        }

        @Override
        public boolean onMenuItemClick(MenuItem menuItem) {
            int i = menuItem.getItemId();

            if (i == R.id.menu_install) {
                ((DownloadInterface)context).installApp(id);
                Toast.makeText(context, context.getString(R.string.starting_download), Toast.LENGTH_LONG).show();
                FlurryAgent.logEvent("Home_Page_Clicked_Install_From_Popup_Menu");
                return true;
            } else if (i == R.id.menu_schedule) {
                return true;
            } else {
                return false;
            }
        }
    }



}
