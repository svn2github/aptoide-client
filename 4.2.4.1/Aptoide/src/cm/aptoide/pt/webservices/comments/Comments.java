/*******************************************************************************
 * Copyright (c) 2012 rmateus.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 ******************************************************************************/
package cm.aptoide.pt.webservices.comments;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import cm.aptoide.pt.R;
import cm.aptoide.pt.configuration.AptoideConfiguration;
import cm.aptoide.pt.util.Utils;
import cm.aptoide.pt.webservices.login.Login;
import org.json.JSONException;
import org.json.JSONObject;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class Comments {

	LinearLayout view;
	Activity context;

	private static final String listApkComments = "webservices/listApkComments/%1$s/%2$s/%3$s/xml";
	private static final String addApkComment = "webservices/addApkComment";
	private static final String DEFAULT_PATH = AptoideConfiguration.getInstance().getWebServicesUri();
	private static final String COMMENTS_TO_LOAD = "5";
	public String WEB_SERVICE_COMMENTS_LIST;
	public String WEB_SERVICE_COMMENTS_POST;

	private boolean submitting = false;


	static AsyncTask<String, Void, ArrayList<Comment>> task;

	public Comments(Activity context, String webservicespath) {
		this.context=context;
		if(webservicespath==null){
			webservicespath = DEFAULT_PATH;
		}
		WEB_SERVICE_COMMENTS_LIST = webservicespath+listApkComments;
		WEB_SERVICE_COMMENTS_POST = webservicespath+addApkComment;


	}

	public void getComments(String repo, String apkid, String version,LinearLayout view,boolean all){
		this.view=view;
		if(task!=null){
			System.out.println("canceling");
			task.cancel(true);
		}
		if(all){
			task = new CommentsGetter().execute(repo,apkid,version,Integer.MAX_VALUE+"");
		}else{
			task = new CommentsGetter().execute(repo,apkid,version,COMMENTS_TO_LOAD);
		}


	}

	public class CommentsGetter extends AsyncTask<String, Void, ArrayList<Comment>>{
        DateFormat dateFormat = android.text.format.DateFormat.getDateFormat(context);
        DateFormat timeFormat = android.text.format.DateFormat.getTimeFormat(context);
        SimpleDateFormat dateFormater = new SimpleDateFormat("yyyy-MM-dd HH:mm");
		View loading;
		Exception errorMessage;
		boolean error;
		private boolean seeAllComments = false;
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			view.removeAllViews();

			loading = LayoutInflater.from(context).inflate(R.layout.loadingfootercomments, null);
			view.addView(loading, new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));

		}

		@Override
		protected ArrayList<Comment> doInBackground(final String... params) {
			final ArrayList<Comment> comments = new ArrayList<Comment>();
			try{
                String url = String.format(WEB_SERVICE_COMMENTS_LIST, params[0],params[1],URLEncoder.encode(params[2], "UTF-8"));


				HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
				System.out.println(connection.getURL());
				connection.setConnectTimeout(10000);
				connection.setReadTimeout(10000);
				BufferedInputStream bis = new BufferedInputStream(connection.getInputStream(),8*1024);

				SAXParserFactory spfact = SAXParserFactory.newInstance();
				SAXParser parser = spfact.newSAXParser();
				parser.parse(bis, new DefaultHandler(){
					int i=0;
					int MAX_COMMENTS = Integer.parseInt(params[3]);
					StringBuilder sb = new StringBuilder();
					Comment comment;
					@Override
					public void startElement(String uri, String localName,
							String qName, Attributes attributes)
							throws SAXException {
						if(localName.equals("entry")){
							comment = new Comment();
						}
						sb.setLength(0);
						super.startElement(uri, localName, qName, attributes);
					}
					@Override
					public void characters(char[] ch, int start, int length)
							throws SAXException {
						super.characters(ch, start, length);
						sb.append(ch,start,length);
					}

					@Override
					public void endElement(String uri, String localName,
							String qName) throws SAXException {
						super.endElement(uri, localName, qName);

						if(localName.equals("entry")){

							comments.add(comment);
							i++;
							if(i==MAX_COMMENTS){
								seeAllComments = true;
								throw new SAXException();
							}
						}else if(localName.equals("username")){
							comment.username=sb.toString();
						}else if(localName.equals("text")){
							comment.text=sb.toString();
						}else if(localName.equals("timestamp")){
							try {
								comment.timeStamp=dateFormater.parse(sb.toString()).getTime();
							} catch (ParseException e) {
								e.printStackTrace();
							}
						}else if(localName.equals("status")){
							if(sb.toString().equals("FAIL")){
								throw new SAXException();
							}
						}

					}
				});

                bis.close();
			}catch(UnknownHostException e){
				internetConnection=false;
				error = true;
				cancel(false);
			}catch(SAXException e){
				System.out.println("saxexception");
                e.printStackTrace();
				if(seeAllComments){
					context.runOnUiThread(new Runnable() {

						@Override
						public void run() {
							context.findViewById(R.id.more_comments).setVisibility(View.VISIBLE);

						}
					});
				}
			}catch(Exception e){
				e.printStackTrace();
				error = true;
				errorMessage = e;
				cancel(false);
			}
			return comments;
		}
		boolean internetConnection = false;
		@Override
		protected void onPostExecute(ArrayList<Comment> result) {
			super.onPostExecute(result);
			System.out.println("onPostExecute");
			loading.setVisibility(View.GONE);
			if(result.isEmpty()){
				TextView tv = new TextView(context);
				tv.setText(context.getString(R.string.no_comments));
				tv.setPadding(8, 2, 2, 2);
				tv.setTextColor(context.getResources().getColor(android.R.color.darker_gray));
				view.addView(tv);
			}
			for(Comment comment : result){
                Date date = new Date(comment.timeStamp);
				View v = LayoutInflater.from(context).inflate(R.layout.row_comment_item, null);
				((TextView) v.findViewById(R.id.author)).setText(comment.username);
				((TextView) v.findViewById(R.id.content)).setText(comment.text);
                ((TextView) v.findViewById(R.id.date)).setText(timeFormat.format(date) + " | " + dateFormat.format(date));
                view.addView(v);
			}

		}

		@Override
		protected void onCancelled() {
			super.onCancelled();
			if(error){
				if(!internetConnection){
					TextView tv = new TextView(context);
					tv.setText(context.getString(R.string.no_internet_connection));
					view.addView(tv);
				}
				loading.setVisibility(View.GONE);
				error=false;
			}


		}


	}

	public void postComment(String repo, String apkid, String version,String comment, String username) {
		if(!submitting){
			new CommentPoster().execute(Login.getToken(context),repo,apkid,version,comment,username);
		}else{
			Toast toast= Toast.makeText(context, context.getString(R.string.another_comment_is_beeing_submited), Toast.LENGTH_SHORT);
					toast.show();
		}
	}


	public enum EnumCommentResponse {
		OK, FAIL
	}

	public class CommentPoster extends AsyncTask<String, Void, EnumCommentResponse>{
		ProgressDialog pd;
        JSONObject object;
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			pd = new ProgressDialog(context);
			pd.setMessage("Please wait.");
			pd.show();

			submitting = true;
		}
		@Override
		protected EnumCommentResponse doInBackground(String... params) {

			String data;
			StringBuilder sb;
			EnumCommentResponse response = null;
			try {
				if(params[5]!=null){
					Login.updateName(params[5]);
				}
				HttpURLConnection connection = (HttpURLConnection) new URL(WEB_SERVICE_COMMENTS_POST).openConnection();
				connection.setConnectTimeout(10000);
				connection.setReadTimeout(10000);
				connection.setDoInput(true);
				connection.setDoOutput(true);
				data = URLEncoder.encode("token", "UTF-8") + "=" + URLEncoder.encode(params[0], "UTF-8");
			    data += "&" + URLEncoder.encode("repo", "UTF-8") + "=" + URLEncoder.encode(params[1],"UTF-8");
			    data += "&" + URLEncoder.encode("apkid", "UTF-8") + "=" + URLEncoder.encode(params[2],"UTF-8");
			    data += "&" + URLEncoder.encode("apkversion", "UTF-8") + "=" + URLEncoder.encode(params[3],"UTF-8");
			    data += "&" + URLEncoder.encode("text", "UTF-8") + "=" + URLEncoder.encode(params[4],"UTF-8");
                data += "&" + URLEncoder.encode("lang", "UTF-8") + "=" + URLEncoder.encode(Utils.getMyCountryCode(context), "UTF-8");
                data += "&" + URLEncoder.encode("mode", "UTF-8") + "=" + URLEncoder.encode("json", "UTF-8");


			    OutputStreamWriter wr = new OutputStreamWriter(connection.getOutputStream());
			    wr.write(data);
			    wr.flush();

			    BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                sb = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    sb.append(line).append("\n");
                }
                wr.close();
                br.close();
				System.out.println(sb.toString());
				System.out.println(connection.getURL());
			    object = new JSONObject(sb.toString());
			    response = EnumCommentResponse.valueOf(object.getString("status").toUpperCase());

			} catch (IOException e) {
				e.printStackTrace();
				return EnumCommentResponse.FAIL;
			} catch (JSONException e) {
				e.printStackTrace();
				return EnumCommentResponse.FAIL;
			} catch (InvalidKeyException e) {
				e.printStackTrace();
			} catch (IllegalStateException e) {
				e.printStackTrace();
			} catch (NoSuchAlgorithmException e) {
				e.printStackTrace();
			}
			return response;
		}

		@Override
		protected void onPostExecute(EnumCommentResponse result) {
			super.onPostExecute(result);
			if(pd.isShowing())pd.dismiss();
			submitting = false;
			Toast toast;
			switch (result) {
			case OK:
				toast= Toast.makeText(context,
						context.getString(R.string.comment_submitted), Toast.LENGTH_SHORT);
						toast.show();
				context.finish();
				break;
			case FAIL:
                try {
                    toast= Toast.makeText(context, object.getJSONArray("errors").getString(0), Toast.LENGTH_SHORT);
                    toast.show();

                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (Exception e){
                    e.printStackTrace();
                }
				break;
			default:
				break;
			}

		}

	}
}
