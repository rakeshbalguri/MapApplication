package com.rakesh.mapapplication;

import android.animation.ValueAnimator;
import android.graphics.Color;
import android.graphics.Point;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private final Handler mHandler = new Handler();


    Handler handler = new Handler();
    Random random = new Random();
//    Runnable runner = new Runnable() {
//        @Override
//        public void run() {
//            setHasOptionsMenu(true);
//        }
//    };


    private static GoogleMap googleMap;

    private SupportMapFragment mapFragment;

    private List<Marker> markers = new ArrayList<Marker>();
    private Marker selectedMarker;

    private Polyline polyLine;
    private PolylineOptions rectOptions = new PolylineOptions();

    private static final LatLng LOWER_MANHATTAN = new LatLng(40.722543,
            -73.998585);
    private static final LatLng TIMES_SQUARE = new LatLng(40.7577, -73.9857);
    private static final LatLng BROOKLYN_BRIDGE = new LatLng(40.7057, -73.9964);


    int currentPt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(final GoogleMap googleMap) {
        this.googleMap = googleMap;

        addDefaultLocations();

        final View mapView = mapFragment.getView();

        if (mapView.getViewTreeObserver().isAlive()) {
            mapView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
                        mapView.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                    } else {
                        mapView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    }
                    fixZoomForMarkers(googleMap,markers);
                }
            });
        }

        Marker latlong = markers.get(markers.size()-1);
        Location targetLocation = new Location("");//provider name is unecessary
        targetLocation.setLatitude(latlong.getPosition().latitude);//your coords of course
        targetLocation.setLongitude(latlong.getPosition().longitude);

        Marker startPosition = markers.get(0);
        Marker newStartPosition = googleMap.addMarker(new MarkerOptions().position(startPosition.getPosition()).flat(true).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));

        animateMarker(targetLocation, newStartPosition);
        currentPt = 0-1;

    }

    private void addDefaultLocations() {
        addMarkerToMap(new LatLng(50.961813797827055,3.5168474167585373));
        addMarkerToMap(new LatLng(50.96085423274633,3.517405651509762));
        addMarkerToMap(new LatLng(50.96020550146382,3.5177918896079063));
        addMarkerToMap(new LatLng(50.95936754348453,3.518972061574459));
        addMarkerToMap(new LatLng(50.95877285446026,3.5199161991477013));
        addMarkerToMap(new LatLng(50.958179213755905,3.520646095275879));
        addMarkerToMap(new LatLng(50.95901719316589,3.5222768783569336));
        addMarkerToMap(new LatLng(50.95954430150347,3.523542881011963));
        addMarkerToMap(new LatLng(50.95873336312275,3.5244011878967285));
        addMarkerToMap(new LatLng(50.95955781702322,3.525688648223877));
        addMarkerToMap(new LatLng(50.958855004782116,3.5269761085510254));
    }


    public void addMarkerToMap(LatLng latLng) {
        Marker marker = googleMap.addMarker(new MarkerOptions().position(latLng)
                .title("title")
                .snippet("snippet"));
        markers.add(marker);

    }


    public  void fixZoomForMarkers(GoogleMap googleMap, List<Marker> markers) {
        if (markers!=null && markers.size() > 0) {
            LatLngBounds.Builder bc = new LatLngBounds.Builder();

            for (Marker marker : markers) {
                bc.include(marker.getPosition());
            }

            googleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bc.build(), 50),4000,null);
        }
    }

    /**
     * Method to animate marker to destination location
     * @param destination destination location (must contain bearing attribute, to ensure
     *                    marker rotation will work correctly)
     * @param startLocation marker to be animated
     */
    public void animateMarker(final Location destination, final Marker startLocation) {

        if(++currentPt < markers.size()){

            final LatLng startPosition = startLocation.getPosition();
            final  LatLng endPosition = new LatLng(destination.getLatitude(), destination.getLongitude());

            final  float startRotation = startLocation.getRotation();

            final   LatLngInterpolator latLngInterpolator = new LatLngInterpolator.Spherical();
            ValueAnimator valueAnimator = ValueAnimator.ofFloat(0, 1);
            valueAnimator.setDuration(15000); // duration 1 second
            valueAnimator.setInterpolator(new LinearInterpolator());
            valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override public void onAnimationUpdate(ValueAnimator animation) {
                    try {
                        float v = animation.getAnimatedFraction();
                        LatLng newPosition = latLngInterpolator.interpolate(v, startPosition, endPosition);
                        startLocation.setPosition(newPosition);
                        googleMap
                                .addPolyline((new PolylineOptions())
                                        .add(startPosition, newPosition
                                                ).width(5).color(Color.BLUE)
                                        .geodesic(true));

                        startLocation.setRotation(computeRotation(v, startRotation, destination.getBearing()));


                        // Call Again for the next set

                        Marker latlong = markers.get(currentPt+1);
                        Location targetLocation = new Location("");//provider name is unecessary
                        targetLocation.setLatitude(latlong.getPosition().latitude);//your coords of course
                        targetLocation.setLongitude(latlong.getPosition().longitude);

                        Marker startPosition = markers.get(currentPt);
                        Marker newStartPosition = googleMap.addMarker(new MarkerOptions().position(startPosition.getPosition()).flat(true).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));

                        animateMarker(targetLocation, newStartPosition);




                    } catch (Exception ex) {
                        // I don't care atm..
                    }
                }
            });

            valueAnimator.start();
        }
    }

    private static float computeRotation(float fraction, float start, float end) {
        float normalizeEnd = end - start; // rotate start to 0
        float normalizedEndAbs = (normalizeEnd + 360) % 360;

        float direction = (normalizedEndAbs > 180) ? -1 : 1; // -1 = anticlockwise, 1 = clockwise
        float rotation;
        if (direction > 0) {
            rotation = normalizedEndAbs;
        } else {
            rotation = normalizedEndAbs - 360;
        }

        float result = fraction * rotation + start;
        return (result + 360) % 360;
    }


}
