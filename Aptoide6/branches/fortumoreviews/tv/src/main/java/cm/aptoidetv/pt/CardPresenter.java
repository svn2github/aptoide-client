package cm.aptoidetv.pt;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.v17.leanback.widget.ImageCardView;
import android.support.v17.leanback.widget.Presenter;
import android.text.Html;
import android.view.View;
import android.view.ViewGroup;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import cm.aptoidetv.pt.Model.BindInterface;

/*
 * A CardPresenter is used to generate Views and bind Objects to them on demand. 
 * It contains an Image CardView
 */
public class CardPresenter extends Presenter {
    private static Context mContext;
    public static final int card_presenter_width = Utils.convertDpToPixel(528,AppTV.getContext());
    public static final int card_presenter_height = Utils.convertDpToPixel(258,AppTV.getContext());
    public static final int ICON_WIDTH = Utils.convertDpToPixel(192,AppTV.getContext());
    public static final int ICON_HEIGHT = Utils.convertDpToPixel(192,AppTV.getContext());
/*  public static final int card_presenter_width = 320;
    public static final int card_presenter_height = 180;
    private static final int CARD_WIDTH = 313;
    private static final int CARD_HEIGHT = 176;
    private static final int ICON_WIDTH = 144;
    private static final int ICON_HEIGHT = 144;*/
    private static final int[] attrs =  new int[]{R.attr.brandColor};
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent) {
        mContext = parent.getContext();

        ImageCardView cardView = new ImageCardView(mContext);
        cardView.setFocusable(true);
        cardView.setFocusableInTouchMode(true);

        TypedArray typedArray = mContext.getTheme().obtainStyledAttributes(ThemePicker.getThemePicker(), attrs);
        typedArray.recycle();

        return new ViewHolder(cardView);
    }

    @Override
    public void onBindViewHolder(Presenter.ViewHolder viewHolder, Object item) {
        ViewHolder myViewHolder = (ViewHolder) viewHolder;
        BindInterface bi = (BindInterface) item;
        myViewHolder.mCardView.setContentText(bi.getText(mContext));
        myViewHolder.mCardView.setTitleText(Html.fromHtml(bi.getName(mContext)));
        myViewHolder.mCardView.setMainImageDimensions(bi.getWidth(),bi.getHeight());
        //myViewHolder.updateCardViewImage(bi.getImage());
        bi.setImage(ICON_WIDTH,ICON_HEIGHT,myViewHolder.getPicassoImageCardViewTarget());
    }

    @Override
    public void onUnbindViewHolder(Presenter.ViewHolder viewHolder) {
        ViewHolder vh = (ViewHolder) viewHolder;
        // Remove references to images so that the garbage collector can free up memory
        vh.mCardView.setBadgeImage(null);
        vh.mCardView.setMainImage(null);
    }

    static class ViewHolder extends Presenter.ViewHolder {
        private ImageCardView mCardView;
        //private Drawable mDefaultCardImage;
        private PicassoImageCardViewTarget mImageCardViewTarget;

        public ViewHolder(View view) {
            super(view);
            mCardView = (ImageCardView) view;
            mImageCardViewTarget = new PicassoImageCardViewTarget(mCardView);

            //mDefaultCardImage = mContext.getResources().getDrawable(R.drawable.movie);
        }
        protected PicassoImageCardViewTarget getPicassoImageCardViewTarget(){
            return mImageCardViewTarget;
        }
    }


    public static class PicassoImageCardViewTarget implements Target {
        private ImageCardView mImageCardView;

        public PicassoImageCardViewTarget(ImageCardView imageCardView) {
            mImageCardView = imageCardView;
        }

        @Override
        public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom loadedFrom) {
            Drawable bitmapDrawable = new BitmapDrawable(mContext.getResources(), bitmap);
            mImageCardView.setMainImage(bitmapDrawable);
        }

        @Override
        public void onBitmapFailed(Drawable drawable) {
            mImageCardView.setMainImage(drawable);
        }

        @Override
        public void onPrepareLoad(Drawable drawable) {
            // Do nothing, default_background manager has its own transitions
        }
    }
}
