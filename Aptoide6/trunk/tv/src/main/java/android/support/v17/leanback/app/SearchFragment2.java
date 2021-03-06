package android.support.v17.leanback.app;

import android.app.Fragment;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.support.v17.leanback.widget.ObjectAdapter;
import android.support.v17.leanback.widget.OnItemClickedListener;
import android.support.v17.leanback.widget.OnItemSelectedListener;
import android.support.v17.leanback.widget.OnItemViewClickedListener;
import android.support.v17.leanback.widget.OnItemViewSelectedListener;
import android.support.v17.leanback.widget.Presenter;
import android.support.v17.leanback.widget.Row;
import android.support.v17.leanback.widget.RowPresenter;
import android.support.v17.leanback.widget.SearchBar;
import android.support.v17.leanback.widget.VerticalGridView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import java.util.ArrayList;
import java.util.List;

import cm.aptoidetv.pt.R;

/**
 * Created by asantos on 12-12-2014.
 */
public class SearchFragment2 extends Fragment {
    private static final String TAG = SearchFragment.class.getSimpleName();
    private static final boolean DEBUG = false;

    private static final String ARG_PREFIX = SearchFragment.class.getCanonicalName();
    private static final String ARG_QUERY =  ARG_PREFIX + ".query";
    private static final String ARG_TITLE = ARG_PREFIX  + ".title";

    private static final long SPEECH_RECOGNITION_DELAY_MS = 300;

    /**
     * Search API to be provided by the application.
     */
    public static interface SearchResultProvider {
        /**
         * <p>Method invoked some time prior to the first call to onQueryTextChange to retrieve
         * an ObjectAdapter that will contain the results to future updates of the search query.</p>
         *
         * <p>As results are retrieved, the application should use the data set notification methods
         * on the ObjectAdapter to instruct the SearchFragment to update the results.</p>
         *
         * @return ObjectAdapter The result object adapter.
         */
        public ObjectAdapter getResultsAdapter();

        /**
         * <p>Method invoked when the search query is updated.</p>
         *
         * <p>This is called as soon as the query changes; it is up to the application to add a
         * delay before actually executing the queries if needed.
         *
         * <p>This method might not always be called before onQueryTextSubmit gets called, in
         * particular for voice input.
         *
         * @param newQuery The current search query.
         * @return whether the results changed as a result of the new query.
         */
        public boolean onQueryTextChange(String newQuery);

        /**
         * Method invoked when the search query is submitted, either by dismissing the keyboard,
         * pressing search or next on the keyboard or when voice has detected the end of the query.
         *
         * @param query The query entered.
         * @return whether the results changed as a result of the query.
         */
        public boolean onQueryTextSubmit(String query);
    }

    private final ObjectAdapter.DataObserver mAdapterObserver = new ObjectAdapter.DataObserver() {
        @Override
        public void onChanged() {
            // onChanged() may be called multiple times e.g. the provider add
            // rows to ArrayObjectAdapter one by one.
            mHandler.removeCallbacks(mResultsChangedCallback);
            mHandler.post(mResultsChangedCallback);
        }
    };

    private final Handler mHandler = new Handler();

    private final Runnable mResultsChangedCallback = new Runnable() {
        @Override
        public void run() {
            if (DEBUG) Log.v(TAG, "adapter size " + mResultAdapter.size());
            if (mRowsFragment != null
                    && mRowsFragment.getAdapter() != mResultAdapter) {
                if (!(mRowsFragment.getAdapter() == null && mResultAdapter.size() == 0)) {
                    mRowsFragment.setAdapter(mResultAdapter);
                }
            }
            mStatus |= RESULTS_CHANGED;
            if ((mStatus & QUERY_COMPLETE) != 0) {
                focusOnResults();
            }
            updateSearchBarNextFocusId();
        }
    };

    private final Runnable mSetSearchResultProvider = new Runnable() {
        @Override
        public void run() {
            // Retrieve the result adapter
            ObjectAdapter adapter = mProvider.getResultsAdapter();
            if (adapter != mResultAdapter) {
                boolean firstTime = mResultAdapter == null;
                releaseAdapter();
                mResultAdapter = adapter;
                if (mResultAdapter != null) {
                    mResultAdapter.registerObserver(mAdapterObserver);
                }
                if (null != mRowsFragment) {
                    // delay the first time to avoid setting a empty result adapter
                    // until we got first onChange() from the provider
                    if (!(firstTime && (mResultAdapter == null || mResultAdapter.size() == 0))) {
                        mRowsFragment.setAdapter(mResultAdapter);
                    }
                    executePendingQuery();
                }
                updateSearchBarNextFocusId();
            }
        }
    };

