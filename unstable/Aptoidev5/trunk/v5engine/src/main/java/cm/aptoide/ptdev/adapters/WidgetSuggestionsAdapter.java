package cm.aptoide.ptdev.adapters;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;
import cm.aptoide.ptdev.AppViewActivity;
import cm.aptoide.ptdev.Aptoide;
import cm.aptoide.ptdev.UninstallRetainFragment;
import cm.aptoide.ptdev.database.schema.Schema;
import cm.aptoide.ptdev.downloadmanager.Utils;
import cm.aptoide.ptdev.model.RollBackItem;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;
import cm.aptoide.ptdev.R;


/**
 * Created by brutus on 06-01-2014.
 */
public class WidgetSuggestionsAdapter extends CursorAdapter {


    public WidgetSuggestionsAdapter(Context context) {
        super(context, null, FLAG_REGISTER_CONTENT_OBSERVER);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View v = LayoutInflater.from(context).inflate(R.layout.row_search_widget_suggestion, parent, false);
        return v;
    }

    @Override
    public void bindView(View view, final Context context, Cursor cursor) {
        WidgetSuggestionHolder holder = (WidgetSuggestionHolder) view.getTag();
        if (holder == null) {
            holder = new WidgetSuggestionHolder();
            holder.searchSuggestion = (TextView) view.findViewById(R.id.search_suggestion);
            view.setTag(holder);
        }

        final String suggestion = cursor.getString(cursor.getColumnIndex(SearchManager.SUGGEST_COLUMN_TEXT_1));

        holder.searchSuggestion.setText(Html.fromHtml(suggestion));
        holder.searchSuggestion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String url = Aptoide.getConfiguration().getUriSearch() + suggestion + "&q=" + Utils.filters(context);

                Intent i = new Intent(Intent.ACTION_VIEW);
                url = url.replaceAll(" ", "%20");
                i.setData(Uri.parse(url));
                context.startActivity(i);

            }
        });
    }

    public static class WidgetSuggestionHolder {
        public TextView searchSuggestion;
    }
}
