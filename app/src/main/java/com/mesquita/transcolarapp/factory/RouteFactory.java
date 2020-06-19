package com.mesquita.transcolarapp.factory;

import com.google.android.gms.maps.model.LatLng;

public class RouteFactory
{
    //Generate route without WayPoints
    public static String generateRouteUrl(LatLng orig, LatLng dest, String directionMode, String key)
    {
        String origStr = String.format("origin=%s,%s", orig.latitude, orig.longitude);
        String destStr = String.format("destination=%s,%s", dest.latitude, dest.longitude);
        String mode = String.format("mode=%s", directionMode);
        String params = String.format("%s&%s&%s", origStr, destStr, mode);
        String output = "json";
        return String.format("https://maps.googleapis.com/maps/api/directions/%s?%s&key=%s", output, params, key);
    }

    //Generate route with WayPoints on it
    public static String generateRouteWithWaypointsUrl(LatLng orig, LatLng dest, LatLng[] waypoints, String directionMode, String key)
    {
        String origStr = String.format("origin=%s,%s", orig.latitude, orig.longitude);
        String destStr = String.format("destination=%s,%s", dest.latitude, dest.longitude);
        String waypointsStr = "waypoints=optimize:true%7C";
        for (LatLng waypoint: waypoints)
        {
            waypointsStr += waypoint.latitude+","+waypoint.longitude+"%7C";
        }
        waypointsStr = waypointsStr.substring(0, waypointsStr.length() - 3);
        String mode = String.format("mode=%s", directionMode);
        String params = String.format("%s&%s&%s&%s", origStr, destStr, mode, waypointsStr);
        String output = "json";
        return String.format("https://maps.googleapis.com/maps/api/directions/%s?%s&key=%s", output, params, key);
    }
}
