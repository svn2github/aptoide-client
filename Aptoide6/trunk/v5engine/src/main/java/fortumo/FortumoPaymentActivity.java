package fortumo;

import android.os.Bundle;
import android.util.Log;

import cm.aptoide.ptdev.R;
import mp.MpUtils;
import mp.PaymentActivity;
import mp.PaymentRequest;
import openiab.webservices.json.PaymentServices;

public class FortumoPaymentActivity extends PaymentActivity {

    public static final String EXTRA_PACKAGE = "EPAGE";
    public static final String EXTRA_ISCONSUMABLE = "ISCONSUMABLE";
    public static final String EXTRA_PAYMENTSERVICE = "EPS";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fortumopayment);
        Log.d("pois", "oncreate FortumoPaymentActivity!");
        PaymentServices ps = getIntent().getParcelableExtra(EXTRA_PAYMENTSERVICE);
        MpUtils.enablePaymentBroadcast(this, "cm.aptoide.ptdev.PAYMENT_BROADCAST_PERMISSION");

        PaymentRequest.PaymentRequestBuilder builder = new PaymentRequest.PaymentRequestBuilder();
        Log.d("pois", "FortumoPaymentActivity Log:");
        Log.d("pois", "price: "+ps.price);
        Log.d("pois", "service_id: "+ps.service_id);
        Log.d("pois", "inapp_secret: "+ps.inapp_secret);
        String packageName=getIntent().getStringExtra(EXTRA_PACKAGE);
        Log.d("pois", "packageName: "+packageName);
        if(packageName==null)
            Log.e("FortumoPaymentActivity","no packageName sent");

        builder.setService(ps.service_id, ps.inapp_secret);
        builder.setDisplayString(ps.getName());      // shown on user receipt
        builder.setProductName(packageName);  // non-consumable purchases are restored using this value
        builder.setType(MpUtils.PRODUCT_TYPE_NON_CONSUMABLE);              // non-consumable items can be later restored
        builder.setIcon(R.drawable.icon_brand_aptoide);
        PaymentRequest pr = builder.build();
        makePayment(pr);
        Log.d("pois", "makePayment feito!");
    }
}
