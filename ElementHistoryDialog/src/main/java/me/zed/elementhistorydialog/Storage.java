package me.zed.elementhistorydialog;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

import me.zed.elementhistorydialog.elements.Node;
import me.zed.elementhistorydialog.elements.OsmElement;
import me.zed.elementhistorydialog.elements.Relation;
import me.zed.elementhistorydialog.elements.RelationMember;
import me.zed.elementhistorydialog.elements.Way;

/**
 * Container for OSM data
 */
public class Storage {

    private static final String DEBUG_TAG = "Storage";

    private final List<Node> nodeList;
    private final List<Way> wayList;
    private final List<Relation> relationList;

    /**
     * Default constructor
     */
    public Storage() {
        nodeList = new ArrayList<>();
        wayList = new ArrayList<>();
        relationList = new ArrayList<>();
    }

    /**
     * Insert a node in to storage regardless of it is already present or not
     *
     * @param node node to insert
     */
    void insertNodeUnsafe(@NonNull final Node node) {
        nodeList.add(node);
    }

    public List<Node> getNodeList() {
        return nodeList;
    }

    /**
     * Insert a way in to storage regardless of it is already present or not
     *
     * @param way way to insert
     */
    void insertWayUnsafe(@NonNull final Way way) {
        wayList.add(way);
    }

    public List<Way> getWayList() {
        return wayList;
    }

    /**
     * Insert a relation in to storage regardless of it is already present or not
     *
     * @param relation relation to insert
     */
    void insertRelationUnsafe(@NonNull final Relation relation) {
        relationList.add(relation);
    }

    public List<Relation> getRelationList() {
        return relationList;
    }

    /**
     * Insert an element in to storage regardless of it is already present or not
     *
     * @param element element to insert
     */
    public void insertElementUnsafe(@Nullable final OsmElement element) {
        if (element instanceof Way) {
            insertWayUnsafe((Way) element);
        } else if (element instanceof Node) {
            insertNodeUnsafe((Node) element);
        } else if (element instanceof Relation) {
            insertRelationUnsafe((Relation) element);
        }
    }

    /**
     * Log the storage contents
     */
    public void logStorage() {
        for (Node n : nodeList) {
            Log.d(DEBUG_TAG, "Node " + n.getOsmId());
            for (String k : n.getTags().keySet()) {
                Log.d(DEBUG_TAG, k + "=" + n.getTags().get(k));
            }
        }
        for (Way w : wayList) {
            Log.d(DEBUG_TAG, "Way " + w.getOsmId());
            for (String k : w.getTags().keySet()) {
                Log.d(DEBUG_TAG, k + "=" + w.getTags().get(k));
            }
            for (String nd : w.getWayNodes()) {
                Log.d(DEBUG_TAG, "\t" + nd);
            }
        }
        for (Relation r : relationList) {
            Log.d(DEBUG_TAG, "Relation " + r.getOsmId());
            for (String k : r.getTags().keySet()) {
                Log.d(DEBUG_TAG, k + "=" + r.getTags().get(k));
            }
            for (RelationMember rm : r.getMembers()) {
                Log.d(DEBUG_TAG, "\t" + rm.getRef() + " " + rm.getRole());
            }
        }
    }
}
