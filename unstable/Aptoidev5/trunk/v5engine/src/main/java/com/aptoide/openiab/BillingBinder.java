package com.aptoide.openiab;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;

import com.aptoide.openiab.webservices.IabConsumeRequest;
import com.aptoide.openiab.webservices.IabPurchasesRequest;
import com.aptoide.openiab.webservices.IabSkuDetailsRequest;
import com.aptoide.openiab.webservices.json.IabConsumeJson;
import com.aptoide.openiab.webservices.json.IabPurchasesJson;
import com.aptoide.openiab.webservices.json.IabSkuDetailsJson;
import com.octo.android.robospice.SpiceManager;
import com.octo.android.robospice.persistence.DurationInMillis;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.RequestListener;
import org.onepf.oms.IOpenInAppBillingService;


import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;

/**
 * Created by j-pac on 12-02-2014.
 */
public class BillingBinder extends IOpenInAppBillingService.Stub {

    // Response result codes
    public static final int RESULT_OK = 0;
    public static final int RESULT_USER_CANCELED = 1;
    public static final int RESULT_BILLING_UNAVAILABLE = 3;
    public static final int RESULT_ITEM_UNAVAILABLE = 4;
    public static final int RESULT_DEVELOPER_ERROR = 5;
    public static final int RESULT_ERROR = 6;
    public static final int RESULT_ITEM_ALREADY_OWNED = 7;
    public static final int RESULT_ITEM_NOT_OWNED = 8;

    // Keys for the responses
    public static final String RESPONSE_CODE = "RESPONSE_CODE";
    public static final String DETAILS_LIST = "DETAILS_LIST";
    public static final String BUY_INTENT = "BUY_INTENT";


    public static final String INAPP_PURCHASE_DATA = "INAPP_PURCHASE_DATA";
    public static final String INAPP_DATA_SIGNATURE = "INAPP_DATA_SIGNATURE";
    public static final String INAPP_PURCHASE_ITEM_LIST = "INAPP_PURCHASE_ITEM_LIST";
    public static final String INAPP_PURCHASE_DATA_LIST = "INAPP_PURCHASE_DATA_LIST";
    public static final String INAPP_DATA_SIGNATURE_LIST = "INAPP_DATA_SIGNATURE_LIST";
    public static final String INAPP_CONTINUATION_TOKEN = "INAPP_CONTINUATION_TOKEN";

    // Param keys
    public static final String ITEM_ID_LIST = "ITEM_ID_LIST";
    public static final String ITEM_TYPE_LIST = "ITEM_TYPE_LIST";

    // Item types
    public static final String ITEM_TYPE_INAPP = "inapp";
    public static final String ITEM_TYPE_SUBS = "subs";
    private final SpiceManager manager;


    private Context context;

    public BillingBinder(Context context, SpiceManager manager) {
        this.context = context;
        this.manager = manager;
    }

    @Override
    public int isBillingSupported(int apiVersion, String packageName, String type) throws RemoteException {
        Log.d("AptoideBillingService", "[isBillingSupported]: " + packageName);

        if (apiVersion >= 3 &&
                (type.equals(BillingBinder.ITEM_TYPE_INAPP) || type.equals(BillingBinder.ITEM_TYPE_SUBS))) {
            return RESULT_OK;
        } else {
            return RESULT_BILLING_UNAVAILABLE;
        }
    }

