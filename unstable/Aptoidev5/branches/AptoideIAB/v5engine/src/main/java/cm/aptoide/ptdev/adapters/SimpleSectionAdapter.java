package cm.aptoide.ptdev.adapters;

/**
 * Created by rmateus on 17-03-2014.
 */
/*
 * Copyright (C) 2012 Mobs and Geeks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */



        import android.content.Context;
        import android.util.Log;
        import android.view.View;
        import android.view.ViewGroup;
        import android.widget.*;
        import cm.aptoide.ptdev.R;
        import cm.aptoide.ptdev.Start;

        import java.util.LinkedHashMap;
        import java.util.Map.Entry;
        import java.util.Set;

/**
 * A very simple adapter that adds sections to adapters written for {@link ListView}s.
 * <br />
 * <b>NOTE: The adapter assumes that the data source of the decorated list adapter is sorted.</b>
 *
 * @author Ragunath Jawahar R <rj@mobsandgeeks.com>
 * @version 0.2
 */
public class SimpleSectionAdapter<T> extends BaseAdapter {

    // Constants
    private static final int VIEW_TYPE_SECTION_HEADER = 0;

    // Attributes
    private Context mContext;
    private BaseAdapter mListAdapter;
    private int mSectionHeaderLayoutId;
    private int mSectionTitleTextViewId;
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

    /**
     * Constructs a {@linkplain SimpleSectionAdapter}.
     *
     * @param context The context for this adapter.
     * @param listAdapter A {@link ListAdapter} that has to be sectioned.
     */
    public SimpleSectionAdapter(Context context, BaseAdapter listAdapter) {
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

    private boolean isTextView(Context context, int layoutId, int textViewId) {
        View inflatedView = View.inflate(context, layoutId, null);
        View foundView = inflatedView.findViewById(textViewId);

        return foundView instanceof TextView;
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
                            ((Start)mContext).updateAll(((UpdatesAdapter)mListAdapter).getUpdateIds());
                            Toast.makeText(mContext, mContext.getString(R.string.starting_download), Toast.LENGTH_LONG).show();
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

    /**
     * Returns the actual index of the object in the data source linked to the this list item.
     *
     * @param position List item position in the {@link ListView}.
     * @return Index of the item in the wrapped list adapter's data source.
     */
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

