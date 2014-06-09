package cm.aptoide.ptdev.webservices;

import cm.aptoide.ptdev.R;

import java.util.HashMap;

/**
 * Created by j-pac on 19-05-2014.
 */
public class Errors {

    private static HashMap<String, Integer> errors = new HashMap<String, Integer>();

    static {

        errors.put("IARG-1", R.string.error_IARG_1);
        errors.put("IARG-2", R.string.error_IARG_2);
        errors.put("IARG-3", R.string.error_IARG_3);
        errors.put("IARG-4", R.string.error_IARG_4);
        errors.put("IARG-100", R.string.error_IARG_100);
        errors.put("IARG-101", R.string.error_IARG_101);
        errors.put("IARG-102", R.string.error_IARG_102);
        errors.put("IARG-107", R.string.error_IARG_107);
        errors.put("IARG-108", R.string.error_IARG_108);
        errors.put("IARG-109", R.string.error_IARG_109);
        errors.put("IARG-200", R.string.error_IARG_200);
        errors.put("IARG-201", R.string.error_IARG_201);

        errors.put("MARG-1", R.string.error_MARG_1);
        errors.put("MARG-2", R.string.error_MARG_2);
        errors.put("MARG-3", R.string.error_MARG_3);
        errors.put("MARG-4", R.string.error_MARG_4);
        errors.put("MARG-6", R.string.error_MARG_6);
        errors.put("MARG-104", R.string.error_MARG_104);
        errors.put("MARG-105", R.string.error_MARG_105);
        errors.put("MARG-106", R.string.error_MARG_106);
        errors.put("MARG-200", R.string.error_MARG_200);
        errors.put("MARG-201", R.string.error_MARG_201);

        errors.put("AUTH-1", R.string.error_AUTH_1);
        errors.put("AUTH-2", R.string.error_AUTH_2);
        errors.put("AUTH-3", R.string.error_AUTH_3);
        errors.put("AUTH-100", R.string.error_AUTH_100);
        errors.put("AUTH-101", R.string.error_AUTH_101);
        errors.put("AUTH-102", R.string.error_AUTH_102);
        errors.put("AUTH-103", R.string.error_AUTH_103);

        errors.put("WOP-1", R.string.error_WOP_1);
        errors.put("WOP-2", R.string.error_WOP_2);
        errors.put("WOP-3", R.string.error_WOP_3);
        errors.put("WOP-4", R.string.error_WOP_4);
        errors.put("WOP-5", R.string.error_WOP_5);

        errors.put("REPO-1", R.string.error_REPO_1);
        errors.put("REPO-2", R.string.error_REPO_2);
        errors.put("REPO-3", R.string.error_REPO_3);
        
        errors.put("APK-1", R.string.error_APK_1);
        errors.put("APK-4", R.string.error_APK_4);

    }

    public static HashMap<String, Integer> getErrorsMap() {
        return errors;
    }
}
