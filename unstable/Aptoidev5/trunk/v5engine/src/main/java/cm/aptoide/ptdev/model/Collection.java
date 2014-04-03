package cm.aptoide.ptdev.model;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.*;
import cm.aptoide.ptdev.*;
import cm.aptoide.ptdev.adapters.PrincipalLayoutAdapter;
import cm.aptoide.ptdev.fragments.HomeItem;
import cm.aptoide.ptdev.utils.IconSizes;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;

import static cm.aptoide.ptdev.utils.AptoideUtils.withSuffix;

/**
 * Created by rmateus on 28-01-2014.
 */
public class Collection extends PrincipalLayoutAdapter.AbstractItem {
    private boolean expanded = false;
    private String name;
    private int marginBottom;
    private ArrayList<HomeItem> appsList;
    private int parentId;
    private boolean hasMore;
    private boolean expanded2;
    private int weeks = -1;
    private Class appViewClass = Aptoide.getConfiguration().getAppViewActivityClass();



    public boolean isExpanded() {
        return expanded;
    }

    public void setExpanded(boolean expanded) {
        this.expanded = expanded;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getMarginBottom() {
        return marginBottom;
    }

    public void setMarginBottom(int marginBottom) {
        this.marginBottom = marginBottom;
    }


    public ArrayList<HomeItem> getAppsList() {
        return appsList;
    }

    public void setAppsList(ArrayList<HomeItem> appsList) {
        this.appsList = appsList;
    }

    public int getParentId() {
        return parentId;
    }

    public void setParentId(int parentId) {
        this.parentId = parentId;
    }

    public void setHasMore(boolean hasMore) {
        this.hasMore = hasMore;
    }

    public boolean isHasMore() {
        return hasMore;
    }

    public boolean isExpanded2() {
        return expanded2;
    }

    public void setExpanded2(boolean expanded2) {
        this.expanded2 = expanded2;
    }

    public void setWeeks(int weeks) {
        this.weeks = weeks;
    }

    public int getWeeks() {
        return weeks;
    }

    @Override
    public View inflateSelf(Context context, int bucketSize) {



        View v = LayoutInflater.from(context).inflate(R.layout.page_collection, null);

        int i = bucketSize;
        LinearLayout containerLinearLayout = (LinearLayout) v.findViewById(R.id.collectionList);

        LinearLayout rowLinearLayout = null;

        for (final HomeItem item : appsList) {
            if(i % bucketSize == 0){
                rowLinearLayout = new LinearLayout(context);
                rowLinearLayout.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT, 1));
                containerLinearLayout.addView(rowLinearLayout);
            }
            FrameLayout tvChild = (FrameLayout) LayoutInflater.from(context).inflate(R.layout.row_app_home, rowLinearLayout, false);
            rowLinearLayout.addView(tvChild);
            i++;

        }

        holder = (ViewHolder) containerLinearLayout.getTag();

        if(holder == null){
            holder = new ViewHolder();
            holder.more = v.findViewById(R.id.more);
//            holder.separatorLayout = v.findViewById(R.id.separatorLayout);
            holder.collectionList = (LinearLayout) v.findViewById(R.id.collectionList);
            holder.collectionName = (TextView) v.findViewById(R.id.collectionName);
            containerLinearLayout.setTag(holder);
        }




        return v;
    }

    @Override
    public String getType() {

        Log.d("Aptoide-HomeLayout", "size: " + appsList.size() );


        return appsList.size()+"";

    }

    public static class ViewHolder{
        View more;
//        View separatorLayout;
        LinearLayout collectionList;
        TextView collectionName;
    }

    ViewHolder holder;

    @Override
    public void fillViewWithData(final Context context, View v) {
        int counter = 0;
        LinearLayout containerLinearLayout = (LinearLayout) v.findViewById(R.id.collectionList);

        if(holder==null){
            holder = (ViewHolder) containerLinearLayout.getTag();
        }
        holder.more.setVisibility(View.VISIBLE);
        holder.more.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Aptoide.getContext(), MoreEditorsChoiceActitivy.class);
                Log.d("Aptoide-HomeLayout", String.valueOf(getParentId()));
                i.putExtra("parentId", getParentId());
                context.startActivity(i);
            }
        });

