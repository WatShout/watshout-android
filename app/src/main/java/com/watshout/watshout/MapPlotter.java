package com.watshout.watshout;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.Log;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
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
    private ArrayList<Marker> markers;
    private GoogleMap googleMap;
    private Boolean isMapFollowing = true;
    private ArrayList<Polyline> polylines = new ArrayList<>();
    private Bitmap profilePic;
    private String uid;


    MapPlotter(ArrayList<Marker> markers, GoogleMap googleMap, boolean isSelf, String uid){
        this.markers = markers;
        this.googleMap = googleMap;
        this.profilePic = null;
        this.uid = uid;

        if (isSelf){
            this.googleMap.setOnCameraMoveListener(new GoogleMap.OnCameraMoveListener() {
                @Override
                public void onCameraMove() {

                    isMapFollowing = false;

                }
            });
        }
    }

    public void setProfilePic(Bitmap profilePic){
        this.profilePic = profilePic;
    }

    public void moveCamera(float zoom) {

        if (markers.size() > 0){

            googleMap.moveCamera(CameraUpdateFactory
                    .newLatLngZoom(markers.get(markers.size() - 1).getPosition(), zoom));

        }
    }

    public void removeFromMap() {
        clearPolyLines();
        clearMarkers();
    }

    public void clearMarkers() {

        for (Marker i : markers){
            i.setVisible(false);
        }

        markers = new ArrayList<>();

    }

    public void clearPolyLines() {

        for (Polyline line : polylines){

            line.remove();

        }
        polylines = new ArrayList<>();

    }

    private Bitmap getCircleBitmap(Bitmap bitmap) {
        final Bitmap output = Bitmap.createBitmap(bitmap.getWidth(),
                bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        final Canvas canvas = new Canvas(output);

        final int color = Color.RED;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        final RectF rectF = new RectF(rect);

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawOval(rectF, paint);

        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);

        //bitmap.recycle();

        return output;
    }

    public void addFriendMarker(double lat, double lon){
        LatLng currentLocation = new LatLng(lat, lon);
        LatLng previousLocation;

        BitmapDescriptor icon;

        if (profilePic == null){
            icon = currentLocationIcon;
        } else {
            icon = BitmapDescriptorFactory.fromBitmap(getCircleBitmap(profilePic));
        }

        // Adds a new marker on the LOCAL map. (The one on the website is written elsewhere).
        Marker newMarker = googleMap.addMarker(new MarkerOptions()
                .position(currentLocation));

        Log.d("MARKERS", markers.toString());

        if (markers.size() == 0) {
            previousLocation = currentLocation;
        } else {
            previousLocation = markers.get(markers.size() - 1).getPosition();
            markers.get(markers.size() - 1).setIcon(null);
            markers.get(markers.size() - 1).setVisible(false);
        }

        markers.add(newMarker);

        if (markers.size() > 0) {

            polylines.add(googleMap.addPolyline(new PolylineOptions()
                    .add(previousLocation, currentLocation)
                    .width(10)));

        }

    }

    public void addMarker(double lat, double lon){

        LatLng currentLocation = new LatLng(lat, lon);
        LatLng previousLocation;

        // Adds a new marker on the LOCAL map. (The one on the website is written elsewhere).
        Marker newMarker = googleMap.addMarker(new MarkerOptions()
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

        if (com.watshout.watshout.MapFragment.currentlyTrackingLocation){

            if (markers.size() > 0) {

                polylines.add(googleMap.addPolyline(new PolylineOptions()
                        .add(previousLocation, currentLocation)
                        .width(10)));

            }

        }

    }
}