    private RowsFragment mRowsFragment;
    private SearchBar mSearchBar;
    private SearchResultProvider mProvider;
    private String mPendingQuery = null;

    private OnItemSelectedListener mOnItemSelectedListener;
    private OnItemClickedListener mOnItemClickedListener;
    private OnItemViewSelectedListener mOnItemViewSelectedListener;
    private OnItemViewClickedListener mOnItemViewClickedListener;
    private ObjectAdapter mResultAdapter;

    private String mTitle;
    private Drawable mBadgeDrawable;

    private SpeechRecognizer mSpeechRecognizer;

    private final int RESULTS_CHANGED = 0x1;
    private final int QUERY_COMPLETE = 0x2;

    private int mStatus;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(android.support.v17.leanback.R.layout.lb_search_fragment, container, false);
        FrameLayout searchFrame = (FrameLayout) root.findViewById(android.support.v17.leanback.R.id.lb_search_frame);
        mSearchBar = (SearchBar) searchFrame.findViewById(android.support.v17.leanback.R.id.lb_search_bar);
        mSearchBar.setSearchBarListener(new SearchBar.SearchBarListener() {
            @Override
            public void onSearchQueryChange(String query) {
                if (null != mProvider) {
                    retrieveResults(query);
                } else {
                    mPendingQuery = query;
                }
            }

            @Override
            public void onSearchQuerySubmit(String query) {
                queryComplete();
                if (null != mProvider) {
                    mProvider.onQueryTextSubmit(query);
                }
            }

            @Override
            public void onKeyboardDismiss(String query) {
                queryComplete();
            }
        });
        //mSearchBar.setSpeechRecognitionCallback(mSpeechRecognitionCallback);

        readArguments(getArguments());
        if (null != mBadgeDrawable) {
            setBadgeDrawable(mBadgeDrawable);
        }
        if (null != mTitle) {
            setTitle(mTitle);
        }

