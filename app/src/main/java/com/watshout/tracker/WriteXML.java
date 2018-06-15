package com.watshout.tracker;

import android.util.Log;

import java.io.File;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class WriteXML {

    private Document doc;

    DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
    DocumentBuilder docBuilder = docFactory.newDocumentBuilder();


    public WriteXML() throws ParserConfigurationException, TransformerException {

        this.doc = docBuilder.newDocument();

        Element rootElement = this.doc.createElement("gpx");

        Attr xmlns = doc.createAttribute("xmlns");
        xmlns.setValue("http://www.topografix.com/GPX/1/1");
        rootElement.setAttributeNode(xmlns);
        Attr xmlnsxsi = doc.createAttribute("xmlns:xsi");
        xmlns.setValue("http://www.w3.org/2001/XMLSchema-instance");
        rootElement.setAttributeNode(xmlnsxsi);
        Attr xsischemaLocation = doc.createAttribute("xsi:schemaLocation");
        xmlns.setValue("http://www.topografix.com/GPX/1/1 http://www.topografix.com/GPX/1/1/gpx.xsd http://www.garmin.com/xmlschemas/GpxExtensions/v3 http://www.garmin.com/xmlschemas/GpxExtensionsv3.xsd http://www.garmin.com/xmlschemas/TrackPointExtension/v1 http://www.garmin.com/xmlschemas/TrackPointExtensionv1.xsd http://www.garmin.com/xmlschemas/GpxExtensions/v3 http://www.garmin.com/xmlschemas/GpxExtensionsv3.xsd http://www.garmin.com/xmlschemas/TrackPointExtension/v1 http://www.garmin.com/xmlschemas/TrackPointExtensionv1.xsd http://www.garmin.com/xmlschemas/GpxExtensions/v3 http://www.garmin.com/xmlschemas/GpxExtensionsv3.xsd http://www.garmin.com/xmlschemas/TrackPointExtension/v1 http://www.garmin.com/xmlschemas/TrackPointExtensionv1.xsd");
        rootElement.setAttributeNode(xsischemaLocation);
        Attr xmlnsgpxtpx = doc.createAttribute("xmlns:gpxtpx");
        xmlns.setValue("http://www.garmin.com/xmlschemas/TrackPointExtension/v1");
        rootElement.setAttributeNode(xmlnsgpxtpx);
        Attr xmlnsgpxx = doc.createAttribute("xmlns:gpxx");
        xmlns.setValue("http://www.garmin.com/xmlschemas/GpxExtensions/v3");
        rootElement.setAttributeNode(xmlnsgpxx);
        this.doc.appendChild(rootElement);

        Element metadata = this.doc.createElement("metadata");
        Element time = doc.createElement("time");
        time.appendChild(doc.createTextNode("test"));
        metadata.appendChild(time);
        rootElement.appendChild(metadata);

        Element name = doc.createElement("name");
        time.appendChild(doc.createTextNode("Test name"));
        rootElement.appendChild(name);

        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        DOMSource source = new DOMSource(doc);
        Log.d("XML", source.toString());
        StreamResult result = new StreamResult(System.out);
        Log.d("XML", result.toString());

        transformer.transform(source, result);


        }

}
