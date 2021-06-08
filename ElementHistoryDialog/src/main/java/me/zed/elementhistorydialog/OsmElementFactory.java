package me.zed.elementhistorydialog;

import androidx.annotation.NonNull;
import java.io.Serializable;

import me.zed.elementhistorydialog.elements.Node;
import me.zed.elementhistorydialog.elements.Relation;
import me.zed.elementhistorydialog.elements.Way;

public class OsmElementFactory implements Serializable {

    @NonNull
    public static Node createNode(long osmId, long osmVersion, int lat, int lon) {
        return new Node(osmId, osmVersion, lat, lon);
    }

    @NonNull
    public static Way createWay(long osmId, long osmVersion) {
        return new Way(osmId, osmVersion);
    }

    @NonNull
    public static Relation createRelation(long osmId, long osmVersion) {
        return new Relation(osmId, osmVersion);
    }

}
