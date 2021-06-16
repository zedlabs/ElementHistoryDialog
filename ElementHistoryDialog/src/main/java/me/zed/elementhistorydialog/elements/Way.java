package me.zed.elementhistorydialog.elements;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * OSM element of type Way
 */
public class Way extends OsmElement {

    private static final String DEBUG_TAG = "Way";

    /**
     * It's name in the OSM-XML-scheme.
     */
    public static final String NAME = "way";
    public static final String NODE = "nd";
    static final String REF = "ref";

    /**
     * List of OSM id's for all the nodes in this way
     */
    private List<String> wayNodes;

    /**
     * Construct a new Way
     *
     * @param osmId      the OSM id
     * @param osmVersion the version
     */
    public Way(final long osmId, final long osmVersion, final String userName, final long timestamp) {
        super(osmId, osmVersion, userName, timestamp);
        wayNodes = new ArrayList<>();
    }

    /**
     * Adds a node ref to the current node list
     *
     * @param ref id of the current node
     */
    public void addWayNode(@NonNull final String ref) {
        wayNodes.add(ref);
    }

    /**
     * Return list of all nodes ids in a way
     *
     * @return a List of Nodes
     */
    @NonNull
    public List<String> getWayNodes() {
        return wayNodes;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String toString() {
        StringBuilder res = new StringBuilder(super.toString());
        if (tags != null) {
            for (Map.Entry<String, String> tag : tags.entrySet()) {
                res.append('\t');
                res.append(tag.getKey());
                res.append('=');
                res.append(tag.getValue());
            }
        }
        return res.toString();
    }

    @Override
    public ElementType getType() {
        return ElementType.WAY;
    }

    @Override
    public ElementType getType(Map<String, String> tags) {
        return getType();
    }

}
