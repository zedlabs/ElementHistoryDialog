package me.zed.elementhistorydialog.elements;

import java.util.Map;

/**
 * OSM element of type Node
 */
public class Node extends OsmElement {

    public static final String LON = "lon";
    public static final String LAT = "lat";

    /**
     * WGS84 decimal Latitude-Coordinate times 1E7.
     */
    int lat;

    /**
     * WGS84 decimal Longitude-Coordinate times 1E7.
     */
    int lon;

    /**
     * It's name in the OSM-XML-scheme.
     */
    public static final String NAME = "node";

    /**
     * Scaling between floating point coordinates and internal representation as an int
     */
    public static final int COORDINATE_SCALE = 7;

    /**
     * Constructor. Call it solely in { OsmElementFactory}!
     *
     * @param osmId      the OSM-ID. When not yet transmitted to the API it is negative.
     * @param osmVersion the version of the element
     * @param lat        WGS84 decimal Latitude-Coordinate times 1E7.
     * @param lon        WGS84 decimal Longitude-Coordinate times 1E7.
     */
    public Node(final long osmId, final long osmVersion, final int lat, final int lon) {
        super(osmId, osmVersion);
        this.lat = lat;
        this.lon = lon;
    }

    /**
     * @return the latitude of the current node
     */
    int getLat() {
        return lat;
    }

    /**
     * @return the longitude of the current node
     */
    int getLon() {
        return lon;
    }

    /**
     * Set the latitude
     *
     * @param lat latitude in WGS84*1E7
     */
    void setLat(final int lat) {
        this.lat = lat;
    }

    /**
     * Set the longitude
     *
     * @param lon longitude in WGS84*1E7
     */
    void setLon(final int lon) {
        this.lon = lon;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getName() {
        return NAME;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return super.toString() + "\tlat: " + lat + "; lon: " + lon;
    }


    @Override
    public ElementType getType() {
        return ElementType.NODE;
    }

    @Override
    public ElementType getType(Map<String, String> tags) {
        return getType();
    }
}
