package cm.aptoide.ptdev.widget;

import android.content.Context;
import android.util.AttributeSet;

/**
 * Created by rmateus on 04-12-2014.
 */
public class RecyclerView extends android.support.v7.widget.RecyclerView {
    public RecyclerView(Context context) {
        super(context);
    }

    public RecyclerView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public RecyclerView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public void scrollTo(int x, int y) {
        //fix
    }
}