    @Override
    public Bundle getSkuDetails(int apiVersion, String packageName, String type, Bundle skusBundle) throws RemoteException {
        Log.d("AptoideBillingService", "[getSkuDetails]: " + packageName + " " + type);

        final Bundle result = new Bundle();

        if (!skusBundle.containsKey(ITEM_ID_LIST) || apiVersion < 3) {
            result.putInt(RESPONSE_CODE, RESULT_DEVELOPER_ERROR);
            return result;
        }

        ArrayList<String> itemIdList = skusBundle.getStringArrayList(ITEM_ID_LIST);

        if (itemIdList == null || itemIdList.size() <= 0 || itemIdList.size() >= 20) {
            result.putInt(RESPONSE_CODE, RESULT_DEVELOPER_ERROR);
            return result;
        }

        try {
            AccountManager accountManager = AccountManager.get(context);
            String token = accountManager.blockingGetAuthToken(accountManager.getAccountsByType("cm.aptoide.pt.dev")[0], "Full access", true);

            if(token != null) {
                IabSkuDetailsRequest request = new IabSkuDetailsRequest();
                request.setApiVersion(Integer.toString(apiVersion));
                request.setPackageName(packageName);
                request.setToken(token);

                for(String itemId : itemIdList) {
                    request.addToSkuList(itemId);
                    Log.d("Aptoide-Binder", "Sku details request for " + itemId);
                }

                final CountDownLatch latch = new CountDownLatch(1);
                manager.execute(request, packageName + "-getSkuDetails-"+request.getSkuList(), DurationInMillis.ONE_MINUTE, new RequestListener<IabSkuDetailsJson>() {
                    @Override
                    public void onRequestFailure(SpiceException spiceException) {

                        result.putInt(RESPONSE_CODE, RESULT_ERROR);
                        latch.countDown();

                    }

                    @Override
                    public void onRequestSuccess(IabSkuDetailsJson response) {
                        ArrayList<String> detailsList = new ArrayList<String>();

                        if("OK".equals(response.getStatus())) {
                            for(IabSkuDetailsJson.PublisherResponse.PurchaseDataObject details : response.getPublisher_response().getDetails_list()) {
                                detailsList.add(details.getJson());
                                Log.d("AptoideBillingService", "Sku Details: " + details.getJson());
                            }

                            if (detailsList.size() == 0) {
                                result.putInt(RESPONSE_CODE, RESULT_ITEM_UNAVAILABLE);
                            } else {
                                result.putInt(RESPONSE_CODE, RESULT_OK);
                                result.putStringArrayList(DETAILS_LIST, detailsList);
                            }
                        } else {
                            result.putInt(RESPONSE_CODE, RESULT_DEVELOPER_ERROR);
                        }
                        latch.countDown();
                    }
                });

                latch.await();


            }

        } catch (OperationCanceledException e) {
            e.printStackTrace();
            result.putInt(RESPONSE_CODE, RESULT_ERROR);
        } catch (IOException e) {
            e.printStackTrace();
            result.putInt(RESPONSE_CODE, RESULT_ERROR);
        } catch (AuthenticatorException e) {
            e.printStackTrace();
            result.putInt(RESPONSE_CODE, RESULT_ERROR);
        } catch (Exception e) {
            e.printStackTrace();
            result.putInt(RESPONSE_CODE, RESULT_ERROR);
        }

        return result;
    }

