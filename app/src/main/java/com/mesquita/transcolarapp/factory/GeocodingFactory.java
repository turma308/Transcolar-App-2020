package com.mesquita.transcolarapp.factory;

import com.google.android.gms.maps.model.LatLng;

public class GeocodingFactory
{
    /***
     *
     * @param cep
     * formatted as 12345-678
     * @param number
     * Place number in the street
     * @param bounds
     * Bound it to a specific place - DOES NOT RESTRICT RESULT TOTHAT PLACE
     * @param key
     * Google maps key
     * @return
     * An URL to get the LatLng for that place
     */
    public static String generateGeocodingUrl(String cep, int number, LatLng bounds, String key)
    {
        String strNumber = String.valueOf(number);
        //cep = cep.replaceAll("\\s", "%20");
        String params = strNumber+"%20"+cep;
        String boundsStr = bounds.latitude+","+bounds.longitude;
        return String.format("https://maps.googleapis.com/maps/api/geocode/json?address=%s&bounds=%s&key=%s", params, boundsStr, key);
    }
}
