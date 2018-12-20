package com.watshout.mobile;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.io.FileUtils;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

class XMLCreator {

    private Document doc;
    private Context context;
    private String uid;
    private DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
    private DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
    private File gpxFile;
    private FirebaseStorage storage = FirebaseStorage.getInstance();
    private StorageReference storageReference = storage.getReference();
    private String date;

    private final String TAG = "XMLCreator";


    XMLCreator(Context context, String uid) throws ParserConfigurationException {

        this.context = context;
        this.uid = uid;

        this.doc = docBuilder.newDocument();
        initializeNewFile();
    }

    private void initializeNewFile() {
        Element rootElement = this.doc.createElement("gpx");

        Attr xmlns = doc.createAttribute("xmlns");
        xmlns.setValue("http://www.topografix.com/GPX/1/1");
        rootElement.setAttributeNode(xmlns);

        Attr xmlnsxsi = doc.createAttribute("xmlns:xsi");
        xmlnsxsi.setValue("http://www.w3.org/2001/XMLSchema-instance");
        rootElement.setAttributeNode(xmlnsxsi);

        Attr xsischemaLocation = doc.createAttribute("xsi:schemaLocation");
        xsischemaLocation.setValue("http://www.topografix.com/GPX/1/1 http://www.topografix.com/GPX/1/1/gpx.xsd http://www.garmin.com/xmlschemas/GpxExtensions/v3 http://www.garmin.com/xmlschemas/GpxExtensionsv3.xsd http://www.garmin.com/xmlschemas/TrackPointExtension/v1 http://www.garmin.com/xmlschemas/TrackPointExtensionv1.xsd http://www.garmin.com/xmlschemas/GpxExtensions/v3 http://www.garmin.com/xmlschemas/GpxExtensionsv3.xsd http://www.garmin.com/xmlschemas/TrackPointExtension/v1 http://www.garmin.com/xmlschemas/TrackPointExtensionv1.xsd http://www.garmin.com/xmlschemas/GpxExtensions/v3 http://www.garmin.com/xmlschemas/GpxExtensionsv3.xsd http://www.garmin.com/xmlschemas/TrackPointExtension/v1 http://www.garmin.com/xmlschemas/TrackPointExtensionv1.xsd");
        rootElement.setAttributeNode(xsischemaLocation);

        Attr xmlnsgpxtpx = doc.createAttribute("xmlns:gpxtpx");
        xmlnsgpxtpx.setValue("http://www.garmin.com/xmlschemas/TrackPointExtension/v1");
        rootElement.setAttributeNode(xmlnsgpxtpx);

        Attr xmlnsgpxx = doc.createAttribute("xmlns:gpxx");
        xmlnsgpxx.setValue("http://www.garmin.com/xmlschemas/GpxExtensions/v3");
        rootElement.setAttributeNode(xmlnsgpxx);

        this.doc.appendChild(rootElement);

        TimeZone tz = TimeZone.getTimeZone("UTC");
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'"); // Quoted "Z" to indicate UTC, no timezone offset
        df.setTimeZone(tz);
        String timeString = df.format(new Date());

        Element metadata = this.doc.createElement("metadata");
        Element time = doc.createElement("time");
        time.appendChild(doc.createTextNode(timeString));
        metadata.appendChild(time);
        rootElement.appendChild(metadata);

        Element trk = doc.createElement("trk");
        rootElement.appendChild(trk);

        Element name = doc.createElement("name");
        name.appendChild(doc.createTextNode("Test name"));
        trk.appendChild(name);

        Element type = doc.createElement("type");
        type.appendChild(doc.createTextNode(Integer.toString(1)));
        trk.appendChild(type);

        Element trkseg = doc.createElement("trkseg");
        Attr id = doc.createAttribute("id");
        id.setValue("trkseg");
        trkseg.setAttributeNode(id);
        trk.appendChild(trkseg);

    }