        // Inject the RowsFragment in the results container
        if (getChildFragmentManager().findFragmentById(android.support.v17.leanback.R.id.lb_results_frame) == null) {
            mRowsFragment = new RowsFragment();
            getChildFragmentManager().beginTransaction()
                    .replace(android.support.v17.leanback.R.id.lb_results_frame, mRowsFragment).commit();
        } else {
            mRowsFragment = (RowsFragment) getChildFragmentManager()
                    .findFragmentById(android.support.v17.leanback.R.id.lb_results_frame);
        }
        mRowsFragment.setOnItemViewSelectedListener(new OnItemViewSelectedListener() {
            @Override
            public void onItemSelected(Presenter.ViewHolder itemViewHolder, Object item,
                                       RowPresenter.ViewHolder rowViewHolder, Row row) {
                int position = mRowsFragment.getVerticalGridView().getSelectedPosition();
                mSearchBar.setVisibility(0 >= position ? View.VISIBLE : View.GONE);
                if (null != mOnItemSelectedListener) {
                    mOnItemSelectedListener.onItemSelected(item, row);
                }
                if (null != mOnItemViewSelectedListener) {
                    mOnItemViewSelectedListener.onItemSelected(itemViewHolder, item,
                            rowViewHolder, row);
                }
            }
        });
        mRowsFragment.setOnItemViewClickedListener(new OnItemViewClickedListener() {
            @Override
            public void onItemClicked(Presenter.ViewHolder itemViewHolder, Object item,
                                      RowPresenter.ViewHolder rowViewHolder, Row row) {

                if (null != mOnItemClickedListener) {
                    mOnItemClickedListener.onItemClicked(item, row);
                }
                if (null != mOnItemViewClickedListener) {
                    mOnItemViewClickedListener.onItemClicked(itemViewHolder, item,
                            rowViewHolder, row);
                }
            }
        });
        mRowsFragment.setExpand(true);
        if (null != mProvider) {
            onSetSearchResultProvider();
        }
        if(SpeechRecognizer.isRecognitionAvailable(getActivity())){
            if (savedInstanceState == null) {
                // auto start recognition if this is the first time create fragment
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mSearchBar.startRecognition();
                    }
                }, SPEECH_RECOGNITION_DELAY_MS);
            }
        }else{
            mSearchBar.findViewById(R.id.lb_search_text_editor).requestFocus();
        }
        return root;
    }

    @Override
    public void onStart() {
        super.onStart();
        VerticalGridView list = mRowsFragment.getVerticalGridView();
        int mContainerListAlignTop = getResources().getDimensionPixelSize(android.support.v17.leanback.R.dimen.lb_search_browse_rows_align_top);
        list.setItemAlignmentOffset(0);
        list.setItemAlignmentOffsetPercent(VerticalGridView.ITEM_ALIGN_OFFSET_PERCENT_DISABLED);
        list.setWindowAlignmentOffset(mContainerListAlignTop);
        list.setWindowAlignmentOffsetPercent(VerticalGridView.WINDOW_ALIGN_OFFSET_PERCENT_DISABLED);
        list.setWindowAlignment(VerticalGridView.WINDOW_ALIGN_NO_EDGE);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (null == mSpeechRecognizer && SpeechRecognizer.isRecognitionAvailable(getActivity())) {
            mSpeechRecognizer = SpeechRecognizer.createSpeechRecognizer(getActivity());
            mSearchBar.setSpeechRecognizer(mSpeechRecognizer);
        }
    }

    @Override
    public void onPause() {
        if (SpeechRecognizer.isRecognitionAvailable(getActivity())){
            releaseRecognizer();
        }
        super.onPause();
    }

    @Override
    public void onDestroy() {
        releaseAdapter();
        super.onDestroy();
    }

    private void releaseRecognizer() {
        if (null != mSpeechRecognizer) {
            mSearchBar.setSpeechRecognizer(null);
            mSpeechRecognizer.destroy();
            mSpeechRecognizer = null;
        }
    }

    /**
     * Starts speech recognition.  Typical use case is that
     * activity receives onNewIntent() call when user clicks a MIC button.
     * Note that SearchFragment automatically starts speech recognition
     * at first time created, there is no need to call startRecognition()
     * when fragment is created.
     */
    public void startRecognition() {
        mSearchBar.startRecognition();
    }

    /**
     * Set the search provider that is responsible for returning results for the
     * search query.
     */
    public void setSearchResultProvider(SearchResultProvider searchResultProvider) {
        if (mProvider != searchResultProvider) {
            mProvider = searchResultProvider;
            onSetSearchResultProvider();
        }
    }

    /**
     * Sets an item selection listener for the results.
     *
     * @param listener The item selection listener to be invoked when an item in
     *        the search results is selected.
     * @deprecated Use {@link #setOnItemViewSelectedListener(OnItemViewSelectedListener)}
     */
    public void setOnItemSelectedListener(OnItemSelectedListener listener) {
        mOnItemSelectedListener = listener;
    }

    /**
     * Sets an item clicked listener for the results.
     *
     * @param listener The item clicked listener to be invoked when an item in
     *        the search results is clicked.
     * @deprecated Use {@link #setOnItemViewClickedListener(OnItemViewClickedListener)}
     */
    public void setOnItemClickedListener(OnItemClickedListener listener) {
        mOnItemClickedListener = listener;
    }

    /**
     * Sets an item selection listener for the results.
     *
     * @param listener The item selection listener to be invoked when an item in
     *        the search results is selected.
     */
    public void setOnItemViewSelectedListener(OnItemViewSelectedListener listener) {
        mOnItemViewSelectedListener = listener;
    }

    /**
     * Sets an item clicked listener for the results.
     *
     * @param listener The item clicked listener to be invoked when an item in
     *        the search results is clicked.
     */
    public void setOnItemViewClickedListener(OnItemViewClickedListener listener) {
        mOnItemViewClickedListener = listener;
    }

    /**
     * Sets the title string to be be shown in an empty search bar. The title
     * may be placed in a call-to-action, such as "Search <i>title</i>" or
     * "Speak to search <i>title</i>".
     */
    public void setTitle(String title) {
        mTitle = title;
        if (null != mSearchBar) {
            mSearchBar.setTitle(title);
        }
    }

    /**
     * Returns the title set in the search bar.
     */
    public String getTitle() {
        if (null != mSearchBar) {
            return mSearchBar.getTitle();
        }
        return null;
    }

    /**
     * Sets the badge drawable that will be shown inside the search bar next to
     * the title.
     */
    public void setBadgeDrawable(Drawable drawable) {
        mBadgeDrawable = drawable;
        if (null != mSearchBar) {
            mSearchBar.setBadgeDrawable(drawable);
        }
    }

    /**
     * Returns the badge drawable in the search bar.
     */
    public Drawable getBadgeDrawable() {
        if (null != mSearchBar) {
            return mSearchBar.getBadgeDrawable();
        }
        return null;
    }

    /**
     * Display the completions shown by the IME. An application may provide
     * a list of query completions that the system will show in the IME.
     *
     * @param completions A list of completions to show in the IME. Setting to
     *        null or empty will clear the list.
     */
    public void displayCompletions(List<String> completions) {
        mSearchBar.displayCompletions(completions);
    }

    /**
     * Set this callback to have the fragment pass speech recognition requests
     * to the activity rather than using an internal recognizer.
     */
