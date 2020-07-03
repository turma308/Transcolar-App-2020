package com.mesquita.transcolarapp.services;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

import com.android.volley.RequestQueue;
import com.google.android.gms.maps.model.LatLng;
import com.mesquita.transcolarapp.R;
import com.mesquita.transcolarapp.activity.NotificacaoActivity;
import com.mesquita.transcolarapp.utils.LocationUtil;

import org.json.JSONObject;

public class TrackUserLocationService extends Service
{
    private final IBinder mBinder = new TrackUserLocationServiceBinder();
    private LocationManager mLocationManager;
    private LatLng schoolLocation;

    public TrackUserLocationService()
    {

    }

    @Override
    public IBinder onBind(Intent intent)
    {
        if (intent.hasExtra("schoolLocation"))
        {
            this.schoolLocation = (LatLng) intent.getExtras().getParcelable("schoolLocation");
            startTrackingLocation();
        }
        return mBinder;
    }

    //Start tracking the user location
    private void startTrackingLocation()
    {
        Criteria mCriteria = new Criteria();
        mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        String bestProvider = String.valueOf(mLocationManager.getBestProvider(mCriteria, true));
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            return;
        mLocationManager.requestLocationUpdates(bestProvider, 5, 1, locationListener);
    }

    private void stopTrackingLocation()
    {
        mLocationManager.removeUpdates(locationListener);
    }

    //Sends a local notification for testing purpose
    //Needs to be changed to firebase notification
    private void sendNotification()
    {
        String canal = getString(R.string.default_notification_channel_id);
        Uri uriSom = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        NotificationCompat.Builder notif = new NotificationCompat.Builder(getApplicationContext(), canal)
                .setContentTitle("Cheguei!")
                .setContentText("Seu filho acabou de chegar na escola.")
                .setSmallIcon(R.drawable.ic_notifi_transcolar)
                .setSound(uriSom)
                .setAutoCancel(true);

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(canal, "canal", NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(channel);
        }

        notificationManager.notify(0, notif.build());
    }

    private LocationListener locationListener = new LocationListener()
    {
        @Override
        public void onLocationChanged(Location location)
        {
            float distance = LocationUtil.findDistance(new LatLng(location.getLatitude(), location.getLongitude()), schoolLocation);
            if(distance <= 10)
            {
                sendNotification();
                stopTrackingLocation();
            }
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras)
        {
        }

        @Override
        public void onProviderEnabled(String provider)
        {
        }

        @Override
        public void onProviderDisabled(String provider)
        {
        }
    };

    //Simple binder that binds the service
    public class TrackUserLocationServiceBinder extends Binder
    {
        public TrackUserLocationService getService()
        {
            return TrackUserLocationService.this;
        }
    }
}