    public void addPoint(double latDouble, double lonDouble, double elevationDouble, int hrInt, String timeString) {

        String latString = Double.toString(latDouble);
        String lonString = Double.toString(lonDouble);

        Element trkseg = doc.getElementById("trkseg");

        Element trkpt = doc.createElement("trkpt");

        Attr lat = doc.createAttribute("lat");
        lat.setValue(latString);
        trkpt.setAttributeNode(lat);

        Attr lon = doc.createAttribute("lon");
        lon.setValue(lonString);
        trkpt.setAttributeNode(lon);

        Element ele = doc.createElement("ele");
        ele.appendChild(doc.createTextNode(Double.toString(elevationDouble)));
        trkpt.appendChild(ele);

        Element time = doc.createElement("time");
        time.appendChild(doc.createTextNode(timeString));
        trkpt.appendChild(time);

        Element extensions = doc.createElement("extensions");
        Element gpxtpxTrackPointExtension = doc.createElement("gpxtpx:TrackPointExtension");
        Element gpxtpxHr = doc.createElement("gpxtpx:hr");
        gpxtpxHr.appendChild(doc.createTextNode(Integer.toString(hrInt)));
        gpxtpxTrackPointExtension.appendChild(gpxtpxHr);
        extensions.appendChild(gpxtpxTrackPointExtension);
        trkpt.appendChild(extensions);

        trkseg.appendChild(trkpt);

    }

    public void saveFile(String date) throws TransformerException, IOException {
        String fileName = date + ".gpx";

        this.date = date;

        File path = context.getExternalFilesDir(null);
        File file = new File(path, fileName);
        this.gpxFile = file;
        assert path != null;
        path.mkdirs();

        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty(OutputKeys.METHOD, "xml");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");

        DOMSource source = new DOMSource(doc);
        StreamResult result = new StreamResult(file);

        transformer.transform(source, result);

        Log.d(TAG, "Saved GPX locally");

    }

    private byte[] fileToBytes(File gpx) throws IOException {

        return FileUtils.readFileToByteArray(gpx);

    }

    public void uploadToFirebaseStorage(final String date, final boolean hasStrava) throws IOException {

        byte[] bytes = fileToBytes(gpxFile);

        String fileName = date + ".gpx";

        /*
        storageReference.child("users").child(uid).child("gpx").child(fileName)
                .putBytes(bytes).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {

                Log.d(TAG, "Uploaded GPX to Firebase Storage correctly");
            }
        });*/

        RequestQueue queue = Volley.newRequestQueue(context);
        String stravaURL = EndpointURL.getInstance().getStravaURL(uid, date);
        String createMapURL = EndpointURL.getInstance().addActivityURL();

        StringRequest createMapRequest = new StringRequest(Request.Method.POST,
                createMapURL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                Log.d(TAG, "Activity uploaded successfully!");
                Toast.makeText(context, "Activity uploaded successfully!", Toast.LENGTH_SHORT).show();

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                Log.d(TAG, "Activity upload failed");
                Log.e(TAG, error.toString());


            }
        }){
            @Override
            protected Map<String,String> getParams(){
                Map<String,String> params = new HashMap<String, String>();
                params.put("uid", uid);
                params.put("time_stamp", date);
                return params;
            }
        };

        createMapRequest.setRetryPolicy(new DefaultRetryPolicy(
                5000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        queue.add(createMapRequest);

        // This code only runs if user wants to upload to Strava
        if (hasStrava) {
            StringRequest stravaRequest = new StringRequest(Request.Method.GET, stravaURL,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {

                            Toast.makeText(context,
                                    "Uploaded to Strava!",
                                    Toast.LENGTH_SHORT).show();
                            Log.d(TAG, "Uploaded GPX to Strava correctly");

                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Toast.makeText(context,
                            "Strava upload failed",
                            Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "Uploading GPX to Strava failed: " + error);
                }
            });

            stravaRequest.setRetryPolicy(new DefaultRetryPolicy(
                    5000,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

            queue.add(stravaRequest);
        }
    }

    public void resetXML() {
        this.doc = docBuilder.newDocument();
        initializeNewFile();
    }


}
