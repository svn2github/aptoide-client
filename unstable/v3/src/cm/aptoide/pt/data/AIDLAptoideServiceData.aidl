/**
 * AIDLAptoideServiceData,		part of Aptoide's ServiceData
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
package cm.aptoide.pt.data;

import cm.aptoide.pt.data.system.ViewScreenDimensions;
import cm.aptoide.pt.AIDLAptoideInterface;
import cm.aptoide.pt.data.display.ViewDisplayListsDimensions;
import cm.aptoide.pt.data.display.ViewDisplayCategory;
import cm.aptoide.pt.data.display.ViewDisplayListApps;
import cm.aptoide.pt.data.display.ViewDisplayListRepos;
import cm.aptoide.pt.data.display.ViewDisplayAppVersionsInfo;
import cm.aptoide.pt.data.display.ViewDisplayAppVersionStats;
import cm.aptoide.pt.data.display.ViewDisplayAppVersionExtras;
import cm.aptoide.pt.data.display.ViewDisplayListComments;
import cm.aptoide.pt.data.model.ViewRepository;
import cm.aptoide.pt.AIDLAppInfo;
import cm.aptoide.pt.AIDLReposInfo;
import cm.aptoide.pt.AIDLSelfUpdate;
import cm.aptoide.pt.data.listeners.ViewMyapp;
import cm.aptoide.pt.data.preferences.ViewSettings;
import cm.aptoide.pt.data.system.ViewHwFilters;
import cm.aptoide.pt.data.webservices.ViewIconDownloadPermissions;

/**
 * AIDLAptoideServiceData, IPC Interface definition for Aptoide's ServiceData
 *
 * @author dsilveira
 * @since 3.0
 *
 */
interface AIDLAptoideServiceData {
	
	void callRegisterSelfUpdateObserver(in AIDLSelfUpdate selfUpdateClient);
	void callAcceptSelfUpdate();
	void callRejectSelfUpdate();

	String callGetAptoideVersionName();
	void callStoreScreenDimensions(in ViewScreenDimensions screenDimensions);
	
	ViewDisplayListsDimensions callGetDisplayListsDimensions();
	
	void callSyncInstalledApps();
	
	void callRegisterReposObserver(in AIDLReposInfo reposInfoObserver);
	ViewDisplayListRepos callGetRepos();
	void callAddRepo(in ViewRepository repo);
	void callRemoveRepo(in int repoHashid);
	void callSetInUseRepo(in int repoHashid);
	void callUnsetInUseRepo(in int repoHashid);
	void callRemoveLogin(in int repoHashid);
	void callUpdateLogin(in ViewRepository repo);
	void callNoRepos();
	void callLoadingRepos();
	
	void callRegisterInstalledAppsObserver(in AIDLAptoideInterface installedAppsObserver);
	void callRegisterAvailableAppsObserver(in AIDLAptoideInterface availableAppsObserver);
	
	boolean callAreListsByCategory();
	void callSetListsBy(in boolean byCategory);
	
	int callGetTotalAvailableApps();
	int callGetTotalAvailableAppsInCategory(in int categoryHashid);
	
	ViewDisplayCategory callGetCategories();
	
	int callGetAppsSortingPolicy();
	void callSetAppsSortingPolicy(in int sortingPolicy);
	
	ViewDisplayListApps callGetInstalledApps();
	ViewDisplayListApps callGetAvailableApps(in int offset, in int range);
	ViewDisplayListApps callGetAvailableAppsByCategory(in int offset, in int range, in int categoryHashid);
	ViewDisplayListApps callGetUpdatableApps();
	
	void callUpdateAll();
	
	ViewDisplayListApps callGetAppSearchResults(in String searchString);
	
	void callRegisterAppInfoObserver(in AIDLAppInfo appInfoObserver, in int appHashid);
	void CallFillAppInfo(in int appHashid);
	void callAddVersionDownloadInfo(in int appHashid, in int repoHashid);
	void callAddVersionStatsInfo(in int appHashid, in int repoHashid);
	void callAddVersionExtraInfo(in int appHashid, in int repoHashid);
	void callRetrieveVersionComments(in int appHashid, in int repoHashid);
	ViewDisplayAppVersionsInfo callGetAppInfo(in int appHashid);
	int callGetAppVersionDownloadSize(in int appFullHashid);
	ViewDisplayAppVersionStats callGetAppStats(in int appFullHashid);
	ViewDisplayAppVersionExtras callGetAppExtras(in int appFullHashid);
	ViewDisplayListComments callGetVersionComments(in int appHashid);
	
	String callGetServerToken();
	int callServerLogin(in String username, in String password);
	void callClearServerLogin();
	int callAddAppVersionLike(in String repoName, in int appHashid, in boolean like);
	int callAddAppVersionComment(in String repoName, in int appHashid, in String commentBody, in String subject, in long answerTo);
	
	void callInstallApp(in int appHashid);
	void callUninstallApp(in int appHashid);
	void callScheduleInstallApp(in int appHashid);
	void callUnscheduleInstallApp(in int appHashid);
	boolean callIsAppScheduledToInstall(in int appHashid);
	
	ViewDisplayListApps callGetScheduledApps();
	
	void callRegisterMyappReceiver(in AIDLAptoideInterface myappObserver);
	void callReceiveMyapp(in String uriString);
	ViewMyapp callGetWaitingMyapp();
	void callInstallMyapp(in ViewMyapp myapp);
	void callRejectedMyapp();
	ViewDisplayListRepos callGetWaitingMyappRepos();
	
	ViewSettings callGetSettings();
	ViewIconDownloadPermissions callGetIconDownloadPermissions();
	void callSetIconDownloadPermissions(in ViewIconDownloadPermissions iconDownloadPermissions);
	void callClearIconCache();
	void callClearApkCache();
	ViewHwFilters callGetHwFilters();
	void callSetHwFilter(in boolean on);
	void callSetAgeRating(in int rating);
	void callSetAutomaticInstall(in boolean on);
	void callResetAvailableApps();
	
}
