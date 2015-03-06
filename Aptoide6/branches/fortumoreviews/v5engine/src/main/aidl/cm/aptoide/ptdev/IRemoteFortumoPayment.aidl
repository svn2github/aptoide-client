package cm.aptoide.ptdev;

interface IRemoteFortumoPayment {

	void getMessage(String ID, boolean isConsumable, int userId,
                                        String PAYMENTSERVICE_ID,
                                        String PAYMENTSERVICE_INAPPSECRET,
                                        String PAYMENTSERVICE_NAME,
                                        in Messenger msger);

}