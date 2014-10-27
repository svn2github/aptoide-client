package cm.aptoide.ptdev;

import android.content.Context;
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

    public static void SendMail(Context c){
        String subject = c.getString(R.string.aptoide_timeline);
        String html =
                "<body style=\"margin:0; background-color:#e8e8e8; font-family: 'Open Sans', sans-serif;\">\n" +
                        "\n" +
                        "\n" +
                        "<table width=\"600\" bgcolor=\"#FFFFFF\" align=\"center\" border=\"0\" cellspacing=\"0\" cellpadding=\"0\">\n" +
                        "    <tr>\n" +
                        "    <td align=\"center\"><p style=\"font-size:19px; color:#343434; margin-top:8px;\">%s</p>\n" +
                        "    <p style=\"font-size:16px; color:#888888; font-weight:200; margin-top:-10px;\">%s</p>\n" +
                        "    </td>\n" +
                        "    </tr>\n" +
                        "   </tr>\n" +
                        "    <tr>\n" +
                        "    <td align=\"left\" style=\"padding:15px;\">\n" +
                        "    <p>%s</p>\n" +
                        "    <p style=\"font-size:13px; color:#888888; line-height:22px; margin-top:-5px;\">%s</p>\n" +
                        "    </td>\n" +
                        "    </tr>\n" +
                        "   <tr>\n" +
                        "    <td align=\"left\" style=\"padding:15px;\"><p>%s</p>\n" +
                        "    <p style=\"font-size:13px; color:#888888; line-height:22px; margin-top:-5px;\">\n" +
                        " %s <a href=\"http://m.aptoide.com/install\">.<br>\n" +
                        " %s</p>\n" +
                        "    </td>\n" +
                        "     </tr>\n" +
                        "       </tr>\n" +
                        "</table>\n" +
                        "</body>\n";

        String username = PreferenceManager.getDefaultSharedPreferences(Aptoide.getContext()).getString("username", null);
        String Invitation= c.getString(R.string.timeline_email_invitation);
        String whatIs= c.getString(R.string.whats_timeline);
        String TOS= c.getString(R.string.facebook_tos).replace("\n\n","<br>");

        String howTo= c.getString(R.string.timeline_email_how_to_join);
        String step1= c.getString(R.string.timeline_email_step1);
        String step2= c.getString(R.string.timeline_email_step2);

        Intent emailIntent = new Intent(Intent.ACTION_SEND);
        emailIntent.setType("text/html");

        emailIntent.putExtra(Intent.EXTRA_SUBJECT,subject);
        emailIntent.putExtra(Intent.EXTRA_TEXT,
                Html.fromHtml(String.format(html,username,Invitation,whatIs,TOS,howTo,step1,step2)));
        try {
            c.startActivity(emailIntent);

        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(c, R.string.feedback_no_email, Toast.LENGTH_LONG).show();
        }
    }

    public void SendMail(View view) {
        SendMail(this);
        finish();
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
