package com.aptoide.partners.drawable;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;

import com.aptoide.partners.R;


/**
 * Created by rmateus on 21-01-2015.
 */
public class CheckmarkDrawable extends Drawable {
    private static Bitmap CHECKMARK;
    private static int sBackgroundColor;
    private final Paint mPaint;
    private float mScaleFraction;
    private float mAlphaFraction;
    private static final Matrix sMatrix = new Matrix();
    public CheckmarkDrawable(final Resources res) {
        if (CHECKMARK == null) {
            CHECKMARK = BitmapFactory.decodeResource(res, R.drawable.ic_check_wht_24dp);
            sBackgroundColor = res.getColor(R.color.checkmark_tile_background_color);
        }
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setFilterBitmap(true);

        mPaint.setColor(sBackgroundColor);
    }
    @Override
    public void draw(final Canvas canvas) {
        final Rect bounds = getBounds();
        if (!isVisible() || bounds.isEmpty()) {
            return;
        }
        canvas.drawCircle(bounds.centerX(), bounds.centerY(), bounds.width() / 2, mPaint);
        // Scale the checkmark.
        sMatrix.reset();
        sMatrix.setScale(mScaleFraction, mScaleFraction, CHECKMARK.getHeight() / 2,
                CHECKMARK.getHeight() / 2);
        sMatrix.postTranslate(bounds.centerX() - CHECKMARK.getWidth() / 2,
                bounds.centerY() - CHECKMARK.getHeight() / 2);
        // Fade the checkmark.
        final int oldAlpha = mPaint.getAlpha();
        // Interpolate the alpha.
        mPaint.setAlpha((int) (oldAlpha * mAlphaFraction));

        canvas.drawBitmap(CHECKMARK, sMatrix, mPaint);
        // Restore the alpha.
        mPaint.setAlpha(oldAlpha);
    }
    @Override
    public void setAlpha(final int alpha) {
        mPaint.setAlpha(alpha);
    }
    @Override
    public void setColorFilter(final ColorFilter cf) {
        mPaint.setColorFilter(cf);
    }
    @Override
    public int getOpacity() {
// Always a gray background.
        return PixelFormat.OPAQUE;
    }
    /**
     * Set value as a fraction from 0f to 1f.
     */
    public void setScaleAnimatorValue(final float value) {
        final float old = mScaleFraction;
        mScaleFraction = value;
        if (old != mScaleFraction) {
            invalidateSelf();
        }
    }
    /**
     * Set value as a fraction from 0f to 1f.
     */
    public void setAlphaAnimatorValue(final float value) {
        final float old = mAlphaFraction;
        mAlphaFraction = value;
        if (old != mAlphaFraction) {
            invalidateSelf();
        }
    }
}