package cm.aptoide.ptdev;

/**
 * Created with IntelliJ IDEA.
 * User: rmateus
 * Date: 26-11-2013
 * Time: 10:50
 * To change this template use File | Settings | File Templates.
 */
public interface FragmentStore {

    void onRefresh();
    void onError();
    void setRefreshing(boolean bool);
}