    @Override
    public Bundle getBuyIntent(int apiVersion, String packageName, String sku, String type, String developerPayload) throws RemoteException {
        Log.d("AptoideBillingService", "[getBuyIntent]: " + packageName + " " + type);

        Bundle result = new Bundle();

        PendingIntent pendingIntent;
        Intent purchaseIntent = new Intent(context, PurchaseActivity.class);

        if (apiVersion < 3 || !(type.equals(ITEM_TYPE_INAPP) || type.equals(ITEM_TYPE_SUBS))) {
            result.putInt(RESPONSE_CODE, RESULT_DEVELOPER_ERROR);
        } else {
            AccountManager accountManager = AccountManager.get(context);
            Account[] accounts = accountManager.getAccountsByType("cm.aptoide.pt.dev");

            if(accounts.length == 0) {
                Log.d("AptoideBillingService", "BillingUnavailable: user not logged in");
            }else{


                try {

                    String token = accountManager.blockingGetAuthToken(accounts[0], "Full access", true);
                    purchaseIntent.putExtra("token", token);
                    purchaseIntent.putExtra("user", accounts[0].name);

                } catch (OperationCanceledException e) {
                    result.putInt(RESPONSE_CODE, RESULT_ERROR);
                    e.printStackTrace();
                } catch (IOException e) {
                    result.putInt(RESPONSE_CODE, RESULT_ERROR);
                    e.printStackTrace();
                } catch (AuthenticatorException e) {
                    result.putInt(RESPONSE_CODE, RESULT_ERROR);
                    e.printStackTrace();
                }
            }

            purchaseIntent.putExtra("apiVersion", apiVersion);
            purchaseIntent.putExtra("type", type);
            purchaseIntent.putExtra("packageName", packageName);
            purchaseIntent.putExtra("sku", sku);


            result.putInt(RESPONSE_CODE, RESULT_OK);

        }

        pendingIntent = PendingIntent.getActivity(context, 0, purchaseIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        result.putParcelable(BUY_INTENT, pendingIntent);
        return result;
    }


    @Override
    public Bundle getPurchases(int apiVersion, String packageName, String type, String continuationToken) throws RemoteException {
        Log.d("AptoideBillingService", "[getPurchases]: " + packageName + " " + type);

        final Bundle result = new Bundle();

        if (apiVersion < 3 || !(type.equals(ITEM_TYPE_INAPP) || type.equals(ITEM_TYPE_SUBS))) {
            result.putInt(RESPONSE_CODE, RESULT_DEVELOPER_ERROR);
            return result;
        }

        AccountManager accountManager = AccountManager.get(context);
        Account[] accounts = accountManager.getAccountsByType("cm.aptoide.pt.dev");

        if(accounts.length == 0) {

            Log.d("AptoideBillingService", "BillingUnavailable: user not logged in");
            result.putStringArrayList(INAPP_PURCHASE_ITEM_LIST, new ArrayList<String>());
            result.putStringArrayList(INAPP_PURCHASE_DATA_LIST, new ArrayList<String>());
            result.putStringArrayList(INAPP_DATA_SIGNATURE_LIST, new ArrayList<String>());
            result.putInt(RESPONSE_CODE, RESULT_OK);

            return result;
        }

        try {
            String token = accountManager.blockingGetAuthToken(accounts[0], "Full access", true);

            if(token != null) {
                final CountDownLatch latch = new CountDownLatch(1);
                IabPurchasesRequest request = new IabPurchasesRequest();
                request.setApiVersion(Integer.toString(apiVersion));
                request.setPackageName(packageName);
                request.setType(type);
                request.setToken(token);

                manager.execute(request, packageName + "-getPurchases-"+type, DurationInMillis.ONE_MINUTE, new RequestListener<IabPurchasesJson>() {
                    @Override
                    public void onRequestFailure(SpiceException spiceException) {
                        result.putInt(RESPONSE_CODE, RESULT_ERROR);

                        latch.countDown();
                    }

                    @Override
                    public void onRequestSuccess(IabPurchasesJson response) {
                        if("OK".equals(response.getStatus())) {

                            ArrayList<String> purchaseItemList = (ArrayList<String>) response.getPublisher_response().getItemList();
                            ArrayList<String> purchaseSignatureList = (ArrayList<String>) response.getPublisher_response().getSignatureList();

                            ArrayList<String> purchaseDataList = new ArrayList<String>();
                            for(IabPurchasesJson.PublisherResponse.PurchaseDataObject purchase : response.getPublisher_response().getPurchaseDataList()) {
                                Log.d("AptoideBillingService", "Purchase: " + purchase.getJson());
                                purchaseDataList.add(purchase.getJson());
                            }

                            result.putStringArrayList(INAPP_PURCHASE_ITEM_LIST, purchaseItemList);
                            result.putStringArrayList(INAPP_PURCHASE_DATA_LIST, purchaseDataList);
                            result.putStringArrayList(INAPP_DATA_SIGNATURE_LIST, purchaseSignatureList);
                            if(response.getPublisher_response().getInapp_continuation_token() != null) {
                                result.putString(INAPP_CONTINUATION_TOKEN, response.getPublisher_response().getInapp_continuation_token());
                            }
                            result.putInt(RESPONSE_CODE, RESULT_OK);
                        } else {
                            result.putInt(RESPONSE_CODE, RESULT_DEVELOPER_ERROR);
                        }
                        latch.countDown();
                    }
                });

                latch.await();


            }

        } catch (Exception e) {
            e.printStackTrace();
            result.putInt(RESPONSE_CODE, RESULT_ERROR);
        }
        return result;
    }


    @Override
    public int consumePurchase(int apiVersion, String packageName, String purchaseToken) throws RemoteException {
        Log.d("AptoideBillingService", "[consumePurchase]: " + packageName + " " + purchaseToken);

        if(apiVersion < 3) {
            return RESULT_DEVELOPER_ERROR;
        }

        try {
            AccountManager accountManager = AccountManager.get(context);
            String token = accountManager.blockingGetAuthToken(accountManager.getAccountsByType("cm.aptoide.pt.dev")[0], "Full access", true);

            final int[] result = {RESULT_OK};
            if(token != null) {
                IabConsumeRequest request = new IabConsumeRequest();
                request.setApiVersion(Integer.toString(apiVersion));
                request.setToken(token);
                request.setPackageName(packageName);
                request.setPurchaseToken(purchaseToken);

                IabConsumeJson response = request.loadDataFromNetwork();
                final CountDownLatch latch = new CountDownLatch(1);
                manager.execute(request, packageName + "-"+purchaseToken, DurationInMillis.ONE_MINUTE, new RequestListener<IabConsumeJson>() {
                    @Override
                    public void onRequestFailure(SpiceException spiceException) {
                        latch.countDown();
                    }

                    @Override
                    public void onRequestSuccess(IabConsumeJson response) {
                        if("OK".equals(response.getStatus())) {
                            result[0] =  RESULT_OK;
                        }
                        latch.countDown();

                    }
                });
                latch.await();
                return result[0];
            }




        } catch (OperationCanceledException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (AuthenticatorException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return RESULT_ITEM_NOT_OWNED;
    }
}
