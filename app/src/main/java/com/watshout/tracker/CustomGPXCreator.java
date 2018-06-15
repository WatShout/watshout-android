package com.watshout.tracker;

import android.content.Context;
import android.util.Log;

import org.alternativevision.gpx.GPXParser;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.TimeZone;

public class CustomGPXCreator {

    private Context context;
    private String currentText;
    private String uid;


    CustomGPXCreator(Context context, String uid) {

        this.context = context;
        this.currentText = addStart();
        this.uid = uid;

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

    public void writeFile(String date) throws IOException {

        // These lines of code write the file locally
        String fileName = date + ".gpx";

        //Log.d("GPXTEST", this.currentText);

        File path = context.getExternalFilesDir(null);
        File file = new File(path, fileName);
        path.mkdirs();
        FileOutputStream outStream = new FileOutputStream(file);

        byte[] bytes = this.currentText.getBytes();

        Log.d("GPXTEST", new String(bytes));

        outStream.write(bytes);
        outStream.close();

        UploadGPX uploadGPX = new UploadGPX(context,
                uid, date, file);

        // Note: This also makes the call to Strava
        uploadGPX.uploadToFirebaseStorage();
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
                " xmlns:gpxx=\"http://www.garmin.com/xmlschemas/GpxExtensions/v3\" >" +
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

    public void resetFile() {
        this.currentText = "";
    }

}
