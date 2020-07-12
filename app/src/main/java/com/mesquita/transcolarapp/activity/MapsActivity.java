package com.mesquita.transcolarapp.activity;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.RoundCap;
import com.google.maps.android.PolyUtil;
import com.mesquita.transcolarapp.R;
import com.mesquita.transcolarapp.config.Permissao;
import com.mesquita.transcolarapp.factory.GeocodingFactory;
import com.mesquita.transcolarapp.factory.RouteFactory;
import com.mesquita.transcolarapp.model.Motorista;
import com.mesquita.transcolarapp.model.Responsavel;
import com.mesquita.transcolarapp.parser.DataParser;
import com.mesquita.transcolarapp.parser.GeocodingParser;
import com.mesquita.transcolarapp.utils.LocationUtil;
import com.mesquita.transcolarapp.utils.MathUtil;
import com.mesquita.transcolarapp.utils.NetworkUtils;
import com.mesquita.transcolarapp.utils.PolylineUtil;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {
    private GoogleMap mMap;
    private List<LatLng> points;
    private boolean hasStartedRoute;
    private Circle userPosition;
    private Polyline route;
    private List<LatLng> allPoints;
    private Motorista motora;

    private LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            Toast.makeText(getApplicationContext(), "Mudou localização", Toast.LENGTH_SHORT).show();

            if (hasStartedRoute) {
                LatLng myLatLngLocation = new LatLng(location.getLatitude(), location.getLongitude());
                Toast.makeText(getApplicationContext(), "Mudou localização: " + location.getLatitude(), Toast.LENGTH_SHORT).show();

                if (!PolyUtil.isLocationOnPath(myLatLngLocation, allPoints, false, 10)) {
                    Toast.makeText(getApplicationContext(), "fora da rota", Toast.LENGTH_SHORT).show();
                    plotRouteOnMap();
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(myLatLngLocation, mMap.getCameraPosition().zoom));
                } else {
                    Toast.makeText(getApplicationContext(), "dentro da rota", Toast.LENGTH_SHORT).show();
                    LatLng myNearestLocation = PolylineUtil.nearestPositionInLine(myLatLngLocation, allPoints);
                    final int positionStartInList = allPoints.indexOf(userPosition.getCenter());
                    final int finalPosition = allPoints.indexOf(myNearestLocation);
                    userPosition.setCenter(myNearestLocation);
//                    final Handler handler = new Handler();
//                    handler.postDelayed(new Runnable() {
//                        int currentPosition = positionStartInList;
//
//                        @Override
//                        public void run() {
//                            userPosition.setCenter(allPoints.get(currentPosition++));
//                            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(allPoints.get(currentPosition), mMap.getCameraPosition().zoom), 400, null);
//                            if (currentPosition != finalPosition) {
//                                handler.postDelayed(this, 50);
//                            }
//                        }
//                    }, 50);
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(myNearestLocation, mMap.getCameraPosition().zoom));
                }
            }
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
        }

        @Override
        public void onProviderEnabled(String provider) {
        }

        @Override
        public void onProviderDisabled(String provider) {
        }
    };

    private GoogleMap.OnCameraMoveListener cameraMoveListener = new GoogleMap.OnCameraMoveListener() {
        @Override
        public void onCameraMove() {
            if (userPosition != null) {
                final double radius = MathUtil.calculateCircleRadiusMeterForMapCircle(7, userPosition.getCenter().latitude, mMap.getCameraPosition().zoom);
                userPosition.setRadius(radius);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_maps);
        findViewById(R.id.route_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                plotRouteOnMap();
                v.setEnabled(false);
            }
        });
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Permissao.validarPermissoes(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, this, 1);
        }
        else
        {
            startMap();
        }
    }

    private void startMap()
    {
        motora = (Motorista) getIntent().getSerializableExtra("mot");
        String url = GeocodingFactory.generateGeocodingUrl("91360-001", 77, LocationUtil.getCurrentLocation(this), getString(R.string.google_maps_key));
        try {
            //Get json from API
            String response = new NetworkUtils().execute(url).get();
            GeocodingParser parser = new GeocodingParser();
            JSONObject jsonObject = new JSONObject(response);
            LatLng location = parser.parse(jsonObject);
        } catch (Exception e)
        {

        }

        //Obtain the LocationManager and get notified of location updates
        LocationManager manager = (LocationManager) this.getSystemService(this.LOCATION_SERVICE);
        Criteria mCriteria = new Criteria();
        String bestProvider = String.valueOf(manager.getBestProvider(mCriteria, true));
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        manager.requestLocationUpdates(bestProvider, 3000, 0, locationListener);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    private void alertaValidacaoPermissao() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Permissões Negadas");
        builder.setMessage("Para utilizar o app é necessário aceitar as permissões de localização.");
        builder.setCancelable(false);
        builder.setPositiveButton("Confirmar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                finish();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

            if (grantResults[0] == PackageManager.PERMISSION_DENIED)
            {
                alertaValidacaoPermissao();
            }
            else {
               startMap();
            }

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        //Prepare Map
        //Set a button center on user location
        //Move camera to current user location
        mMap = googleMap;
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mMap.setMyLocationEnabled(true);
        mMap.setOnCameraMoveListener(cameraMoveListener);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(LocationUtil.getCurrentLocation(this), 15));
    }

    //Plot route on Map
    private void plotRouteOnMap() {
        LatLng[] waypoints = new LatLng[motora.getClientes().size()];
        int z = 0;
        for (Responsavel resp: motora.getClientes()) {
            waypoints[z] = new LatLng(resp.getLatitude(), resp.getLongitude());
            z++;
        }

        LatLng myLatLngLocation = LocationUtil.getCurrentLocation(this);
        LatLng mySchoolLocation = new LatLng(-30.0116, -51.1536);

        //Generate a route URL with waypoints
        String urlStr = RouteFactory.generateRouteWithWaypointsUrl(myLatLngLocation, mySchoolLocation, waypoints, "driving", getString(R.string.google_maps_key));

        try {
            //Get json from API
            String response = new NetworkUtils().execute(urlStr).get();
            DataParser parser = new DataParser();
            // Starts parsing data
            try {
                //Parse response from JSON to route Object
                List<List<HashMap<String, String>>> routes = parser.parse(new JSONObject(response));
                PolylineOptions lineOptions = null;
                CircleOptions circleOptions = new CircleOptions();
                hasStartedRoute = false;

                //Loop through all routes
                for (int i = 0; i < routes.size(); i++) {
                    points = new ArrayList<>();
                    lineOptions = new PolylineOptions();
                    // Fetching i-th route
                    List<HashMap<String, String>> path = routes.get(i);
                    // Loop through points on the i-th route and add its points to the point list
                    for (int j = 0; j < path.size(); j++) {
                        HashMap<String, String> point = path.get(j);
                        double lat = Double.parseDouble(point.get("lat"));
                        double lng = Double.parseDouble(point.get("lng"));
                        LatLng position = new LatLng(lat, lng);
                        points.add(position);
                    }
                    // Adding all the points in the route to LineOptions
                    lineOptions.addAll(points);
                    lineOptions.width(20);
                    lineOptions.startCap(new RoundCap());
                    lineOptions.endCap(new RoundCap());
                    lineOptions.color(Color.parseColor("#3660A4"));
                }

                //Create a list for all points from the first route object and for the ones that are 0.1 meter apart
                allPoints = new ArrayList<LatLng>();

                for (int i = 0; i < points.size(); i++) {
                    //Get the current point from the first list
                    LatLng src = new LatLng(points.get(i).latitude, points.get(i).longitude);

                    //Check wheater i+1 is less than the first list size
                    if (points.size() > i + 1) {
                        //If true, get the next point and create a list of points 0.1 meter apart
                        LatLng dest = new LatLng(points.get(i + 1).latitude, points.get(i + 1).longitude);
                        List<LatLng> splitPoints = PolylineUtil.splitPathIntoPoints(src, dest);
                        allPoints.addAll(splitPoints);
                    } else {
                        //If false, exit the loop
                        break;
                    }
                }

                //Create a circle to represent the user position
                //Plot it on the first position of the route
                circleOptions.center(PolylineUtil.nearestPositionInLine(myLatLngLocation, allPoints));
                circleOptions.radius(1);
                circleOptions.strokeColor(0XFFF1F1F1);
                circleOptions.fillColor(0xFF00AFEC);
                circleOptions.zIndex(999999);
                //lineOptions.addAll(allPoints);

                //Clear previous objects on map
                //Add route to the map
                //Add user representation to the map
                //Disable button to set camera on user location
                //Move camera to user position with an animation
                mMap.clear();
                route = mMap.addPolyline(lineOptions);
                userPosition = mMap.addCircle(circleOptions);
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }
                mMap.setMyLocationEnabled(false);
                desenharEscola(mySchoolLocation);
                desenharWayPoints(waypoints);
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(myLatLngLocation, mMap.getCameraPosition().zoom));
                hasStartedRoute = true;
            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void desenharEscola(LatLng schoolLocation){
        mMap.addMarker(new MarkerOptions().position(schoolLocation)
                                            .title("Escola Mesquita")
        .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_school_64px)));
    }

    private void desenharWayPoints(LatLng[] waypoints){
        for (int i=0; i<waypoints.length; i++) {
            mMap.addMarker(new MarkerOptions().position(waypoints[i])
                    .title("Casa " + i)
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_house_64px)));
        }
    }
}
