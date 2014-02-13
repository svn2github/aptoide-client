package cm.aptoide.ptdev.model;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import cm.aptoide.ptdev.*;
import cm.aptoide.ptdev.adapters.PrincipalLayoutAdapter;
import cm.aptoide.ptdev.fragments.HomeItem;
import cm.aptoide.ptdev.utils.IconSizes;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;

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


        return v;
    }

    @Override
    public String getType() {

        return appsList.size()+"";

    }

    public static class ViewHolder{
        View more;
        View separatorLayout;
        LinearLayout collectionList;
        TextView collectionName;
    }

    ViewHolder holder;

    @Override
    public void fillViewWithData(final Context context, View v) {
        int counter = 0;


        if(holder == null){
            holder = new ViewHolder();
            holder.more = v.findViewById(R.id.more);
            holder.separatorLayout = v.findViewById(R.id.separatorLayout);
            holder.collectionList = (LinearLayout) v.findViewById(R.id.collectionList);
            holder.collectionName = (TextView) v.findViewById(R.id.collectionName);
        }



        LinearLayout containerLinearLayout = holder.collectionList;
        holder.more.setVisibility(View.VISIBLE);
        holder.separatorLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Aptoide.getContext(), MoreEditorsChoiceActitivy.class);
                Log.d("Aptoide-HomeLayout", String.valueOf(getParentId()));
                i.putExtra("parentId", getParentId());
                context.startActivity(i);
            }
        });

        holder.separatorLayout.setClickable(true);

        if(appsList.isEmpty()) return;
        String name = appsList.get(counter).getName();
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
                        Intent i = new Intent(context, AppViewActivity.class);
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
                ImageLoader.getInstance().displayImage(icon, iconIv);
                counter++;
            }
        }


    }


}
