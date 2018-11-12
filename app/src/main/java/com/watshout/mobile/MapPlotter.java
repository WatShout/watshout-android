package com.watshout.mobile;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import com.google.android.gms.maps.model.BitmapDescriptor;
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
    private Context context;
    private Bitmap currentIcon;
    private boolean initial = true;
    Polyline bigPolyLine;

    private int plotCounter = 0;


    MapPlotter(ArrayList<Marker> markers, GoogleMap googleMap, boolean isSelf, String uid,
               Context context){
        this.markers = markers;
        this.googleMap = googleMap;
        this.profilePic = null;
        this.uid = uid;
        this.context = context;


        currentIcon = BitmapFactory.decodeResource(context.getResources(), R.drawable.current);

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
        if (bigPolyLine != null) {
            bigPolyLine.setVisible(false);
            bigPolyLine = null;
        }
    }

    public void oldclearPolyLines() {

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

    public void addFriendMarker(double lat, double lon) {
        Marker newMarker = googleMap.addMarker(new MarkerOptions()
                .position(new LatLng(lat, lon)));

        if (markers.size() > 0) {
            int highestIndex = markers.size() - 1;
            markers.get(highestIndex).setIcon(null);
            markers.get(highestIndex).setVisible(false);
        }

        markers.add(newMarker);

        if (bigPolyLine != null) {
            bigPolyLine.setVisible(false);
        }

        PolylineOptions options = new PolylineOptions();
        options.width(10);
        options.jointType(2);
        int count = markers.size();
        for(int i = 0; i< count; i++){
            options.add(markers.get(i).getPosition());
        }

        bigPolyLine = googleMap.addPolyline(options);

    }

    public void oldaddFriendMarker(double lat, double lon){
        LatLng currentLocation = new LatLng(lat, lon);
        LatLng previousLocation;

        if (plotCounter % 10 == 0) {
            // Adds a new marker on the LOCAL map. (The one on the website is written elsewhere).
            Marker newMarker = googleMap.addMarker(new MarkerOptions()
                    .position(currentLocation));


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
            plotCounter = 0;
        } else {
            plotCounter++;
        }



    }

    public void massPlot(ArrayList<LatLng> points) {
        PolylineOptions options = new PolylineOptions().color(Color.RED)
                .jointType(2)
                .width(10);
        for (int z = 0; z < points.size(); z++) {
            LatLng point = points.get(z);
            options.add(point);
        }
        bigPolyLine = googleMap.addPolyline(options);
    }

    public void addMarker(double lat, double lon) {
        Marker newMarker = googleMap.addMarker(new MarkerOptions()
                .position(new LatLng(lat, lon)));

        newMarker.setIcon(null);
        newMarker.setVisible(false);

        if (markers.size() > 0) {
            int highestIndex = markers.size() - 1;
            markers.get(highestIndex).setIcon(null);
            markers.get(highestIndex).setVisible(false);
        }

        markers.add(newMarker);

        if (bigPolyLine != null) {
            bigPolyLine.setVisible(false);
        }

        PolylineOptions options = new PolylineOptions();
        options.width(10);
        options.jointType(2);
        int count = markers.size();
        for(int i = 0; i< count; i++){
            options.add(markers.get(i).getPosition());
        }

        bigPolyLine = googleMap.addPolyline(options);
    }

    public void oldAddMarker(double lat, double lon){

        if (plotCounter % 5 == 0) {

            LatLng currentLocation = new LatLng(lat, lon);
            LatLng previousLocation;

            // Adds a new marker on the LOCAL map. (The one on the website is written elsewhere).
            Marker newMarker = googleMap.addMarker(new MarkerOptions()
                    .position(currentLocation)
                    .icon(null));

            newMarker.setVisible(false);

            googleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                @Override
                public boolean onMarkerClick(Marker marker) {

                    moveCamera(19);
                    isMapFollowing = true;

                    return false;
                }
            });

            if (isMapFollowing){
                googleMap.moveCamera(CameraUpdateFactory
                        .newLatLngZoom(currentLocation, 19));
            }

            if (markers.size() == 0) {
                previousLocation = currentLocation;
            } else {
                previousLocation = markers.get(markers.size() - 1).getPosition();
                markers.get(markers.size() - 1).setVisible(false);
            }

            markers.add(newMarker);

            for (Marker i : markers) {
                Log.d("PLOT", i.getPosition().latitude + ", " + i.getPosition().longitude);
            }


            if (com.watshout.mobile.MapFragment.currentlyTrackingLocation){

                if (markers.size() > 0) {
                    System.out.println("POLYLINE IN");
                    polylines.add(googleMap.addPolyline(new PolylineOptions()
                            .add(previousLocation, currentLocation)
                            .color(Color.RED)
                            .jointType(2)
                            .width(10)));

                }

            }
            plotCounter = 0;
        } else {
            plotCounter++;
        }

    }


}
