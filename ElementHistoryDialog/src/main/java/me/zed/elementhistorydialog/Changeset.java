package me.zed.elementhistorydialog;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import me.zed.elementhistorydialog.elements.OsmElement;

public class Changeset {
    private static final String DEBUG_TAG = "Changeset";

    long osmId = -1;
    boolean open = false;
    String generator;
    final TreeMap<String, String> tags;

    // Changeset keys
    public static final String KEY_CREATED_BY = "created_by";
    public static final String KEY_COMMENT = "comment";
    public static final String KEY_IMAGERY_USED = "imagery_used";
    public static final String KEY_SOURCE = "source";
    public static final String CHANGESET = "changeset";

    /**
     * Default constructor
     */
    public Changeset() {
        tags = new TreeMap<>();
    }

    /**
     * Create a new Changeset from an InputStream in XML format
     *
     * @param parser an XmlPullParser instance
     * @param is     the InputStream
     * @return a Changeset
     * @throws XmlPullParserException if parsing fails
     * @throws IOException            if an IO operation fails
     */
    @NonNull
    static Changeset parse(@NonNull XmlPullParser parser, @NonNull InputStream is) throws XmlPullParserException, IOException {

        parser.setInput(is, null);
        int eventType;
        Changeset result = new Changeset();

        while ((eventType = parser.next()) != XmlPullParser.END_DOCUMENT) {
            String tagName = parser.getName();
            if (eventType == XmlPullParser.START_TAG) {
                switch (tagName) {
                    case CHANGESET:
                        break;
                    case OsmElement.TAG:
                        String k = parser.getAttributeValue(null, OsmElement.TAG_KEY_ATTR);
                        String v = parser.getAttributeValue(null, OsmElement.TAG_VALUE_ATTR);
                        result.tags.put(k, v);
                        break;
                    default:
                        // nothing
                }
                Log.d(DEBUG_TAG, "#" + result.osmId + " is " + (result.open ? "open" : "closed"));
            }
        }

        Log.e(DEBUG_TAG, "test" + result.tags.size());
        return result;
    }

}