//        holder.separatorLayout.setClickable(true);

        if(appsList.isEmpty()) return;
        String name = getName();
        String categoryName;
        try {
            categoryName = Aptoide.getContext().getString(EnumCategories.getCategoryName(getParentId()));
            Log.d("HomeLayoutAdapter-categ", "Category Name: " + categoryName);
        } catch (Exception e) {
            categoryName = name;
            Log.d("HomeLayoutAdapter-categ", "Untranslated Category Name: " + categoryName);
        }

        holder.collectionName.setText(categoryName);
        final int count1 = containerLinearLayout.getChildCount();

        for (int i = 0; i != count1; i++) {
            LinearLayout linearLayout = (LinearLayout) containerLinearLayout.getChildAt(i);
            final int count = linearLayout.getChildCount();
            for (int j = 0; j != count; j++) {
                final HomeItem item = appsList.get(counter);
                View tvChild = linearLayout.getChildAt(j);
                TextView nameTv = (TextView) tvChild.findViewById(R.id.app_name);
                nameTv.setText(item.getName());
                ImageView iconIv = (ImageView) tvChild.findViewById(R.id.app_icon);

                tvChild.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent i = new Intent(context, appViewClass);
                        long id = item.getId();
                        i.putExtra("id", id);
                        context.startActivity(i);
                    }
                });
                String icon = item.getIcon();
                if (icon.contains("_icon")) {
                    String[] splittedUrl = icon.split("\\.(?=[^\\.]+$)");
                    icon = splittedUrl[0] + "_" + IconSizes.generateSizeString(Aptoide.getContext()) + "." + splittedUrl[1];
                }

                String category;
                try {
                    int cat = Integer.parseInt(item.getCategory());
                    category = context.getString(EnumCategories.getCategoryName(cat));
                    Log.d("Home-categ", "Category Name: " + category);
                }catch (Exception e){
                    category = item.getCategory();
                    Log.d("Home-categ", "Untranslated Category Name: " + category);
                }

                if(getParentId()!=-1){
                    TextView downloadsTv = (TextView) tvChild.findViewById(R.id.app_downloads);
                    downloadsTv.setText(context.getString(R.string.X_download_number, withSuffix(item.getDownloads())));
                    TextView categoryTv = (TextView) tvChild.findViewById(R.id.app_category);
                    categoryTv.setText("");
                    if(item.getName().length()>10){
                        downloadsTv.setMaxLines(1);
                    }else{
                        downloadsTv.setMaxLines(2);
                    }
                    downloadsTv.setVisibility(View.VISIBLE);
                }else{
                    TextView downloadsTv = (TextView) tvChild.findViewById(R.id.app_downloads);
                    downloadsTv.setText("");
                    TextView categoryTv = (TextView) tvChild.findViewById(R.id.app_category);
                    categoryTv.setText(category);
                    if(item.getName().length()>10){
                        categoryTv.setMaxLines(1);
                    }else{
                        categoryTv.setMaxLines(2);
                    }
                    categoryTv.setVisibility(View.VISIBLE);
                }

                ImageView overflow = (ImageView) tvChild.findViewById(R.id.ic_action);;
                overflow.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showPopup(context, v, item.getId());
                    }
                });
                RatingBar rating = (RatingBar) tvChild.findViewById(R.id.app_rating);
                Log.d("Aptoide-Rating", String.valueOf(item.getRating()));
                rating.setRating(item.getRating());
                rating.setOnRatingBarChangeListener(null);
                rating.setVisibility(View.VISIBLE);
                ImageLoader.getInstance().displayImage(icon, iconIv);
                counter++;
            }
        }


    }

    public void showPopup(Context context, View v, long id) {
        android.support.v7.widget.PopupMenu popup = new android.support.v7.widget.PopupMenu(context, v);
        popup.setOnMenuItemClickListener(new MenuListener(context, id));
        popup.inflate(R.menu.menu_actions);
        popup.show();
    }

    static class MenuListener implements android.support.v7.widget.PopupMenu.OnMenuItemClickListener{

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
                return true;
            } else if (i == R.id.menu_schedule) {
                return true;
            } else {
                return false;
            }
        }
    }


}
