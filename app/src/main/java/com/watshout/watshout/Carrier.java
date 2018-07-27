package com.watshout.watshout;

public class Carrier {

    private static XMLCreator XMLCreator;
    private static boolean uploadedOwnProfilePicture = true;

    static void setXMLCreator(XMLCreator XMLCreatorIn){XMLCreator = XMLCreatorIn;}
    static XMLCreator getXMLCreator(){return XMLCreator;}

    static void setUploadedOwnProfilePicture(boolean pfpIn){uploadedOwnProfilePicture = pfpIn;}
    static boolean getUploadedOwnProfilePicture(){return uploadedOwnProfilePicture;}

}
