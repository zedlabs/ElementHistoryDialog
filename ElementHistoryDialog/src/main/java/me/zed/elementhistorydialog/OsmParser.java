package me.zed.elementhistorydialog;

import android.util.Log;

import androidx.annotation.NonNull;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import me.zed.elementhistorydialog.elements.Node;
import me.zed.elementhistorydialog.elements.OsmElement;
import me.zed.elementhistorydialog.elements.Relation;
import me.zed.elementhistorydialog.elements.RelationMember;
import me.zed.elementhistorydialog.elements.Way;

/**
 * Parses a XML (as InputStream), provided by XmlRetriever, and pushes generated OsmElements to the given Storage.
 * <p>
 * Supports API 0.6 output and JOSM OSM files, assumes Node, Ways, Relations ordering of input
 *
 * @author mb
 * @author simon
 */
public class OsmParser extends DefaultHandler {

    private static final String DEBUG_TAG = OsmParser.class.getSimpleName();
    public static final String TIMESTAMP_FORMAT = "yyyy-MM-dd'T'HH:mm:ss'Z'";
    protected static final String OVERPASS_NOTE = "note";
    protected static final String OVERPASS_META = "meta";
    public static final String OSM = "osm";

    private final Storage storage;
    private Node currentNode = null;
    protected Way currentWay = null;
    private Relation currentRelation = null;
    private TreeMap<String, String> currentTags;
    private final List<Exception> exceptions = new ArrayList<>();

    /**
     * Construct a new instance of the parser
     */
    public OsmParser() {
        super();
        storage = new Storage();
    }

    /**
     * Get the Storage instance associated with the parser
     *
     * @return an instance of Storage
     */
    @NonNull
    public Storage getStorage() {
        return storage;
    }

    /**
     * Triggers the beginning of parsing.
     *
     * @param in the InputStream
     * @throws SAXException                 {@see SAXException}
     * @throws IOException                  when the xmlRetriever could not provide any data.
     * @throws ParserConfigurationException if a parser feature is used that is not supported
     */
    public void start(@NonNull final InputStream in) throws SAXException, IOException, ParserConfigurationException {

        SAXParserFactory factory = SAXParserFactory.newInstance(); // NOSONAR
        factory.setNamespaceAware(true);
        SAXParser saxParser = factory.newSAXParser();
        saxParser.parse(in, this);
    }

