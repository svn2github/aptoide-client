/**
 * ManagerPreferences,		auxilliary class to Aptoide's ServiceData
 * Copyright (C) 2011  Duarte Silveira
 * duarte.silveira@caixamagica.pt
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

package cm.aptoide.ptdev.preferences;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import cm.aptoide.ptdev.Aptoide;
import cm.aptoide.ptdev.R;
import cm.aptoide.ptdev.model.IconDownloadPermissions;

import java.util.UUID;

/**
 * ManagerPreferences, manages aptoide's preferences I/O
 *
 * @author dsilveira
 * @since 3.0
 */
public class ManagerPreferences {

    private SharedPreferences getPreferences;
    private SharedPreferences.Editor setPreferences;


    public ManagerPreferences(Context context) {
        getPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        setPreferences = getPreferences.edit();

        if (getAptoideClientUUID() == null) {
            //createLauncherShortcut(context);
            setAptoideClientUUID(UUID.randomUUID().toString());
        }

    }


    public void createLauncherShortcut(Context context, int drawable) {

        //removeLauncherShortcut(context);

        Intent shortcutIntent = context.getPackageManager().getLaunchIntentForPackage(context.getPackageName());


        final Intent intent = new Intent();
        intent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcutIntent);
        intent.putExtra(Intent.EXTRA_SHORTCUT_NAME, Aptoide.getConfiguration().getMarketName());

        Parcelable iconResource;

        iconResource = Intent.ShortcutIconResource.fromContext(context, drawable);

        intent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, iconResource);
        intent.putExtra("duplicate", false);
        intent.setAction("com.android.launcher.action.INSTALL_SHORTCUT");

        context.sendBroadcast(intent);

    }

    private void removeLauncherShortcut(Context context) {
        final Intent shortcutIntent = new Intent(Intent.ACTION_MAIN);
        shortcutIntent.setComponent(new ComponentName(context.getPackageName(), "cm.aptoide.ptdev.Start"));

        final Intent intent = new Intent();
        intent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcutIntent);
        intent.putExtra(Intent.EXTRA_SHORTCUT_NAME, Aptoide.getConfiguration().getMarketName());
        shortcutIntent.setComponent(new ComponentName(context.getPackageName(), "cm.aptoide.ptdev.Start"));
        intent.setAction("com.android.launcher.action.UNINSTALL_SHORTCUT");

        context.sendBroadcast(intent, null);
    }


    public SharedPreferences getPreferences() {
        return getPreferences;
    }

    public SharedPreferences.Editor setPreferences() {
        return setPreferences;
    }


    public void setAptoideClientUUID(String uuid) {
        setPreferences.putString(EnumPreferences.APTOIDE_CLIENT_UUID.name(), uuid);
        setPreferences.commit();
    }

    public String getAptoideClientUUID() {
        return getPreferences.getString(EnumPreferences.APTOIDE_CLIENT_UUID.name(), null);
    }


    public IconDownloadPermissions getIconDownloadPermissions() {
        IconDownloadPermissions permissions = new IconDownloadPermissions(
                getPreferences.getBoolean("wifi", true),
                getPreferences.getBoolean("ethernet", true),
                getPreferences.getBoolean("4g", true),
                getPreferences.getBoolean("3g", true));
        return permissions;
    }


}
