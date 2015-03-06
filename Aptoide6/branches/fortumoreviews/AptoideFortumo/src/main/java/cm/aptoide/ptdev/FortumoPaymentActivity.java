package cm.aptoide.ptdev;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

import mp.MpUtils;
import mp.PaymentActivity;
import mp.PaymentRequest;

public class FortumoPaymentActivity extends PaymentActivity {
    private static final boolean LOG = false;
    private static final String TAG = "pois";

    /* THIS 3 are used on BasePurchaseActivity COPY/PASTE there*/
    private static final String RESULT_EXTRA_ISCONSUMABLE_BOOL = "IEIC";
    private static final String RESULT_EXTRA_PAYCODE_STRING = "IEPC";
    private static final String RESULT_EXTRA_PRODID_INT = "IEPID";
    /* THIS 3 are used on BasePurchaseActivity COPY/PASTE there*/


    public static final String ISCONSUMABLECHAR = "C";
    public static final String NONCONSUMABLECHAR = "N";
    //public static final String EXTRA_USER = "EUSER";
    //public static final String EXTRA_PACKAGE = "EPAGE";
    //public static final String EXTRA_REPO = "EREPO";

    public static final String EXTRA_ISCONSUMABLE = "ISCONSUMABLE";
    public static final String EXTRA_ID = "ID";
    public static final String EXTRA_USER_ID = "USERID";
    public static final String EXTRA_PAYMENTSERVICE_ID = "EPSID";
    public static final String EXTRA_PAYMENTSERVICE_INAPPSECRET = "EPSIAS";
    public static final String EXTRA_PAYMENTSERVICE_NAME = "EPSNAME";
    public static final String EXTRA_PARCELABLE_MSGER  = "EPARCEMSG";

    private MyReceiver receiver;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final Messenger msger = getIntent().getParcelableExtra(EXTRA_PARCELABLE_MSGER);
        registerReceiver(receiver=new MyReceiver(), new IntentFilter("mp.info.PAYMENT_STATUS_CHANGED"));
        receiver.setCallBack(new MyReceiver.CallBack() {
            @Override
            public void onReceive(boolean wasPaid, boolean isconsumable, String payCode, int prodID) {
                if(LOG){Log.d(TAG, "wasPaid: "+wasPaid);}
                try {
                    Bundle b = new Bundle();
                    b.putBoolean(RESULT_EXTRA_ISCONSUMABLE_BOOL,isconsumable);
                    b.putString(RESULT_EXTRA_PAYCODE_STRING,payCode);
                    b.putInt(RESULT_EXTRA_PRODID_INT,prodID);
                    Message msg = Message.obtain();
                    msg.setData(b);
                    msger.send(msg);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }finally {
                    finish();
                }
            }
        });

        setContentView(R.layout.activity_fortumopayment);
        MpUtils.enablePaymentBroadcast(this, "cm.aptoide.ptdev.PAYMENT_BROADCAST_PERMISSION");

        PaymentRequest.PaymentRequestBuilder builder = new PaymentRequest.PaymentRequestBuilder();
        final int UserID = getIntent().getIntExtra(EXTRA_USER_ID, 0);
        boolean isconsumable=getIntent().getBooleanExtra(EXTRA_ISCONSUMABLE, true);
        String productName = getIntent().getStringExtra(EXTRA_ID) +
                '_' +
                String.valueOf(UserID) +
                '_' +
                (isconsumable ? ISCONSUMABLECHAR:NONCONSUMABLECHAR);
        if(LOG){Log.d(TAG, "setProductName: "+productName);}

        builder.setService(getIntent().getStringExtra(EXTRA_PAYMENTSERVICE_ID),
                getIntent().getStringExtra(EXTRA_PAYMENTSERVICE_INAPPSECRET));
        builder.setDisplayString(getIntent().getStringExtra(EXTRA_PAYMENTSERVICE_NAME));      // shown on user receipt
        builder.setProductName(productName);  // non-consumable purchases are restored using this value
        builder.setType(isconsumable?MpUtils.PRODUCT_TYPE_CONSUMABLE : MpUtils.PRODUCT_TYPE_NON_CONSUMABLE); // non-consumable items can be later restored
        builder.setIcon(R.drawable.icon_brand_aptoide);

        PaymentRequest pr = builder.build();
        makePayment(pr);
        if(LOG){Log.d(TAG, "makePayment feito!");}
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        finish();
        if(receiver!=null) unregisterReceiver(receiver);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    public static class MyReceiver extends BroadcastReceiver{
        public void setCallBack(CallBack callBack) {
            this.callBack = callBack;
        }

        public interface CallBack{
            public void onReceive(boolean wasPaid,boolean isconsumable,String payCode,int prodID);
        }

        private CallBack callBack;

        @Override
        public void onReceive(Context context, Intent intent) {
            if(LOG){Log.d(TAG, "MyReceiver onReceive");}
            Bundle extras = intent.getExtras();

            final String product_name =  extras.getString("product_name");
            final boolean isconsumable = product_name.endsWith(FortumoPaymentActivity.ISCONSUMABLECHAR);

/*          Log.d(TAG, "- billing_status:  " + extras.getInt("billing_status"));
            Log.d(TAG, "- credit_amount:   " + extras.getString("credit_amount"));
            Log.d(TAG, "- credit_name:     " + extras.getString("credit_name"));
            Log.d(TAG, "- message_id:      " + extras.getString("message_id") );
            Log.d(TAG, "- payment_code:    " + extras.getString("payment_code"));
            Log.d(TAG, "- price_amount:    " + extras.getString("price_amount"));
            Log.d(TAG, "- price_currency:  " + extras.getString("price_currency"));
            Log.d(TAG, "- service_id:      " + extras.getString("service_id"));
            Log.d(TAG, "- user_id:         " + extras.getString("user_id"));*/

            if(LOG){Log.d(TAG, "- product_name:    " + product_name);}

            if(LOG){Log.d(TAG, "- isconsumable:         " + isconsumable);}

            int billingStatus = extras.getInt("billing_status");
            String payCode = extras.getString("payment_code");
            int prodID = Integer.valueOf(product_name.split("_")[0]);
            if(callBack!=null){
                callBack.onReceive(billingStatus == MpUtils.MESSAGE_STATUS_BILLED, isconsumable, payCode, prodID);
            }
        }
    }
}
