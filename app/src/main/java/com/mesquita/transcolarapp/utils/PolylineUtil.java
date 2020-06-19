package com.mesquita.transcolarapp.utils;

import android.location.Location;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.List;

import static java.lang.Math.toDegrees;
import static java.lang.Math.toRadians;

public class PolylineUtil
{

    //This method find the nearest position in the polyline
    public static LatLng nearestPositionInLine(final LatLng position, List<LatLng> points)
    {
        double minimumDistance = -1;
        LatLng nearest = null;

        //Loop for each point and measure the distance
        for(LatLng point : points)
        {
            double distance = findDistance(position, point);
            //Check if distance is less than the minimum
            if(distance < minimumDistance || minimumDistance == -1)
            {
                minimumDistance = distance;
                nearest = point;
            }
        }
        return nearest;
    }

    //Create a new list of points that are 0.1 meter apart
    public static List<LatLng> splitPathIntoPoints(LatLng source, LatLng destination)
    {
        Float distance = findDistance(source, destination);

        List<LatLng> splitPoints = new ArrayList<>();
        splitPoints.add(source);
        splitPoints.add(destination);

        //Keep doing it if the distance between points is greater than 0.1 meter
        while (distance > 0.1)
        {
            int polypathSize = splitPoints.size();
            List<LatLng> tempPoints = new ArrayList<>();
            tempPoints.addAll(splitPoints);

            int injectionIndex = 1;

            //Loop each point on the list
            for (int i = 0; i < (polypathSize - 1); i++)
            {
                //Get point A and B
                LatLng a = tempPoints.get(i);
                LatLng b = tempPoints.get(i + 1);

                //Get middle point between A and B an then add it to the new List
                splitPoints.add(injectionIndex, findMidPoint(a, b));
                injectionIndex += 2;
            }

            distance = findDistance(splitPoints.get(0), splitPoints.get(1));
        }

        return splitPoints;
    }

    //I copied this method from internet
    //This method calculate a point between A and B
    public static LatLng findMidPoint(LatLng source, LatLng destination)
    {
        double x1 = toRadians(source.latitude);
        double y1 = toRadians(source.longitude);

        double x2 = toRadians(destination.latitude);
        double y2 = toRadians(destination.longitude);

        double Bx = Math.cos(x2) * Math.cos(y2 - y1);
        double By = Math.cos(x2) * Math.sin(y2 - y1);
        double x3 = toDegrees(Math.atan2(Math.sin(x1) + Math.sin(x2), Math.sqrt((Math.cos(x1) + Bx) * (Math.cos(x1) + Bx) + By * By)));
        double y3 = y1 + Math.atan2(By, Math.cos(x1) + Bx);
        y3 = toDegrees((y3 + 540) % 360 - 180);

        return new LatLng(x3, y3);
    }

    //This Method finds the distance between two given points
    public static Float findDistance(LatLng source, LatLng destination)
    {
        Location srcLoc = new Location("srcLoc");
        srcLoc.setLatitude(source.latitude);
        srcLoc.setLongitude(source.longitude);

        Location destLoc = new Location("destLoc");
        destLoc.setLatitude(destination.latitude);
        destLoc.setLongitude(destination.longitude);

        return srcLoc.distanceTo(destLoc);
    }

}
