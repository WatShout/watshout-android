package com.watshout.tracker;

import org.alternativevision.gpx.GPXParser;
import org.alternativevision.gpx.beans.GPX;
import org.alternativevision.gpx.beans.Track;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashSet;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

public class GPXCreator {
    GPX gpxObject;

    public GPXCreator() {
        gpxObject = new GPX();
    }

    public void addTrack(Track t) {
        gpxObject.addTrack(t);
    }

    public void writeGPXFile() {
        GPXParser parser = new GPXParser();
        try {
            File file = new File("watshoutFile.gpx");
            FileOutputStream outStream = new FileOutputStream(file);
            if (!file.exists()) {
                file.createNewFile();
            }

            parser.writeGPX(gpxObject, outStream);
            outStream.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (TransformerException e) {
            e.printStackTrace();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }
    }

    public void resetGPXObject() {
        gpxObject = new GPX();
    }
}