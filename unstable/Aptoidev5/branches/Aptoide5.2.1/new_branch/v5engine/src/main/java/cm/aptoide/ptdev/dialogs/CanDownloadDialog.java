package cm.aptoide.ptdev.dialogs;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;

import com.flurry.android.FlurryAgent;

import cm.aptoide.ptdev.AppViewActivity;
import cm.aptoide.ptdev.Aptoide;
import cm.aptoide.ptdev.R;
import cm.aptoide.ptdev.database.Database;
import cm.aptoide.ptdev.model.Download;
import cm.aptoide.ptdev.webservices.json.GetApkInfoJson;

/**
 * Created by asantos on 07-08-2014.
 */
public class CanDownloadDialog extends DialogFragment {
    GetApkInfoJson json;
    public CanDownloadDialog(){

    }
    public CanDownloadDialog(GetApkInfoJson json){
        this.json=json;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.callback = (AppViewActivity) activity;
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    AppViewActivity callback;
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        getArguments().getString("appName");
        final View view = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_network_data_usage, null);

        return new AlertDialog.Builder(getActivity())
                        .setView(view)
                        .setTitle(getActivity().getString(R.string.Data_Usage_warning))
//                        .setMessage(getActivity().getString(R.string.Data_Usage_Message))
                        .setPositiveButton(R.string.downloadAnyWay, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (Build.VERSION.SDK_INT >= 10)
                                    FlurryAgent.logEvent("Network_Data_Usage_Can_Download_Anyway");
                                if (json != null) {
//                                    Log.d("CanDownloadDialog", "Json != null");
                                    if( callback!=null ) callback.getService().startDownloadFromJson(json,
                                            getArguments().getLong("downloadId"),
                                            (Download) getArguments().getSerializable("download"));
                                } else {
//                                    Log.d("CanDownloadDialog", "Json == null");
                                    if( callback!=null ) callback.getService().startDownloadFromUrl(
                                            getArguments().getString("url"),
                                            getArguments().getString("md5"),
                                            getArguments().getLong("downloadId"),
                                            (Download) getArguments().getSerializable("download"),
                                            getArguments().getString("repoName"));
                                }
                            }
                        })
//                        .setNeutralButton(R.string.schDwnBtn, new DialogInterface.OnClickListener() {
//                            @Override
//                            public void onClick(DialogInterface dialog, int which) {
//                                if (Build.VERSION.SDK_INT >= 10)
//                                    FlurryAgent.logEvent("CanDownLoadDialog_Schedule");
//                                new Database(Aptoide.getDb()).scheduledDownloadIfMd5(
//                                        getArguments().getString("Package_Name"),
//                                        getArguments().getString("md5"),
//                                        getArguments().getString("Version_Name"),
//                                        getArguments().getString("repoName"),
//                                        getArguments().getString("Name"),
//                                        getArguments().getString("Icon"));
//                            }
//                        })
                        .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (Build.VERSION.SDK_INT >= 10) FlurryAgent.logEvent("Network_Data_Usage_Canceled");
                                boolean isSchedule = ((CheckBox)view.findViewById(R.id.checkbox_schedule)).isChecked();
                                if(isSchedule){
                                    if (Build.VERSION.SDK_INT >= 10) FlurryAgent.logEvent("Network_Data_Usage_Canceled_Scheduled_Download");
                                    new Database(Aptoide.getDb()).scheduledDownloadIfMd5(
                                            getArguments().getString("Package_Name"),
                                            getArguments().getString("md5"),
                                            getArguments().getString("Version_Name"),
                                            getArguments().getString("repoName"),
                                            getArguments().getString("Name"),
                                            getArguments().getString("Icon"));
                                }
                            }
                        })
                        .create();
    }
}