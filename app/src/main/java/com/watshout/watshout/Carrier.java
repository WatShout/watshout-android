package com.watshout.tracker;

public class Carrier {

    private static XMLCreator XMLCreator;

    static void setXMLCreator(XMLCreator XMLCreatorIn){XMLCreator = XMLCreatorIn;}
    static XMLCreator getXMLCreator(){return XMLCreator;}

}
