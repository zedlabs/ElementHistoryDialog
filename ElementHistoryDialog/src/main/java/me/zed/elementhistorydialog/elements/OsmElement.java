package me.zed.elementhistorydialog.elements;

import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.Serializable;
import java.util.Collections;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import me.zed.elementhistorydialog.DateFormatter;
import me.zed.elementhistorydialog.OsmParser;

/**
 * Base Class for all OSM elements
 */
public abstract class OsmElement implements Serializable {

    public static final String ID_ATTR = "id";
    public static final String VERSION_ATTR = "version";
    public static final String USER_ATTR = "user";
    public static final String CHANGESET_ATTR = "changeset";
    public static final String TIMESTAMP_ATTR = "timestamp";

    public static final String TAG = "tag";
    public static final String TAG_KEY_ATTR = "k";
    public static final String TAG_VALUE_ATTR = "v";
    public long osmId;
    public long osmVersion;
    public long changeset;
    public String username;
    public String timestamp;

    public TreeMap<String, String> tags;

    /**
     * Construct a new base osm element
     *
     * @param osmId      the id
     * @param osmVersion version
     */
    OsmElement(final long osmId, final long osmVersion, final String userName, final long changeset, final long timestamp) {
        this.osmId = osmId;
        this.osmVersion = osmVersion;
        this.username = userName;
        this.changeset = changeset;
        setTimestamp(timestamp);
        this.tags = new TreeMap<String, String>();
    }

    /**
     * @return the if of the object (&lt; 0 are temporary ids)
     */
    public long getOsmId() {
        return osmId;
    }

    /**
     * @return the version of the object
     */
    public long getOsmVersion() {
        return osmVersion;
    }

    /**
     * Set the OSM id for this element
     *
     * @param osmId the id as a long
     */
    void setOsmId(final long osmId) {
        this.osmId = osmId;
    }

    /**
     * Set the version for this element
     *
     * @param osmVersion the version as a long
     */
    void setOsmVersion(final long osmVersion) {
        this.osmVersion = osmVersion;
    }

    /**
     * @return the tags as a map for the current element
     */
    @NonNull
    public SortedMap<String, String> getTags() {
        if (tags == null) {
            return Collections.unmodifiableSortedMap(new TreeMap<String, String>()); // check can be removed
        }
        return Collections.unmodifiableSortedMap(tags);
    }

    /**
     * add tags to the current element
     *
     * @param tags map of tags to be added
     */
    public void setTags(@Nullable final Map<String, String> tags) {
        this.tags.putAll(tags);
    }

    /**
     * Set the timestamp
     *
     * @param secsSinceUnixEpoch seconds since the Unix Epoch
     */
    public void setTimestamp(long secsSinceUnixEpoch) {
        timestamp = DateFormatter.getUtcFormat(OsmParser.TIMESTAMP_FORMAT).format(secsSinceUnixEpoch * 1000L)
                .replace("T", " ")
                .replace("Z", "");
    }

    /**
     * gives a string description of the element type (e.g. 'node', 'way' or 'relation') - see also {@link #getType()}
     *
     * @return the type of the element
     */
    public abstract String getName();

    /**
     * @param key   the key to search for (case sensitive)
     * @param value the value to search for (case sensitive)
     * @return true if the element has a tag with this key and value.
     */
    static boolean hasTag(final Map<String, String> tags, final String key, final String value) {
        if (tags == null) {
            return false;
        }
        String keyValue = tags.get(key);
        return keyValue != null && keyValue.equals(value);
    }

    /**
     * Returns the OSM element type
     */
    public abstract ElementType getType();

    /**
     * Version of above that uses a potential different set of tags
     *
     * @param tags tags to use
     * @return the ElementType
     */
    public abstract ElementType getType(Map<String, String> tags);

    /**
     * Enum for element types (Node, Way, Closed Ways, Relations, Areas (MPs)
     */
    public enum ElementType {
        NODE, WAY, CLOSEDWAY, RELATION, AREA
    }

}