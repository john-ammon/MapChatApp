package edu.temple.mapchatapp;

import android.os.Parcel;
import android.os.Parcelable;

public class Partner implements Comparable, Parcelable {

    int mData;
    String user;
    double lat;
    double lon;
    double distance;

    public Partner(String user, double lat, double lon, double distance) {
        this.user = user;
        this.lat = lat;
        this.lon = lon;
        this.distance = distance;
    }

    public double getDistance() {
        return this.distance;
    }

    //comparable interface
    @Override
    public int compareTo(Object o) {
        Partner partner = (Partner) o;

        return (int) Math.round(this.getDistance() - partner.getDistance());
    }

    //parcelable interface
    public Partner(Parcel in) {
        this.user = in.readString();
        this.lat = in.readDouble();
        this.lon = in.readDouble();
    }
    public static final Parcelable.Creator<Partner> CREATOR
            = new Parcelable.Creator<Partner>() {
        public Partner createFromParcel(Parcel in) { return new Partner(in); }
        public Partner[] newArray(int size) { return new Partner[size]; }
    };
    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(mData);
    }
    @Override
    public int describeContents() {
        return 0;
    }


}
