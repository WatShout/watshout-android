package com.watshout.tracker;

import android.content.Context;
import android.location.Location;
import android.util.Log;

import com.google.android.gms.location.LocationAvailability;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;

import org.alternativevision.gpx.beans.Track;
import org.alternativevision.gpx.beans.TrackPoint;
import org.alternativevision.gpx.beans.Waypoint;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

public class FusedLocation {

    private Context context;
    private MapPlotter mapPlotter;
    private String uid;
    private ArrayList<Waypoint> trackPoints;
    private static GPXCreator gpxCreator;
    private UploadFinishedActivity uploadFinishedActivity;


    FusedLocation(Context context, MapPlotter mapPlotter, String uid){

        this.context = context;
        this.mapPlotter = mapPlotter;
        this.uid = uid;
        this.trackPoints = new ArrayList<>();
        gpxCreator = new GPXCreator(context, uid);
        this.uploadFinishedActivity = new UploadFinishedActivity(uid);

    }

    public LocationCallback buildLocationCallback() {

        LocationCallback locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {

                MainActivity.GPSconnected = true;

                Location location = locationResult.getLocations().get(0);


                double lat = location.getLatitude();
                double lon = location.getLongitude();
                double speed = location.getSpeed();
                double bearing = location.getBearing();
                double altitude = location.getAltitude();
                long time = location.getTime();

                float accuracy = location.getAccuracy();

                Log.wtf("GPSACCURACY", accuracy + "");

                // Adds the point to the map
                mapPlotter.addMarker(lat, lon);

                Log.wtf("GPS", "Lat: " + lat + "\nLong" + lon + "\nTracking: " + MainActivity.currentlyTrackingLocation);

                if (MainActivity.currentlyTrackingLocation){
                    new LocationObject(context, uid, lat, lon, speed, bearing, altitude, time).uploadToFirebase();
                    TrackPoint temp = new TrackPoint();
                    temp.setLatitude(lat);
                    temp.setLongitude(lon);
                    temp.setTime(new Date(time));
                    trackPoints.add(temp);
                }
                else if (trackPoints.size() > 0) {
                    Track tempTrack = new Track();
                    tempTrack.setTrackPoints(trackPoints);
                    gpxCreator.addTrack(tempTrack);
                    trackPoints = new ArrayList<>();

                    if (!MainActivity.activityRunning) {

                        uploadFinishedActivity.moveCurrentToPast();

                        String date = uploadFinishedActivity.getFormattedDate();

                        try {
                            gpxCreator.writeGPXFile(date);
                            gpxCreator.resetGPXObject();
                        } catch (IOException e) {
                            Log.e("ERROR", e + "");
                        } catch (TransformerException e) {
                            Log.e("ERROR", e + "");
                        } catch (ParserConfigurationException e) {
                            Log.e("ERROR", e + "");
                        }

                    }
                }

            }

            @Override
            public void onLocationAvailability(LocationAvailability locationAvailability) {

            }
        };

        return locationCallback;

    }

    public LocationRequest buildLocationRequest() {

        LocationRequest locationRequest = new LocationRequest()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(5000)
                .setFastestInterval(3000)
                .setSmallestDisplacement(5);

        return locationRequest;

    }

    public ArrayList<Waypoint> getTrackPoints() {
        return trackPoints;
    }
}
