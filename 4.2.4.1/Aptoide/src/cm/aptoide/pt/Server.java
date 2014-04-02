/*******************************************************************************
 * Copyright (c) 2012 rmateus.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 ******************************************************************************/
package cm.aptoide.pt;

import cm.aptoide.pt.views.ViewLogin;

import java.util.ArrayList;

public class Server {

    public boolean isDelta;
    public boolean showError = true;
    public boolean isBare;

    public static enum State { PARSINGLATEST, PARSINGTOP, PARSING, PARSED, QUEUED, FAILED }
	
	public long id;
	public String url = "";
	public String hash = "";
	public String timestamp = "";
	public int n_apk = 0;
	public State state = State.QUEUED;
	public String iconsPath;
	public String basePath;
	public String apkPath;
	public String webservicesPath;
	public String xml;
	public String screenspath;
	public String featuredgraphicPath;
	public String name;
	public String theme;
	public boolean oem;
    public ArrayList<String> coutriesPermitted;
	
	private ViewLogin login;
	
	public Server() {}
	
	public Server(String url){
		this.url = url;
	}
	
	public Server(String url, String delta, long id){
		this.url = url;
		this.hash = delta;
		this.id=id;
	}

       public void clear() {
    		iconsPath = null;
       }

	public ViewLogin getLogin() {
		return login;
	}

	public void setLogin(ViewLogin login) {
		this.login = login;
	}
}
