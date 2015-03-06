package cm.aptoide.ptdev;

import cm.aptoide.ptdev.model.Login;

/**
 * Created by rmateus on 20-03-2014.
 */
public interface CategoryCallback{
    StoreActivity.SortObject getSort();

    void onRefreshStarted();

    void installApp(long id);

    void setLogin(Login login);
    Login getLogin();
}
