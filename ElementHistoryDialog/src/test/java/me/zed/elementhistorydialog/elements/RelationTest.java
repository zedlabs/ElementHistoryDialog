package me.zed.elementhistorydialog.elements;

import static org.junit.Assert.assertEquals;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import me.zed.elementhistorydialog.Storage;

public class RelationTest {


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
    public void destroy(){
        storage = null;
    }

    /**
     * Checks the role of the relation-member returned by getMembersWithRole()
     */
    @Test
    public void getMembersWithRoleTest(){
        RelationMember rm1 = new RelationMember(OsmElement.ElementType.WAY.toString(), 1, "outer");
        RelationMember rm2 = new RelationMember(OsmElement.ElementType.NODE.toString(), 2, "inner");
        RelationMember rm3 = new RelationMember(OsmElement.ElementType.RELATION.toString(), 3, "outer");

        Relation r = new Relation(1, 1);
        r.addMember(rm1);
        r.addMember(rm2);
        r.addMember(rm3);

        assertEquals(r.getMembers().size(), 3);
        assertEquals(r.getMembersWithRole("inner").get(0).ref, 2);
    }

    @Test
    public void getTypeTest(){

    }
}
