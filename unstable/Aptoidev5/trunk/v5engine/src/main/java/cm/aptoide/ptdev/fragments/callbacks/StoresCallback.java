package cm.aptoide.ptdev.fragments.callbacks;

import cm.aptoide.ptdev.model.Login;

import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: rmateus
 * Date: 28-10-2013
 * Time: 11:42
 * To change this template use File | Settings | File Templates.
 */
public interface StoresCallback {
    void showAddStoreDialog();

    void reloadStores(Set<Long> checkedItems);

    boolean isRefreshing(long id);

}
