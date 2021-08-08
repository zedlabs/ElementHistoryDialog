package me.zed.elementhistorydialog.elements;

import org.junit.Test;

import java.util.TreeMap;

import static me.zed.elementhistorydialog.elements.Relation.KEY_TYPE;
import static me.zed.elementhistorydialog.elements.Relation.VALUE_MULTIPOLYGON;
import static org.junit.Assert.assertEquals;

public class RelationTest {

    /**
     * Checks the role of the relation-member returned by getMembersWithRole()
     */
    @Test
    public void getMembersWithRoleTest(){
        RelationMember rm1 = new RelationMember(OsmElement.ElementType.WAY.toString(), 1, "outer");
        RelationMember rm2 = new RelationMember(OsmElement.ElementType.NODE.toString(), 2, "inner");
        RelationMember rm3 = new RelationMember(OsmElement.ElementType.RELATION.toString(), 3, "outer");

        Relation r = new Relation(1, 1, "", 123, 111);
        r.addMember(rm1);
        r.addMember(rm2);
        r.addMember(rm3);

        assertEquals(r.getMembers().size(), 3);
        assertEquals(r.getMembersWithRole("inner").get(0).ref, 2);
    }

    /**
     * Check relation type for the test
     */
    @Test
    public void getTypeTest(){
        Relation r = new Relation(1, 1, "", 123, 111);
        TreeMap<String, String> tags = new TreeMap<>();
        tags.put(KEY_TYPE, "");
        assertEquals(r.getType(tags), OsmElement.ElementType.RELATION);
    }
}
