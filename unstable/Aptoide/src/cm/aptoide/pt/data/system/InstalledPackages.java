/*
 * InstalledPackages, auxiliary class to Aptoide's ServiceData
 * Copyright (C) 2011 Duarte Silveira
 * duarte.silveira@caixamagica.pt
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
package cm.aptoide.pt.data.system;

import java.util.List;
import android.content.pm.PackageInfo;

/**
 * InstalledPackages, models list of installed Packages 
 *
 * @author dsilveira
 * @since 3.0
 *
 */
public class InstalledPackages {
	
	private List<PackageInfo> systemInstalledList;
	String sqlWherePnames;
	String sqlWherePnamesAndInstalledVersions;
		
	
	public InstalledPackages( List<PackageInfo> systemInstalledList ){
		this.systemInstalledList = systemInstalledList;
	}


	public List<PackageInfo> getSystemInstalledList() {
		return systemInstalledList;
	}
	
	public void prepareSqlWhereStrings(){
		sqlWherePnames = null;
		sqlWherePnamesAndInstalledVersions = null;
		int iterations = 0;
		for (PackageInfo installedPackage : systemInstalledList) {
			iterations ++;
			sqlWherePnames += "apkid='"+installedPackage.packageName+"'";		//TODO replace sql column names with new ones
			sqlWherePnamesAndInstalledVersions += "(apkid='"+installedPackage.packageName+"' AND lastvercode='"+installedPackage.versionCode+"')";
			if(iterations < systemInstalledList.size()){
				sqlWherePnames += " OR ";
				sqlWherePnamesAndInstalledVersions += " OR ";
			}
		}
	}
	
	public String getSqlWherePnames(){
		return sqlWherePnames;
	}
	
	public String getSqlWherePnamesAndInstalledVersions(){
		return sqlWherePnamesAndInstalledVersions;
	}
	
}