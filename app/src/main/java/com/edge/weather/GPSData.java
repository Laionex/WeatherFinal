package com.edge.weather;

import java.io.Serializable;

/**
 * Created by kim on 2017. 5. 27..
 */

public class GPSData implements Serializable {
    double longitude;
    double latitude;

    public GPSData(double longitude, double latitude) {
        this.longitude = longitude;
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public double getLatitude() {
        return latitude;
    }
}
