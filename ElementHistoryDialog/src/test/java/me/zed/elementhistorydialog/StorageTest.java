package me.zed.elementhistorydialog;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import me.zed.elementhistorydialog.elements.Node;
import me.zed.elementhistorydialog.elements.Relation;
import me.zed.elementhistorydialog.elements.Way;

import static org.junit.Assert.assertEquals;

public class StorageTest {

    private static final String DEBUG_TAG = "StorageTest";
    private Storage storage;

    /**
     * Pre-test setup
     */
    @Before
    public void setup() {
        storage = new Storage();
    }

    @After
    public void destroy() {
        storage = null;
    }

    /**
     * Check successful insertion of OSM elements
     */
    @Test
    public void insertElementTest() {
        Node n = new Node(1, 1, "", 0, 0, 0, 0);
        Way w = new Way(2, 1, "", 0, 0);
        Relation r = new Relation(3, 1, "", 0, 0);

        storage.insertElementUnsafe(n);
        storage.insertElementUnsafe(w);
        storage.insertElementUnsafe(r);

        assertEquals(storage.getNodeList().size(), 1);
        assertEquals(storage.getRelationList().size(), 1);
        assertEquals(storage.getWayList().size(), 1);

    }
}
