/*
 * This file is auto-generated.  DO NOT MODIFY.
 * Original file: /home/asantos/Aptoide6/Aptoidefortumo/src/main/aidl/cm/aptoide/ptdev/IRemoteFortumoPayment.aidl
 */
package cm.aptoide.ptdev;
public interface IRemoteFortumoPayment extends android.os.IInterface
{
/** Local-side IPC implementation stub class. */
public static abstract class Stub extends android.os.Binder implements cm.aptoide.ptdev.IRemoteFortumoPayment
{
private static final java.lang.String DESCRIPTOR = "cm.aptoide.ptdev.IRemoteFortumoPayment";
/** Construct the stub at attach it to the interface. */
public Stub()
{
this.attachInterface(this, DESCRIPTOR);
}
/**
 * Cast an IBinder object into an cm.aptoide.ptdev.IRemoteFortumoPayment interface,
 * generating a proxy if needed.
 */
public static cm.aptoide.ptdev.IRemoteFortumoPayment asInterface(android.os.IBinder obj)
{
if ((obj==null)) {
return null;
}
android.os.IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
if (((iin!=null)&&(iin instanceof cm.aptoide.ptdev.IRemoteFortumoPayment))) {
return ((cm.aptoide.ptdev.IRemoteFortumoPayment)iin);
}
return new cm.aptoide.ptdev.IRemoteFortumoPayment.Stub.Proxy(obj);
}
@Override public android.os.IBinder asBinder()
{
return this;
}
@Override public boolean onTransact(int code, android.os.Parcel data, android.os.Parcel reply, int flags) throws android.os.RemoteException
{
switch (code)
{
case INTERFACE_TRANSACTION:
{
reply.writeString(DESCRIPTOR);
return true;
}
case TRANSACTION_getMessage:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _arg0;
_arg0 = data.readString();
boolean _arg1;
_arg1 = (0!=data.readInt());
int _arg2;
_arg2 = data.readInt();
java.lang.String _arg3;
_arg3 = data.readString();
java.lang.String _arg4;
_arg4 = data.readString();
java.lang.String _arg5;
_arg5 = data.readString();
android.os.Messenger _arg6;
if ((0!=data.readInt())) {
_arg6 = android.os.Messenger.CREATOR.createFromParcel(data);
}
else {
_arg6 = null;
}
this.getMessage(_arg0, _arg1, _arg2, _arg3, _arg4, _arg5, _arg6);
reply.writeNoException();
return true;
}
}
return super.onTransact(code, data, reply, flags);
}
private static class Proxy implements cm.aptoide.ptdev.IRemoteFortumoPayment
{
private android.os.IBinder mRemote;
Proxy(android.os.IBinder remote)
{
mRemote = remote;
}
@Override public android.os.IBinder asBinder()
{
return mRemote;
}
public java.lang.String getInterfaceDescriptor()
{
return DESCRIPTOR;
}
@Override public void getMessage(java.lang.String ID, boolean isConsumable, int userId, java.lang.String PAYMENTSERVICE_ID, java.lang.String PAYMENTSERVICE_INAPPSECRET, java.lang.String PAYMENTSERVICE_NAME, android.os.Messenger msger) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(ID);
_data.writeInt(((isConsumable)?(1):(0)));
_data.writeInt(userId);
_data.writeString(PAYMENTSERVICE_ID);
_data.writeString(PAYMENTSERVICE_INAPPSECRET);
_data.writeString(PAYMENTSERVICE_NAME);
if ((msger!=null)) {
_data.writeInt(1);
msger.writeToParcel(_data, 0);
}
else {
_data.writeInt(0);
}
mRemote.transact(Stub.TRANSACTION_getMessage, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
}
static final int TRANSACTION_getMessage = (android.os.IBinder.FIRST_CALL_TRANSACTION + 0);
}
public void getMessage(java.lang.String ID, boolean isConsumable, int userId, java.lang.String PAYMENTSERVICE_ID, java.lang.String PAYMENTSERVICE_INAPPSECRET, java.lang.String PAYMENTSERVICE_NAME, android.os.Messenger msger) throws android.os.RemoteException;
}
