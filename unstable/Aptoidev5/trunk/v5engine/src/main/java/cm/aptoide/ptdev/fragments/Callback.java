package cm.aptoide.ptdev.fragments;

import com.actionbarsherlock.view.ActionMode;
import com.actionbarsherlock.view.MenuItem;

/**
 * Created with IntelliJ IDEA.
 * User: rmateus
 * Date: 19-11-2013
 * Time: 14:55
 * To change this template use File | Settings | File Templates.
 */
public interface Callback {
    public boolean onActionItemClicked(ActionMode mode, MenuItem item);
}
