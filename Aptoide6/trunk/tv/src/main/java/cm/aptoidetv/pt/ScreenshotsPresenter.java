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
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.v17.leanback.widget.Presenter;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import cm.aptoidetv.pt.Model.MediaObject;

/*
 * A CardPresenter is used to generate Views and bind Objects to them on demand. 
 * It contains an Image CardView
 */
public class ScreenshotsPresenter extends Presenter {
    private static final String TAG = "CardPresenter";

    private static Context mContext;
    private static int CARD_WIDTH = 313;
    private static int CARD_HEIGHT = 176;

    static class ViewHolder extends Presenter.ViewHolder {
        private ImageView mCardView;
        private Drawable mDefaultCardImage;
        private PicassoImageViewTarget mImageViewTarget;

        public ViewHolder(View view) {
            super(view);
            mCardView = (ImageView) view;
            mImageViewTarget = new PicassoImageViewTarget(mCardView);
            mDefaultCardImage = mContext.getResources().getDrawable(R.drawable.icon_non_available);
        }

        public ImageView getCardView() {
            return mCardView;
        }

        protected void updateCardViewImage(String uri) {
            Picasso.with(mContext)
                    .load(uri)
                    .placeholder(android.R.drawable.sym_def_app_icon)
                    .centerInside()
                    .resize(mCardView.getResources().getInteger(R.integer.card_presenter_width), mCardView.getResources().getInteger(R.integer.card_presenter_height))
                    .error(mDefaultCardImage)
                    .into(mImageViewTarget);
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent) {
        Log.d(TAG, "onCreateViewHolder");
        mContext = parent.getContext();

        ImageView cardView = new ImageView(mContext);
        cardView.setFocusable(true);
        cardView.setFocusableInTouchMode(true);

        TypedArray typedArray = mContext.getTheme().obtainStyledAttributes(ThemePicker.getThemePicker(), new int[]{R.attr.brandColor});
        int brandColorResourceId = typedArray.getResourceId(0, 0);
        typedArray.recycle();

        cardView.setBackgroundColor(mContext.getResources().getColor(brandColorResourceId));
        return new ViewHolder(cardView);
    }

    public void setMainDimensions(ImageView mImageView, int width, int height ){
        ViewGroup.LayoutParams lp = mImageView.getLayoutParams();
        lp.width = width;
        lp.height = height;
        mImageView.setLayoutParams(lp);
    }

    @Override
    public void onBindViewHolder(Presenter.ViewHolder viewHolder, Object item) {
        MediaObject media = (MediaObject) item;

        if (!TextUtils.isEmpty(media.getImageUrl())) {
            int width = ((ViewHolder) viewHolder).mCardView.getContext().getResources().getInteger(R.integer.screenshot_width);
            int height = ((ViewHolder) viewHolder).mCardView.getContext().getResources().getInteger(R.integer.screenshot_height);

            setMainDimensions(((ViewHolder)viewHolder).getCardView(), width, height);
            ((ViewHolder) viewHolder).updateCardViewImage(media.getImageUrl());
        }


    }

    @Override
    public void onUnbindViewHolder(Presenter.ViewHolder viewHolder) {
        Log.d(TAG, "onUnbindViewHolder");
    }

    @Override
    public void onViewAttachedToWindow(Presenter.ViewHolder viewHolder) {
        // TO DO
    }

    public static class PicassoImageViewTarget implements Target {
        private ImageView mImageView;

        public PicassoImageViewTarget(ImageView ImageView) {
            mImageView = ImageView;
        }

        @Override
        public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom loadedFrom) {
            Drawable bitmapDrawable = new BitmapDrawable(mContext.getResources(), bitmap);
            mImageView.setImageDrawable(bitmapDrawable);
            fadeIn(mImageView);
        }

        @Override
        public void onBitmapFailed(Drawable drawable) {
            mImageView.setImageDrawable(drawable);
            fadeIn(mImageView);
        }

        @Override
        public void onPrepareLoad(Drawable drawable) {
            // Do nothing, default_background manager has its own transitions
        }

        private void fadeIn(View v) {
            v.setAlpha(0f);
            v.animate().alpha(1f).setDuration(v.getContext().getResources().getInteger(
                    android.R.integer.config_shortAnimTime)).start();
        }
    }
}
