package cm.aptoide.ptdev.adapters;

import android.content.Context;
import android.database.Cursor;
import android.database.DataSetObserver;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import android.widget.AdapterView.OnItemClickListener;
import cm.aptoide.ptdev.Start;
import cm.aptoide.ptdev.R;
import cm.aptoide.ptdev.Start;


import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;


/**
 * Adapter for sections.
 */
public class UpdatesSectionListAdapter extends BaseAdapter implements ListAdapter,
        OnItemClickListener {
    private final DataSetObserver dataSetObserver = new DataSetObserver() {

        @Override
        public void onChanged() {
            super.onChanged();
            updateSessionCache();
        }

        @Override
        public void onInvalidated() {
            super.onInvalidated();
            updateSessionCache();
        };

    };

    private Context context;
    private final ListAdapter linkedAdapter;
    private final Map<Integer, String> sectionPositions = new LinkedHashMap<Integer, String>();
    private final Map<Integer, Integer> itemPositions = new LinkedHashMap<Integer, Integer>();
    private final Map<View, String> currentViewSections = new HashMap<View, String>();
    private int viewTypeCount;
    protected final LayoutInflater inflater;

    private View transparentSectionView;

    private OnItemClickListener linkedListener;

    public UpdatesSectionListAdapter(Context context, final LayoutInflater inflater,
                                     final ListAdapter linkedAdapter) {
        this.context = context;
        this.linkedAdapter = linkedAdapter;
        this.inflater = inflater;
        linkedAdapter.registerDataSetObserver(dataSetObserver);
        updateSessionCache();
    }

    private boolean isTheSame(final String previousSection,
            final String newSection) {
        if (previousSection == null) {
            return newSection == null;
        } else {
            return previousSection.equals(newSection);
        }
    }

    private synchronized void updateSessionCache() {
        Log.d("Aptoide-SectionAdapter", "Updating session cache");
        int currentPosition = 0;
        sectionPositions.clear();
        itemPositions.clear();
        viewTypeCount = linkedAdapter.getViewTypeCount() + 1;
        String currentSection = null;
        final int count = linkedAdapter.getCount();

        for (int i = 0; i < count; i++) {

            final Cursor item = (Cursor) linkedAdapter.getItem(i);

            if (!isTheSame(currentSection, item.getString(item.getColumnIndex("is_update")))) {
                sectionPositions.put(currentPosition, item.getString(item.getColumnIndex("is_update")));
                currentSection = item.getString(item.getColumnIndex("is_update"));
                currentPosition++;
            }

            itemPositions.put(currentPosition, i);
            currentPosition++;
        }
    }

    @Override
    public synchronized int getCount() {
        int size = sectionPositions.size() + linkedAdapter.getCount();
            Log.d("Aptoide-getCount", String.valueOf(size));
        return size;
    }

    @Override
    public synchronized Object getItem(final int position) {
        if (isSection(position)) {
            return sectionPositions.get(position);
        } else {
            final int linkedItemPosition = getLinkedPosition(position);
            return linkedAdapter.getItem(linkedItemPosition);
        }
    }

    public synchronized boolean isSection(final int position) {
        return sectionPositions.containsKey(position);
    }

    public synchronized String getSectionName(final int position) {
        if (isSection(position)) {
            return sectionPositions.get(position);
        } else {
            return null;
        }
    }

    @Override
    public long getItemId(final int position) {
        if (isSection(position)) {
            return sectionPositions.get(position).hashCode();
        } else {
            return linkedAdapter.getItemId(getLinkedPosition(position));
        }
    }

    protected Integer getLinkedPosition(final int position) {
        if(itemPositions.get(position) == null) {
            return 0;
        }
        return itemPositions.get(position);
    }

    @Override
    public int getItemViewType(final int position) {
        if (isSection(position)) {
            return viewTypeCount - 1;
        }
        return linkedAdapter.getItemViewType(getLinkedPosition(position));
    }

    private View getSectionView(final View convertView, final String section) {
        View theView = convertView;
        if (theView == null) {
            theView = createNewSectionView();
        }
        setSectionText(section, theView);
        replaceSectionViewsInMaps(section, theView);
        return theView;
    }

    protected void setSectionText(final String section, final View sectionView) {
        final TextView textView = (TextView) sectionView
                .findViewById(R.id.separator_label);
        String sectionLabel = "";
        switch (Integer.parseInt(section)){
            case 0:
                sectionLabel = context.getString(R.string.installed_tab);
                sectionView.findViewById(R.id.more).setVisibility(View.GONE);
                break;
            case 1:
                sectionLabel = context.getString(R.string.updates_tab);
                sectionView.findViewById(R.id.more).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ((Start)context).updateAll(((UpdatesAdapter)linkedAdapter).getUpdateIds());
                        Toast.makeText(context, context.getString(R.string.starting_download), Toast.LENGTH_LONG).show();
                    }
                });
                sectionView.findViewById(R.id.more).setVisibility(View.VISIBLE);

                break;
            case 2:
                break;
        }
        textView.setText(sectionLabel);
    }

    protected synchronized void replaceSectionViewsInMaps(final String section,
            final View theView) {
        if (currentViewSections.containsKey(theView)) {
            currentViewSections.remove(theView);
        }
        currentViewSections.put(theView, section);
    }

    protected View createNewSectionView() {
        return inflater.inflate(R.layout.separator_updates, null);
    }

    @Override
    public View getView(final int position, final View convertView,
            final ViewGroup parent) {
        if (isSection(position)) {
            return getSectionView(convertView, sectionPositions.get(position));
        }
        return linkedAdapter.getView(getLinkedPosition(position), convertView,
                parent);
    }

    @Override
    public int getViewTypeCount() {
        return viewTypeCount;
    }

    @Override
    public boolean hasStableIds() {
        return linkedAdapter.hasStableIds();
    }

    @Override
    public boolean isEmpty() {
        return linkedAdapter.isEmpty();
    }

    @Override
    public void registerDataSetObserver(final DataSetObserver observer) {
        linkedAdapter.registerDataSetObserver(observer);
    }

    @Override
    public void unregisterDataSetObserver(final DataSetObserver observer) {
        linkedAdapter.unregisterDataSetObserver(observer);
    }

    @Override
    public boolean areAllItemsEnabled() {
        return linkedAdapter.areAllItemsEnabled();
    }

    @Override
    public boolean isEnabled(final int position) {
        if (isSection(position)) {
            return false;
        }
        return linkedAdapter.isEnabled(getLinkedPosition(position));
    }

    public void makeSectionInvisibleIfFirstInList(final int firstVisibleItem) {
        final String section = getSectionName(firstVisibleItem);
        // only make invisible the first section with that name in case there
        // are more with the same name
        boolean alreadySetFirstSectionIvisible = false;
        for (final Entry<View, String> itemView : currentViewSections
                .entrySet()) {
            if (itemView.getValue().equals(section)
                    && !alreadySetFirstSectionIvisible) {
                itemView.getKey().setVisibility(View.INVISIBLE);
                alreadySetFirstSectionIvisible = true;
            } else {
                itemView.getKey().setVisibility(View.VISIBLE);
            }
        }
        for (final Entry<Integer, String> entry : sectionPositions.entrySet()) {
            if (entry.getKey() > firstVisibleItem + 1) {
                break;
            }
            setSectionText(entry.getValue(), getTransparentSectionView());
        }
    }

    public synchronized View getTransparentSectionView() {
        if (transparentSectionView == null) {
            transparentSectionView = createNewSectionView();
        }
        return transparentSectionView;
    }

    protected void sectionClicked(final String section) {
        // do nothing
    }

    @Override
    public void onItemClick(final AdapterView< ? > parent, final View view,
            final int position, final long id) {
        if (isSection(position)) {
            sectionClicked(getSectionName(position));
        } else if (linkedListener != null) {
            linkedListener.onItemClick(parent, view,
                    getLinkedPosition(position), id);
        }
    }

    public void setOnItemClickListener(final OnItemClickListener linkedListener) {
        this.linkedListener = linkedListener;
    }
}
