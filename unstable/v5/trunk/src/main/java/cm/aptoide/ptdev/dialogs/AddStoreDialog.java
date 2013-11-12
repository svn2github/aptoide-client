package cm.aptoide.ptdev.dialogs;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.EditText;
import cm.aptoide.ptdev.R;
import com.actionbarsherlock.app.SherlockDialogFragment;


/**
 * Created with IntelliJ IDEA.
 * User: rmateus
 * Date: 18-10-2013
 * Time: 17:27
 * To change this template use File | Settings | File Templates.
 */
public class AddStoreDialog extends SherlockDialogFragment {

    private String store;

    private Callback callback;
    public Callback dummyCallback = new Callback() {
        @Override
        public void addStore(String s) {

        }
    };

    public interface Callback{
        public void addStore(String s);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        callback = (Callback) activity;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        callback = dummyCallback;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        if (getDialog() != null) {
            getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        }

        return inflater.inflate(R.layout.dialog_add_store, container, false);
    }

    @Override
    public void onViewCreated(final View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        view.findViewById(R.id.button_dialog_add_store).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callback.addStore(((EditText) view.findViewById(R.id.edit_store_uri)).getText().toString());
                dismiss();
            }
        });
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

    }
}
