package cm.aptoide.ptdev;

import java.util.HashMap;

/**
 * Created by tdeus on 1/29/14.
 */
public class EnumRollbackState {

    public static HashMap<String, Integer> states = new HashMap<String, Integer>();

    static{
        states.put("Installed", R.string.rollback_installed);
        states.put("Uninstalled", R.string.rollback_uninstalled);
        states.put("Updated", R.string.rollback_updated);
        states.put("Downgraded", R.string.rollback_downgraded);
    }

}
