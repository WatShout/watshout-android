package com.watshout.tracker;

import android.content.Context;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

class WriteXML {

    private Document doc;
    private Context context;
    private String uid;
    private DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
    private DocumentBuilder docBuilder = docFactory.newDocumentBuilder();


    WriteXML(Context context, String uid) throws ParserConfigurationException, TransformerException {

        this.context = context;
        this.uid = uid;

        this.doc = docBuilder.newDocument();

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

        File path = context.getExternalFilesDir(null);
        File file = new File(path, fileName);
        assert path != null;
        path.mkdirs();

        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty(OutputKeys.METHOD, "xml");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");

        DOMSource source = new DOMSource(doc);
        StreamResult result = new StreamResult(file);

        transformer.transform(source, result);

        UploadGPX uploadGPX = new UploadGPX(context,
                uid, date, file);

        // Note: This also makes the call to Strava
        uploadGPX.uploadToFirebaseStorage();

    }


}