/*    public void setSpeechRecognitionCallback(SpeechRecognitionCallback callback) {
        mSpeechRecognitionCallback = callback;
        if (mSearchBar != null) {
            mSearchBar.setSpeechRecognitionCallback(mSpeechRecognitionCallback);
        }
        if (callback != null) {
            releaseRecognizer();
        }
    }*/

    /**
     * Sets the text of the search query and optionally submits the query. Either
     * {@link SearchResultProvider#onQueryTextChange onQueryTextChange} or
     * {@link SearchResultProvider#onQueryTextSubmit onQueryTextSubmit} will be
     * called on the provider if it is set.
     *
     * @param query The search query to set.
     * @param submit Whether to submit the query.
     */
    public void setSearchQuery(String query, boolean submit) {
        // setSearchQuery will call onQueryTextChange
        mSearchBar.setSearchQuery(query);
        if (submit) {
            mProvider.onQueryTextSubmit(query);
        }
    }

    /**
     * Sets the text of the search query based on the {@link android.speech.RecognizerIntent#EXTRA_RESULTS} in
     * the given intent, and optionally submit the query.  If more than one result is present
     * in the results list, the first will be used.
     *
     * @param intent Intent received from a speech recognition service.
     * @param submit Whether to submit the query.
     */
    public void setSearchQuery(Intent intent, boolean submit) {
        ArrayList<String> matches = intent.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
        if (matches != null && matches.size() > 0) {
            setSearchQuery(matches.get(0), submit);
        }
    }

    /**
     * Returns an intent that can be used to request speech recognition.
     * Built from the base {@link RecognizerIntent#ACTION_RECOGNIZE_SPEECH} plus
     * extras:
     *
     * <ul>
     * <li>{@link RecognizerIntent#EXTRA_LANGUAGE_MODEL} set to
     * {@link RecognizerIntent#LANGUAGE_MODEL_FREE_FORM}</li>
     * <li>{@link RecognizerIntent#EXTRA_PARTIAL_RESULTS} set to true</li>
     * <li>{@link RecognizerIntent#EXTRA_PROMPT} set to the search bar hint text</li>
     * </ul>
     *
     * For handling the intent returned from the service, see
     * {@link #setSearchQuery(Intent, boolean)}.
     */
    public Intent getRecognizerIntent() {
        Intent recognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true);
        if (mSearchBar != null && mSearchBar.getHint() != null) {
            recognizerIntent.putExtra(RecognizerIntent.EXTRA_PROMPT, mSearchBar.getHint());
        }
        return recognizerIntent;
    }

    private void retrieveResults(String searchQuery) {
        if (DEBUG) Log.v(TAG, String.format("retrieveResults %s", searchQuery));
        mProvider.onQueryTextChange(searchQuery);
        mStatus &= ~QUERY_COMPLETE;
    }

    private void queryComplete() {
        mStatus |= QUERY_COMPLETE;
        focusOnResults();
    }

    private void updateSearchBarNextFocusId() {
        if (mSearchBar == null || mResultAdapter == null) {
            return;
        }
        final int viewId = (mResultAdapter.size() == 0 || mRowsFragment == null ||
                mRowsFragment.getVerticalGridView() == null) ? 0 :
                mRowsFragment.getVerticalGridView().getId();
        mSearchBar.setNextFocusDownId(viewId);
    }

    private void focusOnResults() {
        if (mRowsFragment == null ||
                mRowsFragment.getVerticalGridView() == null ||
                mResultAdapter.size() == 0) {
            return;
        }
        mRowsFragment.setSelectedPosition(0);
        if (mRowsFragment.getVerticalGridView().requestFocus()) {
            mStatus &= ~RESULTS_CHANGED;
        }
    }

    private void onSetSearchResultProvider() {
        mHandler.removeCallbacks(mSetSearchResultProvider);
        mHandler.post(mSetSearchResultProvider);
    }

    private void releaseAdapter() {
        if (mResultAdapter != null) {
            mResultAdapter.unregisterObserver(mAdapterObserver);
            mResultAdapter = null;
        }
    }

    private void executePendingQuery() {
        if (null != mPendingQuery && null != mResultAdapter) {
            String query = mPendingQuery;
            mPendingQuery = null;
            retrieveResults(query);
        }
    }

    private void readArguments(Bundle args) {
        if (null == args) {
            return;
        }
        if (args.containsKey(ARG_QUERY)) {
            setSearchQuery(args.getString(ARG_QUERY));
        }

        if (args.containsKey(ARG_TITLE)) {
            setTitle(args.getString(ARG_TITLE));
        }
    }

    private void setSearchQuery(String query) {
        mSearchBar.setSearchQuery(query);
    }
}
