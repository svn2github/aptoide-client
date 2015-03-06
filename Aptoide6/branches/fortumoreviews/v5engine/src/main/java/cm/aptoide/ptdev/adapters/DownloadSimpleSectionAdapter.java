package cm.aptoide.ptdev.adapters;
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
import android.widget.BaseAdapter;
import android.widget.TextView;
import android.widget.Toast;
import cm.aptoide.ptdev.R;
import cm.aptoide.ptdev.Start;
import cm.aptoide.ptdev.downloadmanager.state.EnumState;
import cm.aptoide.ptdev.model.Download;

import java.util.LinkedHashMap;
import java.util.Map.Entry;
import java.util.Set;

/**
 * A very simple adapter that adds sections to adapters written for {@link android.widget.ListView}s.
 * <br />
 * <b>NOTE: The adapter assumes that the data source of the decorated list adapter is sorted.</b>
 *
 * @author Ragunath Jawahar R <rj@mobsandgeeks.com>
 * @version 0.2
 */
public class DownloadSimpleSectionAdapter extends BaseAdapter {
    // Constants
    private static final int VIEW_TYPE_SECTION_HEADER = 0;

    // Attributes
    private Context mContext;
    private BaseAdapter mListAdapter;
    private Sectionizer<Download> mSectionizer = new Sectionizer<Download>() {
        @Override
        public String getSectionTitleForItem(Download instance) {
            return instance.getDownloadState().name();
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
     * Constructs a {@linkplain cm.aptoide.ptdev.adapters.DownloadSimpleSectionAdapter}.
     *
     * @param context The context for this adapter.
     * @param listAdapter A {@link android.widget.ListAdapter} that has to be sectioned.
     */
    public DownloadSimpleSectionAdapter(Context context, BaseAdapter listAdapter) {
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
                    view = View.inflate(mContext, R.layout.separator_textview, null);
                    sectionHolder = new SectionHolder();
                    sectionHolder.titleTextView = (TextView) view;
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
            sectionHolder.titleTextView.setText(sectionName);
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

        return mSections.values().contains(position) ? mSections.get(position) : mListAdapter.getItem(getIndexForPosition(position));
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
     * @param position List item position in the {@link android.widget.ListView}.
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
            String sectionName = mSectionizer.getSectionTitleForItem((Download) mListAdapter.getItem(i));

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

        EnumState downloadState = ((Download) mListAdapter.getItem(getIndexForPosition(position))).getDownloadState();

        switch (downloadState){
            case ACTIVE:
                title = mContext.getString(R.string.download_active);
                break;
            case COMPLETE:
                title = mContext.getString(R.string.download_completed);
                break;
            case PENDING:
                title = mContext.getString(R.string.download_pending);
                break;
            case INACTIVE:
                title = mContext.getString(R.string.download_inactive);
                break;
            case ERROR:
                title = mContext.getString(R.string.download_error);
                break;
        }

        return title;
    }

}

