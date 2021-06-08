package me.zed.elementhistorydialog.elements;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * OSM element of type Relation
 */
public class Relation extends OsmElement {

    final List<RelationMember> members;

    /**
     * It's name in the OSM-XML-scheme.
     */
    public static final String NAME = "relation";

    public static final String KEY_TYPE = "type";
    public static final String VALUE_MULTIPOLYGON = "multipolygon";
    public static final String VALUE_BOUNDARY = "boundary";

    public static final String MEMBER = "member";
    static final String MEMBER_ROLE = "role";
    static final String MEMBER_REF = "ref";
    static final String MEMBER_TYPE = "type";

    /**
     * Construct a new Relation
     *
     * @param osmId      the OSM id
     * @param osmVersion the version
     */
    public Relation(final long osmId, final long osmVersion) {
        super(osmId, osmVersion);
        members = new ArrayList<>();
    }

    @Override
    public String getName() {
        return NAME;
    }

    /**
     * Append a RelationMember to the Relation
     *
     * @param member the RelationMember to append
     */
    public void addMember(@NonNull final RelationMember member) {
        members.add(member);
    }

    /**
     * Return complete list of relation members
     *
     * @return list of members, or null if there are none
     */
    @Nullable
    public List<RelationMember> getMembers() {
        return members;
    }

    /**
     * /**
     * Return a List of all RelationMembers with a specific role
     *
     * @param role the role we are looking for
     * @return a List of the RelationMembers
     */
    @NonNull
    public List<RelationMember> getMembersWithRole(@NonNull String role) {
        List<RelationMember> rl = new ArrayList<>();
        for (RelationMember rm : members) {
            if (role.equals(rm.getRole())) {
                rl.add(rm);
            }
        }
        return rl;
    }

    @Override
    public ElementType getType() {
        return getType(tags);
    }

    @Override
    public ElementType getType(Map<String, String> tags) {
        if (hasTag(tags, KEY_TYPE, VALUE_MULTIPOLYGON) || hasTag(tags, KEY_TYPE, VALUE_BOUNDARY)) {
            return ElementType.AREA;
        }
        return ElementType.RELATION;
    }

}
