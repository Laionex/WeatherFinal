package com.edge.weather;

import com.google.gson.internal.LinkedTreeMap;

import java.util.Comparator;

/**
 * Created by kim on 2017. 5. 15..
 */

public class ServiceEvent implements Comparator<ServiceEvent>{
    long timeInMillis;
    String data;
    GPSData gpsData;
    public ServiceEvent() {
    }

    public ServiceEvent(long timeInMillis, String data, GPSData gpsData) {
        this.timeInMillis = timeInMillis;
        this.data = data;
        this.gpsData = gpsData;
    }


    public GPSData getGpsData() {
        return gpsData;
    }

    public void setGpsData(GPSData gpsData) {
        this.gpsData = gpsData;
    }

    public long getTimeInMillis() {
        return timeInMillis;
    }

    public void setTimeInMillis(long timeInMillis) {
        this.timeInMillis = timeInMillis;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    @Override
    public int compare(ServiceEvent o1, ServiceEvent o2) {
        if (o1.getTimeInMillis() > o2.getTimeInMillis()) {
            return 1;
        }
        else if (o1.getTimeInMillis() <  o2.getTimeInMillis()) {
            return -1;
        }
        else {
            return 0;
        }
    }
}
