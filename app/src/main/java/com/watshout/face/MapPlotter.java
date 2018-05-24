package com.watshout.face;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;

import static com.google.android.gms.maps.model.BitmapDescriptorFactory.fromResource;

/*


=========================

This is a class for dealing with the google map component.

=========================


 */

public class MapPlotter {

    private BitmapDescriptor currentLocationIcon = fromResource(R.drawable.current);
    private ArrayList<Marker> thisDeviceMarkers;
    private GoogleMap googleMap;


    MapPlotter(ArrayList<Marker> thisDeviceMarkers, GoogleMap googleMap){
        this.thisDeviceMarkers = thisDeviceMarkers;
        this.googleMap = googleMap;
    }

    public void moveCamera(float zoom) {

        if (thisDeviceMarkers.size() > 0){

            googleMap.moveCamera(CameraUpdateFactory
                    .newLatLngZoom(thisDeviceMarkers.get(thisDeviceMarkers.size() - 1).getPosition(), zoom));

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

                return false;
            }
        });

        googleMap.moveCamera(CameraUpdateFactory
               .newLatLngZoom(currentLocation, 16));

        if (thisDeviceMarkers.size() == 0) {
            previousLocation = currentLocation;
        } else {
            previousLocation = thisDeviceMarkers.get(thisDeviceMarkers.size() - 1).getPosition();
            thisDeviceMarkers.get(thisDeviceMarkers.size() - 1).setVisible(false);
        }

        thisDeviceMarkers.add(newMarker);

        if (thisDeviceMarkers.size() > 0) {

            googleMap.addPolyline(new PolylineOptions()
                    .add(previousLocation, currentLocation)
                    .width(10));

        }
    }
}
