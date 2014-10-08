package cm.aptoide.ptdev.adapters;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.flurry.android.FlurryAgent;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map.Entry;
import java.util.Set;

import cm.aptoide.ptdev.R;
import cm.aptoide.ptdev.Start;
import cm.aptoide.ptdev.dialogs.CanUpdateDialog;
import cm.aptoide.ptdev.utils.AptoideUtils;


public class UpdatesSectionAdapter extends BaseAdapter {

    // Constants
    private static final int VIEW_TYPE_SECTION_HEADER = 0;

    // Attributes
    private Context mContext;
    private BaseAdapter mListAdapter;

    private Sectionizer<UpdateItem> mSectionizer = new Sectionizer<UpdateItem>() {
        @Override
        public String getSectionTitleForItem(UpdateItem instance) {
            return instance.isUpdate()?"1":"0";
        }
    };
    private LinkedHashMap<String, Integer> mSections;

    public interface Sectionizer<T> {

        /**
         * Returns the title for the given instance from the data source.
         *
         * @param instance The instance obtained from the data source of the decorated list adapter.
         * @return section title for the given instance.
         */
        String getSectionTitleForItem(T instance);
    }

    public UpdatesSectionAdapter(Context context, BaseAdapter listAdapter) {
        if(context == null) {
            throw new IllegalArgumentException("context cannot be null.");
        } else if(listAdapter == null) {
            throw new IllegalArgumentException("listAdapter cannot be null.");
        }

        this.mContext = context;
        this.mListAdapter = listAdapter;

        this.mSections = new LinkedHashMap<String, Integer>();

        // Find sections
        findSections();
    }

    @Override
    public int getCount() {
        return mListAdapter.getCount() + getSectionCount();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;

        SectionHolder sectionHolder = null;

        switch (getItemViewType(position)) {
            case VIEW_TYPE_SECTION_HEADER:
                if(view == null) {
                    view = View.inflate(mContext, R.layout.separator_updates, null);
                    sectionHolder = new SectionHolder();
                    sectionHolder.titleTextView = (TextView) view.findViewById(R.id.separator_label);
                    sectionHolder.more = (TextView) view.findViewById(R.id.more);
                    view.setTag(sectionHolder);
                } else {
                    sectionHolder = (SectionHolder) view.getTag();
                }
                break;

            default:
                view = mListAdapter.getView(getIndexForPosition(position),
                        convertView, parent);
                break;
        }

        if(sectionHolder != null) {

            String sectionName = sectionTitleForPosition(position);

            String sectionLabel = "";
            switch (Integer.parseInt(sectionName)){
                case 0:
                    sectionLabel = mContext.getString(R.string.installed_tab);
                    sectionHolder.more.findViewById(R.id.more).setVisibility(View.GONE);
                    break;
                case 1:
                    sectionLabel = mContext.getString(R.string.updates_tab);
                    sectionHolder.more.findViewById(R.id.more).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if(Build.VERSION.SDK_INT >= 10) FlurryAgent.logEvent("Updates_Page_Clicked_On_Update_All");
                            ArrayList<Long> ids = ((UpdatesAdapter)mListAdapter).getUpdateIds();
                            if(AptoideUtils.NetworkUtils.isGeneral_DownloadPermitted(mContext)){
                                ((Start)mContext).updateAll(ids);
                            }
                            else {
                                CanUpdateDialog dialog = new CanUpdateDialog();
                                Bundle bundle = new Bundle();
                                bundle.putLongArray(CanUpdateDialog.ARGKEYIDS, AptoideUtils.LongListtolongArray(ids));
                                dialog.setArguments(bundle);
                                dialog.show(((Start) mContext).getSupportFragmentManager(), null);
                            }
                        }
                    });
                    sectionHolder.more.findViewById(R.id.more).setVisibility(View.VISIBLE);
                    break;
                case 2:
                    break;
            }

            sectionHolder.titleTextView.setText(sectionLabel);
        }

        return view;
    }

    @Override
    public boolean areAllItemsEnabled() {
        return mListAdapter.areAllItemsEnabled() ?
                mSections.size() == 0 : false;
    }

    @Override
    public int getItemViewType(int position) {
        int positionInCustomAdapter = getIndexForPosition(position);
        return mSections.values().contains(position) ?
                VIEW_TYPE_SECTION_HEADER :
                mListAdapter.getItemViewType(positionInCustomAdapter) + 1;
    }

    @Override
    public int getViewTypeCount() {
        return mListAdapter.getViewTypeCount() + 1;
    }

    @Override
    public boolean isEnabled(int position) {
        return mSections.values().contains(position) ?
                false : mListAdapter.isEnabled(getIndexForPosition(position));
    }

    @Override
    public Object getItem(int position) {
        return mListAdapter.getItem(getIndexForPosition(position));
    }

    @Override
    public long getItemId(int position) {
        return mListAdapter.getItemId(getIndexForPosition(position));
    }

    @Override
    public void notifyDataSetChanged() {
        mListAdapter.notifyDataSetChanged();
        findSections();
        super.notifyDataSetChanged();
    }

    public int getIndexForPosition(int position) {
        int nSections = 0;

        Set<Entry<String, Integer>> entrySet = mSections.entrySet();
        for(Entry<String, Integer> entry : entrySet) {
            if(entry.getValue() < position) {
                nSections++;
            }
        }

        return position - nSections;
    }

    static class SectionHolder {
        public TextView titleTextView;
        public TextView more;
    }

    private void findSections() {
        int n = mListAdapter.getCount();
        int nSections = 0;
        mSections.clear();

        for(int i=0; i<n; i++) {
            String sectionName = mSectionizer.getSectionTitleForItem((UpdateItem) mListAdapter.getItem(i));

            if(!mSections.containsKey(sectionName)) {
                mSections.put(sectionName, i + nSections);
                nSections ++;
            }

        }
    }

    private int getSectionCount() {
        return mSections.size();
    }

    private String sectionTitleForPosition(int position) {
        String title = null;

        int type = mListAdapter.getItemViewType(getIndexForPosition(position));

        switch (type){
            case 0:
                title = "0";
                break;
            case 1:
                title = "1";
                break;
        }

        return title;
    }

}

