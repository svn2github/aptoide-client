package cm.aptoide.ptdev;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

/**
 * Created with IntelliJ IDEA.
 * User: rmateus
 * Date: 10-10-2013
 * Time: 17:59
 * To change this template use File | Settings | File Templates.
 */
public class IntentReceiver extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent i = new Intent(this, MainActivity.class);
        i.putExtra("newrepo", "");

        startActivity(i);
        finish();



    }
}
