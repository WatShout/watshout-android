package com.watshout.tracker;

import android.util.Log;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.TimeZone;

public class CustomGPXCreator {

    private String currentText;


    CustomGPXCreator() {

        this.currentText = addStart();


    }

    public void addTrackPoint(double lat, double lon, double elevation, int hr){

        TimeZone tz = TimeZone.getTimeZone("UTC");
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'"); // Quoted "Z" to indicate UTC, no timezone offset
        df.setTimeZone(tz);
        String nowAsISO = df.format(new Date());

        this.currentText +=
                "   <trkpt lat=\"" + lat + "\" lon=\"" + lon + "\">\n" +
                "    <ele>" + elevation + "</ele>\n" +
                "    <time>" + nowAsISO + "</time>\n" +
                "    <extensions>\n" +
                "     <gpxtpx:TrackPointExtension>\n" +
                "      <gpxtpx:hr>" + hr + "</gpxtpx:hr>\n" +
                "     </gpxtpx:TrackPointExtension>\n" +
                "    </extensions>\n" +
                "   </trkpt>";


    }



    private String addStart() {

        return
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                        "<gpx " +
                "creator=\"StravaGPX\" " +
                "version=\"1.1\" " +
                "xmlns=\"http://www.topografix.com/GPX/1/1\"" +
                " xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"" +
                " xsi:schemaLocation=\"http://www.topografix.com/GPX/1/1" +
                " http://www.topografix.com/GPX/1/1/gpx.xsd" +
                " http://www.garmin.com/xmlschemas/GpxExtensions/v3" +
                " http://www.garmin.com/xmlschemas/GpxExtensionsv3.xsd" +
                " http://www.garmin.com/xmlschemas/TrackPointExtension/v1" +
                " http://www.garmin.com/xmlschemas/TrackPointExtensionv1.xsd" +
                " http://www.garmin.com/xmlschemas/GpxExtensions/v3" +
                " http://www.garmin.com/xmlschemas/GpxExtensionsv3.xsd" +
                " http://www.garmin.com/xmlschemas/TrackPointExtension/v1" +
                " http://www.garmin.com/xmlschemas/TrackPointExtensionv1.xsd" +
                " http://www.garmin.com/xmlschemas/GpxExtensions/v3" +
                " http://www.garmin.com/xmlschemas/GpxExtensionsv3.xsd" +
                " http://www.garmin.com/xmlschemas/TrackPointExtension/v1" +
                " http://www.garmin.com/xmlschemas/TrackPointExtensionv1.xsd\"" +
                " xmlns:gpxtpx=\"http://www.garmin.com/xmlschemas/TrackPointExtension/v1\"" +
                " xmlns:gpxx=\"http://www.garmin.com/xmlschemas/GpxExtensions/v3\">" +
                " \n<metadata>\n" +
                "  <time>2018-05-31T15:06:27Z</time>\n" +
                " </metadata>" +
                " <trk>\n" +
                "  <name>Fresh start</name>\n" +
                "  <trkseg>";

    }

    public void addFinish() {
        this.currentText +=
                "  </trkseg>\n" +
                " </trk>\n" +
                "</gpx>";
    }

    public String returnFinished() {
        Log.d("GPX", this.currentText);
        return this.currentText;
    }


}
