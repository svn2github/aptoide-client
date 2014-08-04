package com.aptoide.openiab.webservices.json;

import android.os.Parcel;
import android.os.Parcelable;
import com.google.api.client.util.Key;

import java.util.ArrayList;

/**
 * Created by rmateus on 21-05-2014.
 */
public class PaymentServices implements Parcelable {

    @Key
    private int id;
    @Key private String short_name;
    @Key private String name;
    @Key private ArrayList<PaymentType> types;
    @Key private double price;
    @Key private String currency;
    @Key private double taxRate;
    @Key private String sign;

    public double getPrice() {
        return price;
    }

    public String getCurrency() {
        return currency;
    }

    public double getTaxRate() {
        return taxRate;
    }

    public String getSign() {
        return sign;
    }

    public ArrayList<PaymentType> getTypes() {
        return types;
    }

    public int getId() {
        return id;
    }

    public String getShort_name() {
        return short_name;
    }

    public String getName() {
        return name;
    }


    public void writeToParcel(Parcel out, int flags) {
        out.writeInt(id);
        out.writeString(short_name);
        out.writeString(name);
        out.writeList(types);

    }

    public static final Creator<PaymentServices> CREATOR
            = new Creator<PaymentServices>() {
        public PaymentServices createFromParcel(Parcel in) {
            return new PaymentServices(in);
        }

        public PaymentServices[] newArray(int size) {
            return new PaymentServices[size];
        }
    };

    public PaymentServices(){

    }

    private PaymentServices(Parcel in) {
        id = in.readInt();
        short_name = in.readString();
        name = in.readString();
        in.readList(types, getClass().getClassLoader());
    }


    @Override
    public int describeContents() {
        return 0;
    }



    public static class PaymentType implements Parcelable{

        public String getReqType() {
            return reqType;
        }

        public String getLabel() {
            return label;
        }

        @Key String reqType;
        @Key String label;

        public PaymentType(){

        }

        public void writeToParcel(Parcel out, int flags) {
            out.writeString(reqType);
            out.writeString(label);

        }

        public static final Creator<PaymentType> CREATOR
                = new Creator<PaymentType>() {
            public PaymentType createFromParcel(Parcel in) {
                return new PaymentType(in);
            }

            public PaymentType[] newArray(int size) {
                return new PaymentType[size];
            }
        };

        private PaymentType(Parcel in) {
            reqType = in.readString();
            label = in.readString();
        }


        @Override
        public int describeContents() {
            return 0;
        }


    }
}