    @Override
    public void endDocument() {
        Log.d(DEBUG_TAG, "Finished parsing input.");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void startElement(final String uri, final String name, final String qName, final Attributes atts) {
        try {
            switch (name) {
                case Way.NAME:
                case Node.NAME:
                case Relation.NAME:
                    parseOsmElement(name, atts);
                    break;
                case Way.NODE:
                    parseWayNode(atts);
                    break;
                case Relation.MEMBER:
                    parseRelationMember(atts);
                    break;
                case OsmElement.TAG:
                    parseTag(atts);
                    break;
                case OSM:
                case OVERPASS_NOTE:
                case OVERPASS_META:
                    // we don't do anything with these
                    break;
                default:
                    throw new OsmParseException("Unknown element " + name);
            }
        } catch (OsmParseException e) {
            Log.e(DEBUG_TAG, "OsmParseException", e);
            exceptions.add(e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void endElement(final String uri, final String name, final String qName) throws SAXException {
        try {
            switch (name) {
                case Node.NAME:
                    if (currentNode != null) {
                        addTags(currentNode);
                        storage.insertNodeUnsafe(currentNode);
                        currentNode = null;
                    } else {
                        throw new SAXException("State error, null Node");
                    }
                    break;
                case Way.NAME:
                    if (currentWay != null) {
                        addTags(currentWay);
                        if (!currentWay.getWayNodes().isEmpty()) {
                            storage.insertWayUnsafe(currentWay);
                        } else {
                            Log.e(DEBUG_TAG, "Way " + currentWay.getOsmId() + " has no nodes! Ignored.");
                        }
                        currentWay = null;
                    } else {
                        throw new SAXException("State error, null Way");
                    }
                    break;
                case Relation.NAME:
                    if (currentRelation != null) {
                        addTags(currentRelation);
                        storage.insertRelationUnsafe(currentRelation);
                        currentRelation = null;
                    } else {
                        throw new SAXException("State error, null Relation");
                    }
                    break;
                default:
                    // ignore everything else
            }
        } catch (Exception sex) {
            throw new SAXException(sex);
        }
    }

    /**
     * Add accumulated tags to element
     *
     * @param e element to add the tags to
     */
    void addTags(OsmElement e) {
        if (currentTags != null) {
            e.setTags(currentTags);
            currentTags = null;
        }
    }

    /**
     * parse API 0.6 output and JOSM OSM files
     *
     * @param name the OsmElement type ("node", "way", "relation")
     * @param atts the attributes of the current XML start tag
     * @throws if parsing fails
     */
    protected void parseOsmElement(@NonNull final String name, @NonNull final Attributes atts) throws OsmParseException {
        try {
            long osmId = Long.parseLong(atts.getValue(OsmElement.ID_ATTR));
            String version = atts.getValue(OsmElement.VERSION_ATTR);
            long osmVersion = version == null ? 0 : Long.parseLong(version); // hack for JOSM file
            String username = atts.getValue(OsmElement.USER_ATTR);
            long changeset = Long.parseLong(atts.getValue(OsmElement.CHANGESET_ATTR));

            String timestampStr = atts.getValue(OsmElement.TIMESTAMP_ATTR);
            long timestamp = -1L;
            if (timestampStr != null) {
                try {
                    timestamp = DateFormatter.getUtcFormat(OsmParser.TIMESTAMP_FORMAT).parse(timestampStr).getTime() / 1000;
                } catch (ParseException e) {
                    Log.d(DEBUG_TAG, "Invalid timestamp " + timestampStr);
                }
            }

            switch (name) {
                case Node.NAME:
                    int lat = 0, lon = 0;
                    if (atts.getValue(Node.LAT) != null && atts.getValue(Node.LON) != null) {
                        lat = (new BigDecimal(atts.getValue(Node.LAT)).scaleByPowerOfTen(Node.COORDINATE_SCALE)).intValue();
                        lon = (new BigDecimal(atts.getValue(Node.LON)).scaleByPowerOfTen(Node.COORDINATE_SCALE)).intValue();
                    }
                    currentNode = OsmElementFactory.createNode(osmId, osmVersion, username, changeset, timestamp, lat, lon);
                    break;
                case Way.NAME:
                    currentWay = OsmElementFactory.createWay(osmId, osmVersion, username, changeset, timestamp);
                    break;
                case Relation.NAME:
                    currentRelation = OsmElementFactory.createRelation(osmId, osmVersion, username, changeset, timestamp);
                    break;
                default:
                    throw new OsmParseException("Unknown element " + name);
            }
        } catch (NumberFormatException e) {
            throw new OsmParseException("Element unparsable");
        }
    }

    /**
     * Parse tags and accumulate them in a collection for later use
     *
     * @param atts current set of xml attribute
     */
    private void parseTag(final Attributes atts) {
        if (currentTags == null) {
            currentTags = new TreeMap<>();
        }
        String k = atts.getValue("k");
        String v = atts.getValue("v");
        currentTags.put(k, v);
    }

    /**
     * Parse a nd entry in a Way
     *
     * @param atts XML attributes for the current element
     * @throws if parsing fails
     */
    protected void parseWayNode(final Attributes atts) {
        if (currentWay == null) {
            Log.e(DEBUG_TAG, "No currentWay set!");
        } else {
            //way nodes only need the ref to be displayed, might need node object depending on use
            String nid = atts.getValue("ref");
            if (nid != null) {
                currentWay.addWayNode(nid);
            }
        }
    }

    /**
     * Parse relation members, storing information on relations that we haven't seen yet for post processing
     *
     * @param atts XML attributes for the current element
     * @throws if parsing fails
     */
    private void parseRelationMember(final Attributes atts) throws OsmParseException {
        try {
            if (currentRelation == null) {
                Log.e(DEBUG_TAG, "No currentRelation set!");
            } else {
                long ref = Long.parseLong(atts.getValue("ref"));
                String type = atts.getValue("type");
                String role = atts.getValue("role");
                RelationMember member = new RelationMember(type, ref, role);
                currentRelation.addMember(member);
            }
        } catch (NumberFormatException e) {
            throw new OsmParseException("RelationMember unparsable");
        }
    }
}
