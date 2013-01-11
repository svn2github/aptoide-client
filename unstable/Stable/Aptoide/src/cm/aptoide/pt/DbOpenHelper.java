/*******************************************************************************
  * Copyright (c) 2012 rmateus.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 ******************************************************************************/
package cm.aptoide.pt;



import java.util.HashMap;

import cm.aptoide.pt.util.databasecreator.TableCreator;
import cm.aptoide.pt.util.databasecreator.TableCreator.SQLiteType;

import android.app.AlertDialog;
import android.content.Context;
import android.database.MatrixCursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQuery;
import android.database.sqlite.SQLiteQueryBuilder;
import android.widget.Toast;

public class DbOpenHelper extends SQLiteOpenHelper {

	
	
	
	public final static String TABLE_TOP_LATEST_REPO_INFO= "top_latest_repo_info";
	public final static String TABLE_APK= "apk";
	public final static String TABLE_CATEGORY_1ST = "category_1st";
	public final static String TABLE_CATEGORY_2ND = "category_2nd";
	
	public final static String COLUMN__ID = "_id";
	public final static String COLUMN_NAME = "name";
	public final static String COLUMN_VERNAME = "vername";
	public final static String COLUMN_VERCODE = "vercode";
	public final static String COLUMN_ICONPATH = "icon";
	public final static String COLUMN_DOWNLOADS = "downloads";
	public final static String COLUMN_SIZE = "size";
	public final static String COLUMN_RATING = "rating";
	public final static String COLUMN_REMOTE_PATH = "remote_path";
	public final static String COLUMN_CATEGORY_2ND = "category_2nd";
	public final static String COLUMN_CATEGORY_1ST = "category_1st";
	public final static String COLUMN_MD5 = "md5";
	public final static String COLUMN_REPO_ID = "repo_id";
	public final static String COLUMN_DATE = "date";
	public final static String COLUMN_MIN_SDK = "minsdk";
	public final static String COLUMN_MIN_SCREEN = "minscreen";
	public final static String COLUMN_MIN_GLES = "mingles";
	public final static String COLUMN_MATURE = "mature";
	public final static String COLUMN_HASH = "hash";
	
	public DbOpenHelper(Context context) {
		super(context, "aptoide.db", null, 5);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		
		TableCreator creator = new TableCreator(db);
		
		creator.newTable(TABLE_TOP_LATEST_REPO_INFO).addColumn(SQLiteType.TEXT,"").addColumn(SQLiteType.TEXT,"").createTable();
		
		creator.newTable(TABLE_APK).addColumn(SQLiteType.INTEGER,COLUMN__ID);
		creator.newTable(TABLE_CATEGORY_1ST).addColumn(SQLiteType.INTEGER, name);
		MatrixCursor cursor = new MatrixCursor(new String[]{"ola"});
		
		db.execSQL("create table apk (_id integer primary key, apkid text, name text, vername text, vercode integer, imagepath text, downloads integer, size integer,rating text,path text, category2 integer, md5 text,  repo_id integer, date date, minscreen int, minsdk int, mingles real, mature int, exclude_update boolean default false);");
		db.execSQL("create table category1 (_id integer primary key, name text , size integer, unique (name) on conflict ignore) ;");
		db.execSQL("create table category2 (_id integer primary key, catg1_id integer, name text , size integer, unique (name) on conflict ignore);");
		db.execSQL("create table repo (_id integer primary key, url text default '', delta text, appcount integer, iconspath text default '', basepath text default '', status text default '', webservicespath text default '', username text, password text, avatar text default '', name text default '', downloads integer default 0, apkpath text default '');");
		db.execSQL("create table toprepo_extra (_id integer, top_delta text, screenspath text, category text,iconspath text, basepath text, url text, name text);");
		db.execSQL("create table repo_category1 (repo_id integer, catg1_id integer, primary key(repo_id, catg1_id) on conflict ignore);");
		db.execSQL("create table repo_category2 (repo_id integer, catg2_id integer, primary key(repo_id, catg2_id) on conflict ignore);");
		db.execSQL("create table installed (apkid text, vercode integer, vername text, name text);");
		db.execSQL("create table dynamic_apk (_id integer primary key, apkid text, name text, vername text, vercode integer, imagepath text, downloads integer, size integer, rating text,path text, category1 integer, md5 text, repo_id integer, minscreen int, minsdk int, mingles real, mature int);");
		db.execSQL("create table screenshots (_id integer, type integer, path text, repo_id integer);");
		db.execSQL("create table itembasedapkrepo (_id integer primary key, name text, basepath text, iconspath text, screenspath text, featuredgraphicpath text);");
		db.execSQL("create table itembasedapk (_id integer primary key, itembasedapkrepo_id integer, apkid text, name text, vercode integer, vername text, category2 text, downloads integer, rating text, icon text, md5 text, path text, size integer, parent_apkid text, featuredgraphic text, highlight text, minscreen int, minsdk int, mingles real, mature int);");
		db.execSQL("create table itembasedapk_hash (hash text, apkid text primary key, unique (apkid) on conflict replace);");
		db.execSQL("create table userbasedapk (_id integer primary key, apkid text, vercode integer);");
		db.execSQL("create table scheduled (_id integer primary key, name text, apkid text, vercode integer, vername text, remotepath text, md5 text );");
		db.execSQL("CREATE INDEX mytest_id2_idx ON installed(apkid);");
		db.execSQL("CREATE INDEX mytest_id_idx ON apk(apkid,vercode,category2,repo_id);");
		db.execSQL("CREATE INDEX mytest_id3_idx ON dynamic_apk(apkid,vercode,category1,repo_id);");
		
		
	}
	
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		
		db.execSQL("DROP TABLE IF EXISTS apk");
		db.execSQL("DROP TABLE IF EXISTS category1");
		db.execSQL("DROP TABLE IF EXISTS category2");
		db.execSQL("DROP TABLE IF EXISTS repo");
		db.execSQL("DROP TABLE IF EXISTS toprepo_extra");
		db.execSQL("DROP TABLE IF EXISTS repo_category1");
		db.execSQL("DROP TABLE IF EXISTS repo_category2");
		db.execSQL("DROP TABLE IF EXISTS installed");
		db.execSQL("DROP TABLE IF EXISTS dynamic_apk");
		db.execSQL("DROP TABLE IF EXISTS itembasedapkrepo");
		db.execSQL("DROP TABLE IF EXISTS itembasedapk");
		db.execSQL("DROP TABLE IF EXISTS itembasedapk_hash");
		db.execSQL("DROP TABLE IF EXISTS userbasedapk");
		db.execSQL("DROP TABLE IF EXISTS scheduled");
		db.execSQL("DROP TABLE IF EXISTS screenshots");
		
		onCreate(db);
	}
	
	

}
