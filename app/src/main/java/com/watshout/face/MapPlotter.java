package com.watshout.face;

import android.util.Log;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;

import static com.google.android.gms.maps.model.BitmapDescriptorFactory.fromResource;

import static com.watshout.face.MainActivity.GPSconnected;
import static com.watshout.face.MainActivity.currentlyTrackingLocation;

/*


=========================

This is a class for dealing with the google map component.

=========================


 */

public class MapPlotter {

    private BitmapDescriptor currentLocationIcon = fromResource(R.drawable.current);
    private ArrayList<Marker> markers;
    private GoogleMap googleMap;
    private Boolean isMapFollowing = true;


    MapPlotter(ArrayList<Marker> markers, GoogleMap googleMap){
        this.markers = markers;
        this.googleMap = googleMap;

        this.googleMap.setOnCameraMoveListener(new GoogleMap.OnCameraMoveListener() {
            @Override
            public void onCameraMove() {

                isMapFollowing = false;

            }
        });

    }

    public void moveCamera(float zoom) {

        if (markers.size() > 0){

            googleMap.moveCamera(CameraUpdateFactory
                    .newLatLngZoom(markers.get(markers.size() - 1).getPosition(), zoom));

        }
    }

    public void addMarker(double lat, double lon){

        LatLng currentLocation = new LatLng(lat, lon);
        LatLng previousLocation;

        // Adds a new marker on the LOCAL map. (The one on the website is written elsewhere).
        final Marker newMarker = googleMap.addMarker(new MarkerOptions()
                .position(currentLocation)
                .icon(currentLocationIcon));

        googleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {

                moveCamera(16);
                isMapFollowing = true;

                return false;
            }
        });

        if (isMapFollowing){
            googleMap.moveCamera(CameraUpdateFactory
                    .newLatLngZoom(currentLocation, 16));
        }

        if (markers.size() == 0) {
            previousLocation = currentLocation;
        } else {
            previousLocation = markers.get(markers.size() - 1).getPosition();
            markers.get(markers.size() - 1).setVisible(false);
        }

        markers.add(newMarker);

        if (currentlyTrackingLocation){

            if (markers.size() > 0) {

                googleMap.addPolyline(new PolylineOptions()
                        .add(previousLocation, currentLocation)
                        .width(10));

            }

        }

    }
}
