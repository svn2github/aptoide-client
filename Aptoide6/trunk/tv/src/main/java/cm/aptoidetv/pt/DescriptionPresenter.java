/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package cm.aptoidetv.pt;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.v17.leanback.widget.Presenter;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/*
 * A CardPresenter is used to generate Views and bind Objects to them on demand. 
 * It contains an Image CardView
 */
public class DescriptionPresenter extends Presenter {
    private static final String TAG = "DescriptionPresenter";

    private static Context mContext;

    static class ViewHolder extends Presenter.ViewHolder {
        private TextView mCardView;

        public ViewHolder(View view) {
            super(view);
            mCardView = (TextView) view;
        }

    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent) {
        Log.d(TAG, "onCreateViewHolder");
        mContext = parent.getContext();

        TextView cardView = new TextView(mContext);
        cardView.setFocusable(true);
        cardView.setFocusableInTouchMode(true);

        TypedArray typedArray = mContext.getTheme().obtainStyledAttributes(ThemePicker.getThemePicker(), new int[]{R.attr.brandColor});
        int brandColorResourceId = typedArray.getResourceId(0, 0);
        typedArray.recycle();

        cardView.setBackgroundColor(mContext.getResources().getColor(brandColorResourceId));
        return new ViewHolder(cardView);
    }

    @Override
    public void onBindViewHolder(Presenter.ViewHolder viewHolder, Object item) {
        String description = (String) item;

//        ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
//        ((ViewHolder) viewHolder).mCardView.setLayoutParams(lp);

        ((ViewHolder) viewHolder).mCardView.setWidth(1280);

        ((ViewHolder) viewHolder).mCardView.setTextSize(20);
        ((ViewHolder) viewHolder).mCardView.setPadding(50, 50, 50, 50);
        ((ViewHolder) viewHolder).mCardView.setText(Html.fromHtml(description));

    }

    @Override
    public void onUnbindViewHolder(Presenter.ViewHolder viewHolder) {
        Log.d(TAG, "onUnbindViewHolder");
    }

    @Override
    public void onViewAttachedToWindow(Presenter.ViewHolder viewHolder) {
        // TO DO
    }



}
