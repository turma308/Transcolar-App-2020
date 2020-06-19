package com.mesquita.transcolarapp.parser;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONObject;

public class GeocodingParser
{
    /**
     *
     * @param jObject
     * The JObject created from api response
     * @return
     * The parsed location
     */
    public LatLng parse(JSONObject jObject)
    {
        LatLng location = null;
        try
        {
            JSONArray resultArr = jObject.getJSONArray("results");
            JSONObject tempObj = resultArr.getJSONObject(0);
            JSONObject geometry = tempObj.getJSONObject("geometry");
            JSONObject locationJson = geometry.getJSONObject("location");
            location = new LatLng(locationJson.getDouble("lat"), locationJson.getDouble("lng"));
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return location;
    }
}
