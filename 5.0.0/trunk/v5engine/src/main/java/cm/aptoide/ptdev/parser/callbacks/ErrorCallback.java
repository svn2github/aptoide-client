package cm.aptoide.ptdev.parser.callbacks;

import cm.aptoide.ptdev.model.Store;

/**
 * Created with IntelliJ IDEA.
 * User: rmateus
 * Date: 23-10-2013
 * Time: 13:05
 * To change this template use File | Settings | File Templates.
 */
public interface ErrorCallback {

    void onError(Exception e, long repoId);


}
