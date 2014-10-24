package cm.aptoide.ptdev;

import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.text.Html;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

/**
 * Created by asantos on 24-10-2014.
 */

public class TimeLineNoFriendsInviteActivity extends ActionBarActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Aptoide.getThemePicker().setAptoideTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.page_timeline_no_activity);

        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setTitle(getString(R.string.invite_friends));


    }

    public void SendMail(View view) {
        String subject = getString(R.string.aptoide_timeline);
        String text;

        String username = PreferenceManager.getDefaultSharedPreferences(Aptoide.getContext()).getString("username", null);
        String Invitation= getString(R.string.timeline_email_invitation);
        String whatIs= getString(R.string.whats_timeline);
        String TOS= getString(R.string.facebook_tos);

        String howTo= getString(R.string.timeline_email_how_to_join);
        String step1= getString(R.string.timeline_email_step1) +
                "<a href=\"http://m.aptoide.com/install.\">"+getString(R.string.install) +" Aptoide</a>";
        String step2= getString(R.string.timeline_email_step2);

        Intent emailIntent = new Intent(Intent.ACTION_SEND);
        emailIntent.setType("text/html");

        emailIntent.putExtra(Intent.EXTRA_SUBJECT,subject);
        emailIntent.putExtra(Intent.EXTRA_TEXT,
                Html.fromHtml(
                    username+"<br>"+
                    Invitation+"<br>"+
                    whatIs+"<br>"+
                    TOS+"<br>"+
                    howTo+"<br>"+
                    step1+"<br>"+
                    step2));
        try {
            startActivity(emailIntent);
            finish();
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(this, R.string.feedback_no_email, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int i = item.getItemId();
        if (i == android.R.id.home || i == R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
}
