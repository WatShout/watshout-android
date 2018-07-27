package com.watshout.watshout;

import android.graphics.Color;

import java.util.HashMap;

public class Carrier {

    private static XMLCreator XMLCreator;
    private static boolean uploadedOwnProfilePicture = true;
    private static String[] colorNames = {"red","yellow","green","blue","magenta"};
    private static int[] colors = {Color.RED,Color.YELLOW,Color.GREEN,Color.BLUE, Color.MAGENTA};

    static void setXMLCreator(XMLCreator XMLCreatorIn){XMLCreator = XMLCreatorIn;}
    static XMLCreator getXMLCreator(){return XMLCreator;}

    static void setUploadedOwnProfilePicture(boolean pfpIn){uploadedOwnProfilePicture = pfpIn;}
    static boolean getUploadedOwnProfilePicture(){return uploadedOwnProfilePicture;}

    static int getColorFromString(String color){
        for (int i=0;i<colorNames.length;i++){
            if (color.equals(colorNames[i]))
                return colors[i];
        }
        return colors[0];
    }

}