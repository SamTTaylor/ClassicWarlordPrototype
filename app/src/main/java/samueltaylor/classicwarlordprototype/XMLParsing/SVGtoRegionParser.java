package samueltaylor.classicwarlordprototype.XMLParsing;

import android.graphics.Path;
import android.graphics.PathMeasure;
import android.util.Log;
import android.util.Xml;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Sam on 25/06/2015.
 */
public class SVGtoRegionParser {
    private static final String ns = null;

    public List parse(InputStream in) throws XmlPullParserException, IOException {
        try {
            XmlPullParser parser = Xml.newPullParser();
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(in, null);
            parser.nextTag();
            return readWorld(parser);
        } finally {
            in.close();
        }
    }

    private List readWorld(XmlPullParser parser) throws XmlPullParserException, IOException {
        List<Region> regions = new ArrayList<Region>();
        //Start at the world tag
        parser.require(XmlPullParser.START_TAG, ns, "world");
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            // For each region tag found, read that region's information
            // into the regions array
            if (name.equals("region")) {
                regions.add(readRegion(parser));
            } else {
                skip(parser);
            }
        }
        return regions;
    }

    public static class Region {
        public final float[] path;
        public final String name;
        public final String type;
        public final String[] adjacentregions;
        private Region(float[] path, String name, String type, String[] adjacentregions) {
            this.adjacentregions = adjacentregions;
            this.path = path;
            this.name = name;
            this.type = type;
        }
    }

    // Parses the contents of an entry. If it encounters a title, summary, or link tag, hands them off
    // to their respective "read" methods for processing. Otherwise, skips the tag.
    private Region readRegion(XmlPullParser parser) throws XmlPullParserException, IOException {
        parser.require(XmlPullParser.START_TAG, ns, "region");
        float[] path = null;
        String regionname = null;
        String regiontype = null;
        List<String> lstadjacentregions = new ArrayList<>();
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            if (name.equals("path")){
                path = readPath(parser);
            } else if(name.equals("name")){
                regionname = readName(parser);
            } else if (name.equals("type")) {
                regiontype = readType(parser); }
            else if (name.equals("adjacent")){
                lstadjacentregions.add(readAdjacentRegions(parser)); }
            else {
                skip(parser);
            }
        }
        String[] adjacentregions = new String[lstadjacentregions.size()];
        for(int i=0;i<adjacentregions.length;i++){
            adjacentregions[i]=lstadjacentregions.get(i);
        }
        return new Region(path, regionname, regiontype, adjacentregions);
    }
    String[] mPathsstring;
    String[] coordstring;
    // Processes coordinate tags in the feed.
    private float[] readPath(XmlPullParser parser) throws IOException, XmlPullParserException {
        //Paths are taken directly from Inkscape SVGs
        //The format of the point coordinates is x,y x,y etc
        //The structure of the points is right bezier handle, left bezier handle, point
        parser.require(XmlPullParser.START_TAG, ns, "path");
        String innertext = readText(parser);
        parser.require(XmlPullParser.END_TAG, ns, "path");
        //Split the file into points "x,y"
        mPathsstring = innertext.split("M|C|L");
        List<String> stringcoordinates = new ArrayList<>();
        int i = 0;
        for(String s : mPathsstring){
            if(i==0){//skip first value
                i++;
            } else{
                coordstring = s.split(",");
                if(coordstring.length==6){//Curve, interpreted as straight line
                    stringcoordinates.add(coordstring[4]);
                    stringcoordinates.add(coordstring[5]);
                } else { //Line
                    stringcoordinates.add(coordstring[0]);
                    stringcoordinates.add(coordstring[1]);
                }

                //Add Z (default 0.0f)
                stringcoordinates.add("0.0f");
                i++;
            }
        }
        float[] coordinates = new float[stringcoordinates.size()];
        i=0;
        for(String s : stringcoordinates){
            coordinates[i] = Float.parseFloat(stringcoordinates.get(i));
            coordinates[i]/=200;
            i++;
        }
        return coordinates;
    }

    // Processes position tags in the feed.
    private String readName(XmlPullParser parser) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, ns, "name");
        String innertext = readText(parser);
        parser.require(XmlPullParser.END_TAG, ns, "name");
        return innertext;
    }

    // Processes position tags in the feed.
    private String readType(XmlPullParser parser) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, ns, "type");
        String innertext = readText(parser);
        parser.require(XmlPullParser.END_TAG, ns, "type");
        return innertext;
    }

    // Processes position tags in the feed.
    private String readAdjacentRegions(XmlPullParser parser) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, ns, "adjacent");
        String innertext = readText(parser);
        parser.require(XmlPullParser.END_TAG, ns, "adjacent");
        return innertext;
    }


    // For the tags title and summary, extracts their text values.
    private String readText(XmlPullParser parser) throws IOException, XmlPullParserException {
        String result = "";
        if (parser.next() == XmlPullParser.TEXT) {
            result = parser.getText();
            parser.nextTag();
        }
        return result;
    }


    //Skip current tag
    private void skip(XmlPullParser parser) throws XmlPullParserException, IOException {
        if (parser.getEventType() != XmlPullParser.START_TAG) {
            throw new IllegalStateException();
        }
        int depth = 1;
        while (depth != 0) {
            switch (parser.next()) {
                case XmlPullParser.END_TAG:
                    depth--;
                    break;
                case XmlPullParser.START_TAG:
                    depth++;
                    break;
            }
        }
    }


}