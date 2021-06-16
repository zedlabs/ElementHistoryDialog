package me.zed.elementhistorydialog;

import androidx.annotation.NonNull;

import java.io.Serializable;

import me.zed.elementhistorydialog.elements.Node;
import me.zed.elementhistorydialog.elements.Relation;
import me.zed.elementhistorydialog.elements.Way;

public class OsmElementFactory implements Serializable {

    @NonNull
    public static Node createNode(long osmId, long osmVersion, String userName, long timestamp, int lat, int lon) {
        return new Node(osmId, osmVersion, userName, timestamp, lat, lon);
    }

    @NonNull
    public static Way createWay(long osmId, long osmVersion, String userName, long timestamp) {
        return new Way(osmId, osmVersion, userName, timestamp);
    }

    @NonNull
    public static Relation createRelation(long osmId, long osmVersion, String userName, long timestamp) {
        return new Relation(osmId, osmVersion, userName, timestamp);
    }

}
