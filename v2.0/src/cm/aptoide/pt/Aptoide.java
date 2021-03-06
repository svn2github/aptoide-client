/*
 * Copyright (C) 2009  Roberto Jacinto
 * roberto.jacinto@caixamagica.pt
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
*/

package cm.aptoide.pt;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.net.URL;
import java.util.Vector;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ProgressBar;

public class Aptoide extends Activity { 
    
	private static final int OUT = 0;
    private static final String TMP_SRV_FILE = "/sdcard/.aptoide/server";
    
    // Used for Aptoide version update
	private DbHandler db = null;

	private SharedPreferences sPref;
	private SharedPreferences.Editor prefEdit;
	
	 private ProgressBar mProgress;
     private int mProgressStatus = 0;
     private Handler mHandler = new Handler();

    
    private Handler startHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			switch(msg.what){
			case OUT:
				Intent i = new Intent(Aptoide.this, RemoteInTab.class);
				Intent get = getIntent();
				if(get.getData() != null){
					String uri = get.getDataString();
					if(uri.startsWith("aptoiderepo")){
						String repo = uri.substring(14);
						i.putExtra("newrepo", repo);
					}else{
						downloadServ(uri);
						i.putExtra("uri", TMP_SRV_FILE);
					}
				}
				startActivityForResult(i,0);
				break;
			}
			super.handleMessage(msg);
		} 
    }; 
	
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sPref = getSharedPreferences("aptoide_prefs", MODE_PRIVATE);
		prefEdit = sPref.edit();
        
   		db = new DbHandler(this);
   		
   		PackageManager mPm = getPackageManager();
   		try {
			PackageInfo pkginfo = mPm.getPackageInfo("cm.aptoide.pt", 0);
			if(sPref.getInt("version", 0) < pkginfo.versionCode){
		   		db.UpdateTables();
		   		prefEdit.putInt("version", pkginfo.versionCode);
		   		prefEdit.commit();
			}
		} catch (NameNotFoundException e) {	}
        
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.start);
        
        mProgress = (ProgressBar) findViewById(R.id.pbar);
       
        new Thread(new Runnable() {
            public void run() {
            	Vector<ApkNode> apk_lst = db.getAll("abc");
            	mProgress.setMax(apk_lst.size());
        		PackageManager mPm;
        		PackageInfo pkginfo;
        		mPm = getPackageManager();
        		for(ApkNode node: apk_lst){ 
        			if(node.status == 0){
       				 try{
       					 pkginfo = mPm.getPackageInfo(node.apkid, 0);
       					 String vers = pkginfo.versionName;
       					 int verscode = pkginfo.versionCode;
       					 db.insertInstalled(node.apkid, vers, verscode);
       				 }catch(Exception e) {
       					 //Not installed anywhere... does nothing
       				 }
       			 }else{
       				 try{
       					 pkginfo = mPm.getPackageInfo(node.apkid, 0);
       					 String vers = pkginfo.versionName;
       					 int verscode = pkginfo.versionCode;
       					 db.UpdateInstalled(node.apkid, vers, verscode);
       				 }catch (Exception e){
       					 db.removeInstalled(node.apkid);
       				 }
       			 }
                    mProgressStatus++;
                    // Update the progress bar
                    mHandler.post(new Runnable() {
                        public void run() {
                            mProgress.setProgress(mProgressStatus);
                        }
                    });
                }
                Message msg = new Message();
                msg.what = 0;
                startHandler.sendMessage(msg); 
            }
        }).start();        
    }


	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		db.clodeDb();
		super.onActivityResult(requestCode, resultCode, data);
		this.finish();
	}
    
	private void downloadServ(String srv){
		try{
			BufferedInputStream getit = new BufferedInputStream(new URL(srv).openStream());

			File file_teste = new File(TMP_SRV_FILE);
			if(file_teste.exists())
				file_teste.delete();
			
			FileOutputStream saveit = new FileOutputStream(TMP_SRV_FILE);
			BufferedOutputStream bout = new BufferedOutputStream(saveit,1024);
			byte data[] = new byte[1024];
			
			int readed = getit.read(data,0,1024);
			while(readed != -1) {
				bout.write(data,0,readed);
				readed = getit.read(data,0,1024);
			}
			bout.close();
			getit.close();
			saveit.close();
		} catch(Exception e){
			AlertDialog p = new AlertDialog.Builder(this).create();
			p.setTitle("Erro");
			p.setMessage("Não foi possivel conectar ao servidor remoto.");
			p.setButton("Ok", new DialogInterface.OnClickListener() {
			      public void onClick(DialogInterface dialog, int which) {
			          return;
			        } });
			p.show();
		}
	}
}
