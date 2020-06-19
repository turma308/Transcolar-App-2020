package com.mesquita.transcolarapp.utils;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;

import androidx.core.app.ActivityCompat;

import com.google.android.gms.maps.model.LatLng;

public class LocationUtil
{
    /**
     *
     * @param context
     * The activity context. Used to location manager
     * @return
     * The current location
     */
    public static LatLng getCurrentLocation(Context context)
    {
        LocationManager manager = (LocationManager) context.getSystemService(context.LOCATION_SERVICE);
        Criteria mCriteria = new Criteria();
        String bestProvider = String.valueOf(manager.getBestProvider(mCriteria, true));

        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            return null;

        Location mLocation = manager.getLastKnownLocation(bestProvider);
        if (mLocation != null)
        {
            final double currentLatitude = mLocation.getLatitude();
            final double currentLongitude = mLocation.getLongitude();
            LatLng currentLocation = new LatLng(currentLatitude, currentLongitude);
            return currentLocation;
        }
        else
        {
            return null;
        }
    }
}
